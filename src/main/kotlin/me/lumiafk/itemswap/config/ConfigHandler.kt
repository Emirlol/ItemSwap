package me.lumiafk.itemswap.config

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.dsl.*
import kotlinx.coroutines.cancel
import me.lumiafk.itemswap.ItemSwap
import me.lumiafk.itemswap.Util.text
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.awt.Color

object ConfigHandler {
	private val HANDLER = ConfigClassHandler.createBuilder(Config::class.java).serializer {
		GsonConfigSerializerBuilder.create(it)
			.setPath(FabricLoader.getInstance().configDir.resolve("${ItemSwap.NAMESPACE}/config.json5"))
			.setJson5(true)
			.build()
	}.build()

	fun load() = HANDLER.load()

	fun save() = HANDLER.save()

	val config: Config get() = HANDLER.instance()

	val defaults: Config get() = HANDLER.defaults()

	fun createGui(parent: Screen?): Screen = YetAnotherConfigLib("itemswap") {
		title("${ItemSwap.NAME} Config".text)
		save(this@ConfigHandler::save)
		val mainCategory by categories.registering {
			name(Text.translatable("category.${ItemSwap.NAMESPACE}.main"))

			val enabled by rootOptions.registering {
				name(Text.translatable("config.${ItemSwap.NAMESPACE}.enabled"))
				binding(config::enabled, defaults.enabled)
				instant(true)
				val sourceSlotColor by rootOptions.futureRef<Color>()
				val targetSlotColor by rootOptions.futureRef<Color>()
				val resetCounter by rootOptions.futureRef<Long>()
				listener { _, value ->
					sourceSlotColor.onReady {
						it.setAvailable(value)
					}
					targetSlotColor.onReady {
						it.setAvailable(value)
					}
					resetCounter.onReady {
						it.setAvailable(value)
					}
					if (value) {
						ItemSwap.initGlobalJob()
					} else {
						ItemSwap.globalJob?.cancel()
						ItemSwap.globalJob = null
					}
				}

				controller(tickBox())
			}
			val sourceSlotColor by rootOptions.registering {
				name(Text.translatable("config.${ItemSwap.NAMESPACE}.sourceSlotColor"))
				binding(config::sourceSlotColor, defaults.sourceSlotColor)
				controller(colorPicker(true))
			}
			val targetSlotColor by rootOptions.registering {
				name(Text.translatable("config.${ItemSwap.NAMESPACE}.targetSlotColor"))
				binding(config::targetSlotColor, defaults.targetSlotColor)
				controller(colorPicker(true))
			}
			val resetCounter by rootOptions.registering {
				name(Text.translatable("config.${ItemSwap.NAMESPACE}.waitTime"))
				descriptionBuilderDyn {
					text(
						Text.translatable("config.${ItemSwap.NAMESPACE}.waitTime.description[0]"),
						Text.translatable("config.${ItemSwap.NAMESPACE}.waitTime.description[1]", it)
					)
				}
				binding(config::waitTime, defaults.waitTime)
				controller(slider(100L..1500L))
			}
		}
	}.generateScreen(parent)
}