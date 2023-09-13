package com.daimond113.miraculous_miracles.content

import com.daimond113.miraculous_miracles.MiraculousMiracles
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class BurrowBlockEntity(pos: BlockPos, state: BlockState) :
    AbstractPortalBlockEntity(MiraculousMiracles.BURROW_BLOCK_ENTITY, pos, state)
