package dev.southside.utils;

public class EaseInBackAnimation extends UiAnimation {
   public EaseInBackAnimation(int n, float f, float f2, boolean flag) {
      super(n, f, f2, flag);
   }

   @Override
   protected float evaluateEx(float f) {
      float f2 = 1.70158F;
      float f3 = f2 + 1.0F;
      return f3 * f * f * f - f2 * f * f;
   }
}
