package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.MiraculousMiracles
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.LookAroundGoal
import net.minecraft.entity.ai.goal.TemptGoal
import net.minecraft.entity.ai.goal.WanderAroundGoal
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.tag.ItemTags
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

val kwamiFoods = mapOf(
    Pair(MiraculousType.Bee) { itemStack: ItemStack ->
        itemStack.isIn(ItemTags.FLOWERS) || itemStack.item == Items.HONEY_BOTTLE
    },
    Pair(MiraculousType.Turtle) { itemStack: ItemStack ->
        itemStack.item == Items.SEAGRASS || itemStack.item == Items.KELP
    },
    Pair(MiraculousType.Snake) { itemStack: ItemStack ->
        (itemStack.item.isFood && itemStack.item.foodComponent!!.isMeat) || itemStack.item == Items.EGG
    }
)

abstract class AbstractKwami(
    val miraculousType: MiraculousType, entityType: EntityType<out AbstractKwami>, world: World
) : PathAwareEntity(
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
            return createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.65).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.8)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
        }
    }

    override fun interactMob(player: PlayerEntity, hand: Hand): ActionResult {
        if (!isHungry || player.world.server == null) return ActionResult.PASS
        val itemStack = player.getStackInHand(hand)
        if (!kwamiFoods[miraculousType]!!(itemStack)) return ActionResult.PASS

        var miraculousStack: ItemStack? = null
        for (i in 0 until player.inventory.size()) {
            val stack = player.inventory.getStack(i)
            if (stack.item is AbstractMiraculous && (stack.item as AbstractMiraculous).miraculousType == miraculousType && AbstractMiraculous.getOptionalKwamiUuid(
                    stack
                ) == uuid
            ) {
                miraculousStack = stack
                break
            }
        }

        if (miraculousStack == null) return ActionResult.PASS

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
            itemStack.decrement(1)
        }

        isHungry = false
        return ActionResult.CONSUME
    }

    override fun initGoals() {
        goalSelector.add(
            1,
            TemptGoal(this, 1.25, Ingredient.ofItems(*arrayOf(MiraculousMiracles.MIRACULOUSES[miraculousType])), false)
        )
        goalSelector.add(3, WanderAroundGoal(this, 1.0, 200, false))
        goalSelector.add(4, LookAroundGoal(this))
    }

    override fun canBeLeashedBy(player: PlayerEntity): Boolean {
        return false
    }

    override fun fall(
        heightDifference: Double, onGround: Boolean, landedState: BlockState?, landedPosition: BlockPos?
    ) {

    }
}
