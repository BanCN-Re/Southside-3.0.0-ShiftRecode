package dev.southside.mixin.misc;

import net.minecraft.client.gui.screen.world.WorldListWidget.WorldEntry;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldEntry.class)
public interface MixinWorldListWidget_WorldEntry {
   @Accessor("level")
   LevelSummary summary();
}
