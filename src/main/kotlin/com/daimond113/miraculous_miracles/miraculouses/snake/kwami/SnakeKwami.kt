package com.daimond113.miraculous_miracles.miraculouses.snake.kwami

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EntityType
import net.minecraft.world.World

class SnakeKwami(entityType: EntityType<out SnakeKwami>, world: World) : AbstractKwami(
    MiraculousType.Snake, entityType,
    world
)
