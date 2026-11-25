package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.PaperDollHud;
import dev.southside.utils.HudValue;

public final class PaperDoll extends Module {
   @SerializedName("paperDoll")
   public final HudValue paperDoll = new HudValue("Paper Doll", new PaperDollHud());

   public PaperDoll() {
      super(Category.Render, "PaperDoll", "在左上角显示基岩版风格的玩家模型。");
   }
}
