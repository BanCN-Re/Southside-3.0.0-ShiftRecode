package dev.southside.utils;

public class EaseOutBackAnimation extends UiAnimation {
   private static final double DEFAULT_DOUBLE = 1.7;
   private static final double DEFAULT_DOUBLE_VALUE = 2.7;

   public EaseOutBackAnimation(int n, float f, float f2, boolean flag) {
      super(n, f, f2, flag);
   }

   @Override
   protected float evaluateEx(float f) {
      return (float)(1.0 + 2.7 * Math.pow(f - 1.0F, 3.0) + 2.04 * Math.pow(f - 1.0F, 2.0));
   }
}
