package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.MiraculousMiracles
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier

enum class ArmorMaterials(val miraculousType: MiraculousType) : ArmorMaterial {
    Bee(MiraculousType.Bee),
    Turtle(MiraculousType.Turtle),
    Snake(MiraculousType.Snake),
    Ladybug(MiraculousType.Ladybug),
    Horse(MiraculousType.Horse),
    Rabbit(MiraculousType.Rabbit),
    Mouse(MiraculousType.Mouse);

    private val baseDurability = arrayOf(13, 15, 16, 11)
    private val protectionValues = arrayOf(3, 6, 8, 3)

    override fun getDurability(slot: EquipmentSlot): Int {
        return baseDurability[slot.entitySlotId]
    }

    override fun getProtectionAmount(slot: EquipmentSlot): Int {
        return protectionValues[slot.entitySlotId]
    }

    override fun getEnchantability(): Int {
        return 0
    }

    override fun getEquipSound(): SoundEvent {
        return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER
    }

    override fun getRepairIngredient(): Ingredient {
        return Ingredient.EMPTY
    }

    override fun getName(): String {
        return "${miraculousType.toString().lowercase()}_armor"
    }

    override fun getToughness(): Float {
        return 3.8f
    }

    override fun getKnockbackResistance(): Float {
        return 0.15f
    }

    override fun getTexture(): Identifier {
        return Identifier(MiraculousMiracles.MOD_ID, "textures/models/armor/${this.getName()}")
    }
}
