package me.lumiafk.itemswap

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.Vec2f
import org.lwjgl.opengl.GL11

object Util {
	inline val client: MinecraftClient get() = MinecraftClient.getInstance()
	inline val player: ClientPlayerEntity? get() = client.player
	inline val String.text: Text get() = Text.of(this)

	fun ClientPlayerEntity.sendInfo(message: String, overlay: Boolean = false) = sendMessage(prefix.append(Text.literal(message).withColor(0xcdd6f4)), overlay)
	fun ClientPlayerEntity.sendWarning(message: String, overlay: Boolean = false) = sendMessage(prefix.append(Text.literal(message).withColor(0xfab387)), overlay)
	fun ClientPlayerEntity.sendError(message: String, overlay: Boolean = false) = sendMessage(prefix.append(Text.literal(message).withColor(0xf38ba8)), overlay)
	fun ClientPlayerEntity.sendSuccess(message: String, overlay: Boolean = false) = sendMessage(prefix.append(Text.literal(message).withColor(0xa6e3a1)), overlay)

	private val prefix
		get() = Text.empty()
			.append(Text.literal("[").withColor(0xa6adc8))
			.append(Text.literal("ItemSwap").withColor(0xcba6f7))
			.append(Text.literal("]").withColor(0xa6adc8))
			.append(" ")

	fun isHotbarSlot(slotId: Int) = slotId in PlayerScreenHandler.HOTBAR_START..<PlayerScreenHandler.HOTBAR_END

	fun renderLine(from: Vec2f, to: Vec2f, fromColor: Int, toColor: Int, lineWidth: Float = 2f) {
		RenderSystem.assertOnRenderThread()
		GL11.glEnable(GL11.GL_LINE_SMOOTH)
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
		RenderSystem.depthMask(false)
		RenderSystem.disableCull()
		RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES)
		val bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
		RenderSystem.lineWidth(lineWidth)
		val normal = to.add(from.negate()).normalize()

		bufferBuilder.vertex(from.x, from.y, 0.0F).color(fromColor).normal(normal.x, normal.y, 0.0F)
		bufferBuilder.vertex(to.x, to.y, 0.0F).color(toColor).normal(normal.x, normal.y, 0.0F)

		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
		GL11.glDisable(GL11.GL_LINE_SMOOTH)
		RenderSystem.lineWidth(1.0f)
		RenderSystem.enableCull()
		RenderSystem.depthMask(true)
	}
}
