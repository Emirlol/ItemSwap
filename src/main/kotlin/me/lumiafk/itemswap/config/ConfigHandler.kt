@file:Suppress("UnstableApiUsage")

package me.lumiafk.itemswap.config

import com.mojang.serialization.Codec
import dev.isxander.yacl3.api.OptionEventListener
import dev.isxander.yacl3.api.StateManager
import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.dsl.*
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lumiafk.itemswap.CodecUtil
import me.lumiafk.itemswap.ItemSwap
import me.lumiafk.itemswap.Util.text
import me.lumiafk.itemswap.slotswap.SlotSwapChain
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.notExists

val configPath: Path get() = FabricLoader.getInstance().configDir.resolve("${ItemSwap.NAMESPACE}/config.json")

typealias config = ConfigHandler

object ConfigHandler : JsonFileCodecConfig<ConfigHandler>(configPath) {
	val logger: Logger = LoggerFactory.getLogger(ItemSwap.NAMESPACE)

	fun load() {
		if (configPath.notExists()) {
			logger.info("Config file not found, creating one.")
			configPath.createParentDirectories()
			saveToFile()
		}
		loadFromFile()
	}

	val enabled by register(true, Codec.BOOL)
	val sourceSlotColor by register(Color(0xCBA6F7), CodecUtil.COLOR)
	val targetSlotColor by register(Color(0xfab387), CodecUtil.COLOR)
	val waitTime by register(500L, Codec.LONG)
	val slotSwapChains by register (ObjectArrayList(), CodecUtil.objectArrayList(SlotSwapChain.CODEC))

	fun createGui(parent: Screen?): Screen = YetAnotherConfigLib("itemswap") {
		title("${ItemSwap.NAME} Config".text)
		save(::saveToFile)
		val mainCategory by categories.registering {
			name(Text.translatable("category.${ItemSwap.NAMESPACE}.main"))

			val enabled by rootOptions.registering {
				val stateManager = StateManager.createSimple(enabled.asBinding())
				name(Text.translatable("config.${ItemSwap.NAMESPACE}.enabled"))
				stateManager(stateManager)
				val sourceSlotColor by rootOptions.futureRef<Color>()
				val targetSlotColor by rootOptions.futureRef<Color>()
				val resetCounter by rootOptions.futureRef<Long>()
				stateManager.addListener { _, newValue ->
					sourceSlotColor.onReady {
						it.setAvailable(newValue)
					}
					targetSlotColor.onReady {
						it.setAvailable(newValue)
					}
					resetCounter.onReady {
						it.setAvailable(newValue)
					}
				}
				addListener { _, event ->
					if (event == OptionEventListener.Event.INITIAL) {
						sourceSlotColor.onReady {
							it.setAvailable(stateManager.get())
						}
						targetSlotColor.onReady {
							it.setAvailable(stateManager.get())
						}
						resetCounter.onReady {
							it.setAvailable(stateManager.get())
						}
					}
				}
				controller(tickBox())
			}
			val sourceSlotColor = rootOptions.register(sourceSlotColor) {
				name(Text.translatable("config.${ItemSwap.NAMESPACE}.sourceSlotColor"))
				controller(colorPicker(true))
			}
			val targetSlotColor = rootOptions.register(targetSlotColor) {
				name(Text.translatable("config.${ItemSwap.NAMESPACE}.targetSlotColor"))
				controller(colorPicker(true))
			}
			val resetCounter = rootOptions.register(waitTime) {
				name(Text.translatable("config.${ItemSwap.NAMESPACE}.waitTime"))
				descriptionBuilderDyn {
					text(
						Text.translatable("config.${ItemSwap.NAMESPACE}.waitTime.description[0]"),
						Text.translatable("config.${ItemSwap.NAMESPACE}.waitTime.description[1]", it)
					)
				}
				controller(slider(100L..3000L))
			}
		}
	}.generateScreen(parent)
}