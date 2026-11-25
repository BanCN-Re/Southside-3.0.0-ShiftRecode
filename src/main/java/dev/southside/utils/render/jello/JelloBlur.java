package dev.southside.utils.render.jello;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.southside.utils.IMinecraft;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class JelloBlur implements IMinecraft {
   public static ShaderUtil kawaseDown = new ShaderUtil("kawaseDown");
   public static ShaderUtil kawaseUp = new ShaderUtil("kawaseUp");
   public static Framebuffer framebuffer = new SimpleFramebuffer(1, 1, false);
   public static int count;
   public static final List<Framebuffer> framebufferList = new ArrayList<>();

   public static void alphaFunc(float f) {
      GL11.glAlphaFunc(516, (float)(f * 0.01));
   }

   public static boolean isValid(Framebuffer framebuffer2) {
      return framebuffer2 == null || framebuffer2.textureWidth != mc.getWindow().getWidth() || framebuffer2.textureHeight != mc.getWindow().getHeight();
   }

   public static void startBlend() {
      GL11.glDisable(3008);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
   }

   public static void renderFBO(Framebuffer framebuffer2, int n, ShaderUtil shaderUtil, float f) {
      framebuffer2.clear();
      framebuffer2.beginWrite(false);
      shaderUtil.useProgram();
      GlStateManager._bindTexture(n);
      if (shaderUtil == kawaseUp) {
         shaderUtil.setFloat("Radius", f);
      }

      shaderUtil.setFloat("offset", f, f);
      shaderUtil.setInt("inTexture", 0);
      shaderUtil.setInt("check", 0);
      shaderUtil.setFloat("halfpixel", 1.0F / framebuffer2.textureWidth, 1.0F / framebuffer2.textureHeight);
      shaderUtil.setInt("iResolution", framebuffer2.textureWidth, framebuffer2.textureHeight);
      ShaderUtil.drawQuads();
      shaderUtil.useProgramEx();
   }

   public static void setProgress(float f) {
      for (Framebuffer framebuffer2 : framebufferList) {
         framebuffer2.delete();
      }

      framebufferList.clear();
      framebufferList.add(framebuffer = getFramebuffer(null));

      for (int i = 1; i <= f; i++) {
         SimpleFramebuffer simpleFramebuffer = new SimpleFramebuffer(
            (int)(mc.getWindow().getWidth() / Math.pow(2.0, i)), (int)(mc.getWindow().getWidth() / Math.pow(2.0, i)), false
         );
         simpleFramebuffer.setTexFilter(9729);
         short s = 9391;
         GlStateManager._bindTexture(simpleFramebuffer.getColorAttachment());
         long l = 5526991544567696697L;
         GL11.glTexParameteri(3553, 10242, 33648);
         GL11.glTexParameteri(3553, 10243, 33648);
         GlStateManager._bindTexture(0);
         framebufferList.add(simpleFramebuffer);
      }
   }

   public static Framebuffer createFrameBuffer(Framebuffer framebuffer2, boolean flag) {
      if (isValid(framebuffer2) && mc.getWindow().getWidth() != 0 && mc.getWindow().getHeight() != 0) {
         if (framebuffer2 != null) {
            framebuffer2.delete();
         }

         return new SimpleFramebuffer(mc.getWindow().getWidth(), mc.getWindow().getHeight(), flag);
      } else {
         return framebuffer2;
      }
   }

   public static Framebuffer getFramebuffer(Framebuffer framebuffer2) {
      return createFrameBuffer(framebuffer2, false);
   }

   public static void renderRectBlur(float f, float f2, float f3, float f4, int n, int n2, float f5) {
      if (mc.getWindow().getWidth() != 0
         && mc.getWindow().getHeight() != 0
         && (count != n2 || framebuffer.textureWidth != mc.getWindow().getWidth() || framebuffer.textureHeight != mc.getWindow().getHeight())) {
         setProgress(n2);
         count = n2;
      }

      int n3 = (int)(f * mc.getWindow().getScaleFactor());
      int n4 = (int)((int)(mc.getWindow().getHeight() - f4 * mc.getWindow().getScaleFactor()) - f2 * mc.getWindow().getScaleFactor());

      try {
         renderFBO(framebufferList.get(1), mc.getFramebuffer().getColorAttachment(), kawaseDown, f5);
      } catch (IndexOutOfBoundsException e) {
      }

      GL11.glPushMatrix();
      GL11.glEnable(3089);
      GL11.glScissor(n3, n4, (int)(f3 * mc.getWindow().getScaleFactor()), (int)(f4 * mc.getWindow().getScaleFactor()));
      Framebuffer framebuffer2 = framebufferList.get(0);
      framebuffer2.clear();
      framebuffer2.beginWrite(false);
      kawaseUp.useProgram();
      kawaseUp.setFloat("offset", f5, f5);
      kawaseUp.setInt("inTexture", 0);
      kawaseUp.setInt("check", 1);
      kawaseUp.setFloat("Radius", f5);
      kawaseUp.setInt("textureToCheck", 16);
      short s = 26617;
      kawaseUp.setFloat("halfpixel", 1.0F / framebuffer2.textureWidth, 1.0F / framebuffer2.textureHeight);
      long l = 3209770355266309734L;
      kawaseUp.setInt("iResolution", framebuffer2.textureWidth, framebuffer2.textureHeight);
      GL13.glActiveTexture(34000);
      s = 17240;
      GlStateManager._bindTexture(n);
      l = 7137579277656484574L;
      GL13.glActiveTexture(33984);
      s = 23318;
      GlStateManager._bindTexture(framebufferList.get(1).getColorAttachment());
      l = 7179941508975915652L;
      ShaderUtil.drawQuads();
      kawaseUp.useProgramEx();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager._disableBlend();
      GlStateManager._bindTexture(0);
      GL11.glDisable(3089);
      s = 23318;
      GL11.glPopMatrix();
      l = 7179941508975915652L;
      GL11.glPushMatrix();
      GL11.glEnable(3089);
      s = 23318;
      GL11.glScissor(n3, n4, (int)(f3 * mc.getWindow().getScaleFactor()), (int)(f4 * mc.getWindow().getScaleFactor()));
      l = 7179941508975915652L;
      mc.getFramebuffer().beginWrite(true);
      GlStateManager._bindTexture(framebufferList.get(0).getColorAttachment());
      alphaFunc(0.0F);
      startBlend();
      ShaderUtil.drawQuads();
      GlStateManager._bindTexture(0);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager._disableBlend();
      GlStateManager._bindTexture(0);
      GL11.glDisable(3089);
      GL11.glPopMatrix();
   }
}
