package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class ElapsedTimer {
   @SerializedName("startTime")
   public long startTime;

   public long getStartTime() {
      return this.startTime;
   }

   public ElapsedTimer() {
      this.reset();
   }

   public void setStartTime(long l) {
      this.startTime = l;
   }

   public void reset() {
      this.startTime = System.currentTimeMillis();
   }

   public long getElapsedMs() {
      return System.currentTimeMillis() - this.startTime;
   }

   public boolean hasElapsed(long l) {
      return System.currentTimeMillis() - l - this.startTime >= 0L;
   }
}
