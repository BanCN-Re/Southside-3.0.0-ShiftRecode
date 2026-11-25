package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.ScreenEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.KeyBindHandler;
import dev.southside.utils.NanoTimer;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.PlayerInput;

public class InventoryMove extends Module {
   @SerializedName("clickQueue")
   private final LinkedBlockingQueue<Packet<?>> clickQueue = new LinkedBlockingQueue<>();
   @SerializedName("screenCloseTimer")
   private final NanoTimer screenCloseTimer = new NanoTimer();
   @SerializedName("sprintRestoreTimer")
   private final NanoTimer sprintRestoreTimer = new NanoTimer();
   @SerializedName("inHandledScreen")
   private boolean inHandledScreen;
   @SerializedName("closeIntercepted")
   private boolean closeIntercepted;
   @SerializedName("closePending")
   private boolean closePending;
   @SerializedName("interceptInputPackets")
   private boolean interceptInputPackets;
   @SerializedName("wasSprintingOnClose")
   private boolean wasSprintingOnClose;
   @SerializedName("sprintKeyHeldOnClose")
   private boolean sprintKeyHeldOnClose;

   public InventoryMove() {
      super(Category.Movement, "InventoryMove", "在物品栏中移动");
   }

   @Override
   public void onEnable() {
      this.performActionDirect();
   }

   @Override
   public void onDisable() {
      this.performActionAlt();
      this.performActionDirect();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.performActionDirect();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (mc2.player != null && mc2.currentScreen instanceof HandledScreen) {
         this.setPressed(mc2.options.forwardKey);
         this.setPressed(mc2.options.backKey);
         this.setPressed(mc2.options.leftKey);
         this.setPressed(mc2.options.rightKey);
         this.setPressed(mc2.options.jumpKey);
         this.setPressed(mc2.options.sprintKey);
         this.setPressed(mc2.options.useKey);
      }
   }

   @EventTarget(priority = 200)
   public void onX(XEvent xevent) {
      if (mc2.currentScreen != null && !(mc2.currentScreen instanceof ChatScreen)) {
         if (xevent.getPhase() == Stage.PRE) {
            boolean flag = mc2.currentScreen instanceof HandledScreen;
            if (this.inHandledScreen && !flag) {
               this.inHandledScreen = false;
               this.interceptInputPackets = false;
            }

            if (flag) {
               this.inHandledScreen = true;
               this.closeIntercepted = true;
               this.interceptInputPackets = true;
            }
         } else if (xevent.getPhase() == Stage.POST && this.closeIntercepted && this.closePending && this.screenCloseTimer.hasTimePassed(100L)) {
            this.closePending = false;
            this.closeIntercepted = false;
            mc2.setScreen(null);
            this.performActionImpl();
            this.performActionAlt();
            this.performActionDirect();
         }
      }
   }

   @EventTarget
   public void onScreen(ScreenEvent screenEvent) {
      if (mc2.player != null && mc2.currentScreen instanceof HandledScreen && this.closeIntercepted && screenEvent.screen == null) {
         this.sprintKeyHeldOnClose = KeyBindHandler.isValid(mc2.options.sprintKey);
         this.wasSprintingOnClose = mc2.player.isSprinting() || this.sprintKeyHeldOnClose;
         mc2.player.setSprinting(false);
         mc2.options.sprintKey.setPressed(false);
         mc2.options.useKey.setPressed(false);
         screenEvent.setCancelled(true);
         this.screenCloseTimer.reset();
         this.closePending = true;
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getDirection() == TransferOrigin.SEND) {
         Packet packet = directionEvent.getPacket();
         if (!this.inHandledScreen || !(packet instanceof ClickSlotC2SPacket) && !(packet instanceof CloseHandledScreenC2SPacket)) {
            if (this.interceptInputPackets && packet instanceof PlayerInputC2SPacket playerInputC2SPacket) {
               PlayerInput playerInput = playerInputC2SPacket.input();
               directionEvent.setCancelled(true);
               PacketUtils.sendPacketDirect(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, playerInput.jump(), playerInput.sneak(), playerInput.sprint())));
            }
         } else {
            directionEvent.setCancelled(true);
            this.clickQueue.add(packet);
         }
      }
   }

   private void performActionAlt() {
      this.performActionImpl();
      boolean flag = false;

      Packet packet;
      while ((packet = this.clickQueue.poll()) != null) {
         if (packet instanceof CloseHandledScreenC2SPacket) {
            this.performActionImpl();
            flag = true;
         }

         PacketUtils.sendPacketDirect(packet);
      }

      if (flag && this.wasSprintingOnClose && mc2.player != null && this.sprintRestoreTimer.hasTimePassed(50L)) {
         this.sprintRestoreTimer.reset();
         if (this.isValidEx()) {
            mc2.player.setSprinting(true);
         }

         if (this.sprintKeyHeldOnClose) {
            mc2.options.sprintKey.setPressed(true);
         }
      }

      this.interceptInputPackets = false;
      this.setPressed(mc2.options.useKey);
   }

   private boolean isValidEx() {
      return mc2.player != null
         && !mc2.player.isSneaking()
         && !mc2.player.isUsingItem()
         && !mc2.player.horizontalCollision
         && mc2.player.input.movementForward > 0.0F
         && KeyBindHandler.isValid(mc2.options.forwardKey);
   }

   private void performActionImpl() {
      PacketUtils.sendPacketDirect(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
   }

   private void setPressed(KeyBinding keyBinding) {
      keyBinding.setPressed(KeyBindHandler.isValid(keyBinding));
   }

   private void performActionDirect() {
      this.inHandledScreen = false;
      this.closeIntercepted = false;
      this.closePending = false;
      this.interceptInputPackets = false;
      this.wasSprintingOnClose = false;
      this.sprintKeyHeldOnClose = false;
      this.sprintRestoreTimer.reset();
      this.screenCloseTimer.reset();
      this.clickQueue.clear();
   }
}
