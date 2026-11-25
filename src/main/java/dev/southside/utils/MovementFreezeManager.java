package dev.southside.utils;

import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.PlayerPreMoveEvent;
import dev.southside.event.events.StateEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

public final class MovementFreezeManager {
   public static final MovementFreezeManager INSTANCE = new MovementFreezeManager();
   public static int count = 0;
   public static boolean active = false;
   public static Vec3d vector;
   public static float factor = 0.0F;
   public static int countValue = 0;
   public static MinecraftClient mc = MinecraftClient.getInstance();
   public static boolean activeValue;

   private static boolean isValid() {
      return MinecraftClient.getInstance() == null || MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null;
   }

   public static void performAction() {
      if (mc.player != null && mc.world != null && !active) {
         vector = mc.player.getVelocity();
         factor = mc.player.fallDistance;
         active = true;
      }
   }

   public static void performActionEx() {
      if (!isValid() && mc.player.getVelocity() != null && vector != null) {
         active = false;
         count = 0;
         mc.player.setVelocity(vector);
         vector = null;
         mc.player.fallDistance = factor;
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (!isValid() && mc.player.getVelocity() != null) {
         if (countValue > 0) {
            countValue--;
            if (!isValid() && mc.player.getVelocity() != null && !activeValue && mc.player != null && !active) {
               active = true;
               vector = mc.player.getVelocity();
               factor = mc.player.fallDistance;
            }
         }

         if (active) {
            performActionAlt();
         }
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (!isValid() && mc.player.getVelocity() != null) {
         if (stateEvent.getState() == Stage.PRE) {
            activeValue = false;
            performActionEx();
         } else if (active) {
            performActionAlt();
         }
      }
   }

   @EventTarget
   public void onEntityGlowEvent2(PlayerPreMoveEvent playerPreMoveEvent) {
      if (!isValid() && mc.player.getVelocity() != null && active) {
         playerPreMoveEvent.setCancelled(true);
      }
   }

   private static void performActionAlt() {
      mc.player.setVelocity(vector);
      mc.player.fallDistance = factor;
   }

   public static void handlePacket(Packet<?> packet) {
      if (!isValid() && mc.player.getVelocity() != null) {
         if (packet instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket && active && mc.player.getId() == entityVelocityUpdateS2CPacket.getEntityId()) {
            vector = new Vec3d(entityVelocityUpdateS2CPacket.getVelocityX() / 8000.0, entityVelocityUpdateS2CPacket.getVelocityY() / 8000.0, entityVelocityUpdateS2CPacket.getVelocityZ() / 8000.0);
         }

         if (packet instanceof ExplosionS2CPacket explosionS2CPacket && active) {
            explosionS2CPacket.playerKnockback().ifPresent(MovementFreezeManager::performAction);
         }
      }
   }

   public static void onKeyTyped(int n) {
      if (!isValid()) {
         countValue = n;
      }
   }

   public static void performAction(Vec3d vec) {
      vector.add(vec.x, vec.y, vec.z);
   }
}
