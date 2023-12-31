package com.daimond113.miraculous_miracles

import com.daimond113.miraculous_miracles.content.*
import com.daimond113.miraculous_miracles.core.*
import com.daimond113.miraculous_miracles.miraculouses.bee.BeeMiraculous
import com.daimond113.miraculous_miracles.miraculouses.bee.SpinningTop
import com.daimond113.miraculous_miracles.miraculouses.bee.SpinningTopEntity
import com.daimond113.miraculous_miracles.miraculouses.bee.Venom
import com.daimond113.miraculous_miracles.miraculouses.bee.kwami.BeeKwami
import com.daimond113.miraculous_miracles.miraculouses.dog.Ball
import com.daimond113.miraculous_miracles.miraculouses.dog.DogMiraculous
import com.daimond113.miraculous_miracles.miraculouses.dog.kwami.DogKwami
import com.daimond113.miraculous_miracles.miraculouses.horse.HorseMiraculous
import com.daimond113.miraculous_miracles.miraculouses.horse.Horseshoe
import com.daimond113.miraculous_miracles.miraculouses.horse.kwami.HorseKwami
import com.daimond113.miraculous_miracles.miraculouses.ladybug.*
import com.daimond113.miraculous_miracles.miraculouses.ladybug.kwami.LadybugKwami
import com.daimond113.miraculous_miracles.miraculouses.mouse.MouseMiraculous
import com.daimond113.miraculous_miracles.miraculouses.mouse.MultitudeEntity
import com.daimond113.miraculous_miracles.miraculouses.mouse.SkipRope
import com.daimond113.miraculous_miracles.miraculouses.mouse.kwami.MouseKwami
import com.daimond113.miraculous_miracles.miraculouses.rabbit.BurrowChunkGenerator
import com.daimond113.miraculous_miracles.miraculouses.rabbit.RabbitMiraculous
import com.daimond113.miraculous_miracles.miraculouses.rabbit.Umbrella
import com.daimond113.miraculous_miracles.miraculouses.rabbit.kwami.RabbitKwami
import com.daimond113.miraculous_miracles.miraculouses.snake.Lyre
import com.daimond113.miraculous_miracles.miraculouses.snake.SnakeMiraculous
import com.daimond113.miraculous_miracles.miraculouses.snake.kwami.SnakeKwami
import com.daimond113.miraculous_miracles.miraculouses.turtle.ShellterBlock
import com.daimond113.miraculous_miracles.miraculouses.turtle.ShellterEntity
import com.daimond113.miraculous_miracles.miraculouses.turtle.Shield
import com.daimond113.miraculous_miracles.miraculouses.turtle.TurtleMiraculous
import com.daimond113.miraculous_miracles.miraculouses.turtle.kwami.TurtleKwami
import com.daimond113.miraculous_miracles.state.PlayerState
import com.daimond113.miraculous_miracles.state.ServerState
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.*
import net.minecraft.entity.attribute.DefaultAttributeRegistry
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.village.TradeOffer
import net.minecraft.village.VillagerProfession
import net.minecraft.world.World
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qkl.library.blocks.blockSettingsOf
import org.quiltmc.qkl.library.items.itemGroupOf
import org.quiltmc.qkl.library.registry.registryScope
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder
import org.quiltmc.qsl.entity.api.QuiltEntityTypeBuilder
import org.quiltmc.qsl.entity_events.api.LivingEntityDeathCallback
import org.quiltmc.qsl.entity_events.api.ServerEntityLoadEvents
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import org.quiltmc.qsl.tag.api.QuiltTagKey
import org.quiltmc.qsl.tag.api.TagType
import org.quiltmc.qsl.villager.api.TradeOfferHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val ARMOR_ITEM_SETTINGS = itemSettingsOf(maxDamage = 9000, group = MiraculousMiracles.ITEM_GROUP)

val KWAMI_DIMENSIONS = EntityDimensions(0.3f, 0.6f, true)

object MiraculousMiracles : ModInitializer {
    const val MOD_ID = "miraculous_miracles"
    const val MOD_NAME = "Miraculous Miracles"

    val LOGGER: Logger = LoggerFactory.getLogger(MOD_NAME)

    val ITEM_GROUP = itemGroupOf(
        id = Identifier(MOD_ID, "item_group"),
        iconSupplier = { MIRACULOUSES[MiraculousType.Bee]!!.defaultStack }
    )

    val MIRACULOUSES = arrayOf(
        BeeMiraculous(),
        TurtleMiraculous(),
        SnakeMiraculous(),
        LadybugMiraculous(),
        HorseMiraculous(),
        RabbitMiraculous(),
        MouseMiraculous(),
        DogMiraculous()
    ).associateBy { it.miraculousType }

    val KWAMIS = mapOf(
        MiraculousType.Bee to
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::BeeKwami).setDimensions(KWAMI_DIMENSIONS).build(),
        MiraculousType.Turtle to
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::TurtleKwami).setDimensions(KWAMI_DIMENSIONS).build(),
        MiraculousType.Snake to
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::SnakeKwami).setDimensions(KWAMI_DIMENSIONS).build(),
        MiraculousType.Ladybug to
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::LadybugKwami).setDimensions(KWAMI_DIMENSIONS).build(),
        MiraculousType.Horse to
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::HorseKwami).setDimensions(KWAMI_DIMENSIONS).build(),
        MiraculousType.Rabbit to
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::RabbitKwami).setDimensions(KWAMI_DIMENSIONS).build(),
        MiraculousType.Mouse to
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::MouseKwami).setDimensions(KWAMI_DIMENSIONS).build(),
        MiraculousType.Dog to
            QuiltEntityTypeBuilder.create(SpawnGroup.CREATURE, ::DogKwami).setDimensions(KWAMI_DIMENSIONS).build()
    )

    val ARMORS = MiraculousArmorMaterials.values().associate {
        it.miraculousType to arrayOf(
            MiraculousArmorItem(it, EquipmentSlot.FEET, ARMOR_ITEM_SETTINGS),
            MiraculousArmorItem(it, EquipmentSlot.LEGS, ARMOR_ITEM_SETTINGS),
            MiraculousArmorItem(it, EquipmentSlot.CHEST, ARMOR_ITEM_SETTINGS),
            MiraculousArmorItem(it, EquipmentSlot.HEAD, ARMOR_ITEM_SETTINGS),
        )
    }

    val MIRACULOUS_WEAPONS = mapOf(
        MiraculousType.Bee to SpinningTop(),
        MiraculousType.Turtle to Shield(),
        MiraculousType.Snake to Lyre(),
        MiraculousType.Ladybug to Yoyo(),
        MiraculousType.Horse to Horseshoe(),
        MiraculousType.Rabbit to Umbrella(),
        MiraculousType.Mouse to SkipRope(),
        MiraculousType.Dog to Ball()
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

    val LUCKY_CHARM_SWORD = LuckyCharmSword()
    val LUCKY_CHARM_PICKAXE = LuckyCharmPickaxe()

    val LADYBUG_YOYO_ENTITY: EntityType<YoyoEntity> = QuiltEntityTypeBuilder.create(SpawnGroup.MISC, ::YoyoEntity)
        .setDimensions(
            EntityDimensions(0.25f, 0.25f, true)
        )
        .maxChunkTrackingRange(4)
        .trackingTickInterval(10)
        .build()

    val TRANSFORMATION_TIME_LEFT_EFFECT = TransformationTimeLeftEffect()

    val SAFELY_REPLACEABLE_TAG: TagKey<Block> =
        QuiltTagKey.of(Registry.BLOCK.key, Identifier(MOD_ID, "safely_replaceable"), TagType.NORMAL)

    private val CRUCIBLE = Crucible()
    val CRUCIBLE_ENTITY: BlockEntityType<CrucibleEntity> =
        QuiltBlockEntityTypeBuilder.create(::CrucibleEntity, CRUCIBLE).build()
    private val CRUCIBLE_ITEM = BlockItem(CRUCIBLE, itemSettingsOf(group = ITEM_GROUP))
    val METEORITE_POWDER = Item(itemSettingsOf(group = ITEM_GROUP, rarity = Rarity.UNCOMMON))

    val VOYAGE_ITEM = PortalItem(false)
    val VOYAGE_BLOCK = PortalBlock(false)

    val VOYAGE_ENTITY: EntityType<PortalItemEntity> = QuiltEntityTypeBuilder.create(SpawnGroup.MISC, ::PortalItemEntity)
        .setDimensions(
            EntityDimensions(0.25f, 0.25f, true)
        )
        .maxChunkTrackingRange(4)
        .trackingTickInterval(10)
        .build()

    val BURROW_ITEM = PortalItem(true)
    val BURROW_BLOCK = PortalBlock(true)

    val BURROW_ENTITY: EntityType<PortalItemEntity> = QuiltEntityTypeBuilder.create(SpawnGroup.MISC, ::PortalItemEntity)
        .setDimensions(
            EntityDimensions(0.25f, 0.25f, true)
        )
        .maxChunkTrackingRange(4)
        .trackingTickInterval(10)
        .build()

    val BURROW_DIMENSION_BLOCK =
        Block(blockSettingsOf(material = Material.STONE, luminance = 15, hardness = -1.0f, resistance = 3600000.0f))
    val BURROW_WORLD_KEY: RegistryKey<World> = RegistryKey.of(Registry.WORLD_KEY, Identifier(MOD_ID, "burrow"))

    val DEBOUNCES = mutableMapOf<String, Int>()

    val MULTITUDE_PLAYER_ENTITY: EntityType<MultitudeEntity> = QuiltEntityTypeBuilder.create(SpawnGroup.MISC, ::MultitudeEntity)
        .setDimensions(PlayerEntity.STANDING_DIMENSIONS)
        .build()

    val PORTAL_BLOCK_ENTITY: BlockEntityType<PortalBlockEntity> =
        QuiltBlockEntityTypeBuilder.create(::PortalBlockEntity, BURROW_BLOCK, VOYAGE_BLOCK).build()

    override fun onInitialize(mod: ModContainer) {
        Registry.register(Registry.CHUNK_GENERATOR, BURROW_WORLD_KEY.value, BurrowChunkGenerator.CODEC)

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

            TURTLE_SHELLTER_BLOCK withPath "turtle_shellter" toRegistry Registry.BLOCK
            TURTLE_SHELLTER_ITEM withPath "turtle_shellter" toRegistry Registry.ITEM
            TURTLE_SHELLTER_ENTITY withPath "turtle_shellter_entity" toRegistry Registry.ENTITY_TYPE

            LUCKY_CHARM_SWORD withPath "lucky_charm_sword" toRegistry Registry.ITEM
            LUCKY_CHARM_PICKAXE withPath "lucky_charm_pickaxe" toRegistry Registry.ITEM

            LADYBUG_YOYO_ENTITY withPath "ladybug_yoyo_entity" toRegistry Registry.ENTITY_TYPE

            TRANSFORMATION_TIME_LEFT_EFFECT withPath "transformation_time_left" toRegistry Registry.STATUS_EFFECT

            CRUCIBLE withPath "crucible" toRegistry Registry.BLOCK
            CRUCIBLE_ITEM withPath "crucible" toRegistry Registry.ITEM
            CRUCIBLE_ENTITY withPath "crucible_entity" toRegistry Registry.BLOCK_ENTITY_TYPE
            METEORITE_POWDER withPath "meteorite_powder" toRegistry Registry.ITEM

            VOYAGE_ITEM withPath "voyage" toRegistry Registry.ITEM
            VOYAGE_BLOCK withPath "voyage" toRegistry Registry.BLOCK
            VOYAGE_ENTITY withPath "voyage_entity" toRegistry Registry.ENTITY_TYPE

            BURROW_ITEM withPath "burrow" toRegistry Registry.ITEM
            BURROW_BLOCK withPath "burrow" toRegistry Registry.BLOCK
            BURROW_ENTITY withPath "burrow_entity" toRegistry Registry.ENTITY_TYPE
            BURROW_DIMENSION_BLOCK withPath "burrow_dimension_block" toRegistry Registry.BLOCK

            DefaultAttributeRegistry.DEFAULT_ATTRIBUTE_REGISTRY[MULTITUDE_PLAYER_ENTITY] = PlayerEntity.createPlayerAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.1)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .build()
            MULTITUDE_PLAYER_ENTITY withPath "multitude_player_entity" toRegistry Registry.ENTITY_TYPE

            PORTAL_BLOCK_ENTITY withPath "portal_block_entity" toRegistry Registry.BLOCK_ENTITY_TYPE
        }

        TradeOfferHelper.registerVillagerOffers(
            VillagerProfession.CARTOGRAPHER,
            1
        ) { factories ->
            factories.add { _, random ->
                TradeOffer(
                    ItemStack(Items.EMERALD, random.range(6, 21)),
                    ItemStack(METEORITE_POWDER, random.range(1, 4)),
                    random.range(4, 17),
                    3,
                    0.03f
                )
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            ServerState.getPlayerState(handler.player).updateActiveMiraculous(handler.player, false)

            ServerPlayNetworking.send(handler.player, NetworkMessages.SET_DIMENSIONS, PacketByteBufs.create().apply {
                server.worldRegistryKeys.apply {
                    writeInt(size)
                    forEach {
                        writeIdentifier(it.value)
                    }
                }
            })
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

                if (receivedAbility.withKeyBind)
                    receivedAbility
                else
                    null
            }

            if (chosenAbility !is MiraculousAbility) return@registerGlobalReceiver

            playerState.useAbility(chosenAbility, player, null)
        }

        ServerPlayNetworking.registerGlobalReceiver(NetworkMessages.SET_PORTAL_COORDS) { _, player, _, packetByteBuf, _ ->
            val isBurrow = packetByteBuf.readBoolean()
            val playerState = ServerState.getPlayerState(player)
            val abilityNbt =
                playerState.usedAbilities[if (isBurrow) MiraculousAbility.Burrow else MiraculousAbility.Voyage]
                    ?: return@registerGlobalReceiver
            val (x, y, z) = packetByteBuf.readIntArray(3)

            abilityNbt.putInt("x", x)
            abilityNbt.putInt("y", y)
            abilityNbt.putInt("z", z)

            if (isBurrow) {
                abilityNbt.putString("dimension", packetByteBuf.readIdentifier().toString())
            }

            ServerState.getServerState(player.server).markDirty()
        }

        ServerPlayNetworking.registerGlobalReceiver(NetworkMessages.SET_MULTITUDE_AMOUNT) { _, player, _, packetByteBuf, _ ->
            val playerState = ServerState.getPlayerState(player)
            val abilityNbt = playerState.usedAbilities[MiraculousAbility.Multitude] ?: return@registerGlobalReceiver
            val amount = MathHelper.clamp(packetByteBuf.readInt(), 2, 16)

            abilityNbt.putInt("amount", amount)

            ServerState.getServerState(player.server).markDirty()
        }

        LivingEntityDeathCallback.EVENT.register { entity, _ ->
            if (entity !is ServerPlayerEntity) return@register
            val playerState = ServerState.getPlayerState(entity)

            playerState.detransform(entity, playerState.activeMiraculous.keys, true)
        }

        ServerEntityLoadEvents.AFTER_LOAD.register { entity, world ->
            if (entity !is ItemEntity) return@register
            if (entity.stack.item !is AbstractMiraculous) return@register
            AbstractMiraculous.renounceKwami(entity.stack, world)
        }

        ServerTickEvents.END.register { _ ->
            val iterator = DEBOUNCES.iterator()

            while (iterator.hasNext()) {
                val entry = iterator.next()
                entry.setValue(entry.value - 1)
                if (entry.value <= 0) {
                    iterator.remove()
                }
            }
        }
    }
}
