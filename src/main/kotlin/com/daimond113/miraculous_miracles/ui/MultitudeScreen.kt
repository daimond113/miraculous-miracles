package com.daimond113.miraculous_miracles.ui

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.NetworkMessages
import io.wispforest.owo.ui.base.BaseUIModelScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.DiscreteSliderComponent
import io.wispforest.owo.ui.container.FlowLayout
import net.minecraft.util.Identifier
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking


class MultitudeScreen : BaseUIModelScreen<FlowLayout>(
    FlowLayout::class.java,
    DataSource.asset(Identifier(MiraculousMiracles.MOD_ID, "multitude_amount"))
) {
    override fun build(rootComponent: FlowLayout) {
        val sliderComponent = rootComponent.childById(DiscreteSliderComponent::class.java, "amount-slider")

        rootComponent.childById(ButtonComponent::class.java, "submit")!!
            .onPress {
                closeScreen()
                ClientPlayNetworking.send(NetworkMessages.SET_MULTITUDE_AMOUNT, PacketByteBufs.create().apply {
                    writeInt(sliderComponent!!.discreteValue().toInt())
                })
            }
    }
}
