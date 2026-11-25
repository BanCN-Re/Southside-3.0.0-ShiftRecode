package dev.southside.utils;

public final class MathClampUtils {
   public static float clamp01(float f) {
      return clamp(f, 0.0F, 1.0F);
   }

   public static float clamp(float x, float y, float z) {
      return x < y ? y : Math.min(x, z);
   }

   public static float mapRange(float f, float f2, float f3, float f4, float f5) {
      return f4 + (f5 - f4) * ((f - f2) / (f3 - f2));
   }

   public static double mapRangeStepped(double d, double d2, double d3, double d4, double d5, double d6) {
      double d7 = (d - d2) / (d3 - d2);
      double d8 = d7 * (d5 - d4);
      return Math.round((Math.round(d8 / d6) * d6 + d4) * 10000.0) / 10000.0;
   }
}
