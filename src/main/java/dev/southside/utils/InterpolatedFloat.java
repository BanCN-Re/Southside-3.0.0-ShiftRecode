package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class InterpolatedFloat {
   @SerializedName("value")
   private float value;
   @SerializedName("previousValue")
   private float previousValue;
   private static FrameTickAccumulator frameTickAccumulator;

   public InterpolatedFloat(float f) {
      this.value = f;
   }

   public float getValue() {
      return this.value;
   }

   public float getProgress() {
      return interpolate(this.value, this.previousValue);
   }

   public void setPreviousValue(float f) {
      this.previousValue = f;
   }

   public void setProgress(float f) {
      this.previousValue = f;
      this.value = f;
   }

   public void performAction(float x, double y) {
      if (y != 0.0) {
         this.previousValue = this.value;
         double d = Math.max(Math.abs(x - this.value) * 0.35F / (10.0 / y), 0.05);
         if (y <= 0.0) {
            d = -y;
         }

         this.value = computeFloat(x, this.value, d);
         if (Math.abs(x - this.value) <= 0.05) {
            this.value = x;
         }
      }
   }

   public static float computeFloat(float x, float y, double z) {
      float f = y - x;
      double d = z * 3.0;
      if (f > z) {
         y -= (float)(d - 0.005);
         if (y < x) {
            y = x;
         }
      } else if (f < -z) {
         y += (float)(d + 0.005);
         if (y > x) {
            y = x;
         }
      } else {
         y = x;
      }

      return y;
   }

   public static float interpolate(double x, double y) {
      if (frameTickAccumulator == null) {
         frameTickAccumulator = new FrameTickAccumulator(20.0F, System.currentTimeMillis());
      }

      float f = frameTickAccumulator.computeInt(System.currentTimeMillis());
      return (float)(x * f + y * (1.0F - f));
   }
}
