package me.lumiafk.itemswap

import com.mojang.brigadier.Command
import me.lumiafk.itemswap.config.ConfigHandler
import me.lumiafk.itemswap.config.ConfigHandler.config
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.option.KeyBinding
import net.minecraft.screen.slot.SlotActionType
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ItemSwap {
	@Suppress("unused")
	fun onInitializeClient() {
		check(ConfigHandler.load()) { "Failed to load config." }
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
			if (screen !is InventoryScreen) return@register
			ScreenKeyboardEvents.afterKeyRelease(screen).register { _, keyCode, scancode, _ ->
				for (key in keys) {
					if (!key.keyBinding.matchesKey(keyCode, scancode)) continue
					val string = key.configuration.invoke()
					if (string.isNotEmpty() && string.matches(regex)) {
						val (invSlot, hotbarSlot) = string.replace(" ", "").split('>')
						client.interactionManager?.clickSlot(screen.screenHandler.syncId, invSlot.toInt(), hotbarSlot.toInt() - 1, SlotActionType.SWAP, client.player)
					} else {
						logger.warn("Invalid config value: \"$string\"")
					}
				}
			}
		}
	}

	private fun registerKeys() = keys.forEach { KeyBindingHelper.registerKeyBinding(it.keyBinding) }

	const val NAME = "Item Swap"
	const val NAMESPACE = "itemswap"
	val logger: Logger = LoggerFactory.getLogger("ItemSwap")
	val regex = Regex("^ *\\d+ ?> ?[1-9] *$")
	val KEY_1 = Key("key.$NAMESPACE.1", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main") { config.key1 }
	val KEY_2 = Key("key.$NAMESPACE.2", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main") { config.key2 }
	val KEY_3 = Key("key.$NAMESPACE.3", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main") { config.key3 }
	val KEY_4 = Key("key.$NAMESPACE.4", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main") { config.key4 }
	val KEY_5 = Key("key.$NAMESPACE.5", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main") { config.key5 }
	val KEY_6 = Key("key.$NAMESPACE.6", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main") { config.key6 }
	val KEY_7 = Key("key.$NAMESPACE.7", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main") { config.key7 }
	val KEY_8 = Key("key.$NAMESPACE.8", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main") { config.key8 }
	val KEY_9 = Key("key.$NAMESPACE.9", GLFW.GLFW_KEY_UNKNOWN, "category.$NAMESPACE.main") { config.key9 }
	val keys = listOf(KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_7, KEY_8, KEY_9)
}

data class Key(val keyBinding: KeyBinding, val configuration: () -> String) {
	constructor(translationKey: String, code: Int, category: String, configuration: () -> String) : this(KeyBinding(translationKey, code, category), configuration)
}