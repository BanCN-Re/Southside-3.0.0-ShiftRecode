package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class NanoVGImage {
   @SerializedName("image")
   private final int image;
   @SerializedName("width")
   private final int width;
   @SerializedName("height")
   private final int height;

   public NanoVGImage(int n, int n2, int n3) {
      this.image = n;
      this.width = n2;
      this.height = n3;
   }

   public int getImage() {
      return this.image;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }
}
