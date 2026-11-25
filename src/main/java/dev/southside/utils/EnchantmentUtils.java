package dev.southside.utils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;

public class EnchantmentUtils {
   public static int computeInt(RegistryKey<Enchantment> registryKey, ItemStack stack) {
      return stack == null
         ? 0
         : stack.getEnchantments()
            .getEnchantments()
            .stream()
            .filter(registryEntry -> registryEntry.matchesKey(registryKey))
            .mapToInt(registryEntry2 -> EnchantmentHelper.getLevel(registryEntry2, stack))
            .findFirst()
            .orElse(0);
   }
}
