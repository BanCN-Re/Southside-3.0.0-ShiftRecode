package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import java.awt.Color;
import java.io.PrintStream;
import java.lang.reflect.Field;

public class ConfigCheckboxOption extends BasicOption {
   @SerializedName("color")
   private final ColorAnimation color = new ColorAnimation(ColorPalette.INSTANCE3);
   @SerializedName("animation")
   private UiAnimation animation;

   public ConfigCheckboxOption(Field field, Object obj, String text, String text2, String text3, String text4, int n) {
      super(field, obj, text, text2, text3, text4, n);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      if (!this.isValidEx()) {
         nvg.setAlpha(vg, 0.5F);
      }

      boolean flag = false;

      try {
         flag = (Boolean)this.resolveValue();
         if (this.animation == null) {
            this.animation = new DummyAnimation(flag ? 1.0F : 0.0F);
         }
      } catch (IllegalAccessException e) {
      }

      boolean isHovered = input.isHovered(x, y + 4, 24.0F, 24.0F);
      boolean flag2 = input.isValid() && isHovered;
      if (flag2 && this.isValidEx()) {
         flag = !flag;

         try {
            this.performAction(flag);
         } catch (IllegalAccessException e2) {
            PrintStream err = System.err;
            String text = String.valueOf(this);
            String text2 = String.valueOf(this.field);
            boolean flag3 = flag;
            String text3 = text2;
            String text4 = text;
            err.println("failed to write config value: class=" + text4 + " fieldWatching=" + text3 + " valueWrite=" + flag3);
            e2.printStackTrace();
         }
      }

      if (flag == this.animation.isReverse()) {
         this.animation = new EaseInOutCubicAnimation(100, 0.0F, 1.0F, !flag);
      }

      float progress = this.animation.getProgress();
      nvg.drawTextEx(vg, this.name, x + 32, y + 17, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      nvg.drawRoundedRect(vg, x, y + 4, 24.0F, 24.0F, this.color.computeInt(isHovered, isHovered && OneConfigPlatform.getMousePlatform().isKeyDown(0)), 6.0F);
      nvg.drawRoundedRect(vg, x, y + 4, 23.5F, 23.5F, OneConfigColors.GRAY_300, 6.0F, 1.0F);
      nvg.drawRoundedRect(vg, x, y + 4, 24.0F, 24.0F, ArgbColorUtils.withAlpha(OneConfigColors.PRIMARY_400, (int)(progress * 255.0F)), 6.0F);
      nvg.draw(vg, OneConfigSvgs.CHECKBOX_TICK, x, y + 4, 24.0F, 24.0F, new Color(1.0F, 1.0F, 1.0F, progress).getRGB());
      if (flag && isHovered) {
         nvg.drawRoundedRect(vg, x - 1, y + 3, 24.0F, 24.0F, OneConfigColors.PRIMARY_500, 6.0F, 2.0F);
      }

      nvg.setAlpha(vg, 1.0F);
   }

   @Override
   protected float computeFloat(int n) {
      return n + 32;
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
