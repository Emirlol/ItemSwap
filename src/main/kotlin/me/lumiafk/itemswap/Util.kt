package me.lumiafk.itemswap

import net.minecraft.text.Text

object Util {
    inline val String.text: Text get() = Text.of(this)
}