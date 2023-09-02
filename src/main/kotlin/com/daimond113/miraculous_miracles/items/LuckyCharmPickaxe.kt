package com.daimond113.miraculous_miracles.items

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.LuckyCharmWeaponMaterial
import com.daimond113.miraculous_miracles.core.itemSettingsOf
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.util.Rarity

class LuckyCharmPickaxe : PickaxeItem(
    LuckyCharmWeaponMaterial,
    1,
    1f,
    itemSettingsOf(group = MiraculousMiracles.ITEM_GROUP, rarity = Rarity.RARE)
) {
    override fun hasGlint(stack: ItemStack?): Boolean {
        return false
    }
}
