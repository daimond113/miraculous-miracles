package com.daimond113.miraculous_miracles

import com.daimond113.miraculous_miracles.core.AbstractMiraculous
import com.daimond113.miraculous_miracles.core.MiraculousAbility
import com.daimond113.miraculous_miracles.core.MiraculousType
import com.daimond113.miraculous_miracles.core.NetworkMessages
import com.daimond113.miraculous_miracles.kwamis.bee.BeeKwamiModel
import com.daimond113.miraculous_miracles.kwamis.bee.BeeKwamiRenderer
import com.daimond113.miraculous_miracles.kwamis.snake.SnakeKwamiModel
import com.daimond113.miraculous_miracles.kwamis.snake.SnakeKwamiRenderer
import com.daimond113.miraculous_miracles.kwamis.turtle.TurtleKwamiModel
import com.daimond113.miraculous_miracles.kwamis.turtle.TurtleKwamiRenderer
import com.daimond113.miraculous_miracles.radial.RadialAction
import com.daimond113.miraculous_miracles.radial.RadialScreen
import com.daimond113.miraculous_miracles.states.PlayerState
import com.mojang.blaze3d.platform.InputUtil
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.option.KeyBind
import net.minecraft.client.render.RenderLayer
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
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


object MiraculousMiraclesClient : ClientModInitializer {
    val LOGGER: Logger = LoggerFactory.getLogger("${MiraculousMiracles.MOD_NAME} (Client)")

    val MODEL_BEE_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "bee_kwami"), "main")
    val MODEL_TURTLE_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "turtle_kwami"), "main")
    val MODEL_SNAKE_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "snake_kwami"), "main")

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

        BlockRenderLayerMap.put(RenderLayer.getTranslucent(), MiraculousMiracles.TURTLE_SHELLTER_BLOCK)

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

        EntityModelLayerRegistry.registerModelLayer(MODEL_BEE_KWAMI_LAYER, BeeKwamiModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MODEL_TURTLE_KWAMI_LAYER, TurtleKwamiModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MODEL_SNAKE_KWAMI_LAYER, SnakeKwamiModel::getTexturedModelData);


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

        var activeMiraculous: Optional<List<MiraculousType>> = Optional.empty()
        var requested = false

        ClientPlayNetworking.registerGlobalReceiver(NetworkMessages.RECEIVE_ACTIVE_MIRACULOUS) { _, _, buf, _ ->
            activeMiraculous =
                Optional.of(buf.readIntList().map { miraculousId -> PlayerState.getMiraculousTypeById(miraculousId) })
            requested = false
        }

        // TODO: change this into state that's synced with the client based on events
        ClientTickEvents.END.register { client ->
            if (!(detransformKey.isPressed || abilityKey.isPressed) || client.currentScreen != null) {
                activeMiraculous = Optional.empty()
                return@register
            }

            if (!requested && activeMiraculous.isEmpty) {
                requested = true
                ClientPlayNetworking.send(NetworkMessages.GET_ACTIVE_MIRACULOUS, PacketByteBufs.empty())
            }

            if (activeMiraculous.isEmpty || activeMiraculous.get().isEmpty()) return@register

            val miraculous = activeMiraculous.get()

            if (detransformKey.isPressed) {
                if (miraculous.size <= 1) {
                    val firstMiraculous = miraculous.firstOrNull() ?: return@register

                    val packetByteBuf = PacketByteBufs.create()
                    packetByteBuf.writeInt(firstMiraculous.id)

                    ClientPlayNetworking.send(NetworkMessages.DETRANSFORM, packetByteBuf)
                    return@register
                }

                client.setScreen(
                    RadialScreen(
                        "screen.miraculous_miracles.detransform", detransformKey, miraculous.map { miraculousType ->
                            RadialAction(
                                "item.miraculous_miracles.${
                                    miraculousType.toString().lowercase()
                                }_miraculous"
                            ) { ->
                                val packetByteBuf = PacketByteBufs.create()
                                packetByteBuf.writeInt(miraculousType.id)

                                ClientPlayNetworking.send(NetworkMessages.DETRANSFORM, packetByteBuf)
                            }
                        }
                    )
                )
            } else {
                val possibleAbilities = MiraculousAbility.values()
                    .filter { ability -> ability.withKeybind && miraculous.contains(ability.miraculousType) }

                if (possibleAbilities.size <= 1) {
                    val firstAbility = possibleAbilities.firstOrNull() ?: return@register

                    val packetByteBuf = PacketByteBufs.create()
                    packetByteBuf.writeInt(firstAbility.id)

                    ClientPlayNetworking.send(NetworkMessages.USE_MIRACULOUS_ABILITY, packetByteBuf)
                    return@register
                }

                client.setScreen(
                    RadialScreen(
                        "screen.miraculous_miracles.use_ability",
                        abilityKey,
                        possibleAbilities.map { ability ->
                            RadialAction("ability.miraculous_miracles.${ability.toString().lowercase()}") { ->
                                val packetByteBuf = PacketByteBufs.create()
                                packetByteBuf.writeInt(ability.id)

                                ClientPlayNetworking.send(NetworkMessages.USE_MIRACULOUS_ABILITY, packetByteBuf)
                            }
                        }
                    )
                )
            }
        }
    }
}
