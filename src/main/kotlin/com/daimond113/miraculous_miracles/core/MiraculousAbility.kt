package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.items.VoyageItem
import com.daimond113.miraculous_miracles.states.PlayerState
import net.minecraft.block.Blocks
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import java.util.Random

// TODO: improve this mess
enum class MiraculousAbility(
    val id: Int,
    val miraculousType: MiraculousType,
    val execute: (ServerPlayerEntity, NbtCompound, Boolean, Any?) -> AbilityResult,
    val ignoresMinutes: Boolean = false,
    val givesMinutesLeft: Boolean = !ignoresMinutes,
    val withKeybind: Boolean = true,
    val isToggleable: Boolean = false
) {
    @Suppress("unused")
    Venom(0, MiraculousType.Bee, { player, _, _, _ ->
        val stack = ItemStack(MiraculousMiracles.BEE_VENOM)
        stack.addEnchantment(Enchantments.VANISHING_CURSE, 1)

        PlayerState.giveItemStack(stack, player)

        AbilityResult.Success
    }),

    Shellter(1, MiraculousType.Turtle, { player, nbt, hasBeenUsed, landedPos ->
        val centrePos = if (hasBeenUsed)
            BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"))
        else run {
            val blockPos = (if (landedPos is BlockPos) landedPos else player.blockPos).add(0, 1, 0)
            nbt.putInt("x", blockPos.x)
            nbt.putInt("y", blockPos.y)
            nbt.putInt("z", blockPos.z)
            com.daimond113.miraculous_miracles.states.ServerState.getServerState(player.server).markDirty()
            blockPos
        }

        val halfSize = 5 / 2
        for (x in -halfSize..halfSize) {
            for (y in -halfSize..halfSize) {
                for (z in -halfSize..halfSize) {
                    if (x == -halfSize || x == halfSize || y == -halfSize || y == halfSize || z == -halfSize || z == halfSize) {
                        val pos = centrePos.add(x, y, z)
                        if (hasBeenUsed) {
                            if (!player.world.getBlockState(pos)
                                    .isOf(MiraculousMiracles.TURTLE_SHELLTER_BLOCK)
                            ) continue
                            player.world.setBlockState(pos, Blocks.AIR.defaultState)
                        } else {
                            if (!player.world.getBlockState(pos)
                                    .isIn(MiraculousMiracles.SAFELY_REPLACEABLE_TAG)
                            ) continue
                            player.world.setBlockState(pos, MiraculousMiracles.TURTLE_SHELLTER_BLOCK.defaultState)
                        }
                    }
                }
            }
        }

        AbilityResult.Success
    }, withKeybind = false, isToggleable = true),

    @Suppress("unused")
    SecondChance(2, MiraculousType.Snake, { player, nbt, _, _ ->
        val (x, y, z) = if (nbt.contains("x"))
            Triple(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"))
        else run {
            val blockPos = player.blockPos
            nbt.putInt("x", blockPos.x)
            nbt.putInt("y", blockPos.y)
            nbt.putInt("z", blockPos.z)
            com.daimond113.miraculous_miracles.states.ServerState.getServerState(player.server).markDirty()
            Triple(blockPos.x, blockPos.y, blockPos.z)
        }
        // TODO: possibly health & food?
        if (player.hasVehicle()) {
            player.requestTeleportAndDismount(x.toDouble(), y.toDouble(), z.toDouble())
        } else {
            player.requestTeleport(x.toDouble(), y.toDouble(), z.toDouble())
        }
        player.onLanding()

        AbilityResult.Success
    }, ignoresMinutes = true, givesMinutesLeft = true),

    @Suppress("unused")
    LuckyCharm(3, MiraculousType.Ladybug, { player, _, _, _ ->
        val luckyCharmType = run {
            val start = player.getCameraPosVec(1f)
            val end = start.add(player.getRotationVec(1f).multiply(5.0))
            val box = net.minecraft.util.math.Box(start, end)
            val entities = player.world.getOtherEntities(player, box) { true }
            if (entities.isNotEmpty()) return@run 0

            // note: this doesn't output entities, which is the reason for the code above
            if (player.raycast(5.0, 1f, false).type == HitResult.Type.BLOCK) return@run 1

            return@run Random().nextInt(2)
        }

        PlayerState.giveItemStack(
            ItemStack(
                arrayOf(
                    MiraculousMiracles.LUCKY_CHARM_SWORD,
                    MiraculousMiracles.LUCKY_CHARM_PICKAXE
                )[luckyCharmType]
            ).apply {
                addEnchantment(Enchantments.VANISHING_CURSE, 1)
            },
            player
        )

        AbilityResult.Success
    }),

    @Suppress("unused")
    Voyage(
        4,
        MiraculousType.Horse,
        { player, nbt, _, _ ->
            if (!nbt.contains("x") || player.isSneaking) {
                ServerPlayNetworking.send(player, NetworkMessages.REQUEST_SET_VOYAGE_COORDS, PacketByteBufs.empty())
                AbilityResult.Fail
            } else {
                PlayerState.giveItemStack(
                    ItemStack(MiraculousMiracles.VOYAGE_ITEM).apply {
                        addEnchantment(Enchantments.VANISHING_CURSE, 1)

                        VoyageItem.setDestination(this, Triple(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")))
                    },
                    player
                )

                AbilityResult.Success
            }
        },
    );
}
