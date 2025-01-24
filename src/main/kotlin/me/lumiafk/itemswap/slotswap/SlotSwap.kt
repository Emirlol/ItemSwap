package me.lumiafk.itemswap.slotswap

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.lumiafk.itemswap.Util.client
import me.lumiafk.itemswap.Util.isHotbarSlot
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.screen.slot.SlotActionType

/**
 * Represents a one-directional swap of two slots.
 */
data class SlotSwap(val from: Int, val to: Int) {
	/**
	 * Attempts to swap the two slots.
	 *
	 * @return Whether the swap was successful.
	 */
	fun swap(screen: InventoryScreen): Boolean = when {
		isHotbarSlot(from) -> {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, to, from - 36, SlotActionType.SWAP, client.player)
			true
		}

		isHotbarSlot(to) -> {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, from, to - 36, SlotActionType.SWAP, client.player)
			true
		}

		else -> false
	}

	companion object {
		val CODEC: Codec<SlotSwap> = RecordCodecBuilder.create { instance ->
			instance.group(
				Codec.INT.fieldOf("from").forGetter(SlotSwap::from),
				Codec.INT.fieldOf("to").forGetter(SlotSwap::to)
			).apply(instance, ::SlotSwap)
		}
	}
}
