package dev.southside.utils;

public final class ArgbColorUtils {
   public static int getRed(int n) {
      return n >> 16 & 0xFF;
   }

   public static int getGreen(int n) {
      return n >> 8 & 0xFF;
   }

   public static int getBlue(int n) {
      return n & 0xFF;
   }

   public static int getAlpha(int n) {
      return n >> 24 & 0xFF;
   }

   public static int rgbaFloat(float x, float y, float width, float height) {
      return rgba((int)(x * 255.0F), (int)(y * 255.0F), (int)(width * 255.0F), (int)(height * 255.0F));
   }

   public static int rgb(int n, int n2, int n3) {
      return rgba(n, n2, n3, 255);
   }

   public static int rgba(int n, int n2, int n3, int n4) {
      return n4 << 24 | n << 16 | n2 << 8 | n3;
   }

   public static int rgbaBytes(byte b, byte b2, byte b3, byte b4) {
      return rgba(b & 0xFF, b2 & 0xFF, b3 & 0xFF, b4 & 0xFF);
   }

   public static int withRed(int n, int n2) {
      return n & 65535 | n2 << 16;
   }

   public static int withGreen(int n, int n2) {
      return n & 16711935 | n2 << 8;
   }

   public static int withBlue(int n, int n2) {
      return n & 16776960 | n2;
   }

   public static int withAlpha(int n, int n2) {
      return n & 16777215 | n2 << 24;
   }
}
