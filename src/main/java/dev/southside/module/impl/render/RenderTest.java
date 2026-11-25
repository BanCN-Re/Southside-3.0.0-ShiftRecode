package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.HudValue;
import dev.southside.utils.RenderTestHud;

public class RenderTest extends Module {
   @SerializedName("potionHUD")
   public final HudValue potionHUD = new HudValue("Potion HUD", new RenderTestHud(10.0F, 80.0F, 1.0F));

   public RenderTest() {
      super(Category.Render, "RenderTest", "绘制测试。");
   }
}
