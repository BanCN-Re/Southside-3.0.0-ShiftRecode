package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.mixin.accessors.IScroller;
import dev.southside.mixin.misc.MixinMouse;
import dev.southside.utils.MousePositionHelper.Inner;
import org.lwjgl.glfw.GLFW;

public class MousePlatformImpl implements IMousePlatform {
   @SerializedName("lastScrollValue")
   private double lastScrollValue;

   @Override
   public double getCurrentValue() {
      return Inner.getX();
   }

   @Override
   public double getCurrentValueEx() {
      return Inner.getY();
   }

   @Override
   public double getCurrentValueAlt() {
      double cumulVertical = ((IScroller)((MixinMouse)MinecraftAccess.getMc().mouse).getScroller()).getCumulVertical();
      double d = cumulVertical - this.lastScrollValue;
      this.lastScrollValue = cumulVertical;
      return d;
   }

   @Override
   public double getCursorDeltaX() {
      return ((MixinMouse)MinecraftAccess.getMc().mouse).getCursorDeltaX();
   }

   @Override
   public double getCursorDeltaY() {
      return ((MixinMouse)MinecraftAccess.getMc().mouse).getCursorDeltaY();
   }

   @Override
   public int computeInt(int n) {
      return GLFW.glfwGetMouseButton(MinecraftAccess.getWindowHandle(), n);
   }

   @Override
   public boolean isKeyDown(int n) {
      return GLFW.glfwGetMouseButton(MinecraftAccess.getWindowHandle(), n) == 1;
   }
}
