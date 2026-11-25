package dev.southside.mixin.misc;

import net.minecraft.client.gui.EditBox.Substring;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Substring.class)
public interface MixinEditBox_Substring {
   @Accessor("beginIndex")
   int getBeginIndex();
}
