package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import org.lwjgl.opengl.GL45C;

public class ShaderProgram {
   public static int count;
   @SerializedName("previousProgramId")
   public int previousProgramId;
   @SerializedName("programId")
   public int programId;
   @SerializedName("shaderStorageBlocks")
   public final Map<Integer, ShaderStorageBlock> shaderStorageBlocks = new Object2ObjectOpenHashMap();
   @SerializedName("uniformBlocks")
   public final Map<String, UniformBlock> uniformBlocks = new Object2ObjectOpenHashMap();
   @SerializedName("uniforms")
   public final Map<String, ShaderUniform> uniforms = new Object2ObjectOpenHashMap();
   private static final long DEFAULT_LONG = 4717892751406959501L;

   public ShaderProgram(int n) {
      this.programId = n;
      GL45C.glBindFragDataLocation(n, 0, "fragColor");
   }

   public int getProgramId() {
      return this.programId;
   }

   public void bind() {
      count = GL45C.glGetInteger((int)DEFAULT_LONG);
      this.previousProgramId = count;
      if (count != this.programId) {
         GL45C.glUseProgram(this.programId);
         count = this.programId;
      }
   }

   public void unbind() {
      GL45C.glUseProgram(this.previousProgramId);
      count = this.previousProgramId;
   }

   public ShaderUniform getUniform(String text) {
      return this.uniforms.computeIfAbsent(text, text2 -> new ShaderUniform(this, text2));
   }

   public void applyText(String text) {
      UniformBlock uniformBlock = this.uniformBlocks.computeIfAbsent(text, text2 -> new UniformBlock(this, text2));
      uniformBlock.uniformBlockBinding();
   }

   public void performAction(int n, int n2) {
      ShaderStorageBlock shaderStorageBlock = this.shaderStorageBlocks.computeIfAbsent(n, integer -> new ShaderStorageBlock(this, integer));
      shaderStorageBlock.shaderStorageBlockBinding(n2);
   }

   public void performAction() {
      for (ShaderUniform uniform : this.uniforms.values()) {
         uniform.performAction();
      }
   }

   public boolean isValid() {
      return !this.uniformBlocks.isEmpty();
   }

   public void performActionEx() {
      GL45C.glDeleteProgram(this.programId);
      this.programId = 0;
   }
}
