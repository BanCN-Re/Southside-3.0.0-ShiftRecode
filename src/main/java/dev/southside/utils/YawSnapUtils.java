package dev.southside.utils;

import dev.southside.utils.misc.AdjustedYawRecord;

public class YawSnapUtils {
   public static final int[] DEFAULT_VALUES;

   public static AdjustedYawRecord getAdjustedYawRecord(double yaw, double pitch) {
      double d = computeDouble(yaw);
      double d2 = computeDouble(pitch);
      double positive_infinity = Double.POSITIVE_INFINITY;
      int n = 0;
      double d3 = d2;

      for (int n2 : DEFAULT_VALUES) {
         double d4 = computeDouble(d2 + n2);
         double d5 = computeDouble(d4, d);
         if (d5 < positive_infinity) {
            positive_infinity = d5;
            n = n2;
            d3 = d4;
         } else if (Math.abs(d5 - positive_infinity) < 1.0E-9 && Math.abs(n2) < Math.abs(n)) {
            n = n2;
            d3 = d4;
         }
      }

      return new AdjustedYawRecord(d3, n, positive_infinity);
   }

   public static double computeDouble(double d) {
      return ((d + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
   }

   public static double computeDouble(double x, double y) {
      return Math.abs(computeDouble(x - y));
   }

   static {
      int[] values = new int[6];
      values[0] = 0;
      values[1] = -45;
      values[2] = 45;
      values[3] = -90;
      values[4] = 90;
      values[5] = -180;
      DEFAULT_VALUES = values;
   }

}
