package me.lumiafk.templatemod

import net.minecraft.text.Text

object Util {
    val String.text get() = Text.of(this)
    val String.literal get() = Text.literal(this)
}