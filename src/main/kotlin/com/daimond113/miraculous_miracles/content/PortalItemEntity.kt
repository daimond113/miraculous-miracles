package com.daimond113.miraculous_miracles.content

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.MiraculousAbility
import com.daimond113.miraculous_miracles.states.ServerState
import net.minecraft.block.Block
import net.minecraft.block.enums.DoubleBlockHalf
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

class PortalItemEntity : ThrownItemEntity {
    private var destination: BlockPos? = null
    private var dimension: Identifier? = null
    private var isBurrow = false

    constructor(entityType: EntityType<out PortalItemEntity?>, world: World) : super(entityType, world)

    constructor(
        world: World,
        owner: LivingEntity,
        destination: Pair<BlockPos, Identifier?>,
        isBurrow: Boolean
    ) : super(if (isBurrow) MiraculousMiracles.BURROW_ENTITY else MiraculousMiracles.VOYAGE_ENTITY, owner, world) {
        this.destination = destination.first
        this.dimension = destination.second
        this.isBurrow = isBurrow
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        destination?.let {
            nbt.putIntArray("destination", intArrayOf(it.x, it.y, it.z))
        }

        dimension?.let {
            nbt.putString("dimension", it.toString())
        }

        super.writeCustomDataToNbt(nbt)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)

        if (nbt.contains("destination")) {
            destination = nbt.getIntArray("destination").let { BlockPos(it[0], it[1], it[2]) }
        }

        if (nbt.contains("dimension")) {
            dimension = Identifier(nbt.getString("dimension"))
        }
    }

    override fun getDefaultItem(): Item {
        return if (isBurrow) MiraculousMiracles.BURROW_ITEM else MiraculousMiracles.VOYAGE_ITEM
    }

    private fun findSafePortalPos(initialPos: BlockPos, world: World): BlockPos? {
        val remainingY = (world.dimension.minY + world.dimension.height) - initialPos.y
        if (remainingY > 2) {
            for (y in 0 until remainingY) {
                val pos = initialPos.up(y)
                val posUp = pos.up(1)

                if (world.getBlockState(pos).isIn(MiraculousMiracles.SAFELY_REPLACEABLE_TAG) &&
                    world.getBlockState(posUp).isIn(MiraculousMiracles.SAFELY_REPLACEABLE_TAG)
                ) {
                    return pos
                }
            }
        }

        val remainingYDown = world.dimension.minY - remainingY

        if (remainingYDown > 2) {
            for (y in 0 until remainingYDown) {
                val pos = initialPos.up(y)
                val posUp = pos.up(1)

                if (world.getBlockState(pos).isIn(MiraculousMiracles.SAFELY_REPLACEABLE_TAG) &&
                    world.getBlockState(posUp).isIn(MiraculousMiracles.SAFELY_REPLACEABLE_TAG)
                ) {
                    return pos
                }
            }
        }

        return null
    }

    private fun setPortalBlockAt(pos: BlockPos, destinationPos: BlockPos, world: World) {
        val block = if (isBurrow) MiraculousMiracles.BURROW_BLOCK else MiraculousMiracles.VOYAGE_BLOCK

        world.setBlockState(
            pos,
            block.defaultState
                .with(Properties.HORIZONTAL_FACING, owner?.horizontalFacing?.opposite ?: Direction.NORTH),
            Block.NOTIFY_ALL or Block.REDRAW_ON_MAIN_THREAD
        )

        val portalEntity = world.getBlockEntity(pos)

        if (portalEntity is AbstractPortalBlockEntity) {
            portalEntity.destination = destinationPos
            portalEntity.dimension = dimension
            portalEntity.markDirty()
        }

        world.setBlockState(
            pos.up(), world.getBlockState(pos).with(PortalBlock.HALF, DoubleBlockHalf.UPPER),
            Block.NOTIFY_ALL or Block.REDRAW_ON_MAIN_THREAD or Block.FORCE_STATE
        )

        world.scheduleBlockTick(pos, block, 100)
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)

        if (!world.isClient && !this.isRemoved && destination != null) {
            val destinationWorld = if (dimension != null) server!!.getWorld(RegistryKey.of(Registry.WORLD_KEY, dimension)) ?: run {
                MiraculousMiracles.LOGGER.warn("Couldn't get portal destination world! ID: ${dimension}. Defaulting to entity's current world.")
                world
            } else world

            val startPos = findSafePortalPos(blockPos, world)
            val destPos = findSafePortalPos(BlockPos(destination!!.x, destination!!.y, destination!!.z), destinationWorld)

            if (startPos != null && destPos != null) {
                setPortalBlockAt(startPos, destPos, world)
                setPortalBlockAt(destPos, startPos, destinationWorld)
            } else {
                val playerState = ServerState.getPlayerState(owner as PlayerEntity)
                playerState.usedAbilities.remove(if (isBurrow) MiraculousAbility.Burrow else MiraculousAbility.Voyage)
            }

            discard()
        }
    }

    override fun tick() {
        if (owner is PlayerEntity && !(owner as PlayerEntity).isAlive) {
            discard()
        } else {
            super.tick()
        }
    }

    override fun moveToWorld(destination: ServerWorld): Entity? {
        if (owner != null && owner!!.world.registryKey !== destination.registryKey) {
            owner = null
        }

        return super.moveToWorld(destination)
    }

    override fun canUsePortals(): Boolean {
        return false
    }
}
