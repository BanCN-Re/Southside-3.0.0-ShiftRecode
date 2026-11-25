package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import org.lwjgl.opengl.GL45C;

public class ShaderStorageBlock {
   @SerializedName("program")
   public final ShaderProgram program;
   @SerializedName("blockIndex")
   public final int blockIndex;

   public ShaderStorageBlock(ShaderProgram program2, int n) {
      this.program = program2;
      this.blockIndex = n;
   }

   public ShaderProgram getProgram() {
      return this.program;
   }

   public int getBlockIndex() {
      return this.blockIndex;
   }

   public void shaderStorageBlockBinding(int n) {
      GL45C.glShaderStorageBlockBinding(this.program.getProgramId(), this.blockIndex, n);
   }
}
