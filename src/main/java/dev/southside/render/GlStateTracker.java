package dev.southside.render;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.southside.utils.GlStateNode;
import org.lwjgl.opengl.GL11C;

public class GlStateTracker {
   public static final GlStateNode GL_STATE_NODE = new GlStateNode();
   public static GlStateNode glStateNode;

   public static void enableBlend() {
      short s = 6364;
      GlStateManager._enableBlend();
      long l = 7453930718275306546L;
      GlStateManager._blendFunc(770, 771);
   }

   public static void disableCull() {
      GlStateManager._disableCull();
   }

   public static void disableBlend() {
      GlStateManager._disableBlend();
   }

   public static void enableCull() {
      GlStateManager._enableCull();
   }

   public static void pushState() {
      if (glStateNode == null) {
         glStateNode = new GlStateNode();
         GL_STATE_NODE.next = glStateNode;
         glStateNode.previous = GL_STATE_NODE;
      } else {
         glStateNode.next = new GlStateNode();
         glStateNode.next.previous = glStateNode;
         glStateNode = glStateNode.next;
      }

      glStateNode.depthTestEnabled = GL11C.glIsEnabled(2929);
      glStateNode.blendEnabled = GL11C.glIsEnabled(3042);
      glStateNode.cullEnabled = GL11C.glIsEnabled(2884);
      glStateNode.scissorEnabled = GL11C.glIsEnabled(3089);
   }

   public static void performAction() {
      if (glStateNode != null) {
         boolean depthTestEnabled = glStateNode.depthTestEnabled;
         if (depthTestEnabled) {
            GlStateManager._enableDepthTest();
         } else {
            GlStateManager._disableDepthTest();
         }

         depthTestEnabled = glStateNode.blendEnabled;
         if (depthTestEnabled) {
            GlStateManager._enableBlend();
         } else {
            GlStateManager._disableBlend();
         }

         depthTestEnabled = glStateNode.cullEnabled;
         if (depthTestEnabled) {
            GlStateManager._enableCull();
         } else {
            GlStateManager._disableCull();
         }

         depthTestEnabled = glStateNode.scissorEnabled;
         if (depthTestEnabled) {
            GlStateManager._enableScissorTest();
         } else {
            GlStateManager._disableScissorTest();
         }

         glStateNode = glStateNode.previous;
         glStateNode.next = null;
      }
   }

   public static void disableDepthTest() {
      GlStateManager._disableDepthTest();
   }

   public static void enableDepthTest() {
      GlStateManager._enableDepthTest();
   }
}
