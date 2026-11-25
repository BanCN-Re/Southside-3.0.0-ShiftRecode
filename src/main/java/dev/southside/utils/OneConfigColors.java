package dev.southside.utils;

import java.awt.Color;

public class OneConfigColors {
   public static final int TRANSPARENT;
   public static final int BLACK;
   public static final int GRAY_900;
   public static final int GRAY_900_80;
   public static final int GRAY_850;
   public static final int GRAY_850_95;
   public static final int GRAY_800;
   public static final int GRAY_700;
   public static final int GRAY_600;
   public static final int GRAY_500;
   public static final int GRAY_500_80;
   public static final int GRAY_400;
   public static final int GRAY_400_40;
   public static final int GRAY_400_60;
   public static final int GRAY_300;
   public static final int GRAY_400_80;
   public static final int OFF_WHITE;
   public static final int PRIMARY_700;
   public static final int PRIMARY_600;
   public static final int PRIMARY_600_80;
   public static final int PRIMARY_500;
   public static final int PRIMARY_400;
   public static final int PRIMARY_300;
   public static final int WHITE_50;
   public static final int WHITE_60;
   public static final int WHITE_80;
   public static final int WHITE_90;
   public static final int WHITE_95;
   public static final int WHITE;
   public static final int SUCCESS_600;
   public static final int SUCCESS_700;
   public static final int WARNING_500;
   public static final int WARNING_600;
   public static final int ERROR_600_80;
   public static final int ERROR_600;
   public static final int ERROR_700;
   public static final int ERROR_800;
   public static final int ERROR_800_80;
   public static final int ERROR_300;
   public static final int ERROR_300_80;
   public static final int DARK_BG_0;
   public static final int DARK_BG_40;
   public static final int DARK_BG_80;
   private static int[] values;

   static {
      OFF_WHITE = -1118482;
      TRANSPARENT = new Color(0, 0, 0, 0).getRGB();
      BLACK = new Color(0, 0, 0, 255).getRGB();
      GRAY_900 = new Color(13, 14, 15, 255).getRGB();
      GRAY_900_80 = new Color(13, 14, 15, 204).getRGB();
      GRAY_850 = new Color(21, 22, 23, 255).getRGB();
      GRAY_850_95 = new Color(21, 22, 23, 242).getRGB();
      GRAY_800 = new Color(21, 23, 25, 255).getRGB();
      GRAY_700 = new Color(34, 35, 38, 255).getRGB();
      GRAY_600 = new Color(42, 44, 48, 255).getRGB();
      GRAY_500 = new Color(49, 51, 56, 255).getRGB();
      GRAY_500_80 = new Color(49, 51, 56, 204).getRGB();
      GRAY_400 = new Color(55, 59, 69, 255).getRGB();
      GRAY_400_40 = new Color(55, 59, 69, 102).getRGB();
      GRAY_400_60 = new Color(55, 59, 69, 153).getRGB();
      GRAY_300 = new Color(73, 79, 92, 255).getRGB();
      GRAY_400_80 = new Color(55, 59, 69, 204).getRGB();
      PRIMARY_700 = new Color(13, 51, 128, 255).getRGB();
      PRIMARY_600 = new Color(18, 71, 178, 255).getRGB();
      PRIMARY_600_80 = new Color(18, 71, 178, 204).getRGB();
      PRIMARY_500 = new Color(20, 82, 204, 255).getRGB();
      PRIMARY_400 = new Color(25, 103, 255, 255).getRGB();
      PRIMARY_300 = new Color(48, 129, 242, 255).getRGB();
      WHITE_50 = new Color(255, 255, 255, 127).getRGB();
      WHITE_60 = new Color(255, 255, 255, 153).getRGB();
      WHITE_80 = new Color(255, 255, 255, 204).getRGB();
      WHITE_90 = new Color(255, 255, 255, 229).getRGB();
      WHITE_95 = new Color(255, 255, 255, 242).getRGB();
      WHITE = new Color(255, 255, 255, 255).getRGB();
      SUCCESS_600 = new Color(3, 152, 85).getRGB();
      SUCCESS_700 = new Color(2, 121, 72).getRGB();
      WARNING_500 = new Color(247, 144, 9).getRGB();
      WARNING_600 = new Color(220, 104, 3).getRGB();
      ERROR_600_80 = new Color(217, 32, 32, 204).getRGB();
      ERROR_600 = new Color(217, 32, 32).getRGB();
      ERROR_700 = new Color(180, 24, 24).getRGB();
      ERROR_800 = new Color(145, 24, 24).getRGB();
      ERROR_800_80 = new Color(145, 24, 24, 204).getRGB();
      ERROR_300 = new Color(253, 155, 155).getRGB();
      ERROR_300_80 = new Color(253, 155, 155, 204).getRGB();
      DARK_BG_0 = new Color(0, 0, 0, 255).getRGB();
      DARK_BG_40 = new Color(40, 40, 40, 255).getRGB();
      DARK_BG_80 = new Color(80, 80, 80, 255).getRGB();
   }

   public static void setValues(int[] values2) {
      values = values2;
   }

   public static class Inner {
      public static final int DARK_RED;
      public static final int RED;
      public static final int GOLD;
      public static final int YELLOW;
      public static final int DARK_GREEN;
      public static final int GREEN;
      public static final int AQUA;
      public static final int DARK_AQUA;
      public static final int DARK_BLUE;
      public static final int BLUE;
      public static final int LIGHT_PURPLE;
      public static final int DARK_PURPLE;
      public static final int WHITE;
      public static final int GRAY;
      public static final int DARK_GRAY;
      public static final int BLACK;

      static {
         DARK_RED = new Color(170, 0, 0).getRGB();
         RED = new Color(255, 85, 85).getRGB();
         GOLD = new Color(255, 170, 0).getRGB();
         YELLOW = new Color(255, 255, 85).getRGB();
         DARK_GREEN = new Color(0, 170, 0).getRGB();
         GREEN = new Color(85, 255, 85).getRGB();
         AQUA = new Color(85, 255, 255).getRGB();
         DARK_AQUA = new Color(0, 170, 170).getRGB();
         DARK_BLUE = new Color(0, 0, 170).getRGB();
         BLUE = new Color(85, 85, 255).getRGB();
         LIGHT_PURPLE = new Color(255, 85, 255).getRGB();
         DARK_PURPLE = new Color(170, 0, 170).getRGB();
         WHITE = new Color(255, 255, 255).getRGB();
         GRAY = new Color(170, 170, 170).getRGB();
         DARK_GRAY = new Color(85, 85, 85).getRGB();
         BLACK = new Color(0, 0, 0).getRGB();
      }
   }
}
