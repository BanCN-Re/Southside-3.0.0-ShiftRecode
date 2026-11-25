package dev.southside.utils;

import java.awt.Color;

public class RainbowColorUtils {
   public static Color blendColor(int n, int n2, Color color, Color color2, boolean flag) {
      int n3 = (int)((System.currentTimeMillis() / n + n2) % 360L);
      int n4 = n3 >= 180 ? 360 - n3 : n3;
      n3 = n4 * 2;
      return flag ? toHsb(color, color2, n3 / 360.0F) : blendColor(color, color2, n3 / 360.0F);
   }

   public static Color blendColor(Color color, Color color2) {
      return blendColor(color, color2, 0.5);
   }

   public static Color blendColor(Color color, float f) {
      return blendColor(color, (int)(f * 255.0F));
   }

   public static Double getDouble(double x, double y, double z) {
      return x + (y - x) * z;
   }

   public static Color fromHsb(int n, float f, int n2, int n3) {
      int n4 = (int)((System.currentTimeMillis() / n + n3) % 360L);
      float f2 = n4 / 360.0F;
      Color color = new Color(Color.HSBtoRGB(f2, f, 1.0F));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), n2);
   }

   public static Color blendColor(Color color, int n) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), n);
   }

   public static float computeFloat(float x, float y, double z) {
      return getDouble(x, y, (float)z).floatValue();
   }

   public static Color blendColor(int n, float f) {
      return blendColor(new Color(n), f);
   }

   public static int computeInt(int n, int n2, double d) {
      return getDouble(n, n2, (float)d).intValue();
   }

   public static Color blendColor(int n, int n2) {
      return blendColor(new Color(n), n2);
   }

   public static Color toHsb(Color color, Color color2, float f) {
      f = Math.min(1.0F, Math.max(0.0F, f));
      float[] floats = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      float[] floats2 = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
      Color hsbcolor = Color.getHSBColor(computeFloat(floats[0], floats2[0], f), computeFloat(floats[1], floats2[1], f), computeFloat(floats[2], floats2[2], f));
      return blendColor(hsbcolor, computeInt(color.getAlpha(), color2.getAlpha(), f) / 255.0F);
   }

   public static Color blendColor(Color color, Color color2, double d) {
      float f = (float)d;
      float f2 = 1.0F - f;
      float[] floats = new float[3];
      float[] floats2 = new float[3];
      color.getColorComponents(floats);
      color2.getColorComponents(floats2);
      return new Color(floats[0] * f + floats2[0] * f2, floats[1] * f + floats2[1] * f2, floats[2] * f + floats2[2] * f2);
   }

   public static Color blendColor(Color color, Color color2, float f) {
      f = Math.min(1.0F, Math.max(0.0F, f));
      return new Color(
         computeInt(color.getRed(), color2.getRed(), f),
         computeInt(color.getGreen(), color2.getGreen(), f),
         computeInt(color.getBlue(), color2.getBlue(), f),
         computeInt(color.getAlpha(), color2.getAlpha(), f)
      );
   }
}
