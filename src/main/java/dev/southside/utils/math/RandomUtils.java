package dev.southside.utils.math;

import java.util.List;
import java.util.Random;

public class RandomUtils {
   public static Random RAND = new Random();
   public static Random BACKUP = new Random();

   public static <T> T getT(List<? extends T> list) {
      return (T)(list.isEmpty() ? null : list.get(RAND.nextInt(list.size())));
   }

   public static <T> T getT(T[] tArray) {
      return (T)(tArray.length == 0 ? null : tArray[RAND.nextInt(tArray.length)]);
   }

   public static long getDurationMs(int n, int n2) {
      int n3 = n2 - n <= 0 ? n : n + RAND.nextInt(1, n2 - n);
      return n3;
   }

   public static double computeDouble(double x, double y) {
      return RAND.nextDouble() * (y - x) + x;
   }

   public static boolean isValid() {
      return RAND.nextBoolean();
   }

   public static void nextBytes(byte[] bytes) {
      RAND.nextBytes(bytes);
   }

   public static float computeFloat(float x, float y) {
      if (x <= 0.0F || y <= x) {
         throw new IllegalArgumentException("minPrecision must be > 0 and maxPrecision > minPrecision");
      }

      if (RAND.nextBoolean()) {
         return -x - RAND.nextFloat() * (y - x);
      }

      float f;
      do {
         f = -x + RAND.nextFloat() * (y + x);
      } while (Math.abs(f) < x);

      return f;
   }

   public static String formatText(int n) {
      return formatText(n, "123456789");
   }

   public static String formatTextEx(int n) {
      return formatText(n, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
   }

   public static String formatTextAlt(int n) {
      return formatText(n, "0123456789abcdefghijklmnopqrstuvwxyz");
   }

   public static String formatTextImpl(int n) {
      return formatText(n, "0123456789abcdef");
   }

   public static String formatTextDirect(int n) {
      return formatText(n, "1234567890ABCDEF");
   }

   public static String formatTextSafe(int n) {
      return formatText(n, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
   }

   private static String formatText(int n, String text) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < n; i++) {
         sb.append(text.charAt(RAND.nextInt(text.length())));
      }

      return sb.toString();
   }

   public static void performAction(long l) {
      RAND = new Random(l);
   }

   public static void performAction() {
      BACKUP = RAND;
   }

   public static void performActionEx() {
      RAND = BACKUP;
   }
}
