package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;

public class ConfigButtonOption extends BasicOption {
   @SerializedName("button")
   private final BasicButton button;

   public ConfigButtonOption(Runnable action, Object obj, String text, String text2, String text3, String text4, int n, String text5) {
      super(null, obj, text, text2, text3, text4, n);
      this.button = new BasicButton(n == 1 ? 128 : 256, 32, text5, 2, ColorPalette.INSTANCE2);
      this.button.setRunnable(action);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      this.button.setDisabled(!this.isValidEx());
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      if (!this.isValidEx()) {
         nvg.setAlpha(vg, 0.5F);
      }

      nvg.drawTextEx(vg, this.name, x, y + 17, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      this.button.draw(vg, x + (this.size == 1 ? 352 : 736), y, input);
      nvg.setAlpha(vg, 1.0F);
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
