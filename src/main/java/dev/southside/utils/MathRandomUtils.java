package dev.southside.utils;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public class MathRandomUtils {
   public static final SecureRandom secureRandom = new SecureRandom();

   public static double computeDouble(double x, double y) {
      return new SecureRandom().nextDouble() * (y - x) + x;
   }

   public static double computeDoubleEx(double x, double y) {
      return ThreadLocalRandom.current().nextDouble() * (y - x) + x;
   }

   public static float evaluate(float f) {
      float f2 = f % 360.0F;
      if (f2 >= 180.0F) {
         f2 -= 360.0F;
      }

      if (f2 < -180.0F) {
         f2 += 360.0F;
      }

      return f2;
   }

   public static float computeFloat(float x, float y, float z) {
      float f = x % 360.0F;
      if (f >= 180.0F) {
         f -= 360.0F;
      }

      if (f < -180.0F) {
         f += 360.0F;
      }

      return Math.max(y, Math.min(z, f));
   }

   public static boolean isValid(double x, float y) {
      double d = y;
      return Math.abs(x - d) < 1.0E-4;
   }

   public static double computeDouble(double d) {
      double d2 = d % 360.0;
      if (d2 >= 180.0) {
         d2 -= 360.0;
      }

      if (d2 < -180.0) {
         d2 += 360.0;
      }

      return d2;
   }

   public static int computeInt(int n, int n2) {
      return (int)(Math.random() * (n2 - n) + n);
   }

   public static double computeDouble(float x, float y) {
      double d = Math.toRadians(x);
      double d2 = Math.toRadians(y);
      double d3 = Math.cos(d);
      double d4 = Math.sin(d);
      double d5 = Math.cos(d2);
      double d6 = Math.sin(d2);
      return d3 * d5 + d4 * d6;
   }

   public static int computeInt(int n, int n2, int n3) {
      return Math.min(Math.max(n, n2), n3);
   }
}
