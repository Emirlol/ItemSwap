package me.lumiafk.itemswap.config

import dev.isxander.yacl3.api.controller.StringControllerBuilder
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.dsl.YetAnotherConfigLib
import dev.isxander.yacl3.dsl.binding
import dev.isxander.yacl3.dsl.controller
import dev.isxander.yacl3.gui.controllers.string.StringController
import me.lumiafk.itemswap.ItemSwap
import me.lumiafk.itemswap.Util.text
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object ConfigHandler {
	private val HANDLER = ConfigClassHandler.createBuilder(Config::class.java).serializer {
		GsonConfigSerializerBuilder.create(it)
			.setPath(FabricLoader.getInstance().configDir.resolve("${ItemSwap.NAMESPACE}/config.json5"))
			.setJson5(true)
			.build()
	}.build()

	fun load() = HANDLER.load()

	fun save() = HANDLER.save()

	val config get() = HANDLER.instance()

	val defaults get() = HANDLER.defaults()

	fun createGui(parent: Screen?): Screen = YetAnotherConfigLib("itemswap") {
		title("${ItemSwap.NAME} Config".text)
		save(this@ConfigHandler::save)
		val mainCategory by categories.registering {
			title(Text.translatable("category.${ItemSwap.NAMESPACE}.main"))
			val keyOption1 by rootOptions.registering {
				name(Text.translatable("key.${ItemSwap.NAMESPACE}.1"))
				binding(config::key1, "")
				controller(StringControllerBuilder::create)
			}
			val keyOption2 by rootOptions.registering {
				name(Text.translatable("key.${ItemSwap.NAMESPACE}.2"))
				binding(config::key2, "")
				controller(StringControllerBuilder::create)
			}
			val keyOption3 by rootOptions.registering {
				name(Text.translatable("key.${ItemSwap.NAMESPACE}.3"))
				binding(config::key3, "")
				controller(StringControllerBuilder::create)
			}
			val keyOption4 by rootOptions.registering {
				name(Text.translatable("key.${ItemSwap.NAMESPACE}.4"))
				binding(config::key4, "")
				controller(StringControllerBuilder::create)
			}
			val keyOption5 by rootOptions.registering {
				name(Text.translatable("key.${ItemSwap.NAMESPACE}.5"))
				binding(config::key5, "")
				controller(StringControllerBuilder::create)
			}
			val keyOption6 by rootOptions.registering {
				name(Text.translatable("key.${ItemSwap.NAMESPACE}.6"))
				binding(config::key6, "")
				controller(StringControllerBuilder::create)
			}
			val keyOption7 by rootOptions.registering {
				name(Text.translatable("key.${ItemSwap.NAMESPACE}.7"))
				binding(config::key7, "")
				controller(StringControllerBuilder::create)
			}
			val keyOption8 by rootOptions.registering {
				name(Text.translatable("key.${ItemSwap.NAMESPACE}.8"))
				binding(config::key8, "")
				controller(StringControllerBuilder::create)
			}
			val keyOption9 by rootOptions.registering {
				name(Text.translatable("key.${ItemSwap.NAMESPACE}.9"))
				binding(config::key9, "")
				controller(StringControllerBuilder::create)
			}
		}
	}.generateScreen(parent)
}