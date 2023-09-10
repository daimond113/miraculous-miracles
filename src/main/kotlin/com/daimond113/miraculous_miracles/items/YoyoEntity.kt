package com.daimond113.miraculous_miracles.items

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

class YoyoEntity : ThrownItemEntity {
    constructor(entityType: EntityType<out YoyoEntity?>, world: World) : super(entityType, world)

    constructor(world: World, owner: LivingEntity) : super(MiraculousMiracles.LADYBUG_YOYO_ENTITY, owner, world)

    override fun getDefaultItem(): Item {
        return MiraculousMiracles.MIRACULOUS_WEAPONS[MiraculousType.Ladybug]!!
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        entityHitResult.entity.damage(DamageSource.thrownProjectile(this, owner), 0.0f)
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)

        if (!world.isClient && !this.isRemoved) {
            val entity = owner
            if (entity is ServerPlayerEntity) {
                if (entity.networkHandler.getConnection().isOpen && entity.world === world && !entity.isSleeping) {
                    if (entity.hasVehicle()) {
                        entity.requestTeleportAndDismount(this.x, this.y, this.z)
                    } else {
                        entity.requestTeleport(this.x, this.y, this.z)
                    }
                    entity.onLanding()
                }
            } else if (entity != null) {
                entity.requestTeleport(this.x, this.y, this.z)
                entity.onLanding()
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
