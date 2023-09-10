package com.daimond113.miraculous_miracles.kwamis.horse

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EntityType
import net.minecraft.world.World

class HorseKwami(entityType: EntityType<out HorseKwami>, world: World) : AbstractKwami(
    MiraculousType.Horse, entityType,
    world
)
