package com.daimond113.miraculous_miracles.miraculouses.turtle.kwami

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EntityType
import net.minecraft.world.World

class TurtleKwami(entityType: EntityType<out TurtleKwami>, world: World) : AbstractKwami(
    MiraculousType.Turtle, entityType,
    world
)
