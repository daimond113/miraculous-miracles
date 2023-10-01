package com.daimond113.miraculous_miracles.core

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.tag.ItemTags

enum class MiraculousType(val id: Int, val foodPredicate: (ItemStack) -> Boolean, val repairItem: Item) {
    Bee(
        0,
        { itemStack ->
            itemStack.isIn(ItemTags.FLOWERS) || itemStack.item == Items.HONEY_BOTTLE
        },
        Items.HONEYCOMB
    ),
    Turtle(
        1,
        { itemStack ->
            itemStack.item == Items.SEAGRASS || itemStack.item == Items.KELP
        },
        Items.SCUTE
    ),
    Snake(
        2,
        { itemStack ->
            (itemStack.item.isFood && itemStack.item.foodComponent!!.isMeat) || itemStack.item == Items.EGG
        },
        Items.LEATHER
    ),
    Ladybug(
        3,
        { itemStack ->
            itemStack.item == Items.COOKIE
        },
        Items.COCOA_BEANS
    ),
    Horse(
        4,
        { itemStack ->
            itemStack.item == Items.APPLE || itemStack.item == Items.WHEAT
        },
        Items.SADDLE
    ),
    Rabbit(
        5,
        { itemStack ->
            itemStack.item == Items.CARROT
        },
        Items.RABBIT_HIDE
    ),
    Mouse(
        6,
        { itemStack ->
            !(itemStack.item.foodComponent?.isMeat ?: true)
        },
        Items.STRING
    ),
    Dog(
        7,
        { itemStack ->
            itemStack.item.foodComponent?.isMeat ?: false
        },
        Items.BONE
    ),
}
