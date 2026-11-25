package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.player.InvManager;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.IText;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoGApple extends Module {
   public static boolean activeValue;
   @SerializedName("awaitingAppleSwap")
   private boolean awaitingAppleSwap;
   @SerializedName("pendingOffhandSwap")
   private boolean pendingOffhandSwap;

   public AutoGApple() {
      super(Category.Misc, "AutoGApple", "Retard");
   }

   @Override
   public void onEnable() {
      this.pendingOffhandSwap = true;
      activeValue = false;
      this.awaitingAppleSwap = false;
   }

   @Override
   public void onDisable() {
      activeValue = false;
      this.awaitingAppleSwap = false;
      this.pendingOffhandSwap = false;
   }

   private static PlayerInteractItemC2SPacket getPlayerInteractItemC2SPacket(int n) {
      Rotation activeRotation = RotationManager.getActiveRotation();
      return new PlayerInteractItemC2SPacket(Hand.OFF_HAND, n, activeRotation.yaw, activeRotation.pitch);
   }

   private static void performActionAlt() {
      PacketUtils.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE && mc2.player != null && mc2.interactionManager != null) {
         if (this.pendingOffhandSwap) {
            this.pendingOffhandSwap = false;
            short s = 28523;
            performActionAlt();
            short s2 = -11096;
            IText.applyText("1swap");
            this.awaitingAppleSwap = true;
         }

         if (this.awaitingAppleSwap && mc2.player.getMainHandStack().isOf(Items.GOLDEN_APPLE)) {
            this.awaitingAppleSwap = false;
            short s3 = 28522;
            ServerPacketManager.performAction();
            short s4 = -6121;
            performActionAlt();
            IText.applyText("2swap");
            activeValue = true;
            PacketUtils.sendSequencedPacket(AutoGApple::getPlayerInteractItemC2SPacket);
            KillAura.lastTimeUtil.reset();
         }

         if (activeValue) {
            InvManager.lastTimeUtil.reset();
         }
      }
   }
}
