package com.daimond113.miraculous_miracles.items

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.LuckyCharmWeaponMaterial
import net.minecraft.item.PickaxeItem
import net.minecraft.util.Rarity
import org.quiltmc.qkl.library.items.itemSettingsOf

class LuckyCharmPickaxe : PickaxeItem(LuckyCharmWeaponMaterial, 1, 1f, itemSettingsOf(group = MiraculousMiracles.ITEM_GROUP, rarity = Rarity.RARE)) {
}
