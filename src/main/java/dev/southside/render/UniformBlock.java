package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import org.lwjgl.opengl.GL45C;

public class UniformBlock {
   @SerializedName("program")
   public final ShaderProgram program;
   @SerializedName("blockIndex")
   public final int blockIndex;

   public UniformBlock(ShaderProgram program2, String text) {
      this.program = program2;
      this.blockIndex = GL45C.glGetUniformBlockIndex(program2.getProgramId(), text);
   }

   public ShaderProgram getProgram() {
      return this.program;
   }

   public int getBlockIndex() {
      return this.blockIndex;
   }

   public void uniformBlockBinding() {
      GL45C.glUniformBlockBinding(this.program.getProgramId(), this.blockIndex, 0);
   }
}
