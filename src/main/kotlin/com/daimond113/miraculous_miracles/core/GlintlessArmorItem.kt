package com.daimond113.miraculous_miracles.core

import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ItemStack

class GlintlessArmorItem(
    material: ArmorMaterial, slot: EquipmentSlot,
    settings: Settings
) : ArmorItem(material, slot, settings) {
    override fun hasGlint(stack: ItemStack?): Boolean {
        return false
    }
}
