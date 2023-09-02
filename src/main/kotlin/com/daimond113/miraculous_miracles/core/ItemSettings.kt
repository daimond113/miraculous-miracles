/*
 * Copyright 2023 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// copy-pasted from the GitHub repo with little modifications, because the 1.19.2 qkl has not been updated with a maxCount bug fix
package com.daimond113.miraculous_miracles.core

import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Rarity
import org.quiltmc.qsl.item.setting.api.CustomDamageHandler
import org.quiltmc.qsl.item.setting.api.CustomItemSetting
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings

/**
 * Create a [QuiltItemSettings] with the given information.
 * Calling without specifying any parameters will create the default settings.
 * This is enough for many items.
 *
 * @author sschr15
 */
fun itemSettingsOf(
    maxCount: Int? = null,
    maxDamage: Int? = null,
    recipeRemainder: Item? = null,
    rarity: Rarity? = null,
    foodComponent: FoodComponent? = null,
    fireproof: Boolean = false,
    customDamage: CustomDamageHandler? = null,
    equipmentSlot: ((ItemStack) -> EquipmentSlot)? = null,
    group: ItemGroup? = null,
    otherSettings: List<Pair<CustomItemSetting<*>, *>> = emptyList()
): QuiltItemSettings = buildItemSettings {
    if (maxDamage != null) maxDamage(maxDamage)
    if (maxCount != null) maxCount(maxCount)
    if (recipeRemainder != null) recipeRemainder(recipeRemainder)
    if (rarity != null) rarity(rarity)
    if (foodComponent != null) food(foodComponent)
    if (fireproof) fireproof()
    if (customDamage != null) customDamage(customDamage)
    if (equipmentSlot != null) equipmentSlot(equipmentSlot)
    if (group != null) group(group)
    otherSettings.forEach { (setting, value) ->
        @Suppress("UNCHECKED_CAST")
        customSetting(setting as CustomItemSetting<Any>, value)
    }
}

/**
 * Build a [QuiltItemSettings] with builder-style syntax.
 */
fun buildItemSettings(block: QuiltItemSettings.() -> Unit): QuiltItemSettings =
    QuiltItemSettings().apply(block)

/**
 * Add a [CustomItemSetting] to this item settings.
 */
fun <T> QuiltItemSettings.customSetting(value: T, setting: CustomItemSetting<T>) {
    customSetting(setting, value)
}
