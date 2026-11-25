package dev.southside.utils;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;

public class ColorAnimation {
   @SerializedName("palette")
   private ColorPalette palette;
   @SerializedName("duration")
   private final int duration;
   @SerializedName("prevState")
   private int prevState = 0;
   @SerializedName("redAnimation")
   private UiAnimation redAnimation;
   @SerializedName("greenAnimation")
   private UiAnimation greenAnimation;
   @SerializedName("blueAnimation")
   private UiAnimation blueAnimation;
   @SerializedName("alphaAnimation")
   private UiAnimation alphaAnimation;

   public ColorAnimation(ColorPalette palette2, int n) {
      this.palette = palette2;
      this.duration = n;
      this.redAnimation = new DummyAnimation(palette2.getColorNormalf()[0]);
      this.greenAnimation = new DummyAnimation(palette2.getColorNormalf()[1]);
      this.blueAnimation = new DummyAnimation(palette2.getColorNormalf()[2]);
      this.alphaAnimation = new DummyAnimation(palette2.getColorNormalf()[3]);
   }

   public ColorAnimation(ColorPalette palette2) {
      this(palette2, 100);
   }

   public int computeInt(boolean flag, boolean flag2) {
      int n = flag2 ? 2 : (flag ? 1 : 0);
      if (n != this.prevState) {
         float[] floats = flag2 ? this.palette.getColorPressedf() : (flag ? this.palette.getColorHoveredf() : this.palette.getColorNormalf());
         this.redAnimation = new EaseInOutCubicAnimation(this.duration, this.redAnimation.getProgress(), floats[0], false);
         this.greenAnimation = new EaseInOutCubicAnimation(this.duration, this.greenAnimation.getProgress(), floats[1], false);
         this.blueAnimation = new EaseInOutCubicAnimation(this.duration, this.blueAnimation.getProgress(), floats[2], false);
         this.alphaAnimation = new EaseInOutCubicAnimation(this.duration, this.alphaAnimation.getProgress(), floats[3], false);
         this.prevState = n;
      }

      return (int)(this.alphaAnimation.getProgress() * 255.0F) << 24
         | (int)(this.redAnimation.getProgress() * 255.0F) << 16
         | (int)(this.greenAnimation.getProgress() * 255.0F) << 8
         | (int)(this.blueAnimation.getProgress() * 255.0F);
   }

   public float getProgress() {
      return this.alphaAnimation.evaluate(0.0F);
   }

   public ColorPalette getPalette() {
      return this.palette;
   }

   public void performAction(ColorPalette palette2) {
      if (!this.palette.equals(palette2)) {
         this.palette = palette2;
         this.prevState = 3;
      }
   }

   public void performAction(float[] floats) {
      this.redAnimation = new DummyAnimation(floats[0]);
      this.greenAnimation = new DummyAnimation(floats[1]);
      this.blueAnimation = new DummyAnimation(floats[2]);
      this.alphaAnimation = new DummyAnimation(floats[3]);
   }
}
