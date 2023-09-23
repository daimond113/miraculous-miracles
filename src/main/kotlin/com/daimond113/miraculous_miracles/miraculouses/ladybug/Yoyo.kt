package com.daimond113.miraculous_miracles.miraculouses.ladybug

import com.daimond113.miraculous_miracles.core.AbstractWeapon
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class Yoyo : AbstractWeapon("yoyo") {
    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)

        user.itemCooldownManager[this] = 60

        if (!world.isClient) {
            val yoyoEntity = YoyoEntity(world, user)
            yoyoEntity.setItem(itemStack)
            yoyoEntity.setProperties(user, user.pitch, user.yaw, 0.0f, 1.5f, 1.0f)
            world.spawnEntity(yoyoEntity)
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this))

        return TypedActionResult.success(itemStack, world.isClient())
    }
}
