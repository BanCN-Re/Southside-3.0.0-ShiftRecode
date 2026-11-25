package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ItemUseEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.PacketUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FuckOffHand extends Module {
   @SerializedName("pendingSwapBack")
   private boolean pendingSwapBack;

   public FuckOffHand() {
      super(Category.Misc, "FuckOffHand");
   }

   private static boolean isValidItem(ItemStack stack) {
      return stack != null && (stack.isOf(Items.SNOWBALL) || stack.isOf(Items.EGG));
   }

   @EventTarget
   public void onHandEvent2(ItemUseEvent itemUseEvent) {
      if (mc2.player != null
         && mc2.interactionManager != null
         && mc2.crosshairTarget != null
         && mc2.crosshairTarget.getType() == Type.MISS
         && isValidItem(mc2.player.getOffHandStack())
         && !itemUseEvent.getItemStack().isOf(Items.FIRE_CHARGE)) {
         if (itemUseEvent.getHand() == Hand.MAIN_HAND && !isValidItem(itemUseEvent.getItemStack())) {
            if (!(itemUseEvent.getItemStack().copy().getItem().use(mc2.world, mc2.player, Hand.MAIN_HAND) instanceof Success)) {
               itemUseEvent.cancel();
            }
         } else {
            if (itemUseEvent.getHand() == Hand.OFF_HAND && isValidItem(itemUseEvent.getItemStack())) {
               itemUseEvent.cancel();
               this.reset();
            }

            this.pendingSwapBack = true;
         }
      }
   }

   private void reset() {
      performActionAlt();
      ItemStack stack = mc2.player.getMainHandStack().copy();
      mc2.player.setStackInHand(Hand.MAIN_HAND, mc2.player.getOffHandStack());
      mc2.player.setStackInHand(Hand.OFF_HAND, stack);
      if (mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND) instanceof Success success) {
         if (success.swingSource() == SwingSource.CLIENT) {
            mc2.player.swingHand(Hand.MAIN_HAND);
         }

         mc2.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
      }
   }

   private static void performActionAlt() {
      PacketUtils.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE && this.pendingSwapBack) {
         this.pendingSwapBack = false;
         if (mc2.player != null && !isValidItem(mc2.player.getOffHandStack())) {
            performActionAlt();
         }
      }
   }
}
