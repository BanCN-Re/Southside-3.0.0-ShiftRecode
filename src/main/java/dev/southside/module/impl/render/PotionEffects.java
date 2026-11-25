package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.PotionEffectsHud;
import dev.southside.utils.HudValue;
import dev.southside.utils.ModeValue;

public class PotionEffects extends Module {
   public static final ModeValue style = new ModeValue("Style", "Default", "Default", "Simple");
   @SerializedName("potionHUD")
   public final HudValue potionHUD = new HudValue("Potion HUD", new PotionEffectsHud(10.0F, 80.0F, 1.0F));

   public PotionEffects() {
      super(Category.Render, "PotionEffects", "药水显示");
   }
}
