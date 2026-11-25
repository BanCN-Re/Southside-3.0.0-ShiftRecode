package dev.southside.utils;

public class RenderContextHolder {
   public static final int DEFAULT_INT;
   public static RenderDrawBuffer renderDrawBuffer;
   public static RenderDrawBuffer renderDrawBufferValue;

   static {
      long l = 3214920485410005724L;
      long l2 = 2587055966778122972L ^ l;
      byte b = -1;
      long l3 = l2;
      DEFAULT_INT = (int)l3;
   }
}
