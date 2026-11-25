package dev.southside.utils;

public class EaseInOutCubicAnimation extends UiAnimation {
   public EaseInOutCubicAnimation(int n, float f, float f2, boolean flag) {
      super(n, f, f2, flag);
   }

   @Override
   protected float evaluateEx(float f) {
      return f < 0.5 ? 2.0F * f * f : (float)(1.0 - Math.pow(-2.0F * f + 2.0F, 2.0) / 2.0);
   }
}
