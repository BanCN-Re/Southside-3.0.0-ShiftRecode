package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class InputHandler {
   @SerializedName("blockScissors")
   private final ArrayList<ScissorRect> blockScissors = new ArrayList<>();
   @SerializedName("scaleX")
   private double scaleX = 1.0;
   @SerializedName("scaleY")
   private double scaleY = 1.0;
   @SerializedName("blockDWheel")
   private boolean blockDWheel = false;
   @SerializedName("pendingScroll")
   private double pendingScroll = 0.0;

   public void performAction(double x, double y) {
      this.scaleX = x;
      this.scaleY = y;
   }

   public void performAction() {
      this.scaleX = 1.0;
      this.scaleY = 1.0;
   }

   public double getScaleX() {
      return this.scaleX;
   }

   public double getScaleY() {
      return this.scaleY;
   }

   public boolean isHovered(float f, float f2, float f3, float f4, boolean flag) {
      float progress = this.getProgress();
      float progressEx = this.getProgressEx();
      return (flag || this.blockScissors.size() == 0 || !this.isValid(progress, progressEx))
         && progress > f
         && progressEx > f2
         && progress < f + f3
         && progressEx < f2 + f4;
   }

   public boolean isHovered(float x, float y, float width, float height) {
      return this.isHovered(x, y, width, height, false);
   }

   public boolean isHoveredEx(float f, float f2, float f3, float f4, boolean flag) {
      return this.isHovered(f, f2, f3, f4, flag) && this.isValid(false);
   }

   public boolean isHoveredEx(float x, float y, float width, float height) {
      return this.isHoveredEx(x, y, width, height, false);
   }

   public boolean isValid(boolean flag) {
      return GuiUtils.isActive()
         && !OneConfigPlatform.getMousePlatform().isKeyDown(0)
         && (flag || this.blockScissors.size() == 0 || !this.isValid(this.getProgress(), this.getProgressEx()));
   }

   public boolean isValid() {
      return this.isValid(false);
   }

   public boolean isValidEx() {
      return OneConfigPlatform.getMousePlatform().isKeyDown(0);
   }

   public float getProgress() {
      return (float)(OneConfigPlatform.getMousePlatform().getCurrentValue() / this.scaleX);
   }

   public float getProgressEx() {
      return (float)(OneConfigPlatform.getMousePlatform().getCurrentValueEx() / this.scaleY);
   }

   public ScissorRect getScissorRect(float x, float y, float width, float height) {
      ScissorRect clip = new ScissorRect(x, y, width, height);
      this.blockScissors.add(clip);
      return clip;
   }

   public void performAction(ScissorRect clip) {
      this.blockScissors.add(clip);
   }

   public ScissorRect getScissorRect() {
      return this.getScissorRect(0.0F, 0.0F, 1920.0F, 1080.0F);
   }

   public void remove(ScissorRect clip) {
      this.blockScissors.remove(clip);
   }

   public void clear() {
      this.blockScissors.clear();
   }

   public boolean isValidAlt() {
      return !this.blockScissors.isEmpty();
   }

   public void performAction(double d) {
      this.pendingScroll += d;
   }

   public double computeDouble(boolean flag) {
      double d = this.pendingScroll != 0.0 ? this.pendingScroll : OneConfigPlatform.getMousePlatform().getCurrentValueAlt();
      this.pendingScroll = 0.0;
      return !flag && this.blockDWheel ? 0.0 : d;
   }

   public double getCurrentValue() {
      return this.computeDouble(false);
   }

   public void performActionEx() {
      this.blockDWheel = true;
   }

   public void performActionAlt() {
      this.blockDWheel = false;
   }

   private boolean isValid(float x, float y) {
      for (ScissorRect clip : this.blockScissors) {
         if (clip.isValid(x, y)) {
            return true;
         }
      }

      return false;
   }
}
