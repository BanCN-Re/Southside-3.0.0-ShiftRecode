package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.render.shader.ShaderType;
import org.lwjgl.opengl.GL45C;

public class GlShader {
   @SerializedName("type")
   public final ShaderType type;
   @SerializedName("glShaderId")
   public int glShaderId;
   @SerializedName("name")
   public final String name;
   @SerializedName("source")
   public final String source;

   public GlShader(ShaderType shaderType, int n, String text, String text2) {
      this.type = shaderType;
      this.glShaderId = n;
      this.name = text;
      this.source = text2;
   }

   public int getGlShaderId() {
      return this.glShaderId;
   }

   public ShaderType getType() {
      return this.type;
   }

   public String getName() {
      return this.name;
   }

   public String getSource() {
      return this.source;
   }

   public boolean isValid() {
      return this.type == ShaderType.VERTEX;
   }

   public boolean isValidEx() {
      return this.type == ShaderType.FRAGMENT;
   }

   public void attachShader(int n) {
      GL45C.glAttachShader(n, this.glShaderId);
   }

   public void detachShader(int n) {
      GL45C.glDetachShader(n, this.glShaderId);
   }

   public void performAction() {
      if (this.glShaderId != 0) {
         GL45C.glDeleteShader(this.glShaderId);
         this.glShaderId = 0;
      }
   }
}
