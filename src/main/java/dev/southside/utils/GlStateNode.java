package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class GlStateNode {
   @SerializedName("depthTestEnabled")
   public boolean depthTestEnabled;
   @SerializedName("blendEnabled")
   public boolean blendEnabled;
   @SerializedName("cullEnabled")
   public boolean cullEnabled;
   @SerializedName("scissorEnabled")
   public boolean scissorEnabled;
   @SerializedName("next")
   public GlStateNode next;
   @SerializedName("previous")
   public GlStateNode previous;
}
