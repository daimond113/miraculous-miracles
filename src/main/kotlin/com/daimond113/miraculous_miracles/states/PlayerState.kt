package com.daimond113.miraculous_miracles.states

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.AbstractMiraculous
import com.daimond113.miraculous_miracles.core.MiraculousAbility
import com.daimond113.miraculous_miracles.core.MiraculousType
import com.daimond113.miraculous_miracles.effects.Reasons
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ItemScatterer
import java.util.*


class PlayerState {
    var activeMiraculous: MutableSet<MiraculousType> = mutableSetOf()
    var usedAbilities: MutableSet<Pair<MiraculousAbility, NbtCompound>> = mutableSetOf()

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
    }

    fun hasUsedAbility(ability: MiraculousAbility): Boolean {
        return usedAbilities.firstOrNull { (usedAbility) -> ability == usedAbility } != null
    }

    fun detransform(player: ServerPlayerEntity, miraculousTypes: Set<MiraculousType>, forced: Boolean = false) {
        if (player.hasStatusEffect(MiraculousMiracles.TRANSFORMATION_TIME_LEFT_EFFECT)) {
            player.removeStatusEffect(MiraculousMiracles.TRANSFORMATION_TIME_LEFT_EFFECT, Reasons.DETRANSFORMED)
        }

        if (!forced) {
            val firstMiraculousType = miraculousTypes.first().toString().lowercase()
            val text = if (activeMiraculous.size > 1) Text.translatable(
                "text.miraculous_miracles.division",
                Text.translatable("entity.miraculous_miracles.${firstMiraculousType}_kwami")
            ) else Text.translatable("text.miraculous_miracles.$firstMiraculousType.detransformation")

            player.world.server!!.playerManager.method_43512(text, { _ -> text }, false)
        }

        fun shouldBeRemoved(itemStack: ItemStack): Boolean {
            if (itemStack.isEmpty) return false

            // TODO: Unifications
            return miraculousTypes.firstOrNull { miraculousType -> itemStack.isIn(MiraculousMiracles.MIRACULOUS_ITEM_TAGS[miraculousType]) } != null
        }

        for (i in 0 until player.inventory.size()) {
            val stack = player.inventory.getStack(i)
            if (shouldBeRemoved(stack)) {
                player.inventory.setStack(i, ItemStack.EMPTY)
            }
        }

        for (miraculousType in miraculousTypes) {
            activeMiraculous.remove(miraculousType)
            usedAbilities.removeIf { (ability) -> ability.miraculousType == miraculousType }

            val newKwami = MiraculousMiracles.KWAMIS[miraculousType]!!.create(player.world)
            newKwami?.updatePosition(player.x, player.y, player.z)
            player.world.spawnEntity(newKwami)

            if (newKwami != null) {
                val newMiraculous = ItemStack(MiraculousMiracles.MIRACULOUSES[miraculousType])
                newKwami.isHungry = true
                AbstractMiraculous.setNBT(newMiraculous, Optional.of(newKwami.uuid), true)
                giveItemStack(newMiraculous, player)
            }
        }
    }
}
