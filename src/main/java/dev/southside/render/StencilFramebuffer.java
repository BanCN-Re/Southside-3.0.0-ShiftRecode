package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

public class StencilFramebuffer extends Framebuffer {
   public static final StencilFramebuffer INSTANCE = new StencilFramebuffer();
   public static final List<StencilFramebuffer> stencilFramebuffers = new ArrayList<>();
   @SerializedName("depthStencilRenderbuffer")
   public int depthStencilRenderbuffer;
   @SerializedName("active")
   public boolean active;

   public void endWrite() {
      super.endWrite();
      if (this.active) {
         this.active = false;
         stencilFramebuffers.remove(this);
      }
   }

   public void beginWrite(boolean flag) {
      super.beginWrite(flag);
      if (!this.active) {
         stencilFramebuffers.add(this);
         this.active = true;
      }
   }

   public StencilFramebuffer() {
      super(true);
      this.setClearColor(1.0F, 1.0F, 1.0F, 0.0F);
   }

   public static void runTask(Runnable action) {
      performAction(MinecraftClient.getInstance().getFramebuffer(), action);
   }

   public static boolean isValid() {
      return !stencilFramebuffers.isEmpty();
   }

   public static void performAction(Framebuffer framebuffer, Runnable action) {
      RenderSystem.assertOnRenderThreadOrInit();
      StencilFramebuffer stencilFramebuffer = INSTANCE;
      stencilFramebuffer.resize(framebuffer.textureWidth, framebuffer.textureHeight);
      performAction(framebuffer, stencilFramebuffer, 17664);
      stencilFramebuffer.beginWrite(true);
      action.run();
      stencilFramebuffer.endWrite();
      performAction(stencilFramebuffer, framebuffer, 17408);
      stencilFramebuffer.clear();
      framebuffer.beginWrite(false);
   }

   private static void performAction(Framebuffer framebuffer, Framebuffer framebuffer2, int n) {
      GlStateManager._glBindFramebuffer(36008, framebuffer.fbo);
      GlStateManager._glBindFramebuffer(36009, framebuffer2.fbo);
      GL30C.glBlitFramebuffer(0, 0, framebuffer.textureWidth, framebuffer.textureHeight, 0, 0, framebuffer2.textureWidth, framebuffer2.textureHeight, n, 9728);
   }

   public void initFbo(int n, int n2) {
      RenderSystem.assertOnRenderThreadOrInit();
      int n3 = RenderSystem.maxSupportedTextureSize();
      if (n > 0 && n <= n3 && n2 > 0 && n2 <= n3) {
         this.viewportWidth = n;
         short s = 23717;
         this.viewportHeight = n2;
         long l = 1973418208550318036L;
         this.textureWidth = n;
         this.textureHeight = n2;
         this.fbo = GlStateManager.glGenFramebuffers();
         GlStateManager._glBindFramebuffer(36160, this.fbo);
         s = 26169;
         this.colorAttachment = GlStateManager._genTexture();
         l = 7987079074522649950L;
         GlStateManager._bindTexture(this.colorAttachment);
         GlStateManager._texParameter(3553, 10241, 9729);
         GlStateManager._texParameter(3553, 10240, 9729);
         GlStateManager._texImage2D(3553, 0, 32856, n, n2, 0, 6408, 5121, null);
         GL30.glFramebufferTexture2D(36160, 36064, 3553, this.colorAttachment, 0);
         s = 4140;
         this.depthStencilRenderbuffer = GlStateManager.glGenRenderbuffers();
         l = 3885532865858282316L;
         GlStateManager._glBindRenderbuffer(36161, this.depthStencilRenderbuffer);
         GL30.glRenderbufferStorage(36161, 35056, n, n2);
         GL30.glFramebufferRenderbuffer(36160, 33306, 36161, this.depthStencilRenderbuffer);
         GlStateManager._glBindRenderbuffer(36161, 0);
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

   public void delete() {
      RenderSystem.assertOnRenderThreadOrInit();
      this.endRead();
      this.endWrite();
      if (this.fbo > -1) {
         GlStateManager._glBindFramebuffer(36160, 0);
         GlStateManager._glDeleteFramebuffers(this.fbo);
         this.fbo = -1;
      }

      if (this.depthStencilRenderbuffer > -1) {
         GlStateManager._glDeleteRenderbuffers(this.depthStencilRenderbuffer);
         this.depthStencilRenderbuffer = -1;
      }

      if (this.colorAttachment > -1) {
         TextureUtil.releaseTextureId(this.colorAttachment);
         this.colorAttachment = -1;
      }

      this.textureWidth = -1;
      this.textureHeight = -1;
   }
}
