package com.daimond113.miraculous_miracles.content

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.UnspawnableEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.entity.PlayerModelPart
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.passive.TameableEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World

class MultitudeEntity : UnspawnableEntity {
    constructor(entityType: EntityType<out TameableEntity>, world: World) : super(entityType, world)

    constructor(world: World, owner: ServerPlayerEntity) : super(MiraculousMiracles.MULTITUDE_PLAYER_ENTITY, world) {
        this.isTamed = true
        this.ownerUuid = owner.uuid
    }

    val skinTexture: Identifier
        get() {
            return (owner as PlayerEntity?)?.gameProfile?.let {
                MinecraftClient.getInstance().skinProvider.method_44705(
                    it
                )
            }
                ?: DefaultSkinHelper.getTexture()
        }

    fun isPartVisible(modelPart: PlayerModelPart): Boolean {
        return (owner as PlayerEntity?)?.isPartVisible(modelPart) ?: false
    }

    override fun initGoals() {
        goalSelector.add(
            1,
            TrackOwnerAttackerGoal(this)
        )
        goalSelector.add(
            2,
            AttackWithOwnerGoal(this)
        )
        goalSelector.add(
            5,
            MeleeAttackGoal(this, 1.0, true)
        )
        goalSelector.add(
            6,
            FollowOwnerGoal(this, 1.0, 5f, 1f, false)
        )
        goalSelector.add(
            8,
            WanderAroundFarGoal(this, 1.0)
        )
        goalSelector.add(
            10,
            LookAroundGoal(this)
        )
    }
}
