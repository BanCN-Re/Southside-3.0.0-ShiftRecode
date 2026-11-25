package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class CubicBezierAnimation extends UiAnimation {
   private static final float DEFAULT_FLOAT = 0.001F;
   @SerializedName("P0")
   protected final float P0;
   @SerializedName("P1")
   protected final float P1;
   @SerializedName("P2")
   protected final float P2;
   @SerializedName("P3")
   protected final float P3;

   public CubicBezierAnimation(float f, float f2, float f3, float f4, float f5, float f6, float f7, boolean flag) {
      super(f5, f6, f7, flag);
      this.P0 = f;
      this.P1 = f2;
      this.P2 = f3;
      this.P3 = f4;
   }

   public CubicBezierAnimation(float[] floats, float f, float f2, float f3, boolean flag) {
      this(floats[0], floats[1], floats[2], floats[3], f, f2, f3, flag);
   }

   @Override
   protected float evaluateEx(float f) {
      float f2 = 0.0F;
      float f3 = 1.0F;

      while (true) {
         float f4 = (f2 + f3) / 2.0F;
         float f5 = this.interpolate(this.P0, this.P2, f4);
         if (Math.abs(f - f5) < 0.001F) {
            return this.interpolate(this.P1, this.P3, f4);
         }

         if (f5 < f) {
            f2 = f4;
         } else {
            f3 = f4;
         }
      }
   }

   private float interpolate(float x, float y, float z) {
      return 3.0F * x * (1.0F - z) * (1.0F - z) * z + 3.0F * y * (1.0F - z) * z * z + z * z * z;
   }
}
