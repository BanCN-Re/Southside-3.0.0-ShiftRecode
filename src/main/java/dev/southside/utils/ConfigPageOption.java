package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;

public class ConfigPageOption extends BasicOption {
   @SerializedName("page")
   public final GuiPage page;
   @SerializedName("description")
   public final String description;
   @SerializedName("backgroundColor")
   private final ColorAnimation backgroundColor = new ColorAnimation(ColorPalette.INSTANCE3);

   public ConfigPageOption(Field field, Object obj, String text, String text2, String text3, String text4, OptionPage optionPage) {
      super(field, obj, text, "", text3, text4, 2);
      this.description = text2;
      this.page = new ModConfigPage(optionPage);
   }

   public ConfigPageOption(Field field, Object obj, String text, String text2, String text3, String text4, GuiPage guiPage) {
      super(field, obj, text, "", text3, text4, 2);
      this.description = text2;
      this.page = guiPage;
   }

   @Override
   public int getHeight() {
      return this.description.isEmpty() ? 64 : 96;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      int n = this.description.isEmpty() ? 64 : 96;
      boolean flag = input.isHovered(x - 16, y, 1024.0F, n) && this.isValidEx();
      boolean flag2 = flag && input.isValid();
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      if (!this.isValidEx()) {
         nvg.setAlpha(vg, 0.5F);
      }

      boolean flag3 = flag && OneConfigPlatform.getMousePlatform().isKeyDown(0);
      nvg.drawRoundedRect(vg, x - 16, y, 1024.0F, n, this.backgroundColor.computeInt(flag, flag3), 20.0F);
      nvg.drawTextEx(vg, this.name, x + 10, y + 32, OneConfigColors.WHITE_90, 24.0F, OneConfigFonts.ONE_CONFIG_FONT);
      if (!this.description.isEmpty()) {
         nvg.drawTextEx(vg, this.description, x + 10, y + 70, OneConfigColors.WHITE_90, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      }

      nvg.draw(vg, OneConfigSvgs.CARET_RIGHT_BOLD, x + 981.0F, y + (this.description.isEmpty() ? 20.0F : 36.0F), 13.0F, 22.0F);
      if (flag2) {
         OneConfigGui.INSTANCE.performAction(this.page);
      }

      nvg.setAlpha(vg, 1.0F);
   }
}
