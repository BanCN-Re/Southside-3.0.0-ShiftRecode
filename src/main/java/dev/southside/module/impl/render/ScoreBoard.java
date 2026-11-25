package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.gui.hud.ScoreBoardHud;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.HudValue;
import dev.southside.utils.ModeValue;

public class ScoreBoard extends Module {
   @SerializedName("style")
   public final ModeValue style = new ModeValue("Style", "Modern", "Modern", "Vanilla", "Simple");
   @SerializedName("scoreBoard")
   public final HudValue scoreBoard = new HudValue("ScoreBoard", new ScoreBoardHud(20.0F, 20.0F, 1.0F));

   public ScoreBoard() {
      super(Category.Render, "ScoreBoard", "计分板");
   }
}
