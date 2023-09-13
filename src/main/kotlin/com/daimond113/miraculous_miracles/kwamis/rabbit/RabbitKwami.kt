package com.daimond113.miraculous_miracles.kwamis.rabbit

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EntityType
import net.minecraft.world.World

class RabbitKwami(entityType: EntityType<out RabbitKwami>, world: World) : AbstractKwami(
    MiraculousType.Rabbit, entityType,
    world
)
