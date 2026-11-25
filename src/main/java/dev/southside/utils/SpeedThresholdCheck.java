package dev.southside.utils;

public class SpeedThresholdCheck {
   private static final double DEFAULT_DOUBLE = 1.0E-5;
   private static final double DEFAULT_DOUBLE_VALUE = 1.0;

   public static boolean isValid(double x, double y) {
      return x < 1.0E-5 && y > 1.0;
   }
}
