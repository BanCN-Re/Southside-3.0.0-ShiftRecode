package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class NanoSmoothFloat {
   @SerializedName("timer")
   private final NanoTimer timer;
   @SerializedName("speed")
   private final float speed;
   @SerializedName("prevValue")
   private float prevValue;
   @SerializedName("scale")
   private float scale;
   @SerializedName("target")
   public float target;
   @SerializedName("frozen")
   private boolean frozen;
   @SerializedName("frozenValue")
   private float frozenValue;

   public NanoSmoothFloat(float x, float y) {
      this.speed = x;
      this.timer = new NanoTimer();
      this.setProgress(y);
      this.setProgressEx(y);
   }

   public void setProgress(float f) {
      if (this.target != f) {
         this.prevValue = this.getProgress();
         this.timer.reset();
         this.scale = f - this.prevValue;
         this.target = f;
      }
   }

   public void setProgressEx(float f) {
      this.timer.reset();
      this.prevValue = f;
      this.scale = this.target - this.prevValue;
   }

   public float getProgress() {
      if (this.frozen) {
         return this.frozenValue;
      }

      long elapsedMs = this.timer.getElapsedMs();
      return this.prevValue + this.scale * evaluate(Math.min((float)elapsedMs / 1000.0F * this.speed, 1.0F));
   }

   public void performAction() {
      this.frozen = false;
      this.frozenValue = this.getProgress();
      this.frozen = true;
   }

   private static float evaluate(float f) {
      f = 1.0F - f;
      float f2 = 0.0F;
      float f3 = 0.01F;
      float f4 = 0.0F;
      float f5 = 1.0F;
      float f6 = f * f;
      float f7 = f6 * f;
      float f8 = -f2 * f7 + 3.0F * f2 * f6 - 3.0F * f2 * f + f2;
      float f9 = 3.0F * f3 * f7 - 6.0F * f3 * f6 + 3.0F * f3 * f;
      float f10 = -3.0F * f4 * f7 + 3.0F * f4 * f6;
      float f11 = f5 * f7;
      return 1.0F - (f8 + f9 + f10 + f11);
   }

   public static double computeDouble(double x, double y, double z) {
      boolean flag = y > x;
      double d = z * 10.0;
      return Math.abs(Math.max(y, x) - Math.min(y, x)) <= d ? y : x + (flag ? d : -d);
   }

   public static double computeDoubleEx(double x, double y, double z) {
      if (z >= 1.0) {
         return y;
      } else {
         return z == 0.0 ? x : computeDoubleAlt(x, y, z);
      }
   }

   public static double computeDoubleAlt(double x, double y, double z) {
      boolean flag = y > x;
      double d = Math.max(y, x) - Math.min(y, x);
      if (Math.abs(d) <= 0.001) {
         return y;
      }

      double d2 = d * z;
      return x + (flag ? d2 : -d2);
   }

   public static double computeDoubleImpl(double x, double y, double z) {
      if (Math.max(y, x) - Math.min(y, x) < 0.001) {
         return y;
      }

      int n = z < 0.0 ? -1 : 1;
      if (n == -1) {
         z *= -1.0;
      }

      double d = y - x;
      double d2 = d * IMinecraft.mc.getRenderTickCounter().getTickDelta(false) * z;
      return x + computeDouble(d, d2) * n;
   }

   public static double computeDoubleDirect(double x, double y, double z) {
      if (Math.max(y, x) - Math.min(y, x) < 0.001) {
         return y;
      }

      int n = z < 0.0 ? -1 : 1;
      if (n == -1) {
         z *= -1.0;
      }

      double d = y - x;
      double d2 = d * z;
      return x + computeDouble(d, d2) * n;
   }

   public static double computeDoubleSafe(double x, double y, double z) {
      if (Math.max(y, x) - Math.min(y, x) < 0.001) {
         return y;
      }

      int n = z < 0.0 ? -1 : 1;
      if (n == -1) {
         z *= -1.0;
      }

      double d = y - x;
      double t = d * IMinecraft.mc.getRenderTickCounter().getTickDelta(false) / (1.0 / z * (Math.min(240.0, IMinecraft.mc.getCurrentFps()) / 240.0));
      return x + computeDouble(d, t) * n;
   }

   private static double computeDouble(double x, double y) {
      if (x < 0.0 && y < x) {
         return x;
      } else {
         return x > 0.0 && y >= x ? x : y;
      }
   }
}
