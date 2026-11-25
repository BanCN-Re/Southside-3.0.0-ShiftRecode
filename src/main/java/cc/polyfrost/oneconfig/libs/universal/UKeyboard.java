package cc.polyfrost.oneconfig.libs.universal;

import dev.southside.utils.MinecraftAccess;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class UKeyboard {
   public static final int DEFAULT_INT = -100;

   private UKeyboard() {
   }

   public static boolean isKeyDown(int n) {
      if (n == 0 || n == -1) {
         return false;
      } else if (n <= -96) {
         int n2 = n - -100;
         return GLFW.glfwGetMouseButton(MinecraftAccess.getWindowHandle(), n2) == 1;
      } else {
         return InputUtil.isKeyPressed(MinecraftAccess.getWindowHandle(), n);
      }
   }

   public static boolean hasControlDown() {
      return Screen.hasControlDown();
   }

   public static boolean hasShiftDown() {
      return Screen.hasShiftDown();
   }

   public static boolean hasAltDown() {
      return Screen.hasAltDown();
   }

   public static boolean isKeyDownEx(int n) {
      return n == 88 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static String getKeyName(int n, int n2) {
      if (n <= -96) {
         int n3 = n - -100;

         return switch (n3) {
            case 0 -> "Mouse Left";
            case 1 -> "Mouse Right";
            case 2 -> "Mouse Middle";
            default -> "Mouse " + (n3 + 1);
         };
      } else {
         return InputUtil.fromKeyCode(n, n2).getLocalizedText().getString();
      }
   }
}
