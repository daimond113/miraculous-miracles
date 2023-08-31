package com.daimond113.miraculous_miracles.kwamis.ladybug

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EntityType
import net.minecraft.world.World

class LadybugKwami(entityType: EntityType<out LadybugKwami>, world: World) : AbstractKwami(
    MiraculousType.Ladybug, entityType,
    world
) {
}
