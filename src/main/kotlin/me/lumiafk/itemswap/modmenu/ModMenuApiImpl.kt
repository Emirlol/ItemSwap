package me.lumiafk.itemswap.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.lumiafk.itemswap.config.ConfigHandler

class ModMenuApiImpl: ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(ConfigHandler::createGui)
}