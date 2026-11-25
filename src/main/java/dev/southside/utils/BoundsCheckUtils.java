package dev.southside.utils;

public class BoundsCheckUtils {
   public static boolean isValid(double d, double d2, int n, int n2, int n3, int n4) {
      return d > n && d < n3 && d2 > n2 && d2 < n4;
   }

   public static boolean isValid(double d, double d2, float f, float f2, float f3, float f4) {
      return d > f && d < f3 && d2 > f2 && d2 < f4;
   }
}
