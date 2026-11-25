package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.InventoryUtils;
import dev.southside.utils.ElapsedTimer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoSoup extends Module {
   @SerializedName("health")
   public final NumberValue health = new NumberValue("Health", 15, 0, 20, 1);
   @SerializedName("minDelay")
   public final NumberValue minDelay = new NumberValue("Min Delay", 300, 0, 1000, 1);
   @SerializedName("maxDelay")
   public final NumberValue maxDelay = new NumberValue("Max Delay", 500, 0, 1000, 1);
   @SerializedName("autoRefill")
   public final BooleanValue autoRefill = new BooleanValue("Auto Refill", false);
   @SerializedName("useTimer")
   private final ElapsedTimer useTimer = new ElapsedTimer();
   @SerializedName("refillTimer")
   private final ElapsedTimer refillTimer = new ElapsedTimer();
   private static final long n = 50L;

   public AutoSoup() {
      super(Category.Player, "AutoSoup", "自动喝汤");
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (mc2.player != null && mc2.interactionManager != null) {
         if ((Boolean)this.autoRefill.getValue() && !mc2.player.lastSprinting && this.refillTimer.hasElapsed(n)) {
            this.performActionAlt();
            this.refillTimer.reset();
         }

         if (this.useTimer.hasElapsed(this.getDurationMs()) && mc2.player.age > 10 && mc2.player.getHealth() < ((Number)this.health.getValue()).floatValue()) {
            int n2 = this.computeInt();
            if (n2 != -1) {
               this.onRotationApplied(n2);
            }

            this.useTimer.reset();
         }
      }
   }

   private void onRotationApplied(int n2) {
      int selectedSlot = mc2.player.getInventory().selectedSlot;
      mc2.player.getInventory().selectedSlot = n2;

      try {
         if (mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND) instanceof Success success && success.swingSource() == SwingSource.CLIENT) {
            PacketUtils.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
         }

         ItemStack stack = mc2.player.getInventory().getStack(n2);
         if (stack.getItem() == Items.BOWL || stack.getItem() == Items.MUSHROOM_STEW) {
            mc2.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.DROP_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            mc2.player.getInventory().setStack(n2, ItemStack.EMPTY);
         }
      } finally {
         mc2.player.getInventory().selectedSlot = selectedSlot;
         mc2.interactionManager.syncSelectedSlot();
      }
   }

   private void performActionAlt() {
      int n2 = InventoryUtils.computeInt(9, 36, Items.MUSHROOM_STEW);
      if (n2 != -1) {
         for (int i = 0; i < 9; i++) {
            if (mc2.player.getInventory().getStack(i).isEmpty()) {
               mc2.interactionManager.clickSlot(0, n2, i, SlotActionType.SWAP, mc2.player);
               return;
            }
         }
      }
   }

   private int computeInt() {
      for (int i = 0; i < 9; i++) {
         if (mc2.player.getInventory().getStack(i).getItem() == Items.MUSHROOM_STEW) {
            return i;
         }
      }

      return -1;
   }

   private long getDurationMs() {
      double d = ((Number)this.minDelay.getValue()).doubleValue();
      double d2 = ((Number)this.maxDelay.getValue()).doubleValue();
      return (long)(d + Math.random() * (d2 - d));
   }
}
