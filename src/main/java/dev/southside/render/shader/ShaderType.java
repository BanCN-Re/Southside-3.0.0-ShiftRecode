package dev.southside.render.shader;

import com.google.gson.annotations.SerializedName;

public enum ShaderType {
   VERTEX("Vertex", 35633),
   FRAGMENT("Fragment", 35632);

   @SerializedName("displayName")
   public final String displayName;
   @SerializedName("glShaderType")
   public final int glShaderType;

   ShaderType(String text, int n) {
      this.displayName = text;
      this.glShaderType = n;
   }

   // $VF: synthetic method
   private static ShaderType[] getShaderTypeArray() {
      return new ShaderType[]{VERTEX, FRAGMENT};
   }


}
