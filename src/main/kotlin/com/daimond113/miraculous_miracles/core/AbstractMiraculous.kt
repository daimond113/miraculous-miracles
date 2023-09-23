package com.daimond113.miraculous_miracles.core

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.state.PlayerState
import com.daimond113.miraculous_miracles.state.ServerState
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.Rarity
import java.util.*

abstract class AbstractMiraculous(val miraculousType: MiraculousType, slot: ((ItemStack) -> EquipmentSlot)? = null) :
    Item(
        itemSettingsOf(
            rarity = Rarity.EPIC,
            fireproof = true,
            equipmentSlot = slot,
            group = MiraculousMiracles.ITEM_GROUP,
            maxDamage = 128
        )
    ) {
    companion object {
        protected fun getNBT(stack: ItemStack): NbtCompound {
            val nbt = stack.getOrCreateNbt()

            if (!nbt.contains("isHungry")) {
                nbt.putBoolean("isHungry", false)
            }

            return nbt
        }

        fun getCharged(stack: ItemStack): Boolean {
            val nbt = getNBT(stack)
            return !nbt.contains("kwamiUuid")
        }

        fun setNBT(stack: ItemStack, kwamiUuid: Optional<UUID>? = null, kwamiHungry: Boolean? = null) {
            val nbt = getNBT(stack)

            if (kwamiUuid != null) {
                if (kwamiUuid.isPresent) {
                    nbt.putUuid("kwamiUuid", kwamiUuid.get())
                } else {
                    nbt.remove("kwamiUuid")
                }
            }

            if (kwamiHungry != null) {
                nbt.putBoolean("isHungry", kwamiHungry)
            }

            stack.nbt = nbt
        }

        fun getOptionalKwamiUuid(stack: ItemStack): UUID? {
            val nbt = getNBT(stack)

            if (!nbt.contains("kwamiUuid")) return null
            return nbt.getUuid("kwamiUuid")
        }

        fun renounceKwami(
            stack: ItemStack,
            world: ServerWorld
        ) {
            val kwami = getOptionalKwamiUuid(stack)?.let { world.getEntity(it) }
            if (kwami !is AbstractKwami) return
            renounceKwami(stack, kwami)
        }

        fun renounceKwami(
            stack: ItemStack,
            kwami: AbstractKwami
        ) {
            kwami.remove(Entity.RemovalReason.DISCARDED)

            setNBT(stack, Optional.empty())
        }
    }

    override fun onItemEntityDestroyed(entity: ItemEntity) {
        renounceKwami(entity.stack, entity.world as ServerWorld)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player
        val itemStack = player?.getStackInHand(context.hand)
        val hitPos = context.hitPos

        if (itemStack == null || player.world?.server == null) return ActionResult.PASS

        var kwamiUuid = getOptionalKwamiUuid(itemStack)

        fun spawnKwami() {
            val newKwami = MiraculousMiracles.KWAMIS[miraculousType]!!.create(player.world)?.let {
                it.updatePosition(hitPos.x, hitPos.y, hitPos.z)

                kwamiUuid = it.uuid
                it.isHungry = getNBT(itemStack).getBoolean("isHungry")
                it.setOwner(player)

                it
            }

            player.world.spawnEntity(newKwami)
        }

        val playerState = ServerState.getPlayerState(player)

        if (kwamiUuid == null) {
            spawnKwami()
        } else if (
            (player.world as ServerWorld).getEntity(kwamiUuid) == null &&
            !playerState.activeMiraculous.contains(miraculousType)
        ) {
            spawnKwami()
        }

        if (kwamiUuid == null) return ActionResult.PASS

        setNBT(itemStack, Optional.of(kwamiUuid!!))

        return ActionResult.CONSUME
    }

    override fun postHit(stack: ItemStack, kwami: LivingEntity, attacker: LivingEntity): Boolean {
        if (kwami is AbstractKwami &&
            kwami.miraculousType == miraculousType &&
            kwami.uuid == getOptionalKwamiUuid(stack) &&
            attacker.world.server != null &&
            attacker is ServerPlayerEntity
        ) {
            PlayerState.sendMessageFrom(
                Text.translatable(
                    "text.miraculous_miracles.renunciation",
                    Text.translatable("entity.miraculous_miracles.${miraculousType.toString().lowercase()}_kwami")
                ),
                attacker
            )

            renounceKwami(stack, kwami)

            return true
        }

        return false
    }

    override fun useOnEntity(stack: ItemStack, user: PlayerEntity, kwami: LivingEntity, hand: Hand): ActionResult {
        if (kwami is AbstractKwami &&
            kwami.miraculousType == miraculousType &&
            user.world.server != null &&
            kwami.uuid == getOptionalKwamiUuid(stack) &&
            !kwami.isHungry &&
            !user.hasStatusEffect(
                MiraculousMiracles.TRANSFORMATION_TIME_LEFT_EFFECT
            ) &&
            user is ServerPlayerEntity
        ) {
            if (stack.damage >= stack.maxDamage) {
                user.sendMessage(Text.translatable("text.miraculous_miracles.miraculous_too_damaged"), true)
                return ActionResult.PASS
            }

            val playerState = ServerState.getPlayerState(user)

            // TODO: Unifications
            if (playerState.activeMiraculous.isEmpty()) {
                playerState.activeMiraculous[miraculousType] = stack.nbt ?: NbtCompound()
                playerState.updateActiveMiraculous(user)

                val inventory = user.inventory

                val newArmor = MiraculousMiracles.ARMORS[miraculousType]!!.map { armorItem ->
                    val armorStack = ItemStack(armorItem, 1)
                    armorStack.addEnchantment(Enchantments.BINDING_CURSE, 1)
                    armorStack.addEnchantment(Enchantments.VANISHING_CURSE, 1)
                    armorStack
                }

                for (i in 0..3) {
                    val oldArmor = inventory.armor[i]

                    if (!oldArmor.isEmpty) {
                        if (!inventory.insertStack(oldArmor)) {
                            ItemScatterer.spawn(user.world, user.x, user.y, user.z, oldArmor)
                        }
                    }

                    inventory.armor[i] = newArmor[i]
                }

                PlayerState.sendMessageFrom(
                    Text.translatable(
                        "text.miraculous_miracles.${
                            miraculousType.toString().lowercase()
                        }.transformation"
                    ),
                    user
                )

                kwami.remove(Entity.RemovalReason.DISCARDED)

                val weaponStack = ItemStack(MiraculousMiracles.MIRACULOUS_WEAPONS[miraculousType]!!)
                weaponStack.addEnchantment(Enchantments.VANISHING_CURSE, 1)

                // in spite of returning CONSUME which should stop bubbling, the item's `use` is still called, so we add a small cooldown
                user.itemCooldownManager[weaponStack.item] = 15

                PlayerState.replaceItemStack(stack, weaponStack, user)

                return ActionResult.CONSUME
            }
        }

        return ActionResult.PASS
    }

    override fun isDamageable(): Boolean {
        return false
    }
}
