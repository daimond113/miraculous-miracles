package com.daimond113.miraculous_miracles.items

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.util.Identifier
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion
import org.quiltmc.qkl.library.nbt.string


class CrucibleEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(MiraculousMiracles.CRUCIBLE_ENTITY, pos, state) {
    var isHeated = false
    var hasMeteoritePowder = false
    var ingredient: Item? = null
    var miraculousType: MiraculousType? = null
    var miraculousNbt: NbtCompound? = null
    var startedAt: Long? = null

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        hasMeteoritePowder = nbt.getBoolean("hasMeteoritePowder")
        ingredient =
            if (nbt.contains("ingredient")) Registry.ITEM.get(Identifier(nbt.getString("ingredient"))) else null
        miraculousType =
            if (nbt.contains("miraculousType")) MiraculousType.valueOf(nbt.getString("miraculousType")) else null
        miraculousNbt = if (nbt.contains("miraculousNbt")) nbt.getCompound("miraculousNbt") else null
        startedAt = if (nbt.contains("startedAt")) nbt.getLong("startedAt") else null
    }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putBoolean("hasMeteoritePowder", hasMeteoritePowder)
        ingredient?.let { nbt.putString("ingredient", Registry.ITEM.getId(it).toString()) }
        miraculousType?.let { nbt.putString("miraculousType", it.name) }
        miraculousNbt?.let { nbt.put("miraculousNbt", it) }
        startedAt?.let { nbt.putLong("startedAt", it) }
        super.writeNbt(nbt)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.of(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return toNbt()
    }

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, be: CrucibleEntity) {
            be.isHeated = arrayOf(world.getBlockState(pos.down()), world.getBlockState(pos.down(2))).firstOrNull {
                it.isOf(Blocks.FIRE) || it.isOf(Blocks.LAVA) || it.isOf(Blocks.CAMPFIRE) || it.isOf(Blocks.SOUL_CAMPFIRE)
            } != null
            world.setBlockState(pos, state)

            if (be.startedAt != null) {
                val currentTime = world.time / 20.0
                if (currentTime >= be.startedAt!! + 5) {
                    be.startedAt = null

                    val (x, y, z) = Triple(
                        pos.x.toDouble(),
                        pos.y.toDouble() + 3,
                        pos.z.toDouble(),
                    )

                    world.createExplosion(
                        null,
                        x,
                        y,
                        z,
                        0f,
                        false,
                        Explosion.DestructionType.NONE,
                    )

                    ItemScatterer.spawn(
                        world,
                        x,
                        y,
                        z,
                        ItemStack(MiraculousMiracles.MIRACULOUSES[be.miraculousType]).apply {
                            nbt = be.miraculousNbt
                            damage = 0
                        })

                    be.miraculousType = null
                    be.miraculousNbt = null

                    be.markDirty()

                    world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS)
                }
            }
        }
    }
}
