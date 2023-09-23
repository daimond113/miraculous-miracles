package com.daimond113.miraculous_miracles.core

import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack

class MiraculousArmorItem(
    material: MiraculousArmorMaterials,
    slot: EquipmentSlot,
    settings: Settings
) : ArmorItem(material, slot, settings), MiraculousLinkedItem {
    override val miraculousType = material.miraculousType

    override fun hasGlint(stack: ItemStack?): Boolean {
        return false
    }
}
