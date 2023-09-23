package com.daimond113.miraculous_miracles.content

import com.daimond113.miraculous_miracles.core.MiraculousLinkedItem
import com.daimond113.miraculous_miracles.core.MiraculousType
import com.daimond113.miraculous_miracles.core.itemSettingsOf
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class PortalItem(private val isBurrow: Boolean) : Item(itemSettingsOf(rarity = Rarity.RARE, fireproof = true)), MiraculousLinkedItem {
    override val miraculousType = if (isBurrow) MiraculousType.Rabbit else MiraculousType.Horse

    companion object {
        private fun getNBT(stack: ItemStack): NbtCompound {
            val nbt = stack.getOrCreateNbt()

            if (!nbt.contains("destination")) {
                nbt.putIntArray("destination", intArrayOf(0, 0, 0))
            }

            return nbt
        }

        fun getDestination(stack: ItemStack): Pair<BlockPos, Identifier?> {
            return getNBT(stack).run {
                Pair(
                    getIntArray("destination").let { BlockPos(it[0], it[1], it[2]) },
                    if (contains("dimension")) Identifier(getString("dimension")) else null
                )
            }
        }

        fun setDestination(stack: ItemStack, dest: BlockPos, dim: Identifier?) {
            stack.nbt = getNBT(stack).apply {
                putIntArray("destination", intArrayOf(dest.x, dest.y, dest.z))
                dim?.let { putString("dimension", it.toString()) }
            }
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)

        if (!world.isClient) {
            val portalItemEntity = PortalItemEntity(world, user, getDestination(itemStack), isBurrow)
            portalItemEntity.setItem(itemStack)
            portalItemEntity.setProperties(user, user.pitch, user.yaw, 0.0f, 1.5f, 1.0f)
            world.spawnEntity(portalItemEntity)
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
