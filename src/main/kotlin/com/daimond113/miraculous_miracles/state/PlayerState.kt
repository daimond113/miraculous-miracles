package com.daimond113.miraculous_miracles.state

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.*
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
        ServerPlayNetworking.send(player, NetworkMessages.RECEIVE_ACTIVE_MIRACULOUS, PacketByteBufs.create().apply {
            writeIntArray(activeMiraculous.keys.map { miraculousType -> miraculousType.id }
                .toIntArray()
            )
        })

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

        val hasBeenUsed = MiraculousAbility.HasBeenUsed.fromBoolean(
            if (ability.isToggleable)
                nbtCompound.getBoolean("hasBeenUsed")
            else
                false
        )

        if (!ability.ignoresMinutes && player.hasStatusEffect(MiraculousMiracles.TRANSFORMATION_TIME_LEFT_EFFECT) && !hasBeenUsed.value) return

        when (ability.execute(player, nbtCompound, hasBeenUsed, additionalParam)) {
            MiraculousAbility.Result.Success -> {
                if (ability.isToggleable) {
                    nbtCompound.putBoolean("hasBeenUsed", !hasBeenUsed.value)
                }

                sendMessageFrom(
                    Text.translatable(
                        "text.miraculous_miracles.${
                            ability.toString().lowercase()
                        }${if (ability.isToggleable) if (hasBeenUsed.value) ".deinitialize" else ".initialize" else ""}.shout"
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
            player.removeStatusEffect(
                MiraculousMiracles.TRANSFORMATION_TIME_LEFT_EFFECT,
                EffectRemovalReasons.DETRANSFORMED
            )
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

        fun shouldBeRemoved(itemStack: ItemStack, miraculousType: MiraculousType): Boolean {
            if (itemStack.isEmpty) return false

            // TODO: Unifications
            return when (val it = itemStack.item) {
                is MiraculousLinkedItem -> {
                    it.miraculousType == miraculousType
                }

                is AbstractWeapon -> {
                    MiraculousMiracles.MIRACULOUS_WEAPONS[miraculousType] == it
                }

                else -> false
            }
        }

        for (miraculousType in miraculousTypes) {
            for (i in 0 until player.inventory.size()) {
                val stack = player.inventory.getStack(i)
                if (shouldBeRemoved(stack, miraculousType)) {
                    player.inventory.removeStack(i)
                }
            }

            val miraculousNbt = activeMiraculous.remove(miraculousType)

            usedAbilities.entries.removeIf { (ability, nbt) ->
                if (ability.miraculousType == miraculousType && ability.isToggleable && nbt.getBoolean(
                        "hasBeenUsed"
                    )
                ) {
                    ability.execute(player, nbt, MiraculousAbility.HasBeenUsed.TrueAuto, null)
                }

                ability.miraculousType == miraculousType
            }

            ServerState.getServerState(player.server).markDirty()

            val newMiraculous = ItemStack(MiraculousMiracles.MIRACULOUSES[miraculousType]).apply {
                nbt = miraculousNbt
                damage += 1
            }

            val newKwami = MiraculousMiracles.KWAMIS[miraculousType]?.create(player.world)?.let {
                it.updatePosition(player.x, player.y, player.z)
                it.isHungry = true
                player.world.spawnEntity(it)

                it
            }

            AbstractMiraculous.setNBT(newMiraculous, Optional.ofNullable(newKwami?.uuid), true)
            giveItemStack(newMiraculous, player)
        }

        updateActiveMiraculous(player, false)
    }
}
