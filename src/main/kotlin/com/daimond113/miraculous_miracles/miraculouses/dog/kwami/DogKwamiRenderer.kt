package com.daimond113.miraculous_miracles.miraculouses.dog.kwami

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.MiraculousMiraclesClient
import com.daimond113.miraculous_miracles.core.AbstractKwami
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.util.Identifier

class DogKwamiRenderer(context: EntityRendererFactory.Context) :
    MobEntityRenderer<AbstractKwami, DogKwamiModel>(
        context,
        DogKwamiModel(context.getPart(MiraculousMiraclesClient.MODEL_DOG_KWAMI_LAYER)),
        0.5f
    ) {
    override fun getTexture(entity: AbstractKwami): Identifier {
        return Identifier(MiraculousMiracles.MOD_ID, "textures/entity/dog_kwami/kwami.png")
    }
}

