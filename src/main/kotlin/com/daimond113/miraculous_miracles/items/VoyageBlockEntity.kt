package com.daimond113.miraculous_miracles.items

import com.daimond113.miraculous_miracles.MiraculousMiracles
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class VoyageBlockEntity(pos: BlockPos, state: BlockState?) : BlockEntity(
    MiraculousMiracles.VOYAGE_BLOCK_ENTITY, pos,
    state
) {
    var destination: BlockPos? = null

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        destination = if (nbt.contains("destination")) nbt.getIntArray("destination")
            .let { BlockPos(it[0], it[1], it[2]) } else null
    }

    override fun writeNbt(nbt: NbtCompound) {
        destination?.let { nbt.putIntArray("destination", intArrayOf(it.x, it.y, it.z)) }

        super.writeNbt(nbt)
    }
}
