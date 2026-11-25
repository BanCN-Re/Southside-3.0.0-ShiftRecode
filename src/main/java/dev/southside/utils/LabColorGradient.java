package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.MathHelper;

public class LabColorGradient {
   public static class Inner {
      private static final double DEFAULT_DOUBLE = 0.95047;
      private static final double DEFAULT_DOUBLE_VALUE = 1.0;
      private static final double DEFAULT_DOUBLE_DATA = 1.08883;
      private static final double DEFAULT_DOUBLE_REF = 0.206896552;
      private static final double DEFAULT_DOUBLE_PRIMARY = 0.12841855;
      private static final double DEFAULT_DOUBLE_SECONDARY = 0.137931034;
      @SerializedName("firstColor")
      public final Color firstColor;
      @SerializedName("lastColor")
      public final Color lastColor;
      @SerializedName("numColors")
      public final int numColors;
      @SerializedName("colors")
      public final List<Color> colors;

      public Inner(Color color, Color color2, int n) {
         this.firstColor = color;
         this.lastColor = color2;
         this.numColors = n;
         this.colors = new ArrayList<>();
         this.performAction();
      }

      private Color blendColor(double d) {
         double d2 = Math.min(1.0, Math.max(0.0, d));
         if (d2 <= 0.0) {
            return this.firstColor;
         } else if (d2 >= 1.0) {
            return this.lastColor;
         } else {
            return d2 > 0.0 && d2 < 1.0 ? this.blendColor(this.firstColor, this.lastColor, (float)d2) : null;
         }
      }

      public void performAction() {
         for (int i = 0; i < this.numColors; i++) {
            this.colors.add(this.blendColor((double)i / (this.numColors - 1)));
         }
      }

      public int computeInt(double d) {
         double clamped = MathHelper.clamp(d, 0.0, 1.0);
         int n = this.numColors - 1;
         int n2 = (int)Math.floor(Math.max(0.0, clamped * n - 0.001));
         float f = (float)((clamped - (double)n2 / n) * n);
         return ColorUtils.computeInt(this.colors.get(n2), this.colors.get(n2 + 1), f);
      }

      private Color blendColor(Color color, Color color2, float f) {
         float[] floatList = getFloatList(color);
         float[] floatList2 = getFloatList(color2);
         float f2 = floatList[0];
         float f3 = floatList2[0];
         float f4 = floatList[1];
         float f5 = floatList2[1];
         float f6 = floatList[2];
         float f7 = floatList2[2];
         float f8 = -1.0F;
         float f9;
         if (!Float.isNaN(f2) && !Float.isNaN(f3)) {
            float f10;
            if (f3 > f2 && f3 - f2 > 180.0F) {
               f10 = f3 - (f2 + 360.0F);
            } else if (f3 < f2 && f2 - f3 > 180.0F) {
               f10 = f3 + 360.0F - f2;
            } else {
               f10 = f3 - f2;
            }

            f9 = f2 + f * f10;
         } else if (!Float.isNaN(f2)) {
            f9 = f2;
            if (f7 == 1.0F || f7 == 0.0F) {
               f8 = f4;
            }
         } else if (!Float.isNaN(f3)) {
            f9 = f3;
            if (f6 == 1.0F || f6 == 0.0F) {
               f8 = f5;
            }
         } else {
            f9 = Float.NaN;
         }

         if (f8 == -1.0F) {
            f8 = f4 + f * (f5 - f4);
         }

         float f11 = f6 + f * (f7 - f6);
         return this.blendColor(new float[]{f9, f8, f11});
      }

      public static float[] getFloatList(Color color) {
         float[] floats = new float[3];
         performAction(color.getRed(), color.getGreen(), color.getBlue(), floats);
         float[] floats2 = new float[3];
         performAction(floats[0], floats[1], floats[2], floats2);
         return floats2;
      }

      private Color blendColor(float[] floats) {
         int[] intListEx = this.getIntListEx(floats[2], floats[1], floats[0]);

         for (int i = 0; i < 3; i++) {
            intListEx[i] = MathHelper.clamp(intListEx[i], 0, 255);
         }

         return new Color(intListEx[0], intListEx[1], intListEx[2]);
      }

      private static void performAction(int n, int n2, int n3, float[] floats) {
         float[] floats2 = new float[3];
         float[] floats3 = new float[]{n / 255.0F, n2 / 255.0F, n3 / 255.0F};

         for (int i = 0; i < 3; i++) {
            if (!(floats3[i] > 0.04045F)) {
               floats3[i] /= 12.92F;
            } else {
               floats3[i] = (float)Math.pow((floats3[i] + 0.055) / 1.055, 2.4);
            }

            floats3[i] = (float)(floats3[i] * 100.0);
         }

         floats2[0] = floats3[0] * 0.4124564F + floats3[1] * 0.3575761F + floats3[2] * 0.1804375F;
         floats2[1] = floats3[0] * 0.2126729F + floats3[1] * 0.7151522F + floats3[2] * 0.072175F;
         floats2[2] = floats3[0] * 0.0193339F + floats3[1] * 0.119192F + floats3[2] * 0.9503041F;
         floats2[0] /= 95.047F;
         floats2[1] /= 100.0F;
         floats2[2] /= 108.883F;

         for (int j = 0; j < 3; j++) {
            if (!(floats2[j] > 0.008856F)) {
               floats2[j] = 7.787F * floats2[j] + 0.13793103F;
            } else {
               floats2[j] = (float)Math.pow(floats2[j], 0.3333333333333333);
            }
         }

         floats[0] = 116.0F * floats2[1] - 16.0F;
         floats[1] = 500.0F * (floats2[0] - floats2[1]);
         floats[2] = 200.0F * (floats2[1] - floats2[2]);
      }

      private int[] getIntList(double x, double y, double z) {
         double d = (x + 16.0) / 116.0;
         double d2 = Double.isNaN(y) ? d : d + y / 500.0;
         double d3 = Double.isNaN(z) ? d : d - z / 200.0;
         d = 1.0 * this.computeDoubleEx(d);
         double d4 = 0.95047 * this.computeDoubleEx(d2);
         double d5 = 1.08883 * this.computeDoubleEx(d3);
         double d6 = this.computeDouble(3.2404542 * d4 - 1.5371385 * d - 0.4985314 * d5);
         double d7 = this.computeDouble(-0.969266 * d4 + 1.8760108 * d + 0.041556 * d5);
         double d8 = this.computeDouble(0.0556434 * d4 - 0.2040259 * d + 1.0572252 * d5);
         return new int[]{(int)Math.round(d6), (int)Math.round(d7), (int)Math.round(d8), 255};
      }

      private static void performAction(float f, float f2, float f3, float[] floats) {
         float dist = (float)Math.sqrt(f2 * f2 + f3 * f3);
         float f4 = (float)Math.toDegrees(Math.atan2(f3, f2));
         if (f4 < 0.0F) {
            f4 += 360.0F;
         }

         floats[0] = f4;
         floats[1] = dist;
         floats[2] = f;
      }

      public int[] getIntListEx(double x, double y, double z) {
         double[] doubleList = this.getDoubleList(x, y, z);
         return this.getIntList(doubleList[0], doubleList[1], doubleList[2]);
      }

      private double[] getDoubleList(double x, double y, double z) {
         double d = Math.toRadians(Double.isNaN(z) ? 0.0 : z);
         return new double[]{x, Math.cos(d) * y, Math.sin(d) * y};
      }

      private double computeDouble(double d) {
         double d2 = d <= 0.00304 ? 12.92 * d : Math.pow(d, 0.4166666666666667) * 1.055 - 0.055;
         return 255.0 * d2;
      }

      private double computeDoubleEx(double d) {
         return d > 0.206896552 ? d * d * d : 0.12841855 * (d - 0.137931034);
      }
   }
}
