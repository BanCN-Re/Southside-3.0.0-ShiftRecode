package dev.southside.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screen.ingame.BookEditScreen$Position")
public interface MixinBookEditScreen_Position {
   @Accessor("x")
   int getCursorX();

   @Accessor("y")
   int getCursorY();
}
