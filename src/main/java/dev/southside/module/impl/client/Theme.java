package dev.southside.module.impl.client;

import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import java.awt.Color;

public class Theme extends Module {
   public static final ModeValue colorMode;
   public static final NumberValue colorSpeed;
   private static long HUE_CYCLE_DEGREES;

   public Theme() {
      super(Category.Client, "Theme", "主题");
   }

   public static Color blendColor(int n) {
      return switch (colorMode.getDisplayTextEx()) {
         case "Sky" -> {
            int n2 = ((Number)colorSpeed.getValue()).intValue();
            int n3 = (int)((System.currentTimeMillis() / n2 + n) % HUE_CYCLE_DEGREES);
            float f = (float)(n3 / 360.0);
            yield Color.getHSBColor(f < 0.5F ? -f : f, 0.5F, 1.0F);
         }
         default -> Color.WHITE;
      };
   }

   @Override
   public void onEnable() {
      this.setEnabled(false);
   }

   // $VF: Irreducible bytecode was duplicated to produce valid code
   static {
      HUE_CYCLE_DEGREES = 360L;
      colorMode = new ModeValue("Color", "Sky", "Sky");
      colorSpeed = new NumberValue("Color Speed", 18, 1, 54, 1);
   }
}
