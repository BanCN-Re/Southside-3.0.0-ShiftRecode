package dev.southside.mixin.misc;

import net.minecraft.client.Mouse;
import net.minecraft.client.input.Scroller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mouse.class)
public interface MixinMouse {
   @Accessor("cursorDeltaY")
   double getCursorDeltaY();

   @Accessor("cursorDeltaX")
   double getCursorDeltaX();

   @Accessor("scroller")
   Scroller getScroller();
}
