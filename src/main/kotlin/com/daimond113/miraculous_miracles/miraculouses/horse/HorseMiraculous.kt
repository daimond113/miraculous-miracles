package com.daimond113.miraculous_miracles.miraculouses.horse

import com.daimond113.miraculous_miracles.core.AbstractMiraculous
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.EquipmentSlot

class HorseMiraculous : AbstractMiraculous(MiraculousType.Horse, { _ -> EquipmentSlot.HEAD })
