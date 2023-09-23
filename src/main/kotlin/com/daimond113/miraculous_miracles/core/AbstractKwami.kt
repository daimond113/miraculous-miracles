package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.state.PlayerState
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class AbstractKwami(
    val miraculousType: MiraculousType, entityType: EntityType<out AbstractKwami>, world: World
) : UnspawnableEntity(
    entityType, world
) {
    var isHungry = false

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putBoolean("isHungry", isHungry)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        isHungry = nbt.getBoolean("isHungry")
        super.readCustomDataFromNbt(nbt)
    }

    companion object {
        fun createKwamiAttributes(): DefaultAttributeContainer.Builder {
            return createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
        }
    }

    private fun searchForMiraculous(player: PlayerEntity): ItemStack? {
        for (i in 0 until player.inventory.size()) {
            val stack = player.inventory.getStack(i)
            if (stack.item is AbstractMiraculous &&
                (stack.item as AbstractMiraculous).miraculousType == miraculousType &&
                AbstractMiraculous.getOptionalKwamiUuid(stack) == uuid
            ) {
                return stack
            }
        }

        return null
    }

    override fun interactMob(player: PlayerEntity, hand: Hand): ActionResult {
        if (!isHungry || player.world.server == null) return ActionResult.PASS

        val itemStack = player.getStackInHand(hand)
        if (!miraculousType.foodPredicate(itemStack)) return ActionResult.PASS

        val miraculousStack = searchForMiraculous(player)
            ?: owner?.let { searchForMiraculous(it as PlayerEntity) }
            ?: return ActionResult.PASS

        world.playSound(
            null,
            blockPos,
            SoundEvents.ENTITY_GENERIC_EAT,
            SoundCategory.NEUTRAL,
            1f,
            1f
        )

        AbstractMiraculous.setNBT(miraculousStack, kwamiHungry = false)

        if (!player.isCreative) {
            val isHoney = itemStack.item == Items.HONEY_BOTTLE

            itemStack.decrement(1)

            if (isHoney) {
                PlayerState.giveItemStack(ItemStack(Items.GLASS_BOTTLE), player as ServerPlayerEntity)
            }
        }

        isHungry = false

        return ActionResult.CONSUME
    }

    override fun initGoals() {
        goalSelector.add(
            1,
            TemptGoal(this, 1.25, Ingredient.ofItems(MiraculousMiracles.MIRACULOUSES[miraculousType]), false)
        )
        goalSelector.add(
            6,
            FollowOwnerGoal(this, 1.0, 3f, 0.5f, false)
        )
        goalSelector.add(
            8,
            WanderAroundFarGoal(this, 1.0)
        )
        goalSelector.add(
            10,
            LookAroundGoal(this)
        )
    }

    override fun canBeLeashedBy(player: PlayerEntity): Boolean {
        return false
    }

    override fun fall(
        heightDifference: Double, onGround: Boolean, landedState: BlockState?, landedPosition: BlockPos?
    ) {

    }
}
