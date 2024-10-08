package me.lumiafk.itemswap

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.text.Text
import net.minecraft.util.math.Vec2f
import org.lwjgl.opengl.GL11

object Util {
	inline val String.text: Text get() = Text.of(this)
	fun ClientPlayerEntity.sendString(message: String, overlay: Boolean = false) = sendMessage(message.text, overlay)

	fun renderLine(from: Vec2f, to: Vec2f, fromColor: Int, toColor: Int, lineWidth: Float = 2f) {
		RenderSystem.assertOnRenderThread()
		GL11.glEnable(GL11.GL_LINE_SMOOTH)
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
		RenderSystem.depthMask(false)
		RenderSystem.disableCull()
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
		val bufferBuilder = Tessellator.getInstance().buffer
		bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
		RenderSystem.lineWidth(lineWidth)
		val normal = to.add(from.negate()).normalize()

		bufferBuilder.vertex(from.x.toDouble(), from.y.toDouble(), 0.0).color(fromColor).normal(normal.x, normal.y, 0.0F).next()
		bufferBuilder.vertex(to.x.toDouble(), to.y.toDouble(), 0.0).color(toColor).normal(normal.x, normal.y, 0.0F).next()

		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
		GL11.glDisable(GL11.GL_LINE_SMOOTH)
		RenderSystem.lineWidth(1.0f)
		RenderSystem.enableCull()
		RenderSystem.depthMask(true)
	}
}
