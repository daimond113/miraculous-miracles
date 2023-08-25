package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.states.PlayerState
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity

enum class MiraculousAbility(
    val id: Int,
    val miraculousType: MiraculousType,
    val usableMultipleTimes: Boolean,
    val execute: (ServerPlayerEntity, NbtCompound) -> Unit
) {
    venom(0, MiraculousType.Bee, false, { player, _ ->
        val stack = ItemStack(MiraculousMiracles.BEE_VENOM)
        stack.addEnchantment(Enchantments.VANISHING_CURSE, 1)

        PlayerState.giveItemStack(stack, player)
    });
}
