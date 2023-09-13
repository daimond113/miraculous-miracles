package com.daimond113.miraculous_miracles.content

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

abstract class AbstractPortalBlockEntity(
    entityType: BlockEntityType<out AbstractPortalBlockEntity>,
    pos: BlockPos,
    state: BlockState?
) : BlockEntity(entityType, pos, state) {
    var destination: BlockPos? = null
    var dimension: Identifier? = null

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)

        destination = if (nbt.contains("destination")) nbt.getIntArray("destination")
            .let { BlockPos(it[0], it[1], it[2]) } else null

        dimension = if (nbt.contains("dimension")) Identifier(nbt.getString("dimension")) else null
    }

    override fun writeNbt(nbt: NbtCompound) {
        destination?.let { nbt.putIntArray("destination", intArrayOf(it.x, it.y, it.z)) }
        dimension?.let { nbt.putString("dimension", it.toString()) }

        super.writeNbt(nbt)
    }
}
