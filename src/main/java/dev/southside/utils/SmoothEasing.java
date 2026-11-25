package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.render.jello.animation.Easing;

public class SmoothEasing {
   @SerializedName("easing")
   public Easing easing;
   @SerializedName("duration")
   public long duration;
   @SerializedName("lastUpdateTime")
   public long lastUpdateTime;
   @SerializedName("startTime")
   public long startTime;
   @SerializedName("startValue")
   public double startValue;
   @SerializedName("targetValue")
   public double targetValue = Double.NaN;
   @SerializedName("currentValue")
   public double currentValue;
   @SerializedName("finished")
   public boolean finished;

   public SmoothEasing(Easing easing2, long l) {
      this.easing = easing2;
      this.startTime = System.currentTimeMillis();
      this.duration = l;
   }

   public double computeDouble(double d) {
      this.lastUpdateTime = System.currentTimeMillis();
      if (this.targetValue != d) {
         if (!Double.isNaN(this.targetValue)) {
            this.performAction();
         }

         this.targetValue = d;
      } else {
         this.finished = this.lastUpdateTime - this.duration > this.startTime;
         if (this.finished) {
            this.currentValue = d;
            return this.currentValue;
         }
      }

      double d2 = this.easing.getFunction().apply(this.getCurrentValueEx());
      if (this.currentValue > d) {
         this.currentValue = this.startValue - (this.startValue - d) * d2;
      } else {
         this.currentValue = this.startValue + (d - this.startValue) * d2;
      }

      return this.currentValue;
   }

   public void performAction() {
      this.startTime = System.currentTimeMillis();
      this.startValue = this.currentValue;
      this.finished = false;
   }

   public double getCurrentValueEx() {
      return (double)(System.currentTimeMillis() - this.startTime) / this.duration;
   }

   public Easing getEasing() {
      return this.easing;
   }

   public void setEasing(Easing easing2) {
      this.easing = easing2;
   }

   public long getDuration() {
      return this.duration;
   }

   public void setDuration(long l) {
      this.duration = l;
   }

   public long getLastUpdateTime() {
      return this.lastUpdateTime;
   }

   public void setLastUpdateTime(long l) {
      this.lastUpdateTime = l;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public void setStartTime(long l) {
      this.startTime = l;
   }

   public double getStartValue() {
      return this.startValue;
   }

   public void setStartValue(double d) {
      this.startValue = d;
   }

   public double getTargetValue() {
      return this.targetValue;
   }

   public void setTargetValue(double d) {
      this.targetValue = d;
   }

   public double getCurrentValue() {
      return this.currentValue;
   }

   public void setCurrentValue(double d) {
      this.currentValue = d;
   }

   public boolean isFinished() {
      return this.finished;
   }

   public void setFinished(boolean flag) {
      this.finished = flag;
   }
}
