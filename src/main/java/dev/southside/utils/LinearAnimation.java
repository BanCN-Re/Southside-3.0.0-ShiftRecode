package dev.southside.utils;

public class LinearAnimation extends UiAnimation {
   public LinearAnimation(int n, float f, float f2, boolean flag) {
      super(n, f, f2, flag);
   }

   @Override
   protected float evaluateEx(float f) {
      return f == 1.0F ? 1.0F : 1.0F - (float)Math.pow(2.0, -10.0F * f);
   }
}
