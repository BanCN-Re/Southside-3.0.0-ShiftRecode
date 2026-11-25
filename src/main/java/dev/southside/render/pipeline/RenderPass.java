package dev.southside.render.pipeline;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.render.ShaderUniform;
import dev.southside.render.ShaderProgram;
import dev.southside.render.UniformBufferObject;
import dev.southside.render.shader.ShaderFactory;
import dev.southside.utils.IMinecraft;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

public class RenderPass {
   @SerializedName("vertexBufferId")
   public final int vertexBufferId;
   @SerializedName("indexBufferId")
   public final int indexBufferId;
   @SerializedName("vaoId")
   public final int vaoId;
   @SerializedName("vertexData")
   public ByteBuffer vertexData;
   @SerializedName("indexData")
   public ByteBuffer indexData;
   @SerializedName("vertexFormat")
   public final VertexFormat vertexFormat;
   @SerializedName("drawMode")
   public final DrawMode drawMode;
   @SerializedName("shader")
   public ShaderProgram shader;
   @SerializedName("modelViewMatrix")
   public Matrix4f modelViewMatrix;
   @SerializedName("framebuffer")
   public Framebuffer framebuffer;
   @SerializedName("projectionMatrix")
   public Matrix4f projectionMatrix;
   @SerializedName("skipUniformSetup")
   public boolean skipUniformSetup;
   @SerializedName("modelViewBuffer")
   public FloatBuffer modelViewBuffer;
   public static FloatBuffer modelView;
   public static FloatBuffer proj;
   public static long timestamp;

   public RenderPass(VertexFormat vertexFormat2, DrawMode drawMode2) {
      super();
      short s = 12715;
      long l = 2041317627712068703L;
      this.vertexFormat = vertexFormat2;
      this.drawMode = drawMode2;
      this.vertexBufferId = GlStateManager._glGenBuffers();
      this.indexBufferId = GlStateManager._glGenBuffers();
      this.vaoId = GlStateManager._glGenVertexArrays();
      int n = GL45C.glGetInteger(34965);
      s = 17386;
      GlStateManager._glBindVertexArray(this.vaoId);
      l = 7404790562159734294L;
      GlStateManager._glBindBuffer(34962, this.vertexBufferId);
      GlStateManager._glBindBuffer(34963, this.indexBufferId);
      s = 17386;
      vertexFormat2.setupState();
      l = 7404790562159734294L;
      GlStateManager._glBindBuffer(34962, 0);
      GlStateManager._glBindBuffer(34963, n);
      GlStateManager._glBindVertexArray(0);
   }

   public void setShader(ShaderProgram program) {
      this.shader = program;
   }

   public ShaderProgram getShader() {
      return this.shader;
   }

   public void setProjectionMatrix(Matrix4f matrix) {
      this.projectionMatrix = matrix;
   }

   public void setModelViewMatrix(Matrix4f matrix) {
      this.modelViewMatrix = matrix;
   }

   public void setFramebuffer(Framebuffer framebuffer2) {
      this.framebuffer = framebuffer2;
   }

   public int computeInt(Framebuffer framebuffer2) {
      return framebuffer2.fbo;
   }

   public void performAction() {
      this.skipUniformSetup = true;
   }

   public void setVertexData(ByteBuffer buffer) {
      this.vertexData = buffer;
   }

   public void setIndexData(ByteBuffer buffer) {
      this.indexData = buffer;
   }

   public void onKeyTyped(int n) {
      if (this.shader != null) {
         int n2 = GL45C.glGetInteger(34229);
         int n3 = GL45C.glGetInteger(34965);
         this.performAction(this.vertexData);
         this.performAction(this.indexData, n3);
         this.shader.bind();
         if (!this.skipUniformSetup) {
            if (!this.shader.isValid()) {
               ShaderUniform uniform = this.shader.getUniform("ProjMat");
               Matrix4f matrix = this.projectionMatrix != null ? this.projectionMatrix : RenderSystem.getProjectionMatrix();
               uniform.performAction(matrix);
               Camera camera = IMinecraft.mc.gameRenderer.getCamera();
               Matrix4f matrix2 = new Matrix4f().rotation(camera.getRotation().conjugate(new Quaternionf()));
               matrix2.mul(RenderSystem.getModelViewStack());
               this.shader.getUniform("ModelMat").performAction(matrix2);
               this.shader
                  .getUniform("ScreenSize")
                  .performAction((float)IMinecraft.mc.getFramebuffer().textureWidth, (float)IMinecraft.mc.getFramebuffer().textureHeight);
            } else {
               UniformBufferObject uniformBufferObject = ShaderFactory.getUniformBufferObject("matrix");
               uniformBufferObject.bindBufferBase();
               if (this.modelViewBuffer == null) {
                  this.modelViewBuffer = MemoryUtil.memAllocFloat(64);
               }

               Camera camera2 = IMinecraft.mc.gameRenderer.getCamera();
               Matrix4f matrix3 = new Matrix4f().rotation(camera2.getRotation().conjugate(new Quaternionf()));
               matrix3.mul(RenderSystem.getModelViewStack());
               matrix3.get(this.modelViewBuffer);
               uniformBufferObject.upload(64L, 64L, MemoryUtil.memAddress0(this.modelViewBuffer));
            }
         }

         this.shader.performAction();
         short s = 23074;
         GlStateManager._glBindVertexArray(this.vaoId);
         long l = 5294072642323560401L;
         GlStateManager._glBindBuffer(34962, this.vertexBufferId);
         GlStateManager._glBindBuffer(34963, this.indexBufferId);
         BufferRenderer.resetCurrentVertexBuffer();
         GlStateManager._drawElements(this.drawMode.glMode, n, 5125, 0L);
         s = 7125;
         GlStateManager._glBindVertexArray(n2);
         l = 3940495518191023655L;
         GlStateManager._glBindBuffer(34963, n3);
         GlStateManager._glBindBuffer(34962, 0);
         this.shader.unbind();
      }
   }

   public void performAction(ByteBuffer buffer) {
      GlStateManager._glBindBuffer(34962, this.vertexBufferId);
      GlStateManager._glBufferData(34962, buffer, 35048);
      GlStateManager._glBindBuffer(34962, 0);
   }

   public void performAction(ByteBuffer buffer, int n) {
      GlStateManager._glBindBuffer(34963, this.indexBufferId);
      GlStateManager._glBufferData(34963, buffer, 35048);
      GlStateManager._glBindBuffer(34963, n);
   }

   public static void uploadMatrix(Matrix4f matrix) {
      UniformBufferObject uniformBufferObject = ShaderFactory.getUniformBufferObject("matrix");
      if (modelView == null) {
         modelView = MemoryUtil.memAllocFloat(64);
      }

      if (proj == null) {
         proj = MemoryUtil.memAllocFloat(8);
         timestamp = MemoryUtil.memAddress0(proj);
      }

      matrix.get(modelView);
      MemoryUtil.memPutFloat(timestamp, IMinecraft.mc.getFramebuffer().textureWidth);
      MemoryUtil.memPutFloat(timestamp + 4L, IMinecraft.mc.getFramebuffer().textureHeight);
      uniformBufferObject.upload(0L, 64L, MemoryUtil.memAddress0(modelView));
      uniformBufferObject.upload(128L, 8L, timestamp);
   }
}
