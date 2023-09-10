package com.daimond113.miraculous_miracles.kwamis.horse

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.MiraculousMiraclesClient
import com.daimond113.miraculous_miracles.core.AbstractKwami
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.util.Identifier

class HorseKwamiRenderer(context: EntityRendererFactory.Context) :
    MobEntityRenderer<AbstractKwami, HorseKwamiModel>(
        context,
        HorseKwamiModel(context.getPart(MiraculousMiraclesClient.MODEL_HORSE_KWAMI_LAYER)),
        0.5f
    ) {
    override fun getTexture(entity: AbstractKwami): Identifier {
        return Identifier(MiraculousMiracles.MOD_ID, "textures/entity/horse_kwami/kwami.png")
    }
}

