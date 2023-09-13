package com.daimond113.miraculous_miracles.content

import com.daimond113.miraculous_miracles.MiraculousMiracles
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.enums.DoubleBlockHalf
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.random.RandomGenerator
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.TeleportTarget
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import org.quiltmc.qkl.library.blocks.blockSettingsOf
import org.quiltmc.qsl.worldgen.dimension.api.QuiltDimensions


class PortalBlock(private val isBurrow: Boolean) :
    BlockWithEntity(blockSettingsOf(material = Material.PORTAL, hardness = -1.0f, resistance = 3600000.0f)) {
    companion object {
        val HALF: EnumProperty<DoubleBlockHalf> = Properties.DOUBLE_BLOCK_HALF
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }

    init {
        defaultState = stateManager.defaultState
            .with(HALF, DoubleBlockHalf.LOWER)
            .with(FACING, Direction.NORTH)
    }

    @Deprecated(
        "overriding is fine", ReplaceWith(
            "state.with(Crucible.FACING, rotation.rotate(state.get(FACING)))",
            "com.daimond113.miraculous_miracles.items.PortalBlock.Companion.FACING"
        )
    )
    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(Crucible.FACING, rotation.rotate(state.get(FACING)))
    }

    @Deprecated(
        "overriding is fine", ReplaceWith(
            "state.rotate(mirror.getRotation(state.get(FACING)))",
            "com.daimond113.miraculous_miracles.items.PortalBlock.Companion.FACING"
        )
    )
    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state.get(FACING)))
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return if (isBurrow) VoyageBlockEntity(pos, state) else VoyageBlockEntity(pos, state)
    }

    @Deprecated("overriding is fine", ReplaceWith("world.breakBlock(pos, false)"))
    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: RandomGenerator) {
        onBreak(world, pos, state, null)
        world.removeBlock(pos, false)
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER))
    }

    @Deprecated("overriding is fine", ReplaceWith("BlockRenderType.MODEL", "net.minecraft.block.BlockRenderType"))
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    private fun removeBlockIfSameType(
        world: World,
        state: BlockState,
        blockPos: BlockPos,
        player: PlayerEntity? = null
    ) {
        val blockState = world.getBlockState(blockPos)
        if (blockState.isOf(state.block)) {
            world.setBlockState(blockPos, Blocks.AIR.defaultState, NOTIFY_ALL or SKIP_DROPS)
            player?.let {
                spawnBreakParticles(world, it, blockPos, state)
            }
        }
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity?) {
        if (!world.isClient) {
            val half = state.get(HALF)
            val downBlockEntity = world.getBlockEntity(if (half == DoubleBlockHalf.UPPER) pos.down() else pos)

            if (downBlockEntity is AbstractPortalBlockEntity) {
                downBlockEntity.destination?.let { destination ->
                    removeBlockIfSameType(world, state, destination, player)
                    removeBlockIfSameType(world, state, destination.up(), player)
                }
            }

            removeBlockIfSameType(world, state, if (half == DoubleBlockHalf.UPPER) pos.down() else pos.up(), player)
        }

        if (player != null) {
            spawnBreakParticles(world, player, pos, state)
        }

        world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.create(player, state))
    }

    @Deprecated("overriding is fine")
    override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
        if (!world.isClient && entity.canUsePortals() && !entity.hasNetherPortalCooldown()) {
            val dataHolderEntity =
                world.getBlockEntity(if (state.get(HALF) == DoubleBlockHalf.UPPER) pos.down() else pos)
            if (dataHolderEntity is AbstractPortalBlockEntity) {
                dataHolderEntity.destination?.let {
                    val destinationWorld = if (dataHolderEntity.dimension != null)
                        entity.server!!.getWorld(RegistryKey.of(Registry.WORLD_KEY, dataHolderEntity.dimension))
                            ?: run {
                                MiraculousMiracles.LOGGER.warn("Couldn't get portal destination world! ID: ${dataHolderEntity.destination}. Defaulting to entity's current world.")
                                entity.world
                            }
                    else
                        entity.world

                    val destinationBlockState = destinationWorld.getBlockState(it)
                    if (destinationBlockState.block !is PortalBlock) return@let
                    val destinationFacing = destinationBlockState.get(FACING)

                    val offsetMultiplier = 1.0
                    val (x, y, z) = Triple(
                        it.x + 0.5 + (destinationFacing.offsetX * offsetMultiplier),
                        it.y + 0.2,
                        it.z + 0.5 + (destinationFacing.offsetZ * offsetMultiplier)
                    )

                    if (entity is ServerPlayerEntity) {
                        entity.inTeleportationState = true // it is automatically unset, we needn't worry about that
                    }


                    QuiltDimensions.teleport<Entity>(
                        entity, destinationWorld as ServerWorld, TeleportTarget(
                            Vec3d(x, y, z),
                            Vec3d.ZERO,
                            entity.yaw,
                            entity.pitch,
                        )
                    )

                    entity.netherPortalCooldown += 40
                }
            }
        }
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(HALF, FACING)
    }

    @Deprecated("overriding is fine", ReplaceWith(""))
    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return when (state.get(FACING)) {
            Direction.NORTH, Direction.SOUTH -> VoxelShapes.cuboid(0.0, 0.0, 0.5, 1.0, 1.0, 0.5015625)
            Direction.EAST, Direction.WEST -> VoxelShapes.cuboid(
                0.49921874999999993,
                0.0,
                0.0007812499999999556,
                0.50078125,
                1.0,
                1.00078125
            )

            else -> {
                VoxelShapes.fullCube()
            }
        }
    }


    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return super.getPlacementState(ctx)!!
            .with(FACING, ctx.playerFacing.opposite)
    }
}
