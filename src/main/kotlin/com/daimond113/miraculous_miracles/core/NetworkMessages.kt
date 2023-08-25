package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.MiraculousMiracles
import net.minecraft.util.Identifier

object NetworkMessages {
    val GET_ACTIVE_MIRACULOUS = Identifier(MiraculousMiracles.MOD_ID, "get_active_miraculous")
    val RECEIVE_ACTIVE_MIRACULOUS = Identifier(MiraculousMiracles.MOD_ID, "receive_active_miraculous")
    val DETRANSFORM = Identifier(MiraculousMiracles.MOD_ID, "detransform")
    val USE_MIRACULOUS_ABILITY = Identifier(MiraculousMiracles.MOD_ID, "use_miraculous_ability")
}
