package com.daimond113.miraculous_miracles.core

import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.passive.TameableEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

abstract class UnspawnableEntity(entityType: EntityType<out TameableEntity>?, world: World?) : TameableEntity(entityType, world) {
    override fun isCustomNameVisible(): Boolean {
        return false
    }

    override fun createChild(world: ServerWorld, entity: PassiveEntity): PassiveEntity? {
        return null
    }

    override fun canSpawn(world: WorldAccess?, spawnReason: SpawnReason?): Boolean {
        return false
    }

    override fun canSpawn(world: WorldView?): Boolean {
        return false
    }

    override fun canBreedWith(other: AnimalEntity?): Boolean {
        return false
    }

    override fun isBreedingItem(stack: ItemStack?): Boolean {
        return false
    }
}
