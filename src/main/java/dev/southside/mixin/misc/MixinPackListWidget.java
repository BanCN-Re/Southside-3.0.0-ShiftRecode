package dev.southside.mixin.misc;

import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PackListWidget.class)
public interface MixinPackListWidget {
   @Accessor("title")
   Text title();
}
