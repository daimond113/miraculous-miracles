package com.daimond113.miraculous_miracles.items

import com.daimond113.miraculous_miracles.core.itemSettingsOf
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.Rarity
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class VoyageItem : Item(itemSettingsOf(rarity = Rarity.RARE, fireproof = true)) {
    companion object {
        private fun getNBT(stack: ItemStack): NbtCompound {
            val nbt = stack.getOrCreateNbt()

            if (!nbt.contains("destination")) {
                nbt.putIntArray("destination", intArrayOf(0, 0, 0))
            }

            return nbt
        }

        fun getDestination(stack: ItemStack): Triple<Int, Int, Int> {
            return getNBT(stack).getIntArray("destination").let { Triple(it[0], it[1], it[2]) }
        }

        fun setDestination(stack: ItemStack, dest: Triple<Int, Int, Int>) {
            stack.nbt = getNBT(stack).apply {
                putIntArray("destination", dest.toList().toIntArray())
            }
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)

        if (!world.isClient) {
            val voyageEntity = VoyageEntity(world, user, getDestination(itemStack))
            voyageEntity.setItem(itemStack)
            voyageEntity.setProperties(user, user.pitch, user.yaw, 0.0f, 1.5f, 1.0f)
            world.spawnEntity(voyageEntity)
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this))

        if (!user.isCreative) {
            itemStack.count = 0
        }

        return TypedActionResult.success(itemStack, world.isClient())
    }

    override fun hasGlint(stack: ItemStack?): Boolean {
        return false
    }
}
