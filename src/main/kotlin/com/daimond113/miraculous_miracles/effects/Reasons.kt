package com.daimond113.miraculous_miracles.effects

import com.daimond113.miraculous_miracles.MiraculousMiracles
import net.minecraft.util.Identifier
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason

object Reasons {
    val DETRANSFORMED = StatusEffectRemovalReason(Identifier(MiraculousMiracles.MOD_ID, "detransformed"))
}
