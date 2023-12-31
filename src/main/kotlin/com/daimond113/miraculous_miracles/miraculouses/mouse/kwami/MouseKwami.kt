package com.daimond113.miraculous_miracles.miraculouses.mouse.kwami

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EntityType
import net.minecraft.world.World

class MouseKwami(entityType: EntityType<out MouseKwami>, world: World) : AbstractKwami(
    MiraculousType.Mouse, entityType,
    world
)
