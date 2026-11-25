package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class NanoTimer {
   @SerializedName("startTime")
   public long startTime;
   private static final long DEFAULT_LONG = 1000000L;

   public long currentTimeMs() {
      return System.nanoTime() / DEFAULT_LONG;
   }

   public long getElapsedMs() {
      return this.currentTimeMs() - this.startTime;
   }

   public void reset() {
      this.startTime = this.currentTimeMs();
   }

   public NanoTimer getNanoTimer(int n) {
      this.startTime = this.currentTimeMs() + n;
      return this;
   }

   public boolean hasTimePassed(long l) {
      return this.currentTimeMs() - this.startTime > l;
   }

   public boolean hasTimePassedDouble(double d) {
      return d == 0.0 || this.currentTimeMs() - this.startTime >= d;
   }

   public void setStartTime(long l) {
      this.startTime = l;
   }
}
