package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import java.io.PrintStream;
import java.lang.reflect.Field;

public class ConfigSwitchOption extends BasicOption {
   @SerializedName("color")
   private ColorAnimation color;
   @SerializedName("animation")
   private UiAnimation animation;

   public ConfigSwitchOption(Field field, Object obj, String text, String text2, String text3, String text4, int n) {
      super(field, obj, text, text2, text3, text4, n);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      boolean flag = false;

      try {
         flag = (Boolean)this.resolveValue();
         if (this.animation == null) {
            this.animation = new DummyAnimation(flag ? 1.0F : 0.0F);
            this.color = new ColorAnimation(flag ? ColorPalette.INSTANCE2 : ColorPalette.INSTANCE3);
         }
      } catch (IllegalAccessException e) {
      }

      float progress = this.animation.getProgress();
      int n = x + 3 + (int)(progress * 18.0F);
      boolean isHovered = input.isHovered(x, y, 42.0F, 32.0F);
      if (!this.isValidEx()) {
         nvg.setAlpha(vg, 0.5F);
      }

      nvg.drawRoundedRect(vg, x, y + 4, 42.0F, 24.0F, this.color.computeInt(isHovered, isHovered && OneConfigPlatform.getMousePlatform().isKeyDown(0)), 12.0F);
      nvg.drawRoundedRect(vg, n, y + 7, 18.0F, 18.0F, OneConfigColors.WHITE, 9.0F);
      nvg.drawTextEx(vg, this.name, x + 50, y + 17, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      if (input.isHoveredEx(x, y, 42.0F, 32.0F) && this.isValidEx()) {
         flag = !flag;

         try {
            this.performAction(flag);
         } catch (IllegalAccessException e2) {
            PrintStream err = System.err;
            String text = String.valueOf(this);
            String text2 = String.valueOf(this.field);
            boolean flag2 = flag;
            String text3 = text2;
            String text4 = text;
            err.println("failed to write config value: class=" + text4 + " fieldWatching=" + text3 + " valueWrite=" + flag2);
            e2.printStackTrace();
         }
      }

      if (flag == this.animation.isReverse()) {
         if ((Boolean)Preferences.INSTANCE.toggleSwitchBounce.getValue()) {
            this.animation = new EaseOutBackAnimation(200, 0.0F, 1.0F, !flag);
         } else {
            this.animation = new EaseInOutCubicAnimation(150, 0.0F, 1.0F, !flag);
         }

         this.color.performAction(flag ? ColorPalette.INSTANCE2 : ColorPalette.INSTANCE3);
      }

      nvg.setAlpha(vg, 1.0F);
   }

   @Override
   protected float computeFloat(int n) {
      return n + 50;
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
