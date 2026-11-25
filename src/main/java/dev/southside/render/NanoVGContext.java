package dev.southside.render;

import org.lwjgl.nanovg.NanoVGGL3;

public class NanoVGContext {
   public static long timestamp;

   public static void delete() {
      NanoVGGL3.nvgDelete(timestamp);
   }
}
