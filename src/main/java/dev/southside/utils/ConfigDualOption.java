package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import com.google.gson.annotations.SerializedName;
import java.io.PrintStream;
import java.lang.reflect.Field;

public class ConfigDualOption extends BasicOption {
   @SerializedName("left")
   private final String left;
   @SerializedName("right")
   private final String right;
   @SerializedName("posAnimation")
   private UiAnimation posAnimation;

   public ConfigDualOption(Field field, Object obj, String text, String text2, String text3, String text4, int n, String text5, String text6) {
      super(field, obj, text, text2, text3, text4, n);
      this.left = text5;
      this.right = text6;
   }

   @Override
   public int getHeight() {
      return 32;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      boolean flag = false;

      try {
         flag = (Boolean)this.resolveValue();
         if (this.posAnimation == null) {
            this.posAnimation = new DummyAnimation(flag ? 356.0F : 228.0F);
         }
      } catch (IllegalAccessException e) {
      }

      if (!this.isValidEx()) {
         nvg.setAlpha(vg, 0.5F);
      }

      boolean flag2 = input.isHovered(x + 226, y, 128.0F, 32.0F) && this.isValidEx();
      boolean flag3 = input.isHovered(x + 354, y, 128.0F, 32.0F) && this.isValidEx();
      nvg.drawTextEx(vg, this.name, x, y + 16, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      nvg.drawRoundedRect(vg, x + 226, y, 256.0F, 32.0F, OneConfigColors.GRAY_600, 12.0F);
      nvg.drawRoundedRect(vg, x + this.posAnimation.getProgress(), y + 2, 124.0F, 28.0F, OneConfigColors.PRIMARY_500, 10.0F);
      if (!flag2 && this.isValidEx()) {
         nvg.setAlpha(vg, 0.8F);
      }

      nvg.drawTextEx(
         vg,
         this.left,
         x + 290 - nvg.getTextWidth(vg, this.left, 12.0F, OneConfigFonts.ONE_CONFIG_FONT) / 2.0F,
         y + 17,
         OneConfigColors.WHITE,
         12.0F,
         OneConfigFonts.ONE_CONFIG_FONT
      );
      if (this.isValidEx()) {
         nvg.setAlpha(vg, 1.0F);
      }

      if (!flag3 && this.isValidEx()) {
         nvg.setAlpha(vg, 0.8F);
      }

      nvg.drawTextEx(
         vg,
         this.right,
         x + 418 - nvg.getTextWidth(vg, this.right, 12.0F, OneConfigFonts.ONE_CONFIG_FONT) / 2.0F,
         y + 17,
         OneConfigColors.WHITE,
         12.0F,
         OneConfigFonts.ONE_CONFIG_FONT
      );
      nvg.setAlpha(vg, 1.0F);
      if ((flag2 && flag || flag3 && !flag) && input.isValid()) {
         flag = !flag;

         try {
            this.performAction(flag);
         } catch (IllegalAccessException e2) {
            PrintStream err = System.err;
            String text = String.valueOf(this);
            String text2 = String.valueOf(this.field);
            boolean flag4 = flag;
            String text3 = text2;
            String text4 = text;
            err.println("failed to write config value: class=" + text4 + " fieldWatching=" + text3 + " valueWrite=" + flag4);
            e2.printStackTrace();
         }
      }

      if (flag == this.posAnimation.isReverse()) {
         this.posAnimation = new LinearAnimation(300, 228.0F, 356.0F, !flag);
      }
   }
}
