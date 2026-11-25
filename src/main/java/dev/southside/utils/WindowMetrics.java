package dev.southside.utils;

public class WindowMetrics {
   public static float getScaleFactor() {
      return (float)MinecraftAccess.getMc().getWindow().getScaleFactor();
   }

   public static int getScaledWidth() {
      return MinecraftAccess.getMc().getWindow().getScaledWidth();
   }

   public static int getScaledHeight() {
      return MinecraftAccess.getMc().getWindow().getScaledHeight();
   }

   public static int getFramebufferWidth() {
      return MinecraftAccess.getMc().getWindow().getFramebufferWidth();
   }

   public static int getFramebufferHeight() {
      return MinecraftAccess.getMc().getWindow().getFramebufferHeight();
   }

   public static int getWindowWidth() {
      return MinecraftAccess.getMc().getWindow().getFramebufferWidth();
   }

   public static int getWindowHeight() {
      return MinecraftAccess.getMc().getWindow().getFramebufferHeight();
   }
}
