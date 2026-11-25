package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.module.impl.client.Renders;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30;

public class MSAA extends Framebuffer {
   public static final int DEFAULT_INT = GL30.glGetInteger(36183);
   private static final Map<Integer, MSAA> INSTANCES = new HashMap<>();
   private static final List<MSAA> ACTIVE_INSTANCES = new ArrayList<>();
   @SerializedName("samples")
   public final int samples;
   @SerializedName("colorRenderbuffer")
   public int colorRenderbuffer;
   @SerializedName("depthRenderbuffer")
   public int depthRenderbuffer;
   @SerializedName("active")
   public boolean active;

   public static boolean framebufferInUse() {
      return !ACTIVE_INSTANCES.isEmpty();
   }

   public void initFbo(int n, int n2) {
      RenderSystem.assertOnRenderThreadOrInit();
      int n3 = RenderSystem.maxSupportedTextureSize();
      if (n > 0 && n <= n3 && n2 > 0 && n2 <= n3) {
         this.viewportWidth = n;
         short s = 23067;
         this.viewportHeight = n2;
         long l = 1572599713978640400L;
         this.textureWidth = n;
         this.textureHeight = n2;
         this.fbo = GlStateManager.glGenFramebuffers();
         GlStateManager._glBindFramebuffer(36160, this.fbo);
         s = 15084;
         this.colorRenderbuffer = GlStateManager.glGenRenderbuffers();
         l = 9145834521180571887L;
         GlStateManager._glBindRenderbuffer(36161, this.colorRenderbuffer);
         GL30.glRenderbufferStorageMultisample(36161, this.samples, 32856, n, n2);
         GlStateManager._glBindRenderbuffer(36161, 0);
         s = 17639;
         this.depthRenderbuffer = GlStateManager.glGenRenderbuffers();
         l = 4857199036044932847L;
         GlStateManager._glBindRenderbuffer(36161, this.depthRenderbuffer);
         GL30.glRenderbufferStorageMultisample(36161, this.samples, 6402, n, n2);
         GlStateManager._glBindRenderbuffer(36161, 0);
         GL30.glFramebufferRenderbuffer(36160, 36064, 36161, this.colorRenderbuffer);
         GL30.glFramebufferRenderbuffer(36160, 36096, 36161, this.depthRenderbuffer);
         this.colorAttachment = MinecraftClient.getInstance().getFramebuffer().getColorAttachment();
         this.depthAttachment = MinecraftClient.getInstance().getFramebuffer().getDepthAttachment();
         this.checkFramebufferStatus();
         this.clear();
         this.endRead();
      } else {
         int n4 = n3;
         int n5 = n2;
         int n6 = n;
         throw new IllegalArgumentException("Window " + n6 + "x" + n5 + " size out of bounds (max. size: " + n4 + ")");
      }
   }

   public static void runTask(Runnable action) {
      if ((Boolean)Renders.INSTANCE.msaa.getValue()) {
         use(Math.min(16, DEFAULT_INT), MinecraftClient.getInstance().getFramebuffer(), action);
      } else {
         action.run();
      }
   }

   public void endWrite() {
      super.endWrite();
      if (this.active) {
         this.active = false;
         ACTIVE_INSTANCES.remove(this);
      }
   }

   public MSAA(int n) {
      super(true);
      if (n < 2 || n > DEFAULT_INT) {
         throw new IllegalArgumentException(String.format("The number of samples should be >= %s and <= %s, got %s.", 2, DEFAULT_INT, n));
      }

      if ((n & n - 1) != 0) {
         throw new IllegalArgumentException("The number of samples must be a power of two.");
      }

      this.samples = n;
      this.setClearColor(1.0F, 1.0F, 1.0F, 0.0F);
   }

   public void beginWrite(boolean flag) {
      super.beginWrite(flag);
      if (!this.active) {
         ACTIVE_INSTANCES.add(this);
         this.active = true;
      }
   }

   public void resize(int n, int n2) {
      if (this.textureWidth != n || this.textureHeight != n2) {
         super.resize(n, n2);
      }
   }

   public void delete() {
      RenderSystem.assertOnRenderThreadOrInit();
      this.endRead();
      this.endWrite();
      if (this.fbo > -1) {
         GlStateManager._glBindFramebuffer(36160, 0);
         GlStateManager._glDeleteFramebuffers(this.fbo);
         this.fbo = -1;
      }

      if (this.colorRenderbuffer > -1) {
         GlStateManager._glDeleteRenderbuffers(this.colorRenderbuffer);
         this.colorRenderbuffer = -1;
      }

      if (this.depthRenderbuffer > -1) {
         GlStateManager._glDeleteRenderbuffers(this.depthRenderbuffer);
         this.depthRenderbuffer = -1;
      }

      this.colorAttachment = -1;
      this.depthAttachment = -1;
      this.textureWidth = -1;
      this.textureHeight = -1;
   }

   public static MSAA getMsaa(int n) {
      return INSTANCES.computeIfAbsent(n, MSAA::new);
   }

   public static void use(int n, Framebuffer framebuffer, Runnable action) {
      short s = 21501;
      RenderSystem.assertOnRenderThreadOrInit();
      long l = 1580257383180820964L;
      MSAA msaa = getMsaa(n);
      msaa.resize(framebuffer.textureWidth, framebuffer.textureHeight);
      GlStateManager._glBindFramebuffer(36008, framebuffer.fbo);
      GlStateManager._glBindFramebuffer(36009, msaa.fbo);
      GlStateManager._glBlitFrameBuffer(0, 0, msaa.textureWidth, msaa.textureHeight, 0, 0, msaa.textureWidth, msaa.textureHeight, 16384, 9729);
      s = 28531;
      msaa.beginWrite(true);
      l = 8668578733318167933L;
      action.run();
      msaa.endWrite();
      GlStateManager._glBindFramebuffer(36008, msaa.fbo);
      GlStateManager._glBindFramebuffer(36009, framebuffer.fbo);
      GlStateManager._glBlitFrameBuffer(0, 0, msaa.textureWidth, msaa.textureHeight, 0, 0, msaa.textureWidth, msaa.textureHeight, 16384, 9729);
      msaa.clear();
      framebuffer.beginWrite(false);
   }
}
