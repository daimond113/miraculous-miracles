package com.daimond113.miraculous_miracles.items

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
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class VoyageEntity : ThrownItemEntity {
    private var destination: Triple<Int, Int, Int>? = null

    constructor(entityType: EntityType<out VoyageEntity?>, world: World) : super(entityType, world)

    constructor(
        world: World,
        owner: LivingEntity,
        destination: Triple<Int, Int, Int>
    ) : super(MiraculousMiracles.VOYAGE_ENTITY, owner, world) {
        this.destination = destination
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        if (destination != null) {
            nbt.putIntArray("destination", destination!!.toList().toIntArray())
        }

        super.writeCustomDataToNbt(nbt)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)

        if (nbt.contains("portalDestination")) {
            destination = nbt.getIntArray("destination").let { Triple(it[0], it[1], it[2]) }
        }
    }

    override fun getDefaultItem(): Item {
        return MiraculousMiracles.VOYAGE_ITEM
    }

    private fun findSafePortalPos(initialPos: BlockPos): BlockPos? {
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

    private fun setPortalBlockAt(pos: BlockPos, destinationPos: BlockPos) {
        world.setBlockState(
            pos,
            MiraculousMiracles.VOYAGE_BLOCK.defaultState
                .with(Properties.HORIZONTAL_FACING, owner?.horizontalFacing?.opposite ?: Direction.NORTH),
            Block.NOTIFY_ALL or Block.REDRAW_ON_MAIN_THREAD
        )

        val portalEntity = world.getBlockEntity(pos)

        if (portalEntity is VoyageBlockEntity) {
            portalEntity.destination = destinationPos
            portalEntity.markDirty()
        }

        world.setBlockState(
            pos.up(), world.getBlockState(pos).with(VoyageBlock.HALF, DoubleBlockHalf.UPPER),
            Block.NOTIFY_ALL or Block.REDRAW_ON_MAIN_THREAD or Block.FORCE_STATE
        )

        world.scheduleBlockTick(pos, MiraculousMiracles.VOYAGE_BLOCK, 100)
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)

        if (!world.isClient && !this.isRemoved && destination != null) {
            val startPos = findSafePortalPos(blockPos)
            val destPos = findSafePortalPos(BlockPos(destination!!.first, destination!!.second, destination!!.third))

            if (startPos != null && destPos != null) {
                setPortalBlockAt(startPos, destPos)
                setPortalBlockAt(destPos, startPos)
            } else {
                val playerState = ServerState.getPlayerState(owner as PlayerEntity)
                playerState.usedAbilities.remove(MiraculousAbility.Voyage)
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
