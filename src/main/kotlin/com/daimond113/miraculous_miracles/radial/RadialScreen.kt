package com.daimond113.miraculous_miracles.radial

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.InputUtil
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.Tessellator
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.option.KeyBind
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import kotlin.math.*

class RadialScreen(screenName: String, private val key: KeyBind, private val actions: List<RadialAction>) :
    Screen(Text.translatable(screenName)) {

    private var crosshairX = 0
    private var crosshairY = 0
    private var focusedAction = 0
    private var prevFocusedAction = -1
    private val actionAmount = actions.indices.last + 1

    private fun cursorMode(mode: Int) {
        val x = (client!!.window.width / 2).toDouble()
        val y = (client!!.window.height / 2).toDouble()
        InputUtil.setCursorParameters(client!!.window.handle, mode, x, y)
    }

    override fun init() {
        super.init()
        cursorMode(GLFW.GLFW_CURSOR_DISABLED)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!key.isPressed) closeScreen()
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun tick() {
        if (focusedAction == prevFocusedAction) return
        var diff = abs((focusedAction - prevFocusedAction).toDouble()).toInt()
        if (diff >= actionAmount - 1) diff = 1
        val pitch = 1f + diff.toFloat() / actionAmount.toFloat()
        client!!.world!!.playSoundFromEntity(
            client!!.player,
            client!!.player,
            SoundEvents.UI_BUTTON_CLICK,
            SoundCategory.MASTER,
            0.2f,
            pitch
        )
        prevFocusedAction = focusedAction
    }

    override fun closeScreen() {
        cursorMode(GLFW.GLFW_CURSOR_NORMAL)
        if (focusedAction > -1) {
            actions[focusedAction].activated()
        }
        super.closeScreen()
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        // CURSOR
        drawTexture(matrices, GUI_ICONS_TEXTURE, crosshairX - 8, crosshairY - 8, 0, 0, 15, 15, true)

        val radius = (min(height.toDouble(), width.toDouble()) / 2 * 0.5).toInt()

        drawItems(matrices, radius)

        matrices.scale(2f, 2f, 1f)

        var mousePosition = (Vector2(mouseX.toFloat(), mouseY.toFloat()) - Vector2(width / 2f, height / 2f)).normalized

        val distanceFromCenter = hypot((width / 2 - mouseX).toDouble(), (height / 2 - mouseY).toDouble())

        mousePosition *= if (distanceFromCenter < radius) distanceFromCenter.toFloat() else radius.toFloat()

        crosshairX = mousePosition.x.toInt() + width / 2
        crosshairY = mousePosition.y.toInt() + height / 2

        super.render(matrices, mouseX, mouseY, delta)
    }

    private fun drawTexture(
        matrices: MatrixStack,
        id: Identifier,
        x: Int,
        y: Int,
        u: Int,
        v: Int,
        w: Int,
        h: Int,
        invert: Boolean
    ) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShader { GameRenderer.getPositionTexShader() }
        RenderSystem.setShaderTexture(0, id)
        RenderSystem.enableBlend()
        if (invert) RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
            GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        drawTexture(matrices, x, y, u, v, w, h)
    }

    private fun drawItems(matrix: MatrixStack, radius: Int) {
        var lowestDistance = Double.MAX_VALUE

        val actionsData = actions.indices.map { actionIndex ->
            val s = actionIndex.toDouble() / actionAmount * 2 * Math.PI
            val x = Math.round(radius * cos(s) + width / 2) - 8f
            val y = (Math.round(radius * sin(s) + height / 2) - 8f) + textRenderer.fontHeight

            val mouseDistance = hypot((x - crosshairX).toDouble(), (y - crosshairY).toDouble())

            if (mouseDistance < lowestDistance) {
                lowestDistance = mouseDistance
                focusedAction = actionIndex
            }

            actionIndex to Pair(x, y)
        }

//        MiraculousMiracles.LOGGER.info("MouseX: $mouseX MouseY: $mouseY, CrosshairX: $crosshairX CrosshairY: $crosshairY")
//        MiraculousMiracles.LOGGER.info("Lowest: $lowestDistance")

        if (lowestDistance > 20.0) {
            focusedAction = -1
        }

        val centerX = width / 2

        for ((actionIndex, coords) in actionsData) {
            val (x, y) = coords

            val text = Text.translatable(actions[actionIndex].text).asOrderedText()
            val textWidth = textRenderer.getWidth(text)

            val xModifier = if (x <= centerX) textWidth else 0

            if (focusedAction == actionIndex) {
                val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().bufferBuilder)

                textRenderer.drawWithOutline(
                    text,
                    x - xModifier,
                    y,
                    0xffffff,
                    0x000000,
                    matrix.peek().model,
                    immediate,
                    15728880
                )

                immediate.draw()
            } else {
                textRenderer.drawWithShadow(matrix, text, x - xModifier, y, 0xffffff)
            }
        }
    }

    override fun isPauseScreen(): Boolean {
        return false
    }
}
