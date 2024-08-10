package me.lumiafk.itemswap.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
	@Accessor
	@Nullable
	Slot getFocusedSlot();

	@Accessor("x")
	int getX();

	@Accessor("y")
	int getY();
}
