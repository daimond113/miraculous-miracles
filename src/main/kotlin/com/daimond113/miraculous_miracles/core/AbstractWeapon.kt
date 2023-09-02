package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.MiraculousMiracles
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.util.Rarity

abstract class AbstractWeapon(val weaponName: String) : SwordItem(
    WeaponMaterial,
    4,
    -2f,
    itemSettingsOf(maxCount = 1, rarity = Rarity.RARE, group = MiraculousMiracles.ITEM_GROUP)
) {
    override fun hasGlint(stack: ItemStack?): Boolean {
        return false
    }
}
