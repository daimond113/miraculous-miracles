package com.daimond113.miraculous_miracles.core

import net.minecraft.item.ToolMaterial
import net.minecraft.recipe.Ingredient

object WeaponMaterial : ToolMaterial {
    override fun getDurability(): Int {
        return 15_000
    }

    override fun getMiningSpeedMultiplier(): Float {
        return 1f
    }

    override fun getAttackDamage(): Float {
        return 3.5f
    }

    override fun getMiningLevel(): Int {
        return 3
    }

    override fun getEnchantability(): Int {
        return 25
    }

    override fun getRepairIngredient(): Ingredient {
        return Ingredient.EMPTY
    }
}
