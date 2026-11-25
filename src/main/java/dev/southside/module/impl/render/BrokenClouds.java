package dev.southside.module.impl.render;

import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;

public final class BrokenClouds extends Module {
   public BrokenClouds() {
      super(Category.Render, "BrokenClouds", "崩坏的云：以流畅模式重现 Beta 1.7.3 的云层拉伸。");
   }

   public static boolean isValidEx() {
      Southside southside = Southside.INSTANCE;
      return southside != null && southside.getModuleManager() != null && southside.getModuleManager().getModule(BrokenClouds.class).isEnabled();
   }
}
