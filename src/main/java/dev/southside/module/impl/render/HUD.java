package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.HudValue;
import dev.southside.utils.StickerHud;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.StringValue;

public class HUD extends Module {
   @SerializedName("hotbar")
   public final BooleanValue hotbar = new BooleanValue("Hotbar", true);
   @SerializedName("hotbarAnimation")
   public final BooleanValue hotbarAnimation = new BooleanValue("Hotbar Animation", true);
   @SerializedName("info")
   public final BooleanValue info = new BooleanValue("Info", true);
   @SerializedName("userName")
   public final StringValue userName = new StringValue("User Name", "请输入文本", false, false, "", 1, "Player");
   @SerializedName("serverReplaceTo")
   public final StringValue serverReplaceTo = new StringValue("Server Replace to", "布吉岛", false, false, "打几把", 1, "南极岛");
   @SerializedName("sticker")
   public final HudValue sticker = new HudValue("Sticker", new StickerHud());

   public HUD() {
      super(Category.Render, "HUD", "请输入文本。", true);
   }

   @Override
   public void postInit() {
      super.postInit();
      this.addDependency(this.userName.getLabel(), this.info.getLabel());
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if ((Boolean)this.info.getValue()) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.performAction(true, l -> performAction(nvg, (String)this.userName.getValue(), l));
      }
   }

   public static void performAction(NanoVGHelper nvg, String text, long l) {
      float f = mc2.getWindow().getScaledWidth() - nvg.getTextWidth(l, text, 10.0F, OneConfigFonts.WQY_MICROHEI);
      float f2 = mc2.getWindow().getScaledHeight() - nvg.getTextHeight(l, 10.0F, OneConfigFonts.WQY_MICROHEI) / 2.0F;
      nvg.draw(l, text, f, f2, -1, 10.0F, OneConfigFonts.WQY_MICROHEI);
   }
}
