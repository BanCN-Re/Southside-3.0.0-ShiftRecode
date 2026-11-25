package dev.southside.render;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11C;

public class DepthMaskHelper {
   public static void performAction() {
      GlStateManager._depthMask(false);
      GlStateManager._colorMask(true, true, true, true);
   }

   public static void performActionEx() {
      short s = 30120;
      GlStateManager._depthMask(true);
      long l = 7595613357007017339L;
      GlStateManager._clearDepth(1.0);
      GL11C.glClear(256);
      s = 25150;
      GlStateManager._enableDepthTest();
      l = 3316346148776706793L;
      GlStateManager._depthFunc(519);
      GlStateManager._colorMask(false, false, false, false);
   }

   public static void performActionAlt() {
      short s = 24882;
      GlStateManager._clearDepth(1.0);
      long l = 4418312973744210404L;
      GL11C.glClear(256);
      GlStateManager._depthFunc(515);
      GlStateManager._depthMask(false);
   }

   public static void depthFunc() {
      GL11C.glDepthFunc(515);
   }

   public static void depthFuncEx() {
      GL11C.glDepthFunc(517);
   }

   public static void depthFuncAlt() {
      GL11C.glDepthFunc(514);
   }
}
