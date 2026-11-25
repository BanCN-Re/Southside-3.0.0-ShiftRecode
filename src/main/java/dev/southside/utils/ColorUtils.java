package dev.southside.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.regex.Pattern;

public class ColorUtils {
   public static Pattern pattern;

   public static Color blendColor(Color color, float f) {
      return new Color(
         Math.max((int)(color.getRed() * f), 0), Math.max((int)(color.getGreen() * f), 0), Math.max((int)(color.getBlue() * f), 0), color.getAlpha()
      );
   }

   public static int computeInt(int n, int n2) {
      return n >> n2 & 0xFF;
   }

   public static Color blendColor(float[] floats) {
      float f;
      float f2;
      float f3;
      if (floats[1] == 0.0F) {
         f3 = 1.0F;
         f2 = f3;
         f = f3;
      } else {
         float f4 = floats[2] < 0.5 ? floats[2] * (1.0F + floats[1]) : floats[2] + floats[1] - floats[2] * floats[1];
         float f5 = 2.0F * floats[2] - f4;
         f = computeFloat(f5, f4, floats[0] + 0.33333334F);
         f2 = computeFloat(f5, f4, floats[0]);
         f3 = computeFloat(f5, f4, floats[0] - 0.33333334F);
      }

      f *= 255.0F;
      f2 *= 255.0F;
      f3 *= 255.0F;
      return new Color((int)f, (int)f2, (int)f3);
   }

   public static float lerpFloat(float x, float y, double z) {
      return lerp(x, y, (float)z).floatValue();
   }

   public static Double lerp(double x, double y, double z) {
      return x + (y - x) * z;
   }

   public static Color blendColor(Color color, Color color2, float f) {
      return new Color(
         color.getRed() + (int)((color2.getRed() - color.getRed()) * f),
         color.getGreen() + (int)((color2.getGreen() - color.getGreen()) * f),
         color.getBlue() + (int)((color2.getBlue() - color.getBlue()) * f),
         color.getAlpha() + (int)((color2.getAlpha() - color.getAlpha()) * f)
      );
   }

   public static int computeIntEx(int n, int n2) {
      int n3 = n >> 16 & 0xFF;
      int n4 = n >> 8 & 0xFF;
      int n5 = n & 0xFF;
      return (n2 & 0xFF) << 24 | (n3 & 0xFF) << 16 | (n4 & 0xFF) << 8 | n5 & 0xFF;
   }

   public static int computeInt(int n) {
      int n2 = 255 - computeInt(n, 0);
      int n3 = 255 - computeInt(n, 8);
      int n4 = 255 - computeInt(n, 16);
      int n5 = computeInt(n, 24);
      return n2 + (n3 << 8) + (n4 << 16) + (n5 << 24);
   }

   public static int computeInt(float x, float y, float width, float height) {
      return computeInt((int)(x * 255.0F), (int)(y * 255.0F), (int)(width * 255.0F), (int)(height * 255.0F));
   }

   public static int computeInt(int n, int n2, float f) {
      float f2 = Math.min(1.0F, Math.max(0.0F, f));
      Color color = new Color(n);
      Color color2 = new Color(n2);
      return blendColorEx(color, color2, f2).getRGB();
   }

   public static Color blendColor(Color color) {
      return new Color(computeInt(color.getRGB()));
   }

   public static Color blendColorEx(Color color, float f) {
      float f2 = Math.min(1.0F, Math.max(0.0F, f));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * f2));
   }

   public static int computeInt(int n, float f) {
      Color color = new Color(n);
      return blendColorEx(color, f).getRGB();
   }

   public static Color blendColor(Color color, int n) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), n);
   }

   public static int lerpInt(int n, int n2, double d) {
      return lerp(n, n2, (float)d).intValue();
   }

   public static Color blendColorEx(Color color, Color color2, float f) {
      float f2 = Math.min(1.0F, Math.max(0.0F, f));
      return new Color(
         lerpInt(color.getRed(), color2.getRed(), f2),
         lerpInt(color.getGreen(), color2.getGreen(), f2),
         lerpInt(color.getBlue(), color2.getBlue(), f2),
         lerpInt(color.getAlpha(), color2.getAlpha(), f2)
      );
   }

   public static int computeInt(Color color, Color color2, float f) {
      float f2 = Math.min(1.0F, Math.max(0.0F, f));
      return blendColorEx(color, color2, f2).getRGB();
   }

   public static Color blendColor(int n, float f) {
      float f2 = Math.min(1.0F, Math.max(0.0F, f));
      return new Color(n, n, n, (int)(255.0F * f2));
   }

   public static Color blendColorAlt(Color color, Color color2, float f) {
      return new Color(computeInt(color, color2, 255.0F * f / 255.0F));
   }

   public static Color blendColorAlt(Color color, float f) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(f * 255.0F));
   }

   public static Color fromHsb(long l, int n, float f, float f2) {
      Color color = new Color(Color.HSBtoRGB(((float)l + n * -3000000.0F) / 2.0F / 1.0E9F, f, f2));
      return new Color(color.getRed() / 255.0F * 1.0F, color.getGreen() / 255.0F * 1.0F, color.getBlue() / 255.0F * 1.0F, color.getAlpha() / 255.0F);
   }

   public static float computeFloat(float x, float y, float z) {
      float f = z;
      if (z < 0.0F) {
         f = z + 1.0F;
      }

      if (f > 1.0F) {
         f--;
      }

      if (f < 0.16666667F) {
         return x + (y - x) * 6.0F * f;
      } else if (f < 0.5F) {
         return y;
      } else {
         return f < 0.6666667F ? x + (y - x) * (0.6666667F - f) * 6.0F : x;
      }
   }

   public static String stripColorCodes(String text) {
      return pattern.matcher(text).replaceAll("");
   }

   public static Color[] fromHsb(Color color) {
      Color[] colorArray = new Color[2];
      float[] floats = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      float f = 0.083333336F;
      float f2 = floats[0] + f;
      colorArray[0] = new Color(Color.HSBtoRGB(f2, floats[1], floats[2]));
      float f3 = floats[0] - f;
      colorArray[1] = new Color(Color.HSBtoRGB(f3, floats[1], floats[2]));
      return colorArray;
   }

   public static Color blendColor(BufferedImage bufferedImage, int n, int n2, int n3) {
      int[] values = new int[3];
      int n4 = 0;

      while (n4 < n) {
         for (int i = 0; i < n2; i += n3) {
            Color color = new Color(bufferedImage.getRGB(n4, i));
            values[0] += color.getRed();
            values[1] += color.getGreen();
            values[2] += color.getBlue();
         }

         n4 += n3;
      }

      n4 = n * n2 / (n3 * n3);
      return new Color(values[0] / n4, values[1] / n4, values[2] / n4);
   }

   public static Color fromHsb() {
      return new Color(Color.HSBtoRGB((float)Math.random(), (float)(0.5 + Math.random() / 2.0), (float)(0.5 + Math.random() / 2.0)));
   }

   public static Color blendColorEx(Color color, int n) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), n);
   }

   public static int computeInt(int n, int n2, int n3, int n4) {
      return (n4 & 0xFF) << 24 | (n3 & 0xFF) << 16 | (n2 & 0xFF) << 8 | n & 0xFF;
   }

   public static Color blendColor(int n) {
      return blendColor(n, 1.0F);
   }

   static {
      pattern = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");
   }
}
