package com.daimond113.miraculous_miracles.miraculouses.bee

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.MiraculousLinkedItem
import com.daimond113.miraculous_miracles.core.MiraculousType
import com.daimond113.miraculous_miracles.core.itemSettingsOf
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Rarity

class Venom : Item(itemSettingsOf(maxCount = 1, group = MiraculousMiracles.ITEM_GROUP, rarity = Rarity.EPIC)), MiraculousLinkedItem {
    override val miraculousType = MiraculousType.Bee

    override fun postHit(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        target.addStatusEffect(StatusEffectInstance(StatusEffects.SLOWNESS, 600, 15, false, true, true))

        stack.count = 0

        return true
    }

    override fun hasGlint(stack: ItemStack?): Boolean {
        return false
    }
}
