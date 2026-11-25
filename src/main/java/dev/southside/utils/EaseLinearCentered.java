package dev.southside.utils;

public class EaseLinearCentered extends UiAnimation {
   public EaseLinearCentered(int n, float f, float f2, boolean flag) {
      super(n, f, f2, flag);
   }

   @Override
   protected float evaluateEx(float f) {
      return f < 0.5 ? 4.0F * f * f * f : (float)(1.0 - Math.pow(-2.0F * f + 2.0F, 3.0) / 2.0);
   }
}
