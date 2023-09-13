package com.daimond113.miraculous_miracles.ui

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.MiraculousMiraclesClient
import com.daimond113.miraculous_miracles.core.NetworkMessages
import io.wispforest.owo.ui.base.BaseUIModelScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking


class BurrowCoordinateScreen : BaseUIModelScreen<FlowLayout>(
    FlowLayout::class.java,
    DataSource.asset(Identifier(MiraculousMiracles.MOD_ID, "burrow_coords"))
) {
    override fun build(rootComponent: FlowLayout) {
        val intRegex = Regex("-?\\d*")
        val coordinateFields = arrayOf("x", "y", "z")
            .map { rootComponent.childById(TextBoxComponent::class.java, it)!! }
            .onEach { it.setTextPredicate { s -> s.matches(intRegex) } }

        val currentLabel = rootComponent.childById(LabelComponent::class.java, "current-dimension")
        val dimensionNames = MiraculousMiraclesClient.allDimensions.associateWith {
            Text.literal(it.path.split("_")
                .joinToString(" ") { part -> part.replaceFirstChar { char -> char.uppercase() } })
        }
        var currentDimension = MiraculousMiraclesClient.allDimensions.first()

        fun updateCurrentDimension(dimension: Identifier) {
            currentLabel?.text(Text.literal("Current: ").append(dimensionNames[dimension]))
            currentDimension = dimension
        }
        updateCurrentDimension(currentDimension)

        val dimensionButtons = rootComponent.childById(FlowLayout::class.java, "dimension-buttons")

        dimensionButtons?.children(MiraculousMiraclesClient.allDimensions.map {
            Components.button(dimensionNames[it]!!) { _ ->
                updateCurrentDimension(
                    it
                )
            }.horizontalSizing(Sizing.fixed(150))
        })

        rootComponent.childById(ButtonComponent::class.java, "submit")!!
            .onPress {
                ClientPlayNetworking.send(NetworkMessages.SET_PORTAL_COORDS, PacketByteBufs.create().apply {
                    writeBoolean(true)
                    writeIntArray(coordinateFields.map { it.text.toIntOrNull() ?: 0 }.toIntArray())
                    writeIdentifier(currentDimension)
                })
                closeScreen()
            }
    }
}
