package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public final class MillisTimer {
   @SerializedName("lastTime")
   public long lastTime;
   private static final long a;

   public MillisTimer() {
      this.lastTime = a;
   }

   public boolean hasTimeElapsed(long l) {
      return System.currentTimeMillis() - this.lastTime > l;
   }

   public boolean hasElapsed(long l) {
      return System.currentTimeMillis() >= this.lastTime + l;
   }

   public long getDurationMs(long l) {
      return l + this.lastTime - System.currentTimeMillis();
   }

   public long getElapsed() {
      return System.currentTimeMillis() - this.lastTime;
   }

   public void setLastTime(long l) {
      this.lastTime = l;
   }

   public void reset() {
      this.lastTime = System.currentTimeMillis();
   }

   static {
      long l = 6855052312165816039L;
      long l2 = -6855052312165816040L ^ l;
      byte b = -1;
      a = l2;
   }
}
