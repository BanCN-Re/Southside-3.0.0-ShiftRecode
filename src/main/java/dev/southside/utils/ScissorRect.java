package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class ScissorRect {
   @SerializedName("x")
   public float x;
   @SerializedName("y")
   public float y;
   @SerializedName("width")
   public float width;
   @SerializedName("height")
   public float height;

   public ScissorRect(float x2, float y2, float width2, float height2) {
      this.x = x2;
      this.y = y2;
      this.width = width2;
      this.height = height2;
   }

   public ScissorRect(ScissorRect other) {
      this.x = other.x;
      this.y = other.y;
      this.width = other.width;
      this.height = other.height;
   }

   public boolean isValid(float x2, float y2) {
      return x2 >= this.x && x2 <= this.x + this.width && y2 >= this.y && y2 <= this.y + this.height;
   }
}
