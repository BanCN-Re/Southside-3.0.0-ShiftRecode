package dev.southside.mixin.misc;

import net.minecraft.client.gui.screen.pack.PackListWidget.ResourcePackEntry;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResourcePackEntry.class)
public interface MixinPackListWidget_ResourcePackEntry {
   @Accessor("pack")
   Pack pack();
}
