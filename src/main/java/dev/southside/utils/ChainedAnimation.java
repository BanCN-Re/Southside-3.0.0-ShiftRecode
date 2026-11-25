package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class ChainedAnimation extends UiAnimation {
   @SerializedName("animations")
   private final UiAnimation[] animations;
   @SerializedName("currentAnimation")
   private int currentAnimation = 0;
   @SerializedName("value")
   private float value;

   public ChainedAnimation(UiAnimation... uiAnimationArray) {
      super(1.0F, 0.0F, 0.0F, false);
      this.animations = uiAnimationArray;
   }

   @Override
   public float evaluate(float f) {
      if (this.currentAnimation >= this.animations.length) {
         return this.value;
      }

      this.value = this.animations[this.currentAnimation].evaluate(f);
      if (this.animations[this.currentAnimation].isValid()) {
         this.currentAnimation++;
      }

      return this.value;
   }

   @Override
   public boolean isValid() {
      return this.currentAnimation >= this.animations.length;
   }

   @Override
   protected float evaluateEx(float f) {
      return 0.0F;
   }
}
