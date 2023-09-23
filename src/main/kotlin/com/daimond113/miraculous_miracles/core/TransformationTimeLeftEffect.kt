package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.state.ServerState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffectType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason

class TransformationTimeLeftEffect : StatusEffect(StatusEffectType.NEUTRAL, 0xfad64a) {
    override fun onRemoved(
        entity: LivingEntity,
        attributes: AttributeContainer,
        effect: StatusEffectInstance,
        reason: StatusEffectRemovalReason
    ) {
        if (reason != StatusEffectRemovalReason.EXPIRED) return
        if (entity is PlayerEntity) {
            val playerState = ServerState.getPlayerState(entity)

            playerState.detransform(entity as ServerPlayerEntity, playerState.activeMiraculous.keys, true)
        }

        return
    }

    override fun shouldRemove(
        entity: LivingEntity,
        effect: StatusEffectInstance,
        reason: StatusEffectRemovalReason
    ): Boolean {
        return reason == EffectRemovalReasons.DETRANSFORMED
    }
}
