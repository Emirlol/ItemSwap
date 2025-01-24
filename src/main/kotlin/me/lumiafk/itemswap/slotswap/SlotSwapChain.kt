package me.lumiafk.itemswap.slotswap

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lumiafk.itemswap.CodecUtil

/**
 * Represents a chain of slot swaps.
 *
 * @param list The list of slot swaps.
 * @param currentState This is the index of the slot swap that will be performed on the next slot swap click.
 */
data class SlotSwapChain(val list: ObjectArrayList<SlotSwap>, var currentState: Int = 0): Iterable<SlotSwap> {
	fun isSlotInChain(slot: Int): Boolean = list.any { it.from == slot || it.to == slot }

	fun isSlotMappedFrom(slot: Int): Boolean = list.any { it.from == slot }

	fun isSlotMappedTo(slot: Int): Boolean = list.any { it.to == slot }

	/**
	 * Whether this slot is mapped from and to. This means no more slot swaps can be configured on this slot.
	 */
	fun isSlotFull(slot: Int): Boolean = isSlotMappedFrom(slot) && isSlotMappedTo(slot)

	fun nextSlotSwap(): SlotSwap {
		if (list.size == 1) return list.first()
		val cycleLength = 2 * list.lastIndex
		val state = currentState % cycleLength
		val index = if (state < list.size) state else cycleLength - state
		val item = list[index]
		if (currentState++ >= cycleLength) currentState = 0
		return item
	}

	operator fun plusAssign(slotSwap: SlotSwap) {
		list += slotSwap
	}

	override fun iterator(): Iterator<SlotSwap> = list.iterator()

	companion object {
		val CODEC: Codec<SlotSwapChain> = RecordCodecBuilder.create { instance ->
			instance.group(
				CodecUtil.objectArrayList(SlotSwap.CODEC).fieldOf("slotSwaps").forGetter(SlotSwapChain::list),
				Codec.INT.optionalFieldOf("currentState", 0).forGetter(SlotSwapChain::currentState)
			).apply(instance, ::SlotSwapChain)
		}
	}
}
