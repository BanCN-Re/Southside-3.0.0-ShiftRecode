package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class HudDragOffset {
   @SerializedName("offsetX")
   private float offsetX;
   @SerializedName("offsetY")
   private float offsetY;

   public HudDragOffset(float x, float y) {
      this.performAction(x, y);
   }

   public HudDragOffset() {
      this(0.0F, 0.0F);
   }

   public void performAction(float x, float y) {
      this.offsetX = x;
      this.offsetY = y;
   }

   public float getOffsetX() {
      return this.offsetX;
   }

   public float getOffsetY() {
      return this.offsetY;
   }
}
