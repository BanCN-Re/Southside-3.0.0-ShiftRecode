package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.EntityEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.Teams;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NumberValue;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.MathHelper;

public class BalanceHitSelect extends Module {
   private static final long DEFAULT_LONG_VALUE = 750L;
   @SerializedName("holdStartedAt")
   private long holdStartedAt;
   @SerializedName("holdingPackets")
   private boolean holdingPackets;
   @SerializedName("startReach")
   public NumberValue startReach = new NumberValue("Start Reach", 4.0, 0.5, 10.0, 0.01);
   @SerializedName("attackReach")
   public NumberValue attackReach = new NumberValue("Attack Reach", 3.0, 1.0, 6.0, 0.01);
   @SerializedName("fovCheck")
   public BooleanValue fovCheck = new BooleanValue("FOV Check", true);
   @SerializedName("distanceCheck")
   public BooleanValue distanceCheck = new BooleanValue("Distance Check", true);
   @SerializedName("debug")
   public BooleanValue debug = new BooleanValue("Debug", true);
   @SerializedName("queuedPackets")
   private final List<Packet<?>> queuedPackets = new CopyOnWriteArrayList<>();

   public BalanceHitSelect() {
      super(Category.Combat, "Balance Hitselect", "Balance hit select with blink");
   }

   @EventTarget
   public void onEntity(EntityEvent entityEvent) {
      if (entityEvent.phase == Stage.POST && this.holdingPackets) {
         this.isInFOV("§4[BHS] Release | Attack");
         this.performActionAlt();
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.state == Stage.PRE) {
         if (Module.mc.player != null && Module.mc.player.isAlive() && !Module.mc.player.isDead()) {
            PlayerEntity closestPlayer = getClosestPlayer();
            if (closestPlayer != null && !Teams.isValidTarget(closestPlayer)) {
               double d = ((Number)this.startReach.getValue()).doubleValue();
               double d2 = ((Number)this.attackReach.getValue()).doubleValue();
               double dist = Module.mc.player.distanceTo(closestPlayer);
               boolean flag = !(Boolean)this.fovCheck.getValue() || this.isInFOV(closestPlayer, 180.0);
               boolean flag2 = dist < d && flag;
               if ((Boolean)this.distanceCheck.getValue()) {
                  flag2 = flag2 && dist >= d2;
               }

               if (flag2) {
                  if (!this.holdingPackets) {
                     this.isInFOV("§2[BHS] Cache | Distance: %s", dist);
                     this.holdingPackets = true;
                     this.holdStartedAt = System.currentTimeMillis();
                  }
               } else if (this.holdingPackets) {
                  String text = "Condition not met";
                  if ((Boolean)this.distanceCheck.getValue() && dist < d2) {
                     text = "Too Close";
                  } else if (dist >= d) {
                     text = "Too Far";
                  } else if ((Boolean)this.fovCheck.getValue() && !this.isInFOV(closestPlayer, 180.0)) {
                     text = "Out of FOV";
                  }

                  this.isInFOV("§4[BHS] Release | %s | Distance: %s", text, dist);
                  this.performActionAlt();
               }

               if (this.holdingPackets && System.currentTimeMillis() - this.holdStartedAt >= 750L) {
                  this.isInFOV("§4[BHS] Release | Timeout");
                  this.performActionAlt();
               }
            } else {
               if (this.holdingPackets) {
                  this.isInFOV("§4[BHS] Release | No Player");
                  this.performActionAlt();
               }
            }
         } else {
            this.clear();
         }
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.isValid() && this.holdingPackets) {
         this.queuedPackets.add(directionEvent.packet);
         directionEvent.setCancelled(true);
      }
   }

   private void performActionAlt() {
      this.clear();
      this.holdingPackets = false;
   }

   private void clear() {
      for (Packet packet : this.queuedPackets) {
         PacketUtils.sendPacketDirect(packet);
      }

      this.queuedPackets.clear();
   }

   private void isInFOV(String text, Object... objArray) {
      if (Module.mc.world != null && Module.mc.player != null && (Boolean)this.debug.getValue()) {
         this.applyTextImpl(String.format(text, objArray));
      }
   }

   public boolean isInFOV(PlayerEntity player, double d) {
      if (Module.mc.player != null && player != null) {
         double dx = player.getX() - Module.mc.player.getX();
         double dz = player.getZ() - Module.mc.player.getZ();
         double d2 = Math.toDegrees(Math.atan2(dz, dx)) - Module.mc.player.getYaw() - 90.0;
         d2 = MathHelper.wrapDegrees(d2);
         return Math.abs(d2) <= d / 2.0;
      } else {
         return false;
      }
   }

   public static PlayerEntity getClosestPlayer() {
      PlayerEntity player = null;
      double max_value = Double.MAX_VALUE;
      MinecraftClient instance = MinecraftClient.getInstance();
      if (instance.world != null) {
         for (PlayerEntity player2 : instance.world.getPlayers()) {
            if (player2 != instance.player) {
               double d = instance.player == null ? 0.0 : instance.player.distanceTo(player2);
               if (d < max_value) {
                  max_value = d;
                  player = player2;
               }
            }
         }
      }

      return player;
   }
}
