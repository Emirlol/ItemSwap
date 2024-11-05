package me.lumiafk.itemswap

import com.mojang.brigadier.Command
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair
import kotlinx.coroutines.*
import me.lumiafk.itemswap.Util.sendString
import me.lumiafk.itemswap.config.ConfigHandler
import me.lumiafk.itemswap.config.ConfigHandler.config
import me.lumiafk.itemswap.mixin.HandledScreenAccessor
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.Vec2f
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory

typealias Point = Vec2f

object ItemSwap {
	const val NAME = "Item Swap"
	const val NAMESPACE = "itemswap"
	private val logger: Logger = LoggerFactory.getLogger("ItemSwap")
	private val CONFIGURE_KEY = KeyBinding("key.$NAMESPACE.configure", GLFW.GLFW_KEY_L, "category.$NAMESPACE.main")
	private val RESET_KEY = KeyBinding("key.$NAMESPACE.reset", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main")
	private val keys = listOf(CONFIGURE_KEY, RESET_KEY)
	private var globalJob = CoroutineScope(SupervisorJob() + CoroutineName("SlotSwap"))
	private val slotMap get() = config.slotSquareMap

	//This field is used instead of the field in MinecraftClient, so we don't have to type check the current screen every time.
	private var currentScreen: InventoryScreen? = null
	private var slotAtKeyPress: Int? = null

	//Self-resetting state for the reset keybinding to make sure the user wants to reset by requiring the key to be pressed twice within `delay` ms
	private var shouldResetOnNextKey = false
	private var waitJob: Job? = null

	private const val SLOT_SIZE = 16
	private const val HALF_SLOT_SIZE = SLOT_SIZE / 2

	//These 2 aren't configurable, just because
	private const val CONFIGURING_SOURCE_SLOT_COLOR = 0xFF993AFF.toInt()
	private const val CONFIGURING_TARGET_SLOT_COLOR = 0xFFFF9214.toInt()
	private const val OFFSET_TO_CENTER = 1f // This is needed because SlotSquares' x and y values are 1 less than what they should be for being visually in the center.


	@Suppress("unused")
	fun onInitializeClient() {
		check(ConfigHandler.load()) { "Failed to load config." }
		ClientLifecycleEvents.CLIENT_STOPPING.register {
			globalJob.cancel()
			logger.info("Cancelling all coroutines.")
		}
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(
				literal(NAMESPACE)
					.then(literal("config")
						.executes { context ->
							context.source.client.let {
								it.send {
									it.setScreen(ConfigHandler.createGui(it.currentScreen))
								}
							}
							Command.SINGLE_SUCCESS
						})
			)
		}
		registerKeys()
		ScreenEvents.AFTER_INIT.register { client, screen, _, _ ->
			//Since this event is called for each screen, we can just check the config enabled status here once and for all
			if (screen !is InventoryScreen || !config.enabled) return@register
			currentScreen = screen
			ScreenKeyboardEvents.afterKeyPress(screen).register { _, key, scancode, _ ->
				onKeyPress(screen, key, scancode)
			}
			ScreenKeyboardEvents.afterKeyRelease(screen).register { _, key, scancode, _ ->
				onKeyRelease(client, screen, key, scancode)
			}
			ScreenEvents.afterRender(screen).register { _, drawContext, _, _, _ ->
				render(screen, drawContext)
			}
			ScreenMouseEvents.allowMouseClick(screen).register { _, _, _, button ->
				onMouseClick(client, screen, button)
			}
			ScreenEvents.remove(screen).register {
				currentScreen = null
			}
		}
	}

	private fun onMouseClick(client: MinecraftClient, screen: InventoryScreen, button: Int): Boolean {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !InputUtil.isKeyPressed(client.window.handle, GLFW.GLFW_KEY_LEFT_SHIFT)) return true

		val entry: IntIntImmutablePair = screen.accessor.focusedSlot?.id?.let { id ->
			slotMap.firstOrNull { pair -> pair.keyInt() == id || pair.valueInt() == id }
		} ?: return true

		if (isHotbarSlot(entry.keyInt())) {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, entry.valueInt(), entry.keyInt() - 36, SlotActionType.SWAP, client.player)
			return false
		} else if (isHotbarSlot(entry.valueInt())) {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, entry.keyInt(), entry.valueInt() - 36, SlotActionType.SWAP, client.player)
			return false
		}
		return true
	}

	private fun onKeyPress(screen: InventoryScreen, key: Int, scancode: Int) {
		if (!CONFIGURE_KEY.matchesKey(key, scancode) || slotAtKeyPress != null) return
		slotAtKeyPress = screen.getFocusedSlotId()
	}

	private fun onKeyRelease(client: MinecraftClient, screen: InventoryScreen, key: Int, scancode: Int) {
		when {
			CONFIGURE_KEY.matchesKey(key, scancode) -> {
				val slotAtKeyRelease = screen.getFocusedSlotId()
				if (slotAtKeyPress != null && slotAtKeyRelease != null && slotAtKeyPress != slotAtKeyRelease
					&& (isHotbarSlot(slotAtKeyPress!!) || isHotbarSlot(slotAtKeyRelease))
				) { // At least one slot has to be in the hotbar
					addSlotMapping(slotAtKeyPress!!, slotAtKeyRelease)
					logger.info("Added slot mapping from $slotAtKeyPress to $slotAtKeyRelease.")
				}
				slotAtKeyPress = null
			}

			RESET_KEY.matchesKey(key, scancode) -> {
				if (shouldResetOnNextKey) {
					client.player?.sendString("Successfully reset all slot mappings.")
					logger.info("Reset all slot mappings.")
					logger.info("All mappings before reset: $slotMap")
					reset()
					return
				}
				client.player?.sendString("Press the reset key again within ${config.waitTime}ms to confirm resetting all slot mappings.")
				shouldResetOnNextKey = true
				waitJob = globalJob.launch {
					delay(config.waitTime)
					shouldResetOnNextKey = false //Resets back to the initial state after waitTime ms, but if the keybinding is pressed again then this timeout is canceled and the reset happens
				}
			}
		}
	}

	private fun render(screen: InventoryScreen, drawContext: DrawContext) {
		//Slot mappings
		for (entry in slotMap) {
			renderRectanglesAndLine(drawContext, entry.keyInt(), entry.valueInt(), config.sourceSlotColor.rgb, config.targetSlotColor.rgb)
		}
		//Configuring state
		if (slotAtKeyPress == null) return
		val currentSlot = screen.getFocusedSlotId()
		renderRectanglesAndLine(drawContext, slotAtKeyPress!!, currentSlot, CONFIGURING_SOURCE_SLOT_COLOR, CONFIGURING_TARGET_SLOT_COLOR)
	}

	private val InventoryScreen.accessor get() = this as HandledScreenAccessor

	private fun InventoryScreen.getFocusedSlotId() = accessor.focusedSlot?.let { slot -> if (isSlotValid(slot.id)) slot.id else null }

	private fun renderRectanglesAndLine(drawContext: DrawContext, slotId1: Int, slotId2: Int?, sourceColor: Int, targetColor: Int) {
		val pos1 = getPosFromSlotId(slotId1) ?: return
		drawContext.drawBorder(pos1.x, pos1.y, SLOT_SIZE, SLOT_SIZE, sourceColor) // Source slot
		if (slotId1 == slotId2 || slotId2 == null) return
		val pos2 = getPosFromSlotId(slotId2) ?: return
		drawContext.drawBorder(pos2.x, pos2.y, SLOT_SIZE, SLOT_SIZE, targetColor) // Target slot

		//Draws a line between the two slots' centers
		Util.renderLine(getCenterFromPos(pos1), getCenterFromPos(pos2), sourceColor, targetColor)
		//Todo: Find out the intersection point of the line and the square and render from that instead of the center as it looks ugly
		//Disclaimer: Maths is hard
	}

	private fun reset() {
		slotMap.clear()
		ConfigHandler.save()
		slotAtKeyPress = null
		waitJob?.cancel()
		waitJob = null
		shouldResetOnNextKey = false
	}

	private fun isSlotValid(slotId: Int) = slotId in PlayerScreenHandler.EQUIPMENT_START..<PlayerScreenHandler.HOTBAR_END

	private fun isHotbarSlot(slotId: Int) = slotId in PlayerScreenHandler.HOTBAR_START..<PlayerScreenHandler.HOTBAR_END

	private fun addSlotMapping(source: Int, aimed: Int) {
		//Remove all mappings that are related to these keys
		slotMap.removeAll { pair -> pair.keyInt() == source || pair.valueInt() == source || pair.keyInt() == aimed || pair.valueInt() == aimed }
		slotMap += IntIntImmutablePair.of(source, aimed)
		ConfigHandler.save()
	}

	private fun getPosFromSlotId(slotId: Int) = currentScreen?.screenHandler?.getSlot(slotId)?.let { slot ->
		//This will only be reached if the screen isn't null
		currentScreen!!.accessor.let { screen ->
			Vector2i(slot.x + screen.x, slot.y + screen.y)
		}
	}

	private fun getCenterFromPos(pos: Vector2i) = Point(pos.x + HALF_SLOT_SIZE + OFFSET_TO_CENTER, pos.y + HALF_SLOT_SIZE + OFFSET_TO_CENTER)

	private fun registerKeys() = keys.forEach { KeyBindingHelper.registerKeyBinding(it) }
}
