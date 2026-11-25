package dev.southside.utils;

import cc.polyfrost.oneconfig.config.Config;
import java.util.regex.Pattern;

public class OneConfigTextRenderer {
   private static final Pattern pattern = Pattern.compile("(?i)§[0-9a-f]");
   private static boolean active = false;

   public static int computeInt(String text, float f, float f2, int n, int n2) {
      String text2 = pattern.matcher(text).replaceAll("§r");
      active = true;
      int n3 = 0;
      if (n2 / 4 > 3) {
         for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
               if (i * i != j * j) {
                  n3 = (int)(n3 + OneConfigPlatform.getGLPlatform().computeFloat(text2, i / 2.0F + f, j / 2.0F + f2, n2 / 4 << 24, false));
               }
            }
         }
      }

      n3 += (int)OneConfigPlatform.getGLPlatform().computeFloat(text, f, f2, n, false);
      active = false;
      return n3;
   }

   public static float computeFloat(String text) {
      return OneConfigPlatform.getGLPlatform().getWidth(text);
   }

   public static void applyText(String text, float f, float f2, int n, OneConfigTextRenderer.Mode mode, float f3) {
      ModelViewStack.Inner.pushMatrix();
      ModelViewStack.Inner.scale(f3, f3, 1.0F);
      switch (mode) {
         case NONE:
            OneConfigPlatform.getGLPlatform().computeFloat(text, f * (1.0F / f3), f2 * (1.0F / f3), n, false);
            break;
         case SHADOW:
            OneConfigPlatform.getGLPlatform().computeFloat(text, f * (1.0F / f3), f2 * (1.0F / f3), n, true);
            break;
         case FULL:
            computeInt(text, f * (1.0F / f3), f2 * (1.0F / f3), n, 255);
      }

      ModelViewStack.Inner.popMatrix();
   }

   public static boolean isActive() {
      return active;
   }

   public enum Mode {
      NONE,
      SHADOW,
      FULL;

      private static Config[] configCache;

      public static OneConfigTextRenderer.Mode fromOrdinal(int n) {
         return values()[n];
      }

      // $VF: synthetic method
      private static OneConfigTextRenderer.Mode[] createValues() {
         return new OneConfigTextRenderer.Mode[]{NONE, SHADOW, FULL};
      }


      public static void setConfigCache(Config[] configArray) {
         configCache = configArray;
      }

   }
}
