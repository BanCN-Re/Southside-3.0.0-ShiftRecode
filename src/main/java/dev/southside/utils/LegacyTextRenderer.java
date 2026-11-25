package dev.southside.utils;

@Deprecated
public class LegacyTextRenderer {
   public static int computeInt(String text, float f, float f2, int n, int n2) {
      return OneConfigTextRenderer.computeInt(text, f, f2, n, n2);
   }

   public static void applyText(String text, float f, float f2, int n, LegacyTextRenderer.Mode mode, float f3) {
      OneConfigTextRenderer.applyText(text, f, f2, n, OneConfigTextRenderer.Mode.fromOrdinal(mode.ordinal()), f3);
   }

   public enum Mode {
      NONE,
      SHADOW,
      FULL;


      public static LegacyTextRenderer.Mode fromOrdinal(int n) {
         return values()[n];
      }

      // $VF: synthetic method
      private static LegacyTextRenderer.Mode[] createValues() {
         return new LegacyTextRenderer.Mode[]{NONE, SHADOW, FULL};
      }


   }
}
