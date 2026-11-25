package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;

public class ConfigInfoOption extends BasicOption {
   @SerializedName("type")
   private final InfoType type;

   public ConfigInfoOption(Field field, Object obj, String text, String text2, String text3, int n, InfoType infoType) {
      super(field, obj, text, "", text2, text3, n);
      this.type = infoType;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      ScissorHelper scissor = ScissorHelper.INSTANCE;
      ScissorRect clip = scissor.scissor(vg, x, y, this.size == 1 ? 448.0F : 960.0F, 32.0F);
      nvg.draw(vg, this.type, x, y + 4, 24.0F);
      nvg.drawTextEx(vg, this.name, x + 32, y + 18, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      scissor.resetScissor(vg, clip);
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
