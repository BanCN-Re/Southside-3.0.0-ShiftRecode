package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.ServerPacketManager;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class GrimFly extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", new String[]{"1平", "2随意", "3一直"}, "Mode", 1, 0);
   @SerializedName("velocityHUD")
   public final BooleanValue velocityHUD = new BooleanValue("Velocity HUD", "Velocity HUD", true);
   @SerializedName("selectMaxVelocity")
   public final BooleanValue selectMaxVelocity = new BooleanValue("Select Max Velocity", "Select Max Velocity", false);
   @SerializedName("tickCounter")
   public int tickCounter;
   @SerializedName("pendingPings")
   private final LinkedBlockingQueue<Packet<?>> pendingPings = new LinkedBlockingQueue<>();
   @SerializedName("shouldFly")
   private boolean shouldFly;
   @SerializedName("reachedPeak")
   private boolean reachedPeak;
   @SerializedName("peakY")
   private double peakY;
   @SerializedName("wasCustomHeightMode")
   private boolean wasCustomHeightMode;
   @SerializedName("jumpWasPressed")
   private boolean jumpWasPressed;
   @SerializedName("sneakWasPressed")
   private boolean sneakWasPressed;
   @SerializedName("waitingForDescent")
   private boolean waitingForDescent;
   @SerializedName("maxJumpY")
   private double maxJumpY;
   @SerializedName("risingPhase")
   private boolean risingPhase;

   public GrimFly() {
      super(Category.Movement, "GrimFly", "Grim CNMBBB");
   }

   @Override
   public void onEnable() {
      this.update();
   }

   @Override
   public void onDisable() {
      this.releasePackets();
      this.update();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.update();
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (mc2.player != null && directionEvent.getDirection() == TransferOrigin.RECEIVE && !directionEvent.isCancelled()) {
         if (directionEvent.getPacket() instanceof CommonPingS2CPacket) {
            directionEvent.setCancelled(true);
            this.pendingPings.add(directionEvent.getPacket());
         } else if (directionEvent.getPacket() instanceof PlayerPositionLookS2CPacket playerPositionLookS2CPacket) {
            if (this.mode.matches("2随意") && !this.reachedPeak) {
               this.peakY = PlayerPosition.apply(PlayerPosition.fromEntity(mc2.player), playerPositionLookS2CPacket.change(), playerPositionLookS2CPacket.relatives()).position().y;
               this.reachedPeak = true;
            }

            this.releasePackets();
         }
      }
   }

   private void releasePackets() {
      Packet packet;
      while ((packet = this.pendingPings.poll()) != null) {
         if (!ServerPacketManager.shouldHandlePacket(packet)) {
            PacketUtils.sendOrApplyPacket(packet);
         }
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (mc2.player != null && (Boolean)this.velocityHUD.getValue()) {
         Vec3d velocity = mc2.player.getVelocity();
         String formatted = String.format(Locale.US, "x: %.3f y: %.3f z: %.3f", velocity.x, velocity.y, velocity.z);
         int n = (mc2.getWindow().getScaledWidth() - mc2.textRenderer.getWidth(formatted)) / 2;
         int n2 = mc2.getWindow().getScaledHeight() / 2 + 30;
         drawContextEvent.getDrawContext().drawText(mc2.textRenderer, formatted, n, n2, 16777215, true);
      }
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (mc2.player != null && mc2.getNetworkHandler() != null && xevent.getPhase() != Stage.POST) {
         boolean flag = this.mode.matches("2随意");
         if (flag && !this.wasCustomHeightMode) {
            this.performActionImpl();
         }

         switch (this.mode.getDisplayTextEx()) {
            case "1平":
               if (Math.abs(mc2.player.getY() % 1.0 - 0.24919) < 1.0E-4) {
                  this.shouldFly = true;
               }
               break;
            case "2随意":
               this.performActionAlt();
               break;
            case "3一直":
               this.shouldFly = true;
         }

         if (mc2.options.sneakKey.isPressed()) {
            this.shouldFly = false;
         }

         this.wasCustomHeightMode = flag;
         if (++this.tickCounter % 2 == 0 && this.shouldFly) {
            mc2.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc2.player, Mode.START_FALL_FLYING));
         }
      }
   }

   private void performActionAlt() {
      boolean isPressed = mc2.options.jumpKey.isPressed();
      boolean isPressed2 = mc2.options.sneakKey.isPressed();
      double y = mc2.player.getY();
      if ((Boolean)this.selectMaxVelocity.getValue()) {
         if (isPressed && !this.jumpWasPressed) {
            this.maxJumpY = y;
            this.risingPhase = false;
         }

         if (isPressed) {
            if (!this.risingPhase && Math.abs(mc2.player.getVelocity().y) >= 0.4) {
               this.risingPhase = true;
            }

            if (this.risingPhase) {
               this.maxJumpY = Math.max(this.maxJumpY, y);
               if (y > this.peakY) {
                  this.peakY = y;
                  this.reachedPeak = true;
               }
            }
         }

         if (!isPressed && this.jumpWasPressed && this.risingPhase) {
            this.waitingForDescent = true;
         }

         if (this.waitingForDescent) {
            this.maxJumpY = Math.max(this.maxJumpY, y);
            if (mc2.player.getVelocity().y <= 0.0) {
               this.peakY = this.maxJumpY;
               this.reachedPeak = true;
               this.waitingForDescent = false;
            }
         }
      } else if (isPressed && y > this.peakY || !isPressed && this.jumpWasPressed) {
         this.peakY = y;
         this.reachedPeak = true;
      }

      if (!isPressed2 && this.sneakWasPressed) {
         this.peakY = y;
         this.reachedPeak = true;
         this.waitingForDescent = false;
      }

      this.shouldFly = isPressed || this.reachedPeak && !this.waitingForDescent && y <= this.peakY;
      this.jumpWasPressed = isPressed;
      this.sneakWasPressed = isPressed2;
   }

   private void update() {
      this.shouldFly = false;
      this.tickCounter = 0;
      this.wasCustomHeightMode = false;
      this.performActionImpl();
   }

   private void performActionImpl() {
      this.reachedPeak = false;
      this.peakY = 0.0;
      this.jumpWasPressed = false;
      this.sneakWasPressed = false;
      this.waitingForDescent = false;
      this.maxJumpY = 0.0;
      this.risingPhase = false;
   }
}
