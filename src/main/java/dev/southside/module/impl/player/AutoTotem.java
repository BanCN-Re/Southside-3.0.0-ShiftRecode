package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NumberValue;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoTotem extends Module {
   @SerializedName("health")
   public final NumberValue health = new NumberValue("Health", 3, 0, 20);
   @SerializedName("previousSlot")
   private int previousSlot = -1;
   @SerializedName("swapPending")
   private boolean swapPending;

   public AutoTotem() {
      super(Category.Player, "AutoTotem");
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (mc2.player != null && mc2.interactionManager != null) {
         if (this.isValidAlt()) {
            if (this.previousSlot != -1) {
               mc2.player.getInventory().selectedSlot = this.previousSlot;
               this.previousSlot = -1;
            }

            this.swapPending = false;
         } else {
            int n = this.isValidEx() ? this.computeInt() : -1;
            if (n == -1) {
               this.performActionAlt();
            } else {
               this.onKeyTyped(n);
            }
         }
      }
   }

   private boolean isValidEx() {
      float f = mc2.player.hasStatusEffect(StatusEffects.ABSORPTION) ? mc2.player.getAbsorptionAmount() : 0.0F;
      return mc2.player.getHealth() + f <= ((Number)this.health.getValue()).doubleValue() || mc2.player.fallDistance > 10.0F && !mc2.player.isGliding();
   }

   private int computeInt() {
      if (mc2.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
         return mc2.player.getInventory().selectedSlot;
      }

      for (int i = 0; i < 9; i++) {
         if (mc2.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
            return i;
         }
      }

      return -1;
   }

   private void onKeyTyped(int n) {
      if (this.previousSlot == -1) {
         this.previousSlot = mc2.player.getInventory().selectedSlot;
      }

      if (mc2.player.getInventory().selectedSlot != n) {
         mc2.player.getInventory().selectedSlot = n;
         mc2.interactionManager.syncSelectedSlot();
      }

      if (!this.swapPending && mc2.player.getInventory().selectedSlot == n) {
         this.swapPending = true;
         PacketUtils.sendPacketDirect(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
      }
   }

   private boolean isValidAlt() {
      return mc2.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
   }

   private void performActionAlt() {
      if (!this.isValidAlt()) {
         this.swapPending = false;
      }

      this.previousSlot = -1;
   }
}
