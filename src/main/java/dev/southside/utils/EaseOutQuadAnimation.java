package dev.southside.utils;

public class EaseOutQuadAnimation extends UiAnimation {
   public EaseOutQuadAnimation(int n, float f, float f2, boolean flag) {
      super(n, f, f2, flag);
   }

   @Override
   protected float evaluateEx(float f) {
      return 1.0F - (1.0F - f) * (1.0F - f);
   }
}
