package dev.southside.render;

import org.lwjgl.nanovg.NanoVG;

public final class SquirclePath {
   private static final float DEFAULT_FLOAT = 1.25F;
   private static final float DEFAULT_FLOAT_VALUE = 0.1591036F;
   private static final float DEFAULT_FLOAT_DATA = 0.556F;

   private SquirclePath() {
   }

   public static void draw(long vg, float x, float y, float width, float height, float f) {
      draw(vg, x, y, width, height, f, f, f, f);
   }

   public static void draw(long vg, float x, float y, float width, float height, float f, float radius, float f2, float f3) {
      if (!(width <= 0.0F) && !(height <= 0.0F)) {
         float f4 = Math.min(width, height) * 0.5F;
         float f5 = computeFloat(f, f4);
         float f6 = computeFloat(radius, f4);
         float f7 = computeFloat(f2, f4);
         float f8 = computeFloat(f3, f4);
         if (f5 == 0.0F && f6 == 0.0F && f7 == 0.0F && f8 == 0.0F) {
            NanoVG.nvgRect(vg, x, y, width, height);
         } else {
            float f9 = x + width;
            float f10 = y + height;
            NanoVG.nvgMoveTo(vg, x, y + f5);
            draw(vg, x, y, 0.0F, f5, f5, 0.0F);
            NanoVG.nvgLineTo(vg, f9 - f6, y);
            draw(vg, f9, y, -f6, 0.0F, 0.0F, f6);
            NanoVG.nvgLineTo(vg, f9, f10 - f7);
            draw(vg, f9, f10, 0.0F, -f7, -f7, 0.0F);
            NanoVG.nvgLineTo(vg, x + f8, f10);
            draw(vg, x, f10, f8, 0.0F, 0.0F, -f8);
            NanoVG.nvgClosePath(vg);
         }
      }
   }

   private static float computeFloat(float x, float y) {
      return Math.min(Math.max(0.0F, x) * 1.25F, y);
   }

   private static void draw(long vg, float x, float y, float width, float height, float f, float radius) {
      if (width != 0.0F || height != 0.0F) {
         NanoVG.nvgBezierTo(
            vg,
            x + width * 0.556F,
            y + height * 0.556F,
            x + width * 0.3182072F,
            y + height * 0.3182072F,
            x + (width + f) * 0.1591036F,
            y + (height + radius) * 0.1591036F
         );
         NanoVG.nvgBezierTo(vg, x + f * 0.3182072F, y + radius * 0.3182072F, x + f * 0.556F, y + radius * 0.556F, x + f, y + radius);
      }
   }
}
