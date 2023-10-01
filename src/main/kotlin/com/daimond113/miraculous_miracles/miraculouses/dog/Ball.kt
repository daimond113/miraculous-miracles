package com.daimond113.miraculous_miracles.miraculouses.dog

import com.daimond113.miraculous_miracles.core.AbstractWeapon
import com.daimond113.miraculous_miracles.core.MiraculousAbility
import com.daimond113.miraculous_miracles.state.ServerState
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.TeleportTarget
import net.minecraft.world.World
import org.quiltmc.qsl.worldgen.dimension.api.QuiltDimensions
import java.util.*


class Ball : AbstractWeapon("ball") {
    companion object {
        fun storeItem(itemStack: ItemStack, stored: ItemEntity): Boolean {
            itemStack.nbt = itemStack.orCreateNbt.apply {
                val currentAmount = (keys
                    .filter { it.startsWith("stored") }
                    .onEach {
                        if (getUuid(it) == stored.uuid) {
                            return@storeItem false
                        }
                    }
                    .maxOfOrNull { it.removePrefix("stored").toInt() }
                    ?: 0
                    ) + 1
                if (currentAmount > 13) {
                    return false
                }
                putUuid("stored${currentAmount}", stored.uuid)
            }

            return true
        }

        fun getStoredItems(itemStack: ItemStack): List<UUID> {
            return itemStack.orCreateNbt.run {
                keys.filter { it.startsWith("stored") }.map { getUuid(it) }
            }
        }
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = player.getStackInHand(hand)

        if (!world.isClient) {
            if (player.isSneaking) {
                val storedItems = getStoredItems(itemStack)
                if (storedItems.isNotEmpty()) {
                    val storedItem = storedItems.last()
                    val entity = (world as ServerWorld).getEntity(storedItem)
                    if (entity is ItemEntity) {
                        ServerState.getPlayerState(player).useAbility(MiraculousAbility.Fetch, player as ServerPlayerEntity, null) // give minutes, say the ability name

                        QuiltDimensions.teleport<ItemEntity>(
                            entity,
                            world,
                            TeleportTarget(
                                player.pos,
                                player.velocity,
                                player.yaw,
                                player.pitch
                            )
                        )

                        itemStack.nbt = itemStack.orCreateNbt.apply {
                            remove("stored${storedItems.size}")
                        }

                        return TypedActionResult.success(itemStack)
                    }
                }
            }

            val start = player.getCameraPosVec(1f)
            val end = start.add(player.getRotationVec(1f).multiply(3.0))
            val box = net.minecraft.util.math.Box(start, end)
            val entity = player.world.getOtherEntities(player, box) { it is ItemEntity }.firstOrNull()

            if (entity != null) {
                return if (storeItem(itemStack, entity as ItemEntity))
                    TypedActionResult.success(itemStack)
                else
                    TypedActionResult.fail(itemStack)
            }

            return TypedActionResult.pass(itemStack)
        }

        return TypedActionResult.pass(itemStack)
    }
}
