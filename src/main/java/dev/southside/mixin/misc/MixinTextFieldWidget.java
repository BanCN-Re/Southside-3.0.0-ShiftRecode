package dev.southside.mixin.misc;

import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextFieldWidget.class)
public interface MixinTextFieldWidget {
   @Accessor("selectionEnd")
   int selectionEnd();

   @Accessor("editable")
   boolean editable();
}
