//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package com.daimond113.miraculous_miracles.content

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.PlayerModelPart
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.CrossbowItem
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Arm
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.UseAction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import kotlin.math.acos
import kotlin.math.sign
import kotlin.math.sqrt

class MultitudeEntityRenderer(ctx: EntityRendererFactory.Context) :
    LivingEntityRenderer<MultitudeEntity, PlayerEntityModel<MultitudeEntity>>(
        ctx,
        PlayerEntityModel(
            ctx.getPart(EntityModelLayers.PLAYER),
            false
        ),
        0.5f
    ) {
    private val normalModel = model
    private val slimModel = PlayerEntityModel<MultitudeEntity>(
        ctx.getPart(EntityModelLayers.PLAYER_SLIM),
        true
    )
    private val normalArmorFeatureRenderer = ArmorFeatureRenderer(
        this,
        BipedEntityModel(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
        BipedEntityModel(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)),
    )

    private val slimArmorFeatureRenderer = ArmorFeatureRenderer(
        this,
        BipedEntityModel(ctx.getPart(EntityModelLayers.PLAYER_SLIM_INNER_ARMOR)),
        BipedEntityModel(ctx.getPart(EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR)),
    )

    init {
        addFeature(normalArmorFeatureRenderer)
        addFeature(HeldItemFeatureRenderer(this, ctx.heldItemRenderer))
        addFeature(StuckArrowsFeatureRenderer(ctx, this))
//        addFeature(Deadmau5FeatureRenderer(this))
//        addFeature(CapeFeatureRenderer(this))
        addFeature(HeadFeatureRenderer(this, ctx.modelLoader, ctx.heldItemRenderer))
        addFeature(ElytraFeatureRenderer(this, ctx.modelLoader))
//        addFeature(ShoulderParrotFeatureRenderer(this, ctx.modelLoader))
        addFeature(TridentRiptideFeatureRenderer(this, ctx.modelLoader))
        addFeature(StuckStingersFeatureRenderer(this))
    }

    override fun render(
        abstractClientPlayerEntity: MultitudeEntity,
        f: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        setModelPose(abstractClientPlayerEntity)
        features.removeAll { it == slimArmorFeatureRenderer || it == normalArmorFeatureRenderer }
        model = if ((abstractClientPlayerEntity.owner as ClientPlayerEntity?)?.model == "default") {
            features.add(normalArmorFeatureRenderer)
            normalModel
        } else {
            features.add(slimArmorFeatureRenderer)
            slimModel
        }
        super.render(abstractClientPlayerEntity, f, g, matrixStack, vertexConsumerProvider, i)
    }

    override fun getPositionOffset(abstractClientPlayerEntity: MultitudeEntity, f: Float): Vec3d {
        return if (abstractClientPlayerEntity.isInSneakingPose) Vec3d(0.0, -0.125, 0.0) else super.getPositionOffset(
            abstractClientPlayerEntity,
            f
        )
    }

    private fun setModelPose(player: MultitudeEntity) {
        val playerEntityModel = getModel()
        if (player.isSpectator) {
            playerEntityModel!!.setVisible(false)
            playerEntityModel.head.visible = true
            playerEntityModel.hat.visible = true
        } else {
            playerEntityModel!!.setVisible(true)
            playerEntityModel.hat.visible = player.isPartVisible(PlayerModelPart.HAT)
            playerEntityModel.jacket.visible = player.isPartVisible(PlayerModelPart.JACKET)
            playerEntityModel.leftPants.visible = player.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG)
            playerEntityModel.rightPants.visible = player.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG)
            playerEntityModel.leftSleeve.visible = player.isPartVisible(PlayerModelPart.LEFT_SLEEVE)
            playerEntityModel.rightSleeve.visible = player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE)
            playerEntityModel.sneaking = player.isInSneakingPose
            val armPose = getArmPose(player, Hand.MAIN_HAND)
            var armPose2 = getArmPose(player, Hand.OFF_HAND)
            if (armPose.isTwoHanded) {
                armPose2 = if (player.offHandStack.isEmpty) ArmPose.EMPTY else ArmPose.ITEM
            }
            if (player.mainArm == Arm.RIGHT) {
                playerEntityModel.rightArmPose = armPose
                playerEntityModel.leftArmPose = armPose2
            } else {
                playerEntityModel.rightArmPose = armPose2
                playerEntityModel.leftArmPose = armPose
            }
        }
    }

    override fun getTexture(abstractClientPlayerEntity: MultitudeEntity): Identifier {
        return abstractClientPlayerEntity.skinTexture
    }

    override fun scale(abstractClientPlayerEntity: MultitudeEntity, matrixStack: MatrixStack, f: Float) {
        val g = 0.9375f
        matrixStack.scale(g, g, g)
    }

    override fun renderLabelIfPresent(
        abstractClientPlayerEntity: MultitudeEntity,
        text: Text,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        //noop
    }

    override fun setupTransforms(
        abstractClientPlayerEntity: MultitudeEntity,
        matrixStack: MatrixStack,
        f: Float,
        g: Float,
        h: Float
    ) {
        val i = abstractClientPlayerEntity.getLeaningPitch(h)
        val j: Float
        val k: Float
        if (abstractClientPlayerEntity.isFallFlying) {
            super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h)
            j = abstractClientPlayerEntity.roll.toFloat() + h
            k = MathHelper.clamp(j * j / 100.0f, 0.0f, 1.0f)
            if (!abstractClientPlayerEntity.isUsingRiptide) {
                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k * (-90.0f - abstractClientPlayerEntity.pitch)))
            }
            val vec3d = abstractClientPlayerEntity.getRotationVec(h)
            val vec3d2 = abstractClientPlayerEntity.velocity
            val d = vec3d2.horizontalLengthSquared()
            val e = vec3d.horizontalLengthSquared()
            if (d > 0.0 && e > 0.0) {
                val l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / sqrt(d * e)
                val m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x
                matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((sign(m) * acos(l)).toFloat()))
            }
        } else if (i > 0.0f) {
            super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h)
            j = if (abstractClientPlayerEntity.isTouchingWater) -90.0f - abstractClientPlayerEntity.pitch else -90.0f
            k = MathHelper.lerp(i, 0.0f, j)
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k))
            if (abstractClientPlayerEntity.isInSwimmingPose) {
                matrixStack.translate(0.0, -1.0, 0.30000001192092896)
            }
        } else {
            super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h)
        }
    }

    companion object {
        private fun getArmPose(player: MultitudeEntity, hand: Hand): ArmPose {
            val itemStack = player.getStackInHand(hand)
            return if (itemStack.isEmpty) {
                ArmPose.EMPTY
            } else {
                if (player.activeHand == hand && player.itemUseTimeLeft > 0) {
                    val useAction = itemStack.useAction
                    if (useAction == UseAction.BLOCK) {
                        return ArmPose.BLOCK
                    }
                    if (useAction == UseAction.BOW) {
                        return ArmPose.BOW_AND_ARROW
                    }
                    if (useAction == UseAction.SPEAR) {
                        return ArmPose.THROW_SPEAR
                    }
                    if (useAction == UseAction.CROSSBOW && hand == player.activeHand) {
                        return ArmPose.CROSSBOW_CHARGE
                    }
                    if (useAction == UseAction.SPYGLASS) {
                        return ArmPose.SPYGLASS
                    }
                    if (useAction == UseAction.TOOT_HORN) {
                        return ArmPose.TOOT_HORN
                    }
                } else if (!player.handSwinging && itemStack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack)) {
                    return ArmPose.CROSSBOW_HOLD
                }
                ArmPose.ITEM
            }
        }
    }
}
