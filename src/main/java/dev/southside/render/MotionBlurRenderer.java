package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

public final class MotionBlurRenderer {
   @SerializedName("history")
   private Framebuffer history;
   @SerializedName("world")
   private ClientWorld world;
   @SerializedName("camera")
   private Entity camera;
   @SerializedName("perspective")
   private Perspective perspective;
   @SerializedName("lastFrame")
   private long lastFrame;
   private static final long DEFAULT_LONG = 250000000L;

   public void performAction(Framebuffer framebuffer, ClientWorld world2, Entity entity, Perspective perspective2, double d) {
      int textureWidth = framebuffer.textureWidth;
      int textureHeight = framebuffer.textureHeight;
      if (textureWidth > 0 && textureHeight > 0) {
         long now = System.nanoTime();
         long l = now - this.lastFrame;
         boolean flag = this.history != null && this.history.textureWidth == textureWidth && this.history.textureHeight == textureHeight;
         boolean flag2 = flag && this.lastFrame != 0L && l > 0L && l <= DEFAULT_LONG && this.world == world2 && this.camera == entity && this.perspective == perspective2;
         GlStateTracker.pushState();

         try {
            MemoryStack stack = MemoryStack.stackPush();

            try {
               try {
                  RenderSystem.disableScissor();
                  if (!flag) {
                     if (this.history == null) {
                        this.history = new SimpleFramebuffer(textureWidth, textureHeight, false);
                     } else {
                        this.history.resize(textureWidth, textureHeight);
                     }
                  }

                  if (flag2) {
                     framebuffer.beginWrite(true);
                     this.render((float)Math.exp(-Math.log(2.0) * l / (d * 1000000.0)), stack);
                  }

                  GlStateManager._glBindFramebuffer(36008, framebuffer.fbo);
                  GlStateManager._glBindFramebuffer(36009, this.history.fbo);
                  GlStateManager._glBlitFrameBuffer(0, 0, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight, 16384, 9728);
                  this.world = world2;
                  this.camera = entity;
                  this.perspective = perspective2;
                  this.lastFrame = now;
               } finally {
                  framebuffer.beginWrite(true);
               }
            } catch (Throwable e) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable e2) {
                     e.addSuppressed(e2);
                  }
               }

               throw e;
            }

            if (stack != null) {
               stack.close();
            }
         } finally {
            GlStateTracker.performAction();
         }
      } else {
         this.performAction();
      }
   }

   private void render(float f, MemoryStack stack) {
      ShaderProgram shader = RenderSystem.getShader();
      ShaderProgram program = RenderSystem.setShader(ShaderProgramKeys.BLIT_SCREEN);
      if (program == null) {
         RenderSystem.setShader(shader);
      } else {
         FloatBuffer buffer = stack.mallocFloat(4);
         GL11C.glGetFloatv(32773, buffer);
         int n = GL11C.glGetInteger(32969);
         int n2 = GL11C.glGetInteger(32968);
         int n3 = GL11C.glGetInteger(32971);
         int n4 = GL11C.glGetInteger(32970);
         int n5 = GL11C.glGetInteger(32777);
         int n6 = GL11C.glGetInteger(34877);

         try {
            RenderSystem.disableDepthTest();
            short s = 13009;
            RenderSystem.disableCull();
            long l = 6398185423629643972L;
            RenderSystem.enableBlend();
            GL20C.glBlendEquationSeparate(32774, 32774);
            s = 29839;
            GL14C.glBlendColor(0.0F, 0.0F, 0.0F, f);
            l = 6813564563544159896L;
            RenderSystem.blendFuncSeparate(32771, 32772, 0, 1);
            program.addSamplerTexture("InSampler", this.history.getColorAttachment());
            BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.BLIT_SCREEN);
            bufferBuilder.vertex(0.0F, 0.0F, 0.0F);
            bufferBuilder.vertex(1.0F, 0.0F, 0.0F);
            bufferBuilder.vertex(1.0F, 1.0F, 0.0F);
            bufferBuilder.vertex(0.0F, 1.0F, 0.0F);
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
         } finally {
            RenderSystem.blendFuncSeparate(n, n2, n3, n4);
            GL20C.glBlendEquationSeparate(n5, n6);
            GL14C.glBlendColor(buffer.get(0), buffer.get(1), buffer.get(2), buffer.get(3));
            RenderSystem.setShader(shader);
         }
      }
   }

   public void performAction() {
      this.world = null;
      this.camera = null;
      this.lastFrame = 0L;
   }

   public boolean isValid() {
      this.performAction();
      if (this.history == null) {
         return false;
      }

      this.history.delete();
      this.history = null;
      return true;
   }
}
