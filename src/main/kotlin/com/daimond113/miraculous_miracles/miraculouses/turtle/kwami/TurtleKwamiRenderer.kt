package com.daimond113.miraculous_miracles.miraculouses.turtle.kwami

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.MiraculousMiraclesClient
import com.daimond113.miraculous_miracles.core.AbstractKwami
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.util.Identifier

class TurtleKwamiRenderer(context: EntityRendererFactory.Context) : MobEntityRenderer<AbstractKwami, TurtleKwamiModel>(
    context,
    TurtleKwamiModel(context.getPart(MiraculousMiraclesClient.MODEL_TURTLE_KWAMI_LAYER)),
    0.5f
) {
    override fun getTexture(entity: AbstractKwami): Identifier {
        return Identifier(MiraculousMiracles.MOD_ID, "textures/entity/turtle_kwami/kwami.png")
    }
}
