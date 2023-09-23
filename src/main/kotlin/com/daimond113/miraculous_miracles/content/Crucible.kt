package com.daimond113.miraculous_miracles.content

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.AbstractMiraculous
import com.daimond113.miraculous_miracles.core.MiraculousType
import com.daimond113.miraculous_miracles.state.PlayerState
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import org.quiltmc.qkl.library.blocks.blockSettingsOf
import java.util.*


class Crucible :
    BlockWithEntity(
        blockSettingsOf(
            material = Material.METAL,
            requiresTool = true,
            hardness = 5f,
            resistance = 6f,
            soundGroup = BlockSoundGroup.METAL,
            lootTableId = Identifier(MiraculousMiracles.MOD_ID, "blocks/crucible")
        )
    ) {
    companion object {
        val FILLED: BooleanProperty = BooleanProperty.of("filled")
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }

    @Deprecated(
        "overriding is fine", ReplaceWith(
            "state.with(FACING, rotation.rotate(state.get(FACING)))",
            "com.daimond113.miraculous_miracles.items.Crucible.Companion.FACING",
            "com.daimond113.miraculous_miracles.items.Crucible.Companion.FACING"
        )
    )
    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(FACING, rotation.rotate(state.get(FACING)))
    }

    @Deprecated(
        "overriding is fine", ReplaceWith(
            "state.rotate(mirror.getRotation(state.get(FACING)))",
            "com.daimond113.miraculous_miracles.items.Crucible.Companion.FACING"
        )
    )
    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state.get(FACING)))
    }

    init {
        defaultState = stateManager.defaultState
            .with(FILLED, false)
            .with(FACING, Direction.NORTH)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FILLED, FACING)
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

    @Deprecated(
        "overriding is fine", ReplaceWith(
            "VoxelShapes.cuboid(0.1875, 0.0, 0.1875, 0.8125, 0.875, 0.8125)",
            "net.minecraft.util.shape.VoxelShapes"
        )
    )
    override fun getOutlineShape(
        state: BlockState?,
        view: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        return VoxelShapes.cuboid(0.1875, 0.0, 0.1875, 0.8125, 0.875, 0.8125)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return super.getPlacementState(ctx)!!
            .with(FACING, ctx.playerFacing.opposite)
    }
}
