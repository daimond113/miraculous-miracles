package com.daimond113.miraculous_miracles.ui

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.NetworkMessages
import io.wispforest.owo.ui.base.BaseUIModelScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.FlowLayout
import net.minecraft.util.Identifier
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking


class VoyageCoordinateScreen : BaseUIModelScreen<FlowLayout>(
    FlowLayout::class.java,
    DataSource.asset(Identifier(MiraculousMiracles.MOD_ID, "voyage_coords"))
) {
    override fun build(rootComponent: FlowLayout) {
        val intRegex = Regex("-?\\d*")
        val coordinateFields = arrayOf("x", "y", "z")
            .map { rootComponent.childById(TextBoxComponent::class.java, it)!! }
            .onEach { it.setTextPredicate { s -> s.matches(intRegex) } }

        rootComponent.childById(ButtonComponent::class.java, "submit")!!
            .onPress {
                val packetBuf = PacketByteBufs.create()
                packetBuf.writeIntArray(coordinateFields.map { it.text.toIntOrNull() ?: 0 }.toIntArray())

                ClientPlayNetworking.send(NetworkMessages.SET_VOYAGE_COORDS, packetBuf)

                closeScreen()
            }
    }
}
