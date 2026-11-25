package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class FrameTickAccumulator {
   @SerializedName("tickAccumulator")
   private float tickAccumulator;
   @SerializedName("tickDelta")
   private float tickDelta;
   @SerializedName("lastUpdateMillis")
   private long lastUpdateMillis;
   @SerializedName("millisPerTick")
   private float millisPerTick;

   public FrameTickAccumulator(float f, long l) {
      this.millisPerTick = 1000.0F / f;
      this.lastUpdateMillis = l;
   }

   public float getProgress() {
      return this.millisPerTick / 50.0F;
   }

   public void setProgress(float f) {
      this.millisPerTick = 50.0F / f;
   }

   public int computeInt(long l) {
      this.tickDelta = (float)(l - this.lastUpdateMillis) / this.millisPerTick;
      this.lastUpdateMillis = l;
      this.tickAccumulator = this.tickAccumulator + this.tickDelta;
      int tickAccumulator2 = (int)this.tickAccumulator;
      this.tickAccumulator -= tickAccumulator2;
      return tickAccumulator2;
   }
}
