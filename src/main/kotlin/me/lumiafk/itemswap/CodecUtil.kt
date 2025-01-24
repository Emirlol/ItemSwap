package me.lumiafk.itemswap

import com.mojang.serialization.Codec
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.awt.Color
import java.util.function.Function

object CodecUtil {
	val COLOR: Codec<Color> = Codec.INT.xmap(
		{ int -> Color(int, true) },
		{ color -> color.rgb }
	)

	fun <T> objectArrayList(keyCodec: Codec<T>): Codec<ObjectArrayList<T>> = Codec.list(keyCodec).xmap(
		::ObjectArrayList,
		Function.identity()
	)
}