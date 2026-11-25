package dev.southside.utils;

public final class RenderConstants {
   public static final int DEFAULT_INT = 0;
   public static final int DEFAULT_INT_VALUE;

   private RenderConstants() {
   }

   static {
      long l = 5100365340076933122L;
      DEFAULT_INT_VALUE = (int)l;
   }
}
