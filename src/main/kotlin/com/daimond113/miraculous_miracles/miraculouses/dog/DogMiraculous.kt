package com.daimond113.miraculous_miracles.miraculouses.dog

import com.daimond113.miraculous_miracles.core.AbstractMiraculous
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EquipmentSlot

class DogMiraculous : AbstractMiraculous(MiraculousType.Dog, { _ -> EquipmentSlot.HEAD })
