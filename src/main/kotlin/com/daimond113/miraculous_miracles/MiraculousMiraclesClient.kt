package com.daimond113.miraculous_miracles

import com.daimond113.miraculous_miracles.content.CrucibleRenderer
import com.daimond113.miraculous_miracles.core.AbstractMiraculous
import com.daimond113.miraculous_miracles.core.MiraculousAbility
import com.daimond113.miraculous_miracles.core.MiraculousType
import com.daimond113.miraculous_miracles.core.NetworkMessages
import com.daimond113.miraculous_miracles.miraculouses.bee.kwami.BeeKwamiModel
import com.daimond113.miraculous_miracles.miraculouses.bee.kwami.BeeKwamiRenderer
import com.daimond113.miraculous_miracles.miraculouses.horse.kwami.HorseKwamiModel
import com.daimond113.miraculous_miracles.miraculouses.horse.kwami.HorseKwamiRenderer
import com.daimond113.miraculous_miracles.miraculouses.ladybug.kwami.LadybugKwamiModel
import com.daimond113.miraculous_miracles.miraculouses.ladybug.kwami.LadybugKwamiRenderer
import com.daimond113.miraculous_miracles.miraculouses.mouse.MultitudeEntityRenderer
import com.daimond113.miraculous_miracles.miraculouses.rabbit.kwami.MouseKwamiModel
import com.daimond113.miraculous_miracles.miraculouses.rabbit.kwami.MouseKwamiRenderer
import com.daimond113.miraculous_miracles.miraculouses.rabbit.kwami.RabbitKwamiModel
import com.daimond113.miraculous_miracles.miraculouses.rabbit.kwami.RabbitKwamiRenderer
import com.daimond113.miraculous_miracles.miraculouses.snake.kwami.SnakeKwamiModel
import com.daimond113.miraculous_miracles.miraculouses.snake.kwami.SnakeKwamiRenderer
import com.daimond113.miraculous_miracles.miraculouses.turtle.kwami.TurtleKwamiModel
import com.daimond113.miraculous_miracles.miraculouses.turtle.kwami.TurtleKwamiRenderer
import com.daimond113.miraculous_miracles.state.PlayerState
import com.daimond113.miraculous_miracles.ui.*
import com.mojang.blaze3d.platform.InputUtil
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.option.KeyBind
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object MiraculousMiraclesClient : ClientModInitializer {
    val LOGGER: Logger = LoggerFactory.getLogger("${MiraculousMiracles.MOD_NAME} (Client)")

    val MODEL_BEE_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "bee_kwami"), "main")
    val MODEL_TURTLE_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "turtle_kwami"), "main")
    val MODEL_SNAKE_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "snake_kwami"), "main")
    val MODEL_LADYBUG_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "ladybug_kwami"), "main")
    val MODEL_HORSE_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "horse_kwami"), "main")
    val MODEL_RABBIT_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "rabbit_kwami"), "main")
    val MODEL_MOUSE_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "mouse_kwami"), "main")

    private var activeMiraculous: Set<MiraculousType> = setOf()
    var allDimensions: MutableSet<Identifier> = mutableSetOf()

    override fun onInitializeClient(mod: ModContainer) {
        for (miraculous in MiraculousMiracles.MIRACULOUSES.values) {
            ModelPredicateProviderRegistry.register(
                miraculous,
                Identifier(MiraculousMiracles.MOD_ID, "is_charged")
            ) { stack, _, _, _ ->
                if (AbstractMiraculous.getCharged(stack)) 1f else 0f
            }
        }

        // TODO: body suit renderer to make players not look bulky
        // val armorItems = MiraculousMiracles.ARMORS.values.flatMap { armors -> armors.asIterable() }.toTypedArray()

        EntityRendererRegistry.register(MiraculousMiracles.BEE_SPINNING_TOP_ENTITY) { context: EntityRendererFactory.Context ->
            FlyingItemEntityRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.TURTLE_SHELLTER_ENTITY) { context: EntityRendererFactory.Context ->
            FlyingItemEntityRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.LADYBUG_YOYO_ENTITY) { context: EntityRendererFactory.Context ->
            FlyingItemEntityRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.VOYAGE_ENTITY) { context: EntityRendererFactory.Context ->
            FlyingItemEntityRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.BURROW_ENTITY) { context: EntityRendererFactory.Context ->
            FlyingItemEntityRenderer(
                context
            )
        }

        BlockEntityRendererFactories.register(MiraculousMiracles.CRUCIBLE_ENTITY) { CrucibleRenderer() }

        BlockRenderLayerMap.put(RenderLayer.getTranslucent(), MiraculousMiracles.TURTLE_SHELLTER_BLOCK)
        BlockRenderLayerMap.put(RenderLayer.getTranslucent(), MiraculousMiracles.VOYAGE_BLOCK)
        BlockRenderLayerMap.put(RenderLayer.getTranslucent(), MiraculousMiracles.BURROW_BLOCK)

        EntityRendererRegistry.register(MiraculousMiracles.KWAMIS[MiraculousType.Bee]) { context ->
            BeeKwamiRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.KWAMIS[MiraculousType.Turtle]) { context ->
            TurtleKwamiRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.KWAMIS[MiraculousType.Snake]) { context ->
            SnakeKwamiRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.KWAMIS[MiraculousType.Ladybug]) { context ->
            LadybugKwamiRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.KWAMIS[MiraculousType.Horse]) { context ->
            HorseKwamiRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.KWAMIS[MiraculousType.Rabbit]) { context ->
            RabbitKwamiRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.KWAMIS[MiraculousType.Mouse]) { context ->
            MouseKwamiRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.MULTITUDE_PLAYER_ENTITY) { context ->
            MultitudeEntityRenderer(
                context
            )
        }

        EntityModelLayerRegistry.registerModelLayer(MODEL_BEE_KWAMI_LAYER, BeeKwamiModel::getTexturedModelData)
        EntityModelLayerRegistry.registerModelLayer(MODEL_TURTLE_KWAMI_LAYER, TurtleKwamiModel::getTexturedModelData)
        EntityModelLayerRegistry.registerModelLayer(MODEL_SNAKE_KWAMI_LAYER, SnakeKwamiModel::getTexturedModelData)
        EntityModelLayerRegistry.registerModelLayer(MODEL_LADYBUG_KWAMI_LAYER, LadybugKwamiModel::getTexturedModelData)
        EntityModelLayerRegistry.registerModelLayer(MODEL_HORSE_KWAMI_LAYER, HorseKwamiModel::getTexturedModelData)
        EntityModelLayerRegistry.registerModelLayer(MODEL_RABBIT_KWAMI_LAYER, RabbitKwamiModel::getTexturedModelData)
        EntityModelLayerRegistry.registerModelLayer(MODEL_MOUSE_KWAMI_LAYER, MouseKwamiModel::getTexturedModelData)

        val detransformKey = KeyBindingHelper.registerKeyBinding(
            KeyBind(
                "key.miraculous_miracles.detransform",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                MiraculousMiracles.MOD_NAME
            )
        )

        val abilityKey = KeyBindingHelper.registerKeyBinding(
            KeyBind(
                "key.miraculous_miracles.miraculous_ability",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                MiraculousMiracles.MOD_NAME
            )
        )

        ClientPlayNetworking.registerGlobalReceiver(NetworkMessages.RECEIVE_ACTIVE_MIRACULOUS) { _, _, buf, _ ->
            activeMiraculous =
                buf.readIntList().map { miraculousId -> PlayerState.getMiraculousTypeById(miraculousId) }.toSet()
        }

        ClientTickEvents.END.register { client ->
            if (!(detransformKey.isPressed || abilityKey.isPressed) || client.currentScreen != null) {
                return@register
            }

            if (activeMiraculous.isEmpty()) return@register

            if (detransformKey.isPressed) {
                if (activeMiraculous.size <= 1) {
                    ClientPlayNetworking.send(NetworkMessages.DETRANSFORM, PacketByteBufs.create().apply {
                        writeInt(activeMiraculous.firstOrNull()?.id ?: return@register)
                    })

                    return@register
                }

                client.setScreen(
                    RadialScreen(
                        "screen.miraculous_miracles.detransform",
                        detransformKey,
                        activeMiraculous.map { miraculousType ->
                            RadialAction(
                                "item.miraculous_miracles.${
                                    miraculousType.toString().lowercase()
                                }_miraculous"
                            ) {
                                ClientPlayNetworking.send(NetworkMessages.DETRANSFORM, PacketByteBufs.create().apply {
                                    writeInt(miraculousType.id)
                                })
                            }
                        }
                    )
                )
            } else {
                val possibleAbilities = MiraculousAbility.values()
                    .filter { ability -> ability.withKeyBind && activeMiraculous.contains(ability.miraculousType) }

                if (possibleAbilities.size <= 1) {
                    ClientPlayNetworking.send(NetworkMessages.USE_MIRACULOUS_ABILITY, PacketByteBufs.create().apply {
                        writeInt(possibleAbilities.firstOrNull()?.id ?: return@register)
                    })

                    return@register
                }

                client.setScreen(
                    RadialScreen(
                        "screen.miraculous_miracles.use_ability",
                        abilityKey,
                        possibleAbilities.map { ability ->
                            RadialAction("text.miraculous_miracles.ability.${ability.toString().lowercase()}") {
                                ClientPlayNetworking.send(
                                    NetworkMessages.USE_MIRACULOUS_ABILITY,
                                    PacketByteBufs.create().apply {
                                        writeInt(ability.id)
                                    })
                            }
                        }
                    )
                )
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(NetworkMessages.REQUEST_SET_PORTAL_COORDS) { client, _, packetByteBuf, _ ->
            val isBurrow = packetByteBuf.readBoolean()

            client.execute {
                if (client.currentScreen != null) return@execute

                client.setScreen(if (isBurrow) BurrowCoordinateScreen() else VoyageCoordinateScreen())
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(NetworkMessages.SET_DIMENSIONS) { _, _, packetBuf, _ ->
            allDimensions.clear()
            for (int in 0 until packetBuf.readInt()) {
                allDimensions.add(packetBuf.readIdentifier())
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(NetworkMessages.REQUEST_SET_MULTITUDE_AMOUNT) { client, _, _, _ ->
            client.execute {
                if (client.currentScreen != null) return@execute

                client.setScreen(MultitudeScreen())
            }
        }
    }
}
