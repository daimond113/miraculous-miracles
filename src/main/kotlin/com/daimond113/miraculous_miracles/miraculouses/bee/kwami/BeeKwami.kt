package com.daimond113.miraculous_miracles.miraculouses.bee.kwami

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EntityType
import net.minecraft.world.World

class BeeKwami(entityType: EntityType<out BeeKwami>, world: World) : AbstractKwami(
    MiraculousType.Bee, entityType,
    world
)
