package com.daimond113.miraculous_miracles.miraculouses.snake.kwami

import com.daimond113.miraculous_miracles.core.AbstractKwami
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.model.*
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack

// Made with Blockbench 4.8.1

class SnakeKwamiModel(root: ModelPart) : EntityModel<AbstractKwami?>() {
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
                    .uv(4, 0).cuboid(-0.9f, -2.2f, -0.5f, 0.8f, 2.0f, 1.0f, Dilation(0.0f))
                    .uv(3, 0).cuboid(0.1f, -2.2f, -0.5f, 0.8f, 2.0f, 1.0f, Dilation(0.0f))
                    .uv(9, 3).cuboid(-0.9f, -5.0f, -0.6f, 1.9f, 2.8f, 1.2f, Dilation(0.0f))
                    .uv(9, 13).cuboid(-0.5f, -3.0f, 0.4f, 1.0f, 0.8f, 2.2f, Dilation(0.0f))
                    .uv(6, 0).cuboid(-2.475f, -9.05f, 2.05f, 4.95f, 4.1f, 0.0f, Dilation(0.05f)),
                ModelTransform.pivot(0.0f, 24.0f, 0.0f)
            )

            bb_main.addChild(
                "cube_r1",
                ModelPartBuilder.create().uv(0, 8).cuboid(-2.5f, -0.2f, 0.15f, 5.0f, 1.1f, 0.0f, Dilation(0.0005f)),
                ModelTransform.of(0.0f, -9.0f, -2.4f, 2.4435f, 0.0f, 3.1416f)
            )

            bb_main.addChild(
                "cube_r2",
                ModelPartBuilder.create().uv(0, 10).cuboid(-2.5f, -2.6f, 2.05f, 5.0f, 4.6f, 0.0f, Dilation(0.05f)),
                ModelTransform.of(0.0f, -7.0f, 0.0f, 1.5708f, 0.0f, 0.0f)
            )

            bb_main.addChild(
                "cube_r3",
                ModelPartBuilder.create().uv(7, 12).cuboid(-0.475f, 1.0f, -2.2f, 0.95f, 0.8f, 3.2f, Dilation(0.0f)),
                ModelTransform.of(0.0f, -2.6f, 4.0f, -2.0944f, 0.0f, 0.0f)
            )

            bb_main.addChild(
                "cube_r4",
                ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, 0.0f, -0.5f, 1.0f, 2.5f, 1.0f, Dilation(0.0f)),
                ModelTransform.of(-1.0f, -5.0f, 0.0f, 0.0f, 0.0f, 0.0524f)
            )

            bb_main.addChild(
                "cube_r5",
                ModelPartBuilder.create().uv(11, 3).cuboid(1.0244f, -2.3496f, -4.1f, 1.0f, 2.5f, 1.0f, Dilation(0.0f)),
                ModelTransform.of(0.1f, -2.6f, 3.6f, 0.0f, 0.0f, -0.0524f)
            )
            return TexturedModelData.of(modelData, 16, 16)
        }
    }
}

