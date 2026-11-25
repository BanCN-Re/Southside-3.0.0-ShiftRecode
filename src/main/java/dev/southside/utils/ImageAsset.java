package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class ImageAsset {
   @SerializedName("filePath")
   public final String filePath;
   @SerializedName("flags")
   public final int flags;
   private static long timestamp;
   public ImageAsset(String text, int n) {
      this.filePath = text;
      this.flags = n;
   }

   public ImageAsset(String text) {
      this(text, (int)timestamp);
   }

   static {
      long l = 5106507216324657159L;
      timestamp = l;
   }
}
