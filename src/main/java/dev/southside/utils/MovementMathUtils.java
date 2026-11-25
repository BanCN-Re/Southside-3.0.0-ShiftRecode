package dev.southside.utils;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.math.Vec3d;

public class MovementMathUtils {
   public static Vec3d computePosition(Vec3d vec, double d, double d2, boolean flag, float f) {
      if (!flag) {
         return new Vec3d(0.0, vec.y, 0.0);
      }

      double d3 = vec.x * (1.0 - d2);
      double d4 = vec.z * (1.0 - d2);
      double d5 = d * d2;
      double d6 = Math.toRadians(f);
      double d7 = -Math.sin(d6) * d5 + d3;
      double d8 = Math.cos(d6) * d5 + d4;
      return new Vec3d(d7, vec.y, d8);
   }

   public static boolean isValid(UseAction useAction) {
      return useAction == UseAction.EAT || useAction == UseAction.DRINK || useAction == UseAction.BOW || useAction == UseAction.CROSSBOW;
   }

   public static boolean isCrossbowItem(ItemStack stack) {
      return stack != null && stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack);
   }

   public static float computeFloat(float f, MovementInputState movementInputState) {
      boolean flag = movementInputState.isForward() && !movementInputState.isBackward();
      boolean flag2 = movementInputState.isBackward() && !movementInputState.isForward();
      boolean flag3 = movementInputState.isLeft() && !movementInputState.isRight();
      boolean flag4 = movementInputState.isRight() && !movementInputState.isLeft();
      float f2 = f;
      float f3 = 1.0F;
      if (flag2) {
         f2 = f + 180.0F;
         f3 = -0.5F;
      } else if (flag) {
         f3 = 0.5F;
      }

      if (flag3) {
         f2 -= 90.0F * f3;
      }

      if (flag4) {
         f2 += 90.0F * f3;
      }

      return f2;
   }

   public static boolean isValid() {
      if (IMinecraft.mc.player == null) {
         return false;
      }

      ItemStack mainHandStack = IMinecraft.mc.player.getMainHandStack();
      ItemStack offHandStack = IMinecraft.mc.player.getOffHandStack();
      return !mainHandStack.isEmpty() && !offHandStack.isEmpty() && (mainHandStack.getItem() == offHandStack.getItem() || isValid(mainHandStack.getUseAction()) && isValid(offHandStack.getUseAction()));
   }

   public static boolean isValidEx(UseAction useAction) {
      return useAction == UseAction.BOW || useAction == UseAction.CROSSBOW || useAction == UseAction.SPEAR;
   }
}
