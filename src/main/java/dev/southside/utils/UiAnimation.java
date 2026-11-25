package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public abstract class UiAnimation {
   @SerializedName("reverse")
   protected final boolean reverse;
   @SerializedName("duration")
   protected final float duration;
   @SerializedName("start")
   protected final float start;
   @SerializedName("change")
   protected final float change;
   @SerializedName("timePassed")
   public float timePassed = 0.0F;

   public UiAnimation(float f, float f2, float f3, boolean flag) {
      this.duration = f;
      if (flag) {
         float f4 = f2;
         f2 = f3;
         f3 = f4;
      }

      this.start = f2;
      this.change = f3 - f2;
      this.reverse = flag;
   }

   public float evaluate(float f) {
      this.timePassed += f;
      return this.timePassed >= this.duration ? this.start + this.change : this.evaluateEx(this.timePassed / this.duration) * this.change + this.start;
   }

   public float getProgress() {
      return this.evaluate(GuiUtils.getProgress());
   }

   public boolean isValid() {
      return this.timePassed >= this.duration;
   }

   public boolean isReverse() {
      return this.reverse;
   }

   public float getStart() {
      return this.start;
   }

   public float getProgressEx() {
      return this.start + this.change;
   }

   protected abstract float evaluateEx(float f);
}
