package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.event.events.ItemUseEvent;
import dev.southside.event.events.RightClickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult.Fail;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public class ItemUseDispatcher implements IMinecraft {
   public static boolean isValid() {
      RightClickEvent rightClickEvent = new RightClickEvent();
      Southside.INSTANCE.getEventBus().postCancellable(rightClickEvent);
      return rightClickEvent.isCancelled();
   }

   public static void performAction() {
      if (!isValid() && !mc.interactionManager.isBreakingBlock()) {
         mc.itemUseCooldown = 4;
         if (!mc.player.isRiding()) {
            for (Hand hand : Hand.values()) {
               ItemStack stackInHand = mc.player.getStackInHand(hand);
               if (!stackInHand.isItemEnabled(mc.world.getEnabledFeatures())) {
                  return;
               }

               if (mc.crosshairTarget != null) {
                  if (mc.crosshairTarget.getType().equals(Type.ENTITY)) {
                     EntityHitResult crosshairTarget = (EntityHitResult)mc.crosshairTarget;
                     Entity entity = crosshairTarget.getEntity();
                     if (!mc.world.getWorldBorder().contains(entity.getBlockPos())) {
                        return;
                     }

                     ActionResult actionResult = mc.interactionManager.interactEntityAtLocation(mc.player, entity, crosshairTarget, hand);
                     if (!actionResult.isAccepted()) {
                        actionResult = mc.interactionManager.interactEntity(mc.player, entity, hand);
                     }

                     if (actionResult instanceof Success success) {
                        if (success.swingSource() == SwingSource.CLIENT) {
                           mc.player.swingHand(hand);
                        }

                        return;
                     }
                  }

                  if (mc.crosshairTarget.getType().equals(Type.BLOCK)) {
                     BlockHitResult crosshairTarget2 = (BlockHitResult)mc.crosshairTarget;
                     int count = stackInHand.getCount();
                     ActionResult actionResult2 = mc.interactionManager.interactBlock(mc.player, hand, crosshairTarget2);
                     if (actionResult2 instanceof Success success2) {
                        if (success2.swingSource() == SwingSource.CLIENT) {
                           mc.player.swingHand(hand);
                           if (!stackInHand.isEmpty() && (stackInHand.getCount() != count || mc.interactionManager.hasCreativeInventory())) {
                              mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                           }
                        }

                        return;
                     }

                     if (actionResult2 instanceof Fail) {
                        return;
                     }
                  }
               }

               if (!stackInHand.isEmpty() && !isValid(hand, stackInHand) && mc.interactionManager.interactItem(mc.player, hand) instanceof Success success3) {
                  if (success3.swingSource() == SwingSource.CLIENT) {
                     mc.player.swingHand(hand);
                  }

                  mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                  return;
               }
            }
         }
      }
   }

   public static boolean isValid(Hand hand, ItemStack stack) {
      ItemUseEvent itemUseEvent = new ItemUseEvent(hand, stack);
      Southside.INSTANCE.getEventBus().postCancellable(itemUseEvent);
      return itemUseEvent.isCancelled();
   }
}
