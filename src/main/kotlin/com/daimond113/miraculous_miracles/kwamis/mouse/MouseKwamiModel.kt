package com.daimond113.miraculous_miracles.kwamis.rabbit

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.model.*
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack


// Made with Blockbench 4.8.1

class MouseKwamiModel(root: ModelPart) : EntityModel<AbstractKwami?>() {
    private val bb_main: ModelPart

    init {
        bb_main = root.getChild("bb_main")
    }

    override fun setAngles(
        entity: AbstractKwami?,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
    }

    override fun render(
        matrices: MatrixStack?,
        vertexConsumer: VertexConsumer?,
        light: Int,
        overlay: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha)
    }

    companion object {
        fun getTexturedModelData(): TexturedModelData {
            val modelData = ModelData()
            val modelPartData = modelData.root
            val bb_main = modelPartData.addChild(
                "bb_main",
                ModelPartBuilder.create().uv(0, 0).cuboid(-2.0f, -9.0f, -2.0f, 4.0f, 4.0f, 4.0f, Dilation(0.0f))
                    .uv(1, 11).cuboid(-0.9f, -2.2f, -0.5f, 0.8f, 2.0f, 1.0f, Dilation(0.0f))
                    .uv(0, 11).cuboid(0.1f, -2.2f, -0.5f, 0.8f, 2.0f, 1.0f, Dilation(0.0f))
                    .uv(0, 11).cuboid(-0.9f, -5.0f, -0.6f, 1.9f, 2.8f, 1.2f, Dilation(0.0f)),
                ModelTransform.pivot(0.0f, 24.0f, 0.0f)
            )

            bb_main.addChild(
                "cube_r1",
                ModelPartBuilder.create().uv(7, 9).cuboid(-0.65f, -0.5f, 1.7f, 1.3f, 3.0f, 2.9f, Dilation(0.0f)),
                ModelTransform.of(-0.9f, -10.5f, 0.0f, 0.0f, 1.5708f, 0.0f)
            )

            bb_main.addChild(
                "cube_r2",
                ModelPartBuilder.create().uv(7, 9).mirrored()
                    .cuboid(-0.65f, -0.5f, 1.7f, 1.3f, 3.0f, 2.9f, Dilation(0.0f)).mirrored(false),
                ModelTransform.of(0.9f, -10.5f, 0.0f, 0.0f, -1.5708f, 0.0f)
            )

            bb_main.addChild(
                "cube_r3",
                ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, 0.0f, -0.5f, 1.0f, 2.5f, 1.0f, Dilation(0.0f)),
                ModelTransform.of(-1.0f, -5.0f, 0.0f, 0.0f, 0.0f, 0.0524f)
            )

            bb_main.addChild(
                "cube_r4",
                ModelPartBuilder.create().uv(1, 11).cuboid(0.0f, 0.0f, -0.5f, 1.0f, 2.5f, 1.0f, Dilation(0.0f)),
                ModelTransform.of(1.0f, -5.0f, 0.0f, 0.0f, 0.0f, -0.0524f)
            )

            return TexturedModelData.of(modelData, 16, 16)
        }
    }
}

