package dev.southside.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HotbarItemFinder {
   public static int computeInt(int n, int n2, Item item) {
      for (int i = n; i < n2; i++) {
         ItemStack stack = IMinecraft.mc.player.getInventory().getStack(i);
         if (stack != null && stack.getItem() == item) {
            return i;
         }
      }

      return -1;
   }
}
