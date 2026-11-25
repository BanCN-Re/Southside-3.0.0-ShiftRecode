package dev.southside.module.impl.movement;

import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.EnchantmentUtils;
import net.minecraft.enchantment.Enchantments;

public class KeepSprint extends Module {
   public KeepSprint() {
      super(Category.Movement, "KeepSprint");
   }

   public static boolean canKeepSprint() {
      return mc2.player != null
         && Southside.INSTANCE.getModuleManager().getModule(KeepSprint.class).isEnabled()
         && EnchantmentUtils.computeInt(Enchantments.KNOCKBACK, mc2.player.getMainHandStack()) <= 0
         && EnchantmentUtils.computeInt(Enchantments.KNOCKBACK, mc2.player.getOffHandStack()) <= 0;
   }
}
