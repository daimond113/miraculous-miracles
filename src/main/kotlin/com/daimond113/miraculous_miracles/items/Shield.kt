package com.daimond113.miraculous_miracles.items

import com.daimond113.miraculous_miracles.core.AbstractWeapon
import com.daimond113.miraculous_miracles.core.MiraculousAbility
import com.daimond113.miraculous_miracles.states.ServerState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class Shield : AbstractWeapon("shield") {
    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)

        user.itemCooldownManager[this] = 120

        if (!world.isClient) {
            val playerState = ServerState.getPlayerState(user as ServerPlayerEntity)
            if (playerState.hasUsedAbility(MiraculousAbility.Shellter, true)) {
                if (!playerState.usedAbilities[MiraculousAbility.Shellter]!!.getBoolean("hasBeenUsed"))
                    return TypedActionResult.success(itemStack, false)
                playerState.useAbility(MiraculousAbility.Shellter, user, null)
            } else {
                val shellterEntity = ShellterEntity(world, user)
                shellterEntity.setItem(itemStack)
                shellterEntity.setProperties(user, user.pitch, user.yaw, 0.0f, 1.5f, 1.0f)
                world.spawnEntity(shellterEntity)
            }
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this))

        return TypedActionResult.success(itemStack, !world.isClient)
    }
}
