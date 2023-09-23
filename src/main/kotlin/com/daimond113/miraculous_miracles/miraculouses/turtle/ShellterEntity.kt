package com.daimond113.miraculous_miracles.miraculouses.turtle

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.MiraculousAbility
import com.daimond113.miraculous_miracles.state.ServerState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

class ShellterEntity : ThrownItemEntity {
    constructor(entityType: EntityType<out ShellterEntity?>, world: World) : super(entityType, world)

    constructor(world: World, owner: LivingEntity) : super(MiraculousMiracles.TURTLE_SHELLTER_ENTITY, owner, world)

    override fun getDefaultItem(): Item {
        return MiraculousMiracles.TURTLE_SHELLTER_ITEM
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
    }

    private fun landed() {
        val owner = this.owner
        if (owner !is ServerPlayerEntity) return
        val playerState = ServerState.getPlayerState(owner)
        playerState.useAbility(MiraculousAbility.Shellter, owner, this.blockPos)
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)

        if (!world.isClient && !this.isRemoved) {
            val entity = owner
            if (entity is ServerPlayerEntity) {
                if (entity.networkHandler.getConnection().isOpen && entity.world === world && !entity.isSleeping) {
                    this.landed()
                }
            } else if (entity != null) {
                this.landed()
            }
            discard()
        }
    }

    override fun tick() {
        if (owner is PlayerEntity && !(owner as PlayerEntity).isAlive) {
            discard()
        } else {
            super.tick()
        }
    }

    override fun moveToWorld(destination: ServerWorld): Entity? {
        if (owner != null && owner!!.world.registryKey !== destination.registryKey) {
            owner = null
        }

        return super.moveToWorld(destination)
    }
}
