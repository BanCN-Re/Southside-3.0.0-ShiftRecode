package dev.southside.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Function;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

public final class MenuSkyRenderer {
   private static final int DEFAULT_INT;
   private static final int DEFAULT_INT_VALUE;
   private static final int DEFAULT_INT_DATA;
   private static final Identifier SUN_ID;
   private static final Function<Identifier, RenderLayer> function;
   private static final int[][][] INT_ARRAY_ARRAY_ARRAY;
   private static final MenuSkyRenderer.Entry[] ENTRY_ARRAY;

   public void renderSky(DrawContext context, int n, int n2, float f) {
      if (n > 0 && n2 > 0) {
         context.draw();
         short s = 12498;
         float[] shaderColor = RenderSystem.getShaderColor();
         long l = 8559029916661369995L;
         float f2 = shaderColor[0];
         float f3 = shaderColor[1];
         float f4 = shaderColor[2];
         float f5 = shaderColor[3];
         boolean flag = GL11.glIsEnabled(3042);
         int n3 = GL11.glGetInteger(32969);
         int n4 = GL11.glGetInteger(32968);
         int n5 = GL11.glGetInteger(32971);
         int n6 = GL11.glGetInteger(32970);
         boolean flag2 = GL11.glIsEnabled(2929);
         boolean flag3 = GL11.glGetBoolean(2930);
         int n7 = GL11.glGetInteger(2932);

         try {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableDepthTest();
            RenderLayer guiOverlay = RenderLayer.getGuiOverlay();
            int n8 = Math.max(1, Math.round(n2 * 0.72F));
            context.fillGradient(guiOverlay, 0, 0, n, n8, -7032634, -3745565, 0);
            context.fillGradient(guiOverlay, 0, n8, n, n2, -3745565, -1971216, 0);
            context.draw();
            drawTexture(context, n, n2);
            context.draw();
            RenderSystem.disableDepthTest();
            RenderSystem.defaultBlendFunc();
            float f6 = Math.max(1.0F, Math.min(n / 96.0F, n2 / 54.0F));
            float f7 = Float.isFinite(f) ? Math.max(0.0F, f) : 0.0F;

            for (MenuSkyRenderer.Entry entry : ENTRY_ARRAY) {
               render(context, guiOverlay, entry, n, n2, f6, f7);
            }
         } finally {
            try {
               context.draw();
            } finally {
               RenderSystem.setShaderColor(f2, f3, f4, f5);
               RenderSystem.blendFuncSeparate(n3, n4, n5, n6);
               if (flag) {
                  RenderSystem.enableBlend();
               } else {
                  RenderSystem.disableBlend();
               }

               RenderSystem.depthMask(flag3);
               RenderSystem.depthFunc(n7);
               if (flag2) {
                  RenderSystem.enableDepthTest();
               } else {
                  RenderSystem.disableDepthTest();
               }
            }
         }
      }
   }

   private static void drawTexture(DrawContext context, int n, int n2) {
      int n3 = Math.max(18, Math.round(Math.min(n * 0.12F, n2 * 0.21F)));
      int n4 = Math.round(n * 0.8F - n3 / 2.0F);
      int n5 = Math.round(n2 * 0.17F - n3 / 2.0F);
      context.drawTexture(function, SUN_ID, n4, n5, 0.0F, 0.0F, n3, n3, 32, 32, 32, 32);
   }

   private static void render(DrawContext context, RenderLayer renderLayer, MenuSkyRenderer.Entry entry, int n, int n2, float f, float f2) {
      float f3 = f * entry.getScale();
      float f4 = f3 * 14.0F;
      float f5 = f3 * 2.0F;
      float f6 = n + f4 + f5 * 2.0F;
      float f7 = f2 * entry.getSpeed() * f * 0.08F;
      float f8 = (entry.getPosition() * f6 + f7) % f6 - f4 - f5;
      float f9 = n2 * entry.getHeight();
      int n3 = Math.max(1, Math.round(f3 * 0.45F));
      boolean flag = entry.getScale() < 0.75F;
      int n4 = flag ? -3088665 : -3417629;
      int n5 = flag ? -2037008 : -1116936;
      int[][] val = INT_ARRAY_ARRAY_ARRAY[entry.getShape()];

      for (int[] values : val) {
         fill(context, renderLayer, values, f8, f9 + n3, f3, n4);
      }

      for (int[] values2 : val) {
         fill(context, renderLayer, values2, f8, f9, f3, n5);
      }
   }

   private static void fill(DrawContext context, RenderLayer renderLayer, int[] values, float f, float f2, float f3, int n) {
      context.fill(
         renderLayer, Math.round(f + values[0] * f3), Math.round(f2 + values[1] * f3), Math.round(f + values[2] * f3), Math.round(f2 + values[3] * f3), n
      );
   }

   static {
      DEFAULT_INT_VALUE = -3745565;
      DEFAULT_INT_DATA = -1971216;
      DEFAULT_INT = -7032634;
      SUN_ID = Identifier.ofVanilla("textures/environment/sun.png");
      function = RenderLayer::getCelestial;
      INT_ARRAY_ARRAY_ARRAY = new int[][][]{
         {{0, 1, 12, 3}, {2, 0, 7, 1}, {7, 3, 11, 4}},
         {{0, 1, 14, 3}, {3, 0, 9, 1}, {1, 3, 6, 4}, {11, 0, 13, 1}},
         {{0, 1, 10, 3}, {1, 0, 5, 1}, {7, 0, 12, 2}, {4, 3, 8, 4}}
      };
      ENTRY_ARRAY = new MenuSkyRenderer.Entry[]{
         new MenuSkyRenderer.Entry(0.12F, 0.12F, 0.9F, 0.85F, 0),
         new MenuSkyRenderer.Entry(0.52F, 0.08F, 1.1F, 0.75F, 1),
         new MenuSkyRenderer.Entry(0.94F, 0.27F, 1.25F, 1.0F, 2),
         new MenuSkyRenderer.Entry(0.31F, 0.28F, 0.85F, 0.65F, 2),
         new MenuSkyRenderer.Entry(0.68F, 0.37F, 0.65F, 0.5F, 0),
         new MenuSkyRenderer.Entry(0.09F, 0.46F, 0.55F, 0.4F, 1),
         new MenuSkyRenderer.Entry(0.99F, 0.49F, 0.45F, 0.35F, 0)
      };
   }


   private record Entry(float position, float height, float scale, float speed, int shape) {
      public float getPosition() {
         return this.position;
      }

      public float getHeight() {
         return this.height;
      }

      public float getScale() {
         return this.scale;
      }

      public float getSpeed() {
         return this.speed;
      }

      public int getShape() {
         return this.shape;
      }
   }
}
