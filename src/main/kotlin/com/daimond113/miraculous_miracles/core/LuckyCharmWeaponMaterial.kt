package com.daimond113.miraculous_miracles.core

import net.minecraft.item.ToolMaterial
import net.minecraft.recipe.Ingredient

object LuckyCharmWeaponMaterial : ToolMaterial {
    override fun getDurability(): Int {
        return 16
    }

    override fun getMiningSpeedMultiplier(): Float {
        return 9.2f
    }

    override fun getAttackDamage(): Float {
        return 4f
    }

    override fun getMiningLevel(): Int {
        return 4
    }

    override fun getEnchantability(): Int {
        return 25
    }

    override fun getRepairIngredient(): Ingredient {
        return Ingredient.EMPTY
    }
}
