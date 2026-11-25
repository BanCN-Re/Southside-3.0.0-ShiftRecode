package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.EntityGlowEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.TimerSpeedManager;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.IText;
import dev.southside.utils.NumberValue;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;

public class LongJump2 extends Module {
   @SerializedName("timer")
   public final NumberValue timer = new NumberValue("Timer", 2.0F, 1.0F, 3.0F, 0.05);
   @SerializedName("phase")
   private LongJump2.Mode phase = LongJump2.Mode.STARTING;
   @SerializedName("jumpTicks")
   private int jumpTicks;
   @SerializedName("waitingTicks")
   private int waitingTicks;
   @SerializedName("cancelNextMove")
   private boolean cancelNextMove;

   public LongJump2() {
      super(Category.Movement, "LongJump2");
   }

   @Override
   public void onEnable() {
      this.performActionAlt();
   }

   @Override
   public void onDisable() {
      TimerSpeedManager.setTimerSpeed(1.0F);
      this.performActionAlt();
   }

   @EventTarget
   public void onEntityGlow(EntityGlowEvent entityGlowEvent) {
      if (mc2.player != null) {
         if (this.jumpTicks == 0) {
            mc2.player.jump();
         } else if (this.jumpTicks == 1) {
            for (int i = 0; i < 10; i++) {
               PacketUtils.sendPacket(new OnGroundOnly(true, mc2.player.horizontalCollision));
            }

            if (mc2.inGameHud != null) {
               mc2.inGameHud.getChatHud().addMessage(Text.empty().append(IText.TEXT).append(Text.literal("321")));
            }

            this.cancelNextMove = true;
            this.phase = LongJump2.Mode.WAITING;
         }

         this.jumpTicks++;
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (mc2.player != null) {
         if (directionEvent.getDirection() == TransferOrigin.SEND) {
            if (directionEvent.getPacket() instanceof PlayerMoveC2SPacket && this.cancelNextMove) {
               this.cancelNextMove = false;
               directionEvent.setCancelled(true);
            }
         } else if (directionEvent.getPacket() instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket && entityVelocityUpdateS2CPacket.getEntityId() == mc2.player.getId() && entityVelocityUpdateS2CPacket.getVelocityY() < 0.0) {
            this.setEnabled(false);
         } else if (directionEvent.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.phase = LongJump2.Mode.CORRECTED;
         }
      }
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (mc2.player != null && xevent.getPhase() != Stage.POST) {
         TimerSpeedManager.setTimerSpeed(((Number)this.timer.getValue()).floatValue());
         xevent.setPitch(xevent.getPitch() + (float)(Math.random() * 0.1));
         mc2.player.setVelocity(mc2.player.getVelocity().add(0.0, 0.42, 0.0));
         if (this.phase == LongJump2.Mode.CORRECTED) {
            this.phase = LongJump2.Mode.BOOSTING;
            this.waitingTicks = 0;
         } else if (this.phase == LongJump2.Mode.WAITING) {
            mc2.player.setVelocity(mc2.player.getVelocity().multiply(0.0));
            this.cancelNextMove = true;
            if (++this.waitingTicks > 20) {
               this.setEnabled(false);
            }
         }
      }
   }

   private void performActionAlt() {
      this.phase = LongJump2.Mode.STARTING;
      this.jumpTicks = 0;
      this.waitingTicks = 0;
      this.cancelNextMove = false;
   }

   private enum Mode {
      STARTING,
      CORRECTED,
      BOOSTING,
      WAITING;


      // $VF: synthetic method
      private static LongJump2.Mode[] createValues() {
         return new LongJump2.Mode[]{STARTING, CORRECTED, BOOSTING, WAITING};
      }


   }
}
