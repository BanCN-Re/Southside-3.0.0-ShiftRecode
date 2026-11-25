package dev.southside.utils;

import java.awt.Color;
import net.minecraft.util.math.MathHelper;

public class HealthColorUtils {
   private static final float[] DEFAULT_FLOATS = new float[]{0.0F, 0.3F, 0.7F, 1.0F};
   private static final Color[] COLOR_ARRAY = new Color[]{Color.RED, Color.RED, Color.YELLOW, Color.GREEN};
   private static final long DEFAULT_LONG = -1594704112711106305L;

   public static Color blendColor(float x, float y) {
      float f = x * (20.0F / y);
      float clamped = MathHelper.clamp(f / 20.0F, 0.0F, 1.0F);
      int[] intList = getIntList(DEFAULT_FLOATS, clamped);
      float f2 = DEFAULT_FLOATS[intList[0]];
      float f3 = DEFAULT_FLOATS[intList[1]];
      float f4 = (clamped - f2) / (f3 - f2);
      return blendColor(COLOR_ARRAY[intList[0]], COLOR_ARRAY[intList[1]], 1.0F - f4).brighter();
   }

   public static Color blendColor(Color color, int n) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(n, 0, (int)DEFAULT_LONG));
   }

   public static Color blendColor(Color color, Color color2, float f) {
      float f2 = 1.0F - f;
      float[] colorComponents = color.getColorComponents(null);
      float[] colorComponents2 = color2.getColorComponents(null);
      float f3 = colorComponents[0] * f + colorComponents2[0] * f2;
      float f4 = colorComponents[1] * f + colorComponents2[1] * f2;
      float f5 = colorComponents[2] * f + colorComponents2[2] * f2;
      f3 = Math.min(255.0F, Math.max(0.0F, f3));
      f4 = Math.min(255.0F, Math.max(0.0F, f4));
      f5 = Math.min(255.0F, Math.max(0.0F, f5));
      return new Color(f3, f4, f5);
   }

   public static int[] getIntList(float[] floats, float f) {
      int[] values = new int[2];
      int n = 0;

      while (n < floats.length && floats[n] <= f) {
         n++;
      }

      if (n >= floats.length) {
         n = floats.length - 1;
      }

      values[0] = n - 1;
      values[1] = n;
      return values;
   }

   public static Color blendColor(Color color, float f) {
      f = Math.min(1.0F, Math.max(0.0F, f));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * f));
   }

   public static int computeInt(int n, float f) {
      Color color = new Color(n);
      return blendColor(color, f).getRGB();
   }
}
