package dev.southside.utils;

public class MousePositionHelper {
   public static final class Inner {
      public static double getX() {
         return MinecraftAccess.getMc().mouse.getX();
      }

      public static double getY() {
         return MinecraftAccess.getMc().mouse.getY();
      }
   }
}
