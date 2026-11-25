package dev.southside.mixin.misc;

import java.util.List;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldListWidget.class)
public interface MixinWorldListWidget {
   @Invoker("tryGet")
   List<LevelSummary> loadedWorlds();

   @Accessor("levels")
   List<LevelSummary> visibleWorlds();

   @Invoker("show")
   void showWorlds(List<LevelSummary> list);
}
