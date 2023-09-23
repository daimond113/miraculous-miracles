package com.daimond113.miraculous_miracles.miraculouses.rabbit.kwami

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.MiraculousMiraclesClient
import com.daimond113.miraculous_miracles.core.AbstractKwami
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.util.Identifier

class RabbitKwamiRenderer(context: EntityRendererFactory.Context) :
    MobEntityRenderer<AbstractKwami, RabbitKwamiModel>(
        context,
        RabbitKwamiModel(context.getPart(MiraculousMiraclesClient.MODEL_RABBIT_KWAMI_LAYER)),
        0.5f
    ) {
    override fun getTexture(entity: AbstractKwami): Identifier {
        return Identifier(MiraculousMiracles.MOD_ID, "textures/entity/rabbit_kwami/kwami.png")
    }
}

