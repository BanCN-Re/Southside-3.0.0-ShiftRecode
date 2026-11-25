package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class SmoothFloat {
   @SerializedName("timer")
   public final ElapsedTimer timer;
   @SerializedName("progressPerSecond")
   public final float progressPerSecond;
   @SerializedName("startValue")
   public float startValue;
   @SerializedName("delta")
   public float delta;
   @SerializedName("targetValue")
   public float targetValue;
   @SerializedName("frozen")
   public boolean frozen;
   @SerializedName("frozenValue")
   public float frozenValue;

   public void setTarget(float f) {
      if (this.targetValue != f) {
         this.startValue = this.getValue();
         this.timer.reset();
         this.delta = f - this.startValue;
         this.targetValue = f;
      }
   }

   public void freeze() {
      this.frozen = false;
      this.frozenValue = this.getValue();
      this.frozen = true;
   }

   public static float evaluate(float f) {
      float f2 = 1.0F - f;
      float f3 = 0.0F;
      float f4 = 0.01F;
      float f5 = 0.0F;
      float f6 = 1.0F;
      float f7 = f2 * f2;
      float f8 = f7 * f2;
      float f9 = -f3 * f8 + 3.0F * f3 * f7 - 3.0F * f3 * f2 + f3;
      float f10 = 3.0F * f4 * f8 - 6.0F * f4 * f7 + 3.0F * f4 * f2;
      float f11 = -3.0F * f5 * f8 + 3.0F * f5 * f7;
      float f12 = f6 * f8;
      return 1.0F - (f9 + f10 + f11 + f12);
   }

   public float getValue() {
      if (this.frozen) {
         return this.frozenValue;
      }

      long elapsedMs = this.timer.getElapsedMs();
      return this.startValue + this.delta * evaluate(Math.min((float)elapsedMs / 1000.0F * this.progressPerSecond, 1.0F));
   }

   public SmoothFloat(float x, float y) {
      this.progressPerSecond = x;
      this.timer = new ElapsedTimer();
      this.setTarget(y);
      this.setImmediate(y);
   }

   public void setImmediate(float f) {
      this.timer.reset();
      this.startValue = f;
      this.delta = this.targetValue - this.startValue;
   }
}
