package me.lumiafk.itemswap

import net.minecraft.text.MutableText
import net.minecraft.text.Text

object Util {
    inline val String.text: Text get() = Text.of(this)
    inline val String.literal : MutableText get() = Text.literal(this)
}