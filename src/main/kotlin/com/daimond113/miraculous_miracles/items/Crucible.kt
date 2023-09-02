package com.daimond113.miraculous_miracles.items

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.AbstractMiraculous
import com.daimond113.miraculous_miracles.core.MiraculousType
import com.daimond113.miraculous_miracles.states.PlayerState
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import org.quiltmc.qkl.library.blocks.blockSettingsOf
import java.util.*


class Crucible :
    BlockWithEntity(blockSettingsOf(material = Material.METAL, requiresTool = true, hardness = 5f, resistance = 6f, soundGroup = BlockSoundGroup.METAL)) {
    companion object {
        val FILLED: BooleanProperty = BooleanProperty.of("filled")
    }

    init {
        defaultState = stateManager.defaultState
            .with(FILLED, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FILLED)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return CrucibleEntity(pos, state)
    }

    @Deprecated("Overriding is fine", ReplaceWith("BlockRenderType.MODEL", "net.minecraft.block.BlockRenderType"))
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return checkType(
            type,
            MiraculousMiracles.CRUCIBLE_ENTITY
        ) { worldInner, pos, state1, be ->
            CrucibleEntity.tick(
                worldInner,
                pos,
                state1,
                be
            )
        }
    }

    @Deprecated("Overriding is fine")
    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        val stack = player.getStackInHand(hand)
        val blockEntity = world.getBlockEntity(pos)

        if (blockEntity !is CrucibleEntity) return ActionResult.PASS

        val filled = state.get(FILLED)

        if (stack.isOf(Items.GOLD_INGOT) && !filled && blockEntity.isHeated) {
            if (!player.isCreative) {
                stack.decrement(1)
            }

            if (!world.isClient) {
                world.playSound(
                    null,
                    pos,
                    SoundEvents.BLOCK_FIRE_AMBIENT,
                    SoundCategory.BLOCKS,
                    1f,
                    1f,
                )
            }

            world.setBlockState(pos, state.with(FILLED, true))

            return ActionResult.SUCCESS
        }

        if (!world.isClient) {
            if (((stack.isOf(MiraculousMiracles.METEORITE_POWDER) && !blockEntity.hasMeteoritePowder) || (MiraculousType.values()
                    .firstOrNull { stack.isOf(it.repairItem) } != null && blockEntity.ingredient == null)) && filled
            ) {
                world.playSound(
                    null,
                    pos,
                    SoundEvents.ENTITY_GENERIC_BURN,
                    SoundCategory.BLOCKS,
                    0.5f,
                    1f,
                )

                if (stack.isOf(MiraculousMiracles.METEORITE_POWDER)) {
                    blockEntity.hasMeteoritePowder = true
                } else {
                    blockEntity.ingredient = stack.item
                }

                blockEntity.markDirty()

                if (!player.isCreative) {
                    stack.decrement(1)
                }

                return ActionResult.SUCCESS
            }
        }

        if (stack.item is AbstractMiraculous && stack.damage > 0 && blockEntity.hasMeteoritePowder && blockEntity.ingredient == (stack.item as AbstractMiraculous).miraculousType.repairItem && filled && blockEntity.miraculousType == null) {
            blockEntity.hasMeteoritePowder = false
            blockEntity.ingredient = null

            blockEntity.startedAt = world.time / 20
            blockEntity.miraculousType = (stack.item as AbstractMiraculous).miraculousType
            world.setBlockState(pos, state.with(FILLED, false))

            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS)

            if (!world.isClient) {
                val kwamiUuid = AbstractMiraculous.getOptionalKwamiUuid(stack)

                if (kwamiUuid != null) {
                    (world as ServerWorld).getEntity(kwamiUuid)?.remove(Entity.RemovalReason.DISCARDED)
                }

                AbstractMiraculous.setNBT(stack, kwamiUuid = Optional.empty())


                PlayerState.sendMessageFrom(
                    Text.translatable("text.miraculous_miracles.repair_miraculous"),
                    player as ServerPlayerEntity
                )
            }

            blockEntity.miraculousNbt = stack.nbt

            blockEntity.markDirty()

            stack.count = 0

            return ActionResult.SUCCESS
        }

        return ActionResult.PASS
    }

    @Deprecated("")
    override fun getOutlineShape(
        state: BlockState?,
        view: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        var shape = VoxelShapes.empty()
        shape = VoxelShapes.combine(
            shape,
            VoxelShapes.cuboid(0.1875, 0.0, 0.1875, 0.8125, 0.125, 0.8125),
            BooleanBiFunction.OR
        )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.3125, 0.125, 0.6875, 0.6875, 0.5, 0.8125),
                BooleanBiFunction.OR
            )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.1875, 0.125, 0.3125, 0.3125, 0.5, 0.6875),
                BooleanBiFunction.OR
            )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.6875, 0.125, 0.3125, 0.8125, 0.5, 0.6875),
                BooleanBiFunction.OR
            )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.5625, 0.125, 0.1875, 0.6875, 0.5, 0.3125),
                BooleanBiFunction.OR
            )
        shape = VoxelShapes.combine(
            shape,
            VoxelShapes.cuboid(0.3125, 0.125, 0.3125, 0.6875, 0.13749999999999996, 0.6875),
            BooleanBiFunction.OR
        )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.71875, 0.5, 0.5125, 0.78125, 0.75, 0.54375),
                BooleanBiFunction.OR
            )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.1875, 0.75, 0.4375, 0.8125, 0.875, 0.5625),
                BooleanBiFunction.OR
            )
        shape = VoxelShapes.combine(
            shape,
            VoxelShapes.cuboid(0.71875, 0.5, 0.45625000000000004, 0.78125, 0.75, 0.48750000000000004),
            BooleanBiFunction.OR
        )
        shape = VoxelShapes.combine(
            shape,
            VoxelShapes.cuboid(0.21875, 0.5, 0.45625000000000004, 0.28125, 0.75, 0.48750000000000004),
            BooleanBiFunction.OR
        )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.21875, 0.5, 0.5125, 0.28125, 0.75, 0.54375),
                BooleanBiFunction.OR
            )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.3125, 0.125, 0.1875, 0.4375, 0.5, 0.3125),
                BooleanBiFunction.OR
            )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.4375, 0.125, 0.1875, 0.5625, 0.4375, 0.3125),
                BooleanBiFunction.OR
            )
        shape = VoxelShapes.combine(
            shape,
            VoxelShapes.cuboid(0.48125, 0.4378125, 0.150625, 0.5187499999999999, 0.4409375, 0.169375),
            BooleanBiFunction.OR
        )
        shape = VoxelShapes.combine(
            shape,
            VoxelShapes.cuboid(0.4625, 0.4378125, 0.169375, 0.5375, 0.4409375, 0.188125),
            BooleanBiFunction.OR
        )
        shape =
            VoxelShapes.combine(
                shape,
                VoxelShapes.cuboid(0.3125, 0.125, 0.3125, 0.6875, 0.4375, 0.6875),
                BooleanBiFunction.OR
            )

        return shape!!
    }
}
