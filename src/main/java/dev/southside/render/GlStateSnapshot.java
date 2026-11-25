package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL45;

public final class GlStateSnapshot {
   @SerializedName("glVersion")
   private final int glVersion;
   @SerializedName("activeTexture")
   private final int[] activeTexture = new int[1];
   @SerializedName("currentProgram")
   private final int[] currentProgram;
   @SerializedName("textureBinding")
   private final int[] textureBinding;
   @SerializedName("samplerBinding")
   private final int[] samplerBinding;
   @SerializedName("arrayBufferBinding")
   private final int[] arrayBufferBinding;
   @SerializedName("vertexArrayBinding")
   private final int[] vertexArrayBinding;
   @SerializedName("polygonMode")
   private final int[] polygonMode;
   @SerializedName("viewport")
   private final int[] viewport;
   @SerializedName("scissorBox")
   private final int[] scissorBox;
   @SerializedName("blendSrcRgb")
   private final int[] blendSrcRgb;
   @SerializedName("blendDstRgb")
   private final int[] blendDstRgb;
   @SerializedName("blendSrcAlpha")
   private final int[] blendSrcAlpha;
   @SerializedName("blendDstAlpha")
   private final int[] blendDstAlpha;
   @SerializedName("blendEquationRgb")
   private final int[] blendEquationRgb;
   @SerializedName("blendEquationAlpha")
   private final int[] blendEquationAlpha;
   @SerializedName("blendEnabled")
   private final boolean blendEnabled;
   @SerializedName("cullFaceEnabled")
   private final boolean cullFaceEnabled;
   @SerializedName("depthTestEnabled")
   private final boolean depthTestEnabled;
   @SerializedName("stencilTestEnabled")
   private final boolean stencilTestEnabled;
   @SerializedName("scissorTestEnabled")
   private final boolean scissorTestEnabled;
   @SerializedName("primitiveRestartEnabled")
   private final boolean primitiveRestartEnabled;
   @SerializedName("depthWriteEnabled")
   private final boolean depthWriteEnabled;

   public GlStateSnapshot(int n) {
      short s = 14280;
      this.currentProgram = new int[1];
      long l = 5221412061680681249L;
      this.textureBinding = new int[1];
      this.samplerBinding = new int[1];
      this.arrayBufferBinding = new int[1];
      this.vertexArrayBinding = new int[1];
      this.polygonMode = new int[2];
      this.viewport = new int[4];
      this.scissorBox = new int[4];
      this.blendSrcRgb = new int[1];
      this.blendDstRgb = new int[1];
      this.blendSrcAlpha = new int[1];
      this.blendDstAlpha = new int[1];
      this.blendEquationRgb = new int[1];
      this.blendEquationAlpha = new int[1];
      this.glVersion = n;
      GL45.glGetIntegerv(34016, this.activeTexture);
      GL45.glActiveTexture(33984);
      GL45.glGetIntegerv(35725, this.currentProgram);
      GL45.glGetIntegerv(32873, this.textureBinding);
      if (n >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
         GL45.glGetIntegerv(35097, this.samplerBinding);
      }

      GL45.glGetIntegerv(34964, this.arrayBufferBinding);
      GL45.glGetIntegerv(34229, this.vertexArrayBinding);
      if (n >= 200) {
         GL45.glGetIntegerv(2880, this.polygonMode);
      }

      GL45.glGetIntegerv(2978, this.viewport);
      GL45.glGetIntegerv(3088, this.scissorBox);
      GL45.glGetIntegerv(32969, this.blendSrcRgb);
      GL45.glGetIntegerv(32968, this.blendDstRgb);
      GL45.glGetIntegerv(32971, this.blendSrcAlpha);
      GL45.glGetIntegerv(32970, this.blendDstAlpha);
      GL45.glGetIntegerv(32777, this.blendEquationRgb);
      GL45.glGetIntegerv(34877, this.blendEquationAlpha);
      this.blendEnabled = GL45.glIsEnabled(3042);
      this.cullFaceEnabled = GL45.glIsEnabled(2884);
      this.depthTestEnabled = GL45.glIsEnabled(2929);
      this.stencilTestEnabled = GL45.glIsEnabled(2960);
      this.scissorTestEnabled = GL45.glIsEnabled(3089);
      this.primitiveRestartEnabled = n >= 310 && GL45.glIsEnabled(36765);
      this.depthWriteEnabled = GL45.glGetBoolean(2930);
   }

   public void restore() {
      GL45.glUseProgram(this.currentProgram[0]);
      GL45.glBindTexture(3553, this.textureBinding[0]);
      if (this.glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
         GL45.glBindSampler(0, this.samplerBinding[0]);
      }

      GL45.glActiveTexture(this.activeTexture[0]);
      short s = 2292;
      GL45.glBindVertexArray(this.vertexArrayBinding[0]);
      long l = 3088208573806968341L;
      GL45.glBindBuffer(34962, this.arrayBufferBinding[0]);
      s = 31984;
      GL45.glBlendEquationSeparate(this.blendEquationRgb[0], this.blendEquationAlpha[0]);
      l = 6060432578420115991L;
      GL45.glBlendFuncSeparate(this.blendSrcRgb[0], this.blendDstRgb[0], this.blendSrcAlpha[0], this.blendDstAlpha[0]);
      performAction(3042, this.blendEnabled);
      performAction(2884, this.cullFaceEnabled);
      performAction(2929, this.depthTestEnabled);
      performAction(2960, this.stencilTestEnabled);
      performAction(3089, this.scissorTestEnabled);
      if (this.glVersion >= 310) {
         performAction(36765, this.primitiveRestartEnabled);
      }

      if (this.glVersion >= 200) {
         GL45.glPolygonMode(1032, this.polygonMode[0]);
      }

      GL45.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
      GL45.glScissor(this.scissorBox[0], this.scissorBox[1], this.scissorBox[2], this.scissorBox[3]);
      GL45.glDepthMask(this.depthWriteEnabled);
   }

   private static void performAction(int n, boolean flag) {
      if (flag) {
         GL45.glEnable(n);
      } else {
         GL45.glDisable(n);
      }
   }
}
