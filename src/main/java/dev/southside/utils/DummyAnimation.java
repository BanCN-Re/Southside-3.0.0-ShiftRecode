package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.concurrent.Callable;

public class DummyAnimation extends UiAnimation {
   @SerializedName("value")
   protected final float value;
   @SerializedName("done")
   protected Callable<Boolean> done = null;

   public DummyAnimation(float f, Callable<Boolean> callable) {
      super(0.0F, f, f, false);
      this.value = f;
      this.done = callable;
   }

   public DummyAnimation(float x, float y) {
      super(y, x, x, false);
      this.value = x;
   }

   public DummyAnimation(float f) {
      this(f, 0.0F);
   }

   @Override
   public float evaluate(float f) {
      super.timePassed += f;
      return this.value;
   }

   @Override
   public boolean isValid() {
      if (this.done != null) {
         try {
            return this.done.call();
         } catch (Exception e) {
         }
      }

      return super.isValid();
   }

   @Override
   protected float evaluateEx(float f) {
      return f;
   }
}
