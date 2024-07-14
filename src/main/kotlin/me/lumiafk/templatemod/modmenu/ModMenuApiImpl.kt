package me.lumiafk.templatemod.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.lumiafk.templatemod.config.ConfigHandler

class ModMenuApiImpl: ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(ConfigHandler::createGui)
}