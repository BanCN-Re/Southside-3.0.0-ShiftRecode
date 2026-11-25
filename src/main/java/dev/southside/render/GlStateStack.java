package dev.southside.render;

import java.util.Stack;
import org.lwjgl.opengl.GL30;

public final class GlStateStack {
   private static final int DEFAULT_INT;
   private static final Stack<GlStateSnapshot> stack;

   private GlStateStack() {
   }

   public static void pushState() {
      stack.push(new GlStateSnapshot(DEFAULT_INT));
   }

   public static void popState() {
      if (stack.isEmpty()) {
         throw new IllegalStateException("No state to restore.");
      }

      stack.pop().restore();
   }

   static {
      stack = new Stack<>();
      int[] major = new int[1];
      int[] minor = new int[1];
      GL30.glGetIntegerv(GL30.GL_MAJOR_VERSION, major);
      GL30.glGetIntegerv(GL30.GL_MINOR_VERSION, minor);
      DEFAULT_INT = major[0] * 100 + minor[0] * 10;
   }

}
