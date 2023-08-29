package com.daimond113.miraculous_miracles

import com.daimond113.miraculous_miracles.core.*
import com.daimond113.miraculous_miracles.effects.TransformationTimeLeftEffect
import com.daimond113.miraculous_miracles.items.*
import com.daimond113.miraculous_miracles.kwamis.bee.BeeKwami
import com.daimond113.miraculous_miracles.kwamis.snake.SnakeKwami
import com.daimond113.miraculous_miracles.kwamis.turtle.TurtleKwami
import com.daimond113.miraculous_miracles.miraculouses.BeeMiraculous
import com.daimond113.miraculous_miracles.miraculouses.SnakeMiraculous
import com.daimond113.miraculous_miracles.miraculouses.TurtleMiraculous
import com.daimond113.miraculous_miracles.states.PlayerState
import com.daimond113.miraculous_miracles.states.ServerState
import net.minecraft.block.Block
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.DefaultAttributeRegistry
import net.minecraft.item.ArmorItem
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qkl.library.items.itemGroupOf
import org.quiltmc.qkl.library.items.itemSettingsOf
import org.quiltmc.qkl.library.registry.registryScope
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.entity.api.QuiltEntityTypeBuilder
import org.quiltmc.qsl.entity_events.api.LivingEntityDeathCallback
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import org.quiltmc.qsl.tag.api.QuiltTagKey
import org.quiltmc.qsl.tag.api.TagType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val ARMOR_ITEM_SETTINGS = itemSettingsOf(maxCount = 1, maxDamage = 9000, group = MiraculousMiracles.ITEM_GROUP)

val KWAMI_DIMENSIONS = EntityDimensions(0.3f, 0.6f, true)

object MiraculousMiracles : ModInitializer {
    const val MOD_ID = "miraculous_miracles"
    const val MOD_NAME = "Miraculous Miracles"

    val LOGGER: Logger = LoggerFactory.getLogger(MOD_NAME)

    val ITEM_GROUP = itemGroupOf(
        id = Identifier(MOD_ID, "item_group"),
        iconSupplier = { -> ItemStack(MIRACULOUSES[MiraculousType.Bee]) })

    val MIRACULOUSES = run {
        val map = mutableMapOf<MiraculousType, AbstractMiraculous>()

        for (miraculousInstance in arrayOf(BeeMiraculous(), TurtleMiraculous(), SnakeMiraculous())) {
            map[miraculousInstance.miraculousType] = miraculousInstance
        }

        map
    }

    val KWAMIS = mapOf(
        Pair(
            MiraculousType.Bee,
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::BeeKwami).setDimensions(KWAMI_DIMENSIONS).build()
        ),
        Pair(
            MiraculousType.Turtle,
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::TurtleKwami).setDimensions(KWAMI_DIMENSIONS).build()
        ),
        Pair(
            MiraculousType.Snake,
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::SnakeKwami).setDimensions(KWAMI_DIMENSIONS).build()
        )
    )

    val ARMORS = run {
        val map = mutableMapOf<MiraculousType, Array<ArmorItem>>()

        for (armorMaterial in ArmorMaterials.values()) {
            map[armorMaterial.miraculousType] =
                arrayOf(
                    ArmorItem(armorMaterial, EquipmentSlot.FEET, ARMOR_ITEM_SETTINGS),
                    ArmorItem(armorMaterial, EquipmentSlot.LEGS, ARMOR_ITEM_SETTINGS),
                    ArmorItem(armorMaterial, EquipmentSlot.CHEST, ARMOR_ITEM_SETTINGS),
                    ArmorItem(armorMaterial, EquipmentSlot.HEAD, ARMOR_ITEM_SETTINGS),
                )
        }

        map
    }

    val MIRACULOUS_WEAPONS = mapOf(
        Pair(MiraculousType.Bee, SpinningTop()),
        Pair(MiraculousType.Turtle, Shield()),
        Pair(MiraculousType.Snake, Lyre())
    )

    val BEE_VENOM = Venom()
    val BEE_SPINNING_TOP_ENTITY: EntityType<SpinningTopEntity> =
        QuiltEntityTypeBuilder.create(SpawnGroup.MISC, ::SpinningTopEntity)
            .setDimensions(
                EntityDimensions(0.25f, 0.25f, true)
            )
            .maxChunkTrackingRange(4)
            .trackingTickInterval(10)
            .build()

    val TURTLE_SHELLTER_BLOCK = ShellterBlock()
    val TURTLE_SHELLTER_ITEM = BlockItem(TURTLE_SHELLTER_BLOCK, itemSettingsOf(group = ITEM_GROUP))

    val TURTLE_SHELLTER_ENTITY: EntityType<ShellterEntity> =
        QuiltEntityTypeBuilder.create(SpawnGroup.MISC, ::ShellterEntity)
            .setDimensions(
                EntityDimensions(0.25f, 0.25f, true)
            )
            .maxChunkTrackingRange(4)
            .trackingTickInterval(10)
            .build()

    val TRANSFORMATION_TIME_LEFT_EFFECT = TransformationTimeLeftEffect()

    val MIRACULOUS_ITEM_TAGS = MiraculousType.values().associateWith { miraculousType ->
        QuiltTagKey.of(Registry.ITEM.key, Identifier(MOD_ID, miraculousType.toString().lowercase()), TagType.NORMAL)
    }

    val SHELLTER_REPLACEABLE_TAG: TagKey<Block> = QuiltTagKey.of(Registry.BLOCK.key, Identifier(MOD_ID, "shellter_replaceable"), TagType.NORMAL)

    override fun onInitialize(mod: ModContainer) {
        val kwamiAttributes = AbstractKwami.createKwamiAttributes().build()

        registryScope(mod.metadata().id()) {
            for ((miraculousType, miraculous) in MIRACULOUSES) {
                miraculous withPath "${miraculousType.toString().lowercase()}_miraculous" toRegistry Registry.ITEM
            }

            for ((miraculousType, kwami) in KWAMIS) {
                DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY[kwami] = kwamiAttributes
                kwami withPath "${miraculousType.toString().lowercase()}_kwami" toRegistry Registry.ENTITY_TYPE
            }

            for ((miraculousType, armors) in ARMORS) {
                for (armor in armors) {
                    armor withPath "${miraculousType.toString().lowercase()}_${
                        armor.slotType.toString().lowercase()
                    }" toRegistry Registry.ITEM
                }
            }

            for ((miraculousType, weapon) in MIRACULOUS_WEAPONS) {
                weapon withPath "${miraculousType.toString().lowercase()}_${weapon.weaponName}" toRegistry Registry.ITEM
            }

            BEE_VENOM withPath "bee_venom" toRegistry Registry.ITEM
            BEE_SPINNING_TOP_ENTITY withPath "bee_spinning_top_entity" toRegistry Registry.ENTITY_TYPE

            TURTLE_SHELLTER_BLOCK withPath "turtle_shellter_block" toRegistry Registry.BLOCK
            TURTLE_SHELLTER_ITEM withPath "turtle_shellter" toRegistry Registry.ITEM
            TURTLE_SHELLTER_ENTITY withPath "turtle_shellter_entity" toRegistry Registry.ENTITY_TYPE

            TRANSFORMATION_TIME_LEFT_EFFECT withPath "transformation_time_left" toRegistry Registry.STATUS_EFFECT
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val playerState = ServerState.getPlayerState(handler.player)

            val packetByteBuf = PacketByteBufs.create()

            packetByteBuf.writeIntArray(playerState.activeMiraculous.map { miraculousType -> miraculousType.id }
                .toIntArray())

            ServerPlayNetworking.send(handler.player, NetworkMessages.RECEIVE_ACTIVE_MIRACULOUS, packetByteBuf)
        }

        ServerPlayNetworking.registerGlobalReceiver(NetworkMessages.DETRANSFORM) { _, player, _, packetByteBuf, _ ->
            val playerState = ServerState.getPlayerState(player)

            if (playerState.activeMiraculous.isEmpty()) return@registerGlobalReceiver

            val chosenMiraculous = run {
                val miraculousId = packetByteBuf.readInt()
                val miraculousType = PlayerState.getMiraculousTypeById(miraculousId)

                if (playerState.activeMiraculous.contains(miraculousType)) miraculousType else null
            } ?: return@registerGlobalReceiver

            playerState.detransform(player, setOf(chosenMiraculous))
        }

        ServerPlayNetworking.registerGlobalReceiver(NetworkMessages.USE_MIRACULOUS_ABILITY) { _, player, _, packetByteBuf, _ ->
            val playerState = ServerState.getPlayerState(player)

            val chosenAbility = run {
                val abilityId = packetByteBuf.readInt()
                val receivedAbility = PlayerState.getAbilityById(abilityId)

                if (receivedAbility.withKeybind)
                    receivedAbility
                else
                    null
            }

            if (chosenAbility !is MiraculousAbility) return@registerGlobalReceiver

            playerState.useAbility(chosenAbility, player, null)
        }

        LivingEntityDeathCallback.EVENT.register { entity, _ ->
            if (entity !is ServerPlayerEntity) return@register
            val playerState = ServerState.getPlayerState(entity)

            playerState.detransform(entity, playerState.activeMiraculous, true)
        }
    }
}
