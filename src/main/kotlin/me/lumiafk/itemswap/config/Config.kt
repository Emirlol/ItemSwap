package me.lumiafk.itemswap.config

import dev.isxander.yacl3.config.v2.api.SerialEntry
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.awt.Color

class Config {
	@SerialEntry
	var enabled = true

	@SerialEntry
	var slotSquareMap = ObjectArrayList<IntIntImmutablePair>(9) // Hotbar slot count

	@SerialEntry
	var sourceSlotColor = Color(0xE86DFF)

	@SerialEntry
	var targetSlotColor = Color(0xFFBB00)

	@SerialEntry
	var waitTime = 500L
}