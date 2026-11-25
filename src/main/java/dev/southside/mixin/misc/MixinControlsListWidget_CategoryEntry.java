package dev.southside.mixin.misc;

import net.minecraft.client.gui.screen.option.ControlsListWidget.CategoryEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CategoryEntry.class)
public interface MixinControlsListWidget_CategoryEntry {
   @Accessor("text")
   Text category();
}
