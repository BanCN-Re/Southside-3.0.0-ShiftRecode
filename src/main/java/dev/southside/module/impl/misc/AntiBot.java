package dev.southside.module.impl.misc;

import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class AntiBot extends Module {
   public static final BooleanValue checkShop = new BooleanValue("Check Shop", true);
   public static final BooleanValue checkInvisible = new BooleanValue("Check Invisible", true);
   public static final BooleanValue checkLatestBot = new BooleanValue("Check LatestBot", true);
   private static final List<PlayerEntity> exclude = new ArrayList<>();

   @Override
   public void onDisable() {
      exclude.clear();
   }

   public AntiBot() {
      super(Category.Misc, "AntiBot");
   }

   public static boolean isBot(Entity entity) {
      if (!Southside.INSTANCE.getModuleManager().getModule(AntiBot.class).isEnabled()) {
         return false;
      }

      if (entity instanceof PlayerEntity player) {
         if (player.getGameProfile().getName().isEmpty() && (Boolean)checkShop.getValue()) {
            return true;
         }

         if ((Boolean)checkLatestBot.getValue()) {
            if (exclude.contains(player)) {
               return false;
            }

            boolean flag = true;

            for (ItemStack stack : player.getInventory().armor) {
               if (!stack.isEmpty()) {
                  flag = false;
                  break;
               }
            }

            boolean flag2 = player.getMainHandStack().isEmpty() && player.getOffHandStack().isEmpty();
            if (flag && flag2) {
               float f = Math.abs(MathHelper.wrapDegrees(player.getYaw()));
               if (f <= 0.0F || Math.abs(f - 90.0F) <= 0.0F || Math.abs(f - 180.0F) <= 0.0F) {
                  return true;
               }

               exclude.add(player);
            }
         }

         return !player.isAlive() || player.isInvisible() || player.getHealth() == 0.0F;
      } else {
         return true;
      }
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      exclude.clear();
   }
}
