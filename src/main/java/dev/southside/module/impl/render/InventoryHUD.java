package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.gui.hud.InventoryHud;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.HudValue;
import dev.southside.utils.ModeValue;

public class InventoryHUD extends Module {
   public static final ModeValue style = new ModeValue("Style", "Default", "Default", "Simple");
   @SerializedName("inventory")
   public final HudValue inventory = new HudValue("Inventory", new InventoryHud());

   public InventoryHUD() {
      super(Category.Render, "InventoryHUD", "Display inventory on screen");
   }
}
