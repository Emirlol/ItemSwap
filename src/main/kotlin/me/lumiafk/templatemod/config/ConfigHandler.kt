package me.lumiafk.templatemod.config

import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import me.lumiafk.templatemod.TemplateMod
import me.lumiafk.templatemod.Util.text
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen

object ConfigHandler {
    private val HANDLER = ConfigClassHandler.createBuilder(Config::class.java).serializer {
        GsonConfigSerializerBuilder.create(it)
            .setPath(FabricLoader.getInstance().configDir.resolve("TemplateMod/config.json5"))
            .setJson5(true)
            .build()
    }.build()

    fun load() = HANDLER.load()

    fun save() = HANDLER.save()

    fun getConfig() = HANDLER.instance()

    fun getDefaultConfig() = HANDLER.defaults()

    fun createGui(parent: Screen?): Screen = YetAnotherConfigLib.createBuilder()
        .title("${TemplateMod.NAME} Config".text)
        .handleCategories()
        .save(this::save)
        .build()
        .generateScreen(parent)

    private fun YetAnotherConfigLib.Builder.handleCategories(): YetAnotherConfigLib.Builder = this.categories(
        listOf(
            ConfigCategory.createBuilder()
                .name("General".text)
                .build()
        )
    )
}