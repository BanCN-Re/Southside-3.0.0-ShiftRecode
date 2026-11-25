package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class ChatLineAnimation {
   @SerializedName("xAnimation")
   public UiAnimation xAnimation;
   @SerializedName("yAnimation")
   public UiAnimation yAnimation;
   @SerializedName("alphaAnimation")
   public UiAnimation alphaAnimation;
   @SerializedName("backgroundWidthAnimation")
   public UiAnimation backgroundWidthAnimation;
   @SerializedName("backgroundHeightAnimation")
   public UiAnimation backgroundHeightAnimation;
   @SerializedName("backgroundAlphaAnimation")
   public UiAnimation backgroundAlphaAnimation;

   public float getProgress() {
      return this.yAnimation.getProgress();
   }

   public void performAction(int n, float f, float f2, float f3) {
      this.backgroundWidthAnimation = new EaseOutQuadAnimation(n, this.backgroundWidthAnimation.getProgress(), f, false);
      this.backgroundHeightAnimation = new EaseOutQuadAnimation(n, this.backgroundHeightAnimation.getProgress(), f2, false);
      this.backgroundAlphaAnimation = new EaseOutQuadAnimation(n, this.backgroundAlphaAnimation.getProgress(), f3, false);
   }

   public float getProgressEx() {
      return this.backgroundWidthAnimation.getProgress();
   }

   public boolean isValid(boolean flag) {
      boolean flag2;
      if (flag) {
         if (this.backgroundWidthAnimation.isValid()
            && this.backgroundHeightAnimation.isValid()
            && this.backgroundAlphaAnimation.isValid()) {
            flag2 = true;
         } else {
            flag2 = false;
         }
      } else if (this.xAnimation.isValid() && this.yAnimation.isValid() && this.alphaAnimation.isValid()) {
         flag2 = true;
      } else {
         flag2 = false;
      }

      return flag2;
   }

   public ChatLineAnimation(int n, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10) {
      this.xAnimation = new EaseLinearCentered(n, f, f6, false);
      this.yAnimation = new EaseLinearCentered(n, f2, f7, false);
      this.alphaAnimation = new EaseLinearCentered(n, f3, f8, false);
      this.backgroundWidthAnimation = new EaseOutQuadAnimation(n, f4, f9, false);
      this.backgroundHeightAnimation = new EaseOutQuadAnimation(n, f5, f10, false);
      this.backgroundAlphaAnimation = new EaseOutQuadAnimation(n, f3, f8, false);
   }

   public ChatLineAnimation(int n, float f, float f2, float f3) {
      this(n, f, f2, f3, 0.0F, 0.0F);
   }

   public float getProgressAlt() {
      return this.alphaAnimation.getProgressEx();
   }

   public float getProgressImpl() {
      return this.alphaAnimation.getProgress();
   }

   public float getProgressDirect() {
      return this.xAnimation.getProgress();
   }

   public float getProgressSafe() {
      return this.yAnimation.getProgressEx();
   }

   public ChatLineAnimation(int n, float f, float f2, float f3, float f4, float f5) {
      this.xAnimation = new EaseLinearCentered(n, f, f, false);
      this.yAnimation = new EaseLinearCentered(n, f2, f2, false);
      this.alphaAnimation = new EaseLinearCentered(n, f3, f3, false);
      this.backgroundWidthAnimation = new EaseOutQuadAnimation(n, f4, f4, false);
      this.backgroundHeightAnimation = new EaseOutQuadAnimation(n, f5, f5, false);
      this.backgroundAlphaAnimation = new EaseOutQuadAnimation(n, f3, f3, false);
   }

   public void performActionEx(int n, float f, float f2, float f3) {
      this.xAnimation = new EaseLinearCentered(n, this.xAnimation.getProgress(), f, false);
      this.yAnimation = new EaseLinearCentered(n, this.yAnimation.getProgress(), f2, false);
      this.alphaAnimation = new EaseOutQuadAnimation(n, this.alphaAnimation.getProgress(), f3, false);
   }

   public float getProgressCustom() {
      return this.xAnimation.getProgressEx();
   }

   public float getProgressWithOffset() {
      return this.backgroundAlphaAnimation.getProgress();
   }

   public float getProgressByIndex() {
      return this.backgroundHeightAnimation.getProgress();
   }
}
