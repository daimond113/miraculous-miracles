package com.daimond113.miraculous_miracles.states

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.*
import com.daimond113.miraculous_miracles.effects.Reasons
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.ItemScatterer
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import java.util.*


class PlayerState {
    var activeMiraculous: MutableMap<MiraculousType, NbtCompound> = mutableMapOf()
    var usedAbilities: MutableMap<MiraculousAbility, NbtCompound> = mutableMapOf()

    companion object {
        fun getMiraculousTypeById(id: Int): MiraculousType {
            return MiraculousType.values().first { it.id == id }
        }

        fun getAbilityById(id: Int): MiraculousAbility {
            return MiraculousAbility.values().first { it.id == id }
        }

        fun giveItemStack(stack: ItemStack, player: ServerPlayerEntity) {
            if (!player.inventory.insertStack(stack)) {
                ItemScatterer.spawn(player.world, player.x, player.y, player.z, stack)
            }
        }

        fun replaceItemStack(replace: ItemStack, with: ItemStack, player: ServerPlayerEntity) {
            val index = player.inventory.getSlotWithStack(replace)
            if (index < 0) return
            player.inventory.removeStack(index)
            player.inventory.updateItems()
            player.inventory.setStack(index, with)
        }

        fun sendMessageFrom(text: Text, player: ServerPlayerEntity) {
            var prependCode = ""
            val resetCode = Formatting.RESET.toString()

            if (player.scoreboardTeam != null) {
                prependCode = player.scoreboardTeam!!.color.toString()
            }

            val prefixedMessage = Text.literal("<${prependCode}${player.name.string}${resetCode}> ").append(text)

            player.world.server!!.playerManager.method_43512(prefixedMessage, { _ -> prefixedMessage }, false)
        }
    }

    fun updateActiveMiraculous(player: ServerPlayerEntity, asDirty: Boolean = true) {
        val packetByteBuf = PacketByteBufs.create()

        packetByteBuf.writeIntArray(activeMiraculous.keys.map { miraculousType -> miraculousType.id }
            .toIntArray())

        ServerPlayNetworking.send(player, NetworkMessages.RECEIVE_ACTIVE_MIRACULOUS, packetByteBuf)
        if (asDirty) ServerState.getServerState(player.server).markDirty()
    }

    fun useAbility(ability: MiraculousAbility, player: ServerPlayerEntity, additionalParam: Any?) {
        if (!activeMiraculous.contains(ability.miraculousType)) return

        val serverState = ServerState.getServerState(player.server)

        val nbtCompound = usedAbilities.getOrPut(ability) {
            serverState.markDirty()

            NbtCompound().apply {
                if (ability.isToggleable) {
                    putBoolean("hasBeenUsed", false)
                }
            }
        }

        val hasBeenUsed = if (ability.isToggleable)
            run {
                val beenUsed = nbtCompound.getBoolean("hasBeenUsed")
                nbtCompound.putBoolean("hasBeenUsed", !beenUsed)
                beenUsed
            }
        else
            false

        if (!ability.ignoresMinutes && player.hasStatusEffect(MiraculousMiracles.TRANSFORMATION_TIME_LEFT_EFFECT) && !hasBeenUsed) return

        when (ability.execute(player, nbtCompound, hasBeenUsed, additionalParam)) {
            AbilityResult.Success -> {
                sendMessageFrom(
                    Text.translatable(
                        "text.miraculous_miracles.${
                            ability.toString().lowercase()
                        }${if (ability.isToggleable) if (hasBeenUsed) ".deinitialize" else ".initialize" else ""}.shout"
                    ),
                    player
                )

                if (ability.givesMinutesLeft && (usedAbilities.size >= MiraculousAbility.values()
                        .filter { activeMiraculous.contains(it.miraculousType) }.size)
                ) {
                    player.addStatusEffect(
                        StatusEffectInstance(
                            MiraculousMiracles.TRANSFORMATION_TIME_LEFT_EFFECT,
                            6000,
                            0,
                            false,
                            false,
                            true
                        )
                    )
                }
            }

            else -> {}
        }
    }

    fun detransform(player: ServerPlayerEntity, miraculousTypes: Set<MiraculousType>, forced: Boolean = false) {
        if (player.hasStatusEffect(MiraculousMiracles.TRANSFORMATION_TIME_LEFT_EFFECT)) {
            player.removeStatusEffect(MiraculousMiracles.TRANSFORMATION_TIME_LEFT_EFFECT, Reasons.DETRANSFORMED)
        }

        if (!forced) {
            val firstMiraculousType = miraculousTypes.first().toString().lowercase()

            sendMessageFrom(
                if (activeMiraculous.size > 1) Text.translatable(
                    "text.miraculous_miracles.division",
                    Text.translatable("entity.miraculous_miracles.${firstMiraculousType}_kwami")
                ) else Text.translatable("text.miraculous_miracles.$firstMiraculousType.detransformation"),
                player
            )
        }

        fun shouldBeRemoved(itemStack: ItemStack): Boolean {
            if (itemStack.isEmpty) return false

            // TODO: Unifications
            return miraculousTypes.firstOrNull { miraculousType -> itemStack.isIn(MiraculousMiracles.MIRACULOUS_ITEM_TAGS[miraculousType]) } != null
        }

        for (i in 0 until player.inventory.size()) {
            val stack = player.inventory.getStack(i)
            if (shouldBeRemoved(stack)) {
                player.inventory.removeStack(i)
            }
        }

        for (miraculousType in miraculousTypes) {
            val nbt = activeMiraculous[miraculousType]
            activeMiraculous.remove(miraculousType)
            usedAbilities.entries.removeIf { (ability, nbt) ->
                if (ability.miraculousType == miraculousType && ability.isToggleable && nbt.getBoolean(
                        "hasBeenUsed"
                    )
                ) {
                    ability.execute(player, nbt, true, null)
                }

                ability.miraculousType == miraculousType
            }

            ServerState.getServerState(player.server).markDirty()


            val newKwami = MiraculousMiracles.KWAMIS[miraculousType]!!.create(player.world)
            newKwami?.updatePosition(player.x, player.y, player.z)
            player.world.spawnEntity(newKwami)

            if (newKwami != null) {
                newKwami.isHungry = true
                val newMiraculous = ItemStack(MiraculousMiracles.MIRACULOUSES[miraculousType])
                newMiraculous.nbt = nbt
                AbstractMiraculous.setNBT(newMiraculous, Optional.of(newKwami.uuid), true)
                newMiraculous.damage += 1
                giveItemStack(newMiraculous, player)
            }
        }

        updateActiveMiraculous(player, false)
    }
}
