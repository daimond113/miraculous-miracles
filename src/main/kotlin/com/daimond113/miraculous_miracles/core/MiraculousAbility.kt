package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.content.MultitudeEntity
import com.daimond113.miraculous_miracles.content.PortalItem
import com.daimond113.miraculous_miracles.states.PlayerState
import net.minecraft.block.Blocks
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import virtuoel.pehkui.api.ScaleTypes
import java.util.*

// TODO: improve this mess
enum class MiraculousAbility(
    val id: Int,
    val miraculousType: MiraculousType,
    val execute: (ServerPlayerEntity, NbtCompound, HasBeenUsed, Any?) -> Result,
    val ignoresMinutes: Boolean = false,
    val givesMinutesLeft: Boolean = !ignoresMinutes,
    val withKeyBind: Boolean = true,
    val isToggleable: Boolean = false
) {
    @Suppress("unused")
    Venom(0, MiraculousType.Bee, { player, _, _, _ ->
        val stack = ItemStack(MiraculousMiracles.BEE_VENOM)
        stack.addEnchantment(Enchantments.VANISHING_CURSE, 1)

        PlayerState.giveItemStack(stack, player)

        Result.Success
    }),

    Shellter(1, MiraculousType.Turtle, { player, nbt, hasBeenUsed, landedPos ->
        val centrePos = if (hasBeenUsed.value)
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
                        if (hasBeenUsed.value) {
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

        Result.Success
    }, withKeyBind = false, isToggleable = true),

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

        Result.Success
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

        Result.Success
    }),

    @Suppress("unused")
    Voyage(
        4,
        MiraculousType.Horse,
        { player, nbt, _, _ ->
            if (!nbt.contains("x") || player.isSneaking) {
                ServerPlayNetworking.send(
                    player,
                    NetworkMessages.REQUEST_SET_PORTAL_COORDS,
                    PacketByteBufs.create().apply {
                        writeBoolean(false)
                    })
                Result.Fail
            } else {
                PlayerState.giveItemStack(
                    ItemStack(MiraculousMiracles.VOYAGE_ITEM).apply {
                        addEnchantment(Enchantments.VANISHING_CURSE, 1)

                        PortalItem.setDestination(
                            this,
                            BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")),
                            null
                        )
                    },
                    player
                )

                Result.Success
            }
        },
    ),

    @Suppress("unused")
    Burrow(
        5,
        MiraculousType.Rabbit,
        { player, nbt, _, _ ->
            if (player.world.registryKey != MiraculousMiracles.BURROW_WORLD_KEY) {
                if (!player.inventory.containsAny(setOf(MiraculousMiracles.BURROW_ITEM))) {
                    PlayerState.giveItemStack(
                        ItemStack(MiraculousMiracles.BURROW_ITEM).apply {
                            addEnchantment(Enchantments.VANISHING_CURSE, 1)

                            PortalItem.setDestination(
                                this, BlockPos(7, 1, 7),
                                MiraculousMiracles.BURROW_WORLD_KEY.value
                            )
                        },
                        player
                    )

                    Result.Success
                } else {
                    Result.Fail
                }
            } else if (!nbt.contains("x") || player.isSneaking) {
                ServerPlayNetworking.send(
                    player,
                    NetworkMessages.REQUEST_SET_PORTAL_COORDS,
                    PacketByteBufs.create().apply {
                        writeBoolean(true)
                    })

                Result.Fail
            } else {
                if (!player.inventory.containsAny(setOf(MiraculousMiracles.BURROW_ITEM))) {
                    PlayerState.giveItemStack(
                        ItemStack(MiraculousMiracles.BURROW_ITEM).apply {
                            addEnchantment(Enchantments.VANISHING_CURSE, 1)

                            PortalItem.setDestination(
                                this, BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")),
                                net.minecraft.util.Identifier(nbt.getString("dimension"))
                            )
                        },
                        player
                    )

                    Result.Success
                } else {
                    Result.Fail
                }
            }
        },
        givesMinutesLeft = false,
        ignoresMinutes = true,
    ),

    @Suppress("unused")
    Multitude(
        6,
        MiraculousType.Mouse,
        { player, nbt, hasBeenUsed, _ ->
            val debounceKey = "multitude-${player.uuidAsString}"

            if (hasBeenUsed != HasBeenUsed.TrueAuto && MiraculousMiracles.DEBOUNCES.contains(debounceKey)) {
                Result.Fail
            } else if (hasBeenUsed.value) {
                ScaleTypes.WIDTH.getScaleData(player).targetScale = ScaleTypes.WIDTH.defaultBaseScale
                ScaleTypes.HEIGHT.getScaleData(player).targetScale = ScaleTypes.HEIGHT.defaultBaseScale

                for (key in nbt.keys) {
                    if (!key.startsWith("multitudeEntity")) continue
                    (player.world as ServerWorld).getEntity(nbt.getUuid(key))?.remove(Entity.RemovalReason.DISCARDED)
                }

                Result.Success
            } else if (!nbt.contains("amount") || player.isSneaking) {
                ServerPlayNetworking.send(
                    player,
                    NetworkMessages.REQUEST_SET_MULTITUDE_AMOUNT,
                    PacketByteBufs.empty()
                )

                Result.Fail
            } else {
                MiraculousMiracles.DEBOUNCES[debounceKey] = 30

                val maxAmount = 16f
                val amount = nbt.getInt("amount")
                val scale = 1 - (amount / maxAmount) * 0.65f

                ScaleTypes.WIDTH.getScaleData(player).targetScale = scale
                ScaleTypes.HEIGHT.getScaleData(player).targetScale = scale

                repeat(amount - 1) {
                    val fakePlayerEntity = MultitudeEntity(player.world, player)
                    fakePlayerEntity.setPos(player.x, player.y, player.z)
                    fakePlayerEntity.yaw = player.yaw
                    fakePlayerEntity.pitch = player.pitch

                    EquipmentSlot.values().forEach { slot ->
                        fakePlayerEntity.equipStack(slot, player.getEquippedStack(slot).copy())
                        fakePlayerEntity.setEquipmentDropChance(slot, 0f)
                    }

                    ScaleTypes.WIDTH.getScaleData(fakePlayerEntity).scale = scale
                    ScaleTypes.HEIGHT.getScaleData(fakePlayerEntity).targetScale = scale

                    player.world.spawnEntity(fakePlayerEntity)

                    nbt.putUuid("multitudeEntity$it", fakePlayerEntity.uuid)
                }

                Result.Success
            }
        },
        isToggleable = true
    );

    enum class Result {
        Success,
        Fail
    }

    enum class HasBeenUsed(val value: Boolean) {
        True(true),
        False(false),
        TrueAuto(true);

        companion object {
            fun fromBoolean(bool: Boolean): HasBeenUsed {
                return if (bool) True else False
            }
        }
    }
}
