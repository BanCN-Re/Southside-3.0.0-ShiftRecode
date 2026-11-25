package dev.southside.utils;

import dev.southside.render.UIMatrixStack;

public interface IGLPlatform {
   default float computeFloat(String text, float f, float f2, int n, boolean flag) {
      return this.computeFloat(null, text, f, f2, n, flag);
   }

   float computeFloat(UIMatrixStack matrices, String text, float f, float f2, int n, boolean flag);

   int getWidth(String text);

   void render(UIMatrixStack matrices, float f, float f2, float f3, float f4, int n);

   void runTask(Runnable action);
}
