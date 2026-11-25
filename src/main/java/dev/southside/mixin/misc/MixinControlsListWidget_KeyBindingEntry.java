package dev.southside.mixin.misc;

import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBindingEntry.class)
public interface MixinControlsListWidget_KeyBindingEntry {
   @Accessor("bindingName")
   Text bindingName();
}
