package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class SvgAsset {
   @SerializedName("filePath")
   public final String filePath;
   @SerializedName("flags")
   public final int flags;
   private static final long a;

   public SvgAsset(String text, int n) {
      this.filePath = text;
      this.flags = n;
   }

   public SvgAsset(String text) {
      this(text, (int)a);
   }

   static {
      long l = 5930631717699647874L;
      long l2 = -7386356391517685371L ^ l;
      byte b = -1;
      a = l2;
   }
}
