package com.daimond113.miraculous_miracles.items

import com.daimond113.miraculous_miracles.core.AbstractWeapon
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class SpinningTop : AbstractWeapon("spinning_top") {
    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)

        user.itemCooldownManager[this] = 60

        if (!world.isClient) {
            val spinningTopEntity = SpinningTopEntity(world, user)
            spinningTopEntity.setItem(itemStack)
            spinningTopEntity.setProperties(user, user.pitch, user.yaw, 0.0f, 1.5f, 1.0f)
            world.spawnEntity(spinningTopEntity)
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this))

        return TypedActionResult.success(itemStack, world.isClient())
    }
}
