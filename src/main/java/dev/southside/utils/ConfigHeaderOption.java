package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import java.lang.reflect.Field;

public class ConfigHeaderOption extends BasicOption {
   public ConfigHeaderOption(Field field, Object obj, String text, String text2, String text3, int n) {
      super(field, obj, text, "", text2, text3, n);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      ScissorHelper scissor = ScissorHelper.INSTANCE;
      ScissorRect clip = scissor.scissor(vg, x, y, this.size == 1 ? 480.0F : 992.0F, 32.0F);
      NanoVGHelper.INSTANCE.drawTextEx(vg, this.name, x, y + 17, super.nameColor, 24.0F, OneConfigFonts.ONE_CONFIG_FONT);
      scissor.resetScissor(vg, clip);
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
