package com.daimond113.miraculous_miracles

import com.daimond113.miraculous_miracles.core.AbstractMiraculous
import com.daimond113.miraculous_miracles.core.MiraculousAbility
import com.daimond113.miraculous_miracles.core.MiraculousType
import com.daimond113.miraculous_miracles.core.NetworkMessages
import com.daimond113.miraculous_miracles.kwamis.bee.BeeKwamiModel
import com.daimond113.miraculous_miracles.kwamis.bee.BeeKwamiRenderer
import com.daimond113.miraculous_miracles.radial.RadialAction
import com.daimond113.miraculous_miracles.radial.RadialScreen
import com.daimond113.miraculous_miracles.states.PlayerState
import com.mojang.blaze3d.platform.InputUtil
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.option.KeyBind
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


object MiraculousMiraclesClient : ClientModInitializer {
    val LOGGER: Logger = LoggerFactory.getLogger("${MiraculousMiracles.MOD_NAME} (Client)")

    val MODEL_BEE_KWAMI_LAYER = EntityModelLayer(Identifier(MiraculousMiracles.MOD_ID, "bee_kwami"), "main")

    override fun onInitializeClient(mod: ModContainer) {
        for (miraculous in MiraculousMiracles.MIRACULOUSES.values) {
            ModelPredicateProviderRegistry.register(
                miraculous,
                Identifier(MiraculousMiracles.MOD_ID, "is_charged")
            ) { stack, _, _, _ ->
                if (AbstractMiraculous.getCharged(stack)) 1f else 0f
            }
        }

        val armorItems = MiraculousMiracles.ARMORS.values.flatMap { armors -> armors.asIterable() }.toTypedArray()

        EntityRendererRegistry.register(MiraculousMiracles.BEE_SPINNING_TOP_ENTITY) { context: EntityRendererFactory.Context ->
            FlyingItemEntityRenderer(
                context
            )
        }

        EntityRendererRegistry.register(MiraculousMiracles.KWAMIS[MiraculousType.Bee]) { context ->
            BeeKwamiRenderer(
                context
            )
        }

        EntityModelLayerRegistry.registerModelLayer(MODEL_BEE_KWAMI_LAYER, BeeKwamiModel::getTexturedModelData);

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
                    ClientPlayNetworking.send(NetworkMessages.DETRANSFORM, PacketByteBufs.empty())
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
                if (miraculous.size <= 1) {
                    ClientPlayNetworking.send(NetworkMessages.USE_MIRACULOUS_ABILITY, PacketByteBufs.empty())
                    return@register
                }

                client.setScreen(
                    RadialScreen(
                        "screen.miraculous_miracles.use_ability",
                        abilityKey,
                        MiraculousAbility.values().filter { ability -> miraculous.contains(ability.miraculousType) }
                            .map { ability ->
                                RadialAction("ability.miraculous_miracles.$ability") { ->
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
