package com.daimond113.miraculous_miracles.miraculouses.dog.kwami

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EntityType
import net.minecraft.world.World

class DogKwami(entityType: EntityType<out DogKwami>, world: World) : AbstractKwami(
    MiraculousType.Dog, entityType,
    world
)
