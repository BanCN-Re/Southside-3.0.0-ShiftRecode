package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.HudValue;

public class ItemHud extends Module {
   @SerializedName("itemHUD")
   public final HudValue itemHUD = new HudValue("Item HUD", new dev.southside.gui.hud.ItemHud(20.0F, 120.0F, 1.0F));

   public ItemHud() {
      super(Category.Render, "ItemHud", "物资统计HUD");
   }
}
