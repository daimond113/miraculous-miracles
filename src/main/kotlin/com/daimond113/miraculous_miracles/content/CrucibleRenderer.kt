package com.daimond113.miraculous_miracles.content

import com.daimond113.miraculous_miracles.MiraculousMiracles
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack


class CrucibleRenderer : BlockEntityRenderer<CrucibleEntity> {
    override fun render(
        blockEntity: CrucibleEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val miraculousItem =
            if (blockEntity.miraculousType != null) MiraculousMiracles.MIRACULOUSES[blockEntity.miraculousType] else null

        if (miraculousItem != null && blockEntity.startedAt != null) {
            matrices.push()

            val time = (blockEntity.world!!.time + tickDelta) / 20.0 // convert ticks to seconds
            val duration = 3
            val offset =
                if (time - blockEntity.startedAt!! < duration) (time - blockEntity.startedAt!!) * 3 / duration else 3.0

            matrices.translate(0.5, offset, 0.5)

            val lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.world, blockEntity.pos.up())
            MinecraftClient.getInstance().itemRenderer.renderItem(
                ItemStack(miraculousItem),
                ModelTransformation.Mode.FIXED,
                lightAbove,
                overlay,
                matrices,
                vertexConsumers,
                0
            );

            // Mandatory call after GL calls
            matrices.pop();
        }
    }
}
