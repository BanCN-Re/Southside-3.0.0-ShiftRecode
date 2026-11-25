package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NumberValue;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.registry.tag.EntityTypeTags;

public class NoFall extends Module {
   private static final double DEFAULT_DOUBLE = 1.0E-8;
   private static final int DEFAULT_INT = 40;
   private static final int DEFAULT_INT_VALUE = 20;
   @SerializedName("fallDistance")
   public final NumberValue fallDistance = new NumberValue("Fall Distance", 3.0F, 1.0F, 15.0F, 1.0F);
   @SerializedName("trackedPlayer")
   private ClientPlayerEntity trackedPlayer;
   @SerializedName("state")
   private volatile NoFall.Mode state = NoFall.Mode.IDLE;
   @SerializedName("stateTicks")
   private int stateTicks;
   @SerializedName("correctionId")
   private int correctionId;
   @SerializedName("forcePositionUpdate")
   private boolean forcePositionUpdate;

   public NoFall() {
      super(Category.Player, "NoFall", "NoFallNoFallNoFallNoFallNoFallNoFall");
   }

   @Override
   public void onEnable() {
      this.performActionImpl();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      if (mc2.player != null && mc2.player == this.trackedPlayer && (this.isValidEx() || this.forcePositionUpdate)) {
         mc2.player.ticksSinceLastPositionPacketSent = 20;
      }

      this.performActionImpl();
      super.onDisable();
   }

   public boolean isValidEx() {
      NoFall.Mode state2 = this.state;
      return state2 == NoFall.Mode.WAIT_CORRECTION || state2 == NoFall.Mode.WAIT_CONFIRM || state2 == NoFall.Mode.REPLACE_POSITION;
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.performActionImpl();
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      ClientPlayerEntity player = mc2.player;
      if (player != null && mc2.world != null && player.isAlive()) {
         if (player != this.trackedPlayer) {
            this.performActionImpl();
            this.trackedPlayer = player;
         }

         if (this.forcePositionUpdate) {
            player.ticksSinceLastPositionPacketSent = 20;
            this.forcePositionUpdate = false;
         }

         if (this.isValidEx()) {
            if (++this.stateTicks > 40) {
               player.ticksSinceLastPositionPacketSent = 20;
               this.performActionAlt();
            }
         } else if (isValid(player) && this.isValidEx(player)) {
            if (this.state == NoFall.Mode.IDLE) {
               this.state = NoFall.Mode.WAIT_CONTACT;
            }
         } else {
            if (this.state == NoFall.Mode.WAIT_CONTACT) {
               this.performActionAlt();
            }
         }
      } else {
         this.performActionImpl();
      }
   }

   @EventTarget(priority = -200)
   public void onDirection(DirectionEvent directionEvent) {
      if (!directionEvent.isCancelled()) {
         if (!directionEvent.isValid()) {
            if (directionEvent.getPacket() instanceof PlayerPositionLookS2CPacket playerPositionLookS2CPacket) {
               ClientPlayerEntity player = mc2.player;
               mc2.execute(
                  () -> {
                     if (this.isEnabled()
                        && player != null
                        && player == mc2.player
                        && player == this.trackedPlayer
                        && mc2.getNetworkHandler() != null
                        && mc2.getNetworkHandler().getConnection() == directionEvent.getClientConnection()
                        && this.isValidEx()) {
                        this.correctionId = playerPositionLookS2CPacket.teleportId();
                        this.stateTicks = 0;
                        this.state = NoFall.Mode.WAIT_CONFIRM;
                     }
                  }
               );
            }
         } else {
            ClientPlayerEntity player2 = mc2.player;
            if (player2 != null
               && player2 == this.trackedPlayer
               && mc2.world != null
               && mc2.getNetworkHandler() != null
               && mc2.getNetworkHandler().getConnection() == directionEvent.getClientConnection()
               && directionEvent.getClientConnection().isOpen()) {
               if (directionEvent.getPacket() instanceof PlayerMoveC2SPacket playerMoveC2SPacket) {
                  if (this.state == NoFall.Mode.WAIT_CONTACT && playerMoveC2SPacket.isOnGround()) {
                     this.performAction(directionEvent, player2);
                  } else if (this.state == NoFall.Mode.REPLACE_POSITION && playerMoveC2SPacket instanceof Full) {
                     this.performAction(directionEvent, playerMoveC2SPacket, player2);
                  } else if (this.isValidEx()) {
                     directionEvent.setCancelled(true);
                  }
               } else if (this.state == NoFall.Mode.WAIT_CONFIRM
                  && directionEvent.getPacket() instanceof TeleportConfirmC2SPacket teleportConfirmC2SPacket
                  && teleportConfirmC2SPacket.getTeleportId() == this.correctionId) {
                  try {
                     PacketUtils.sendPacketDirect(teleportConfirmC2SPacket);
                     directionEvent.setCancelled(true);
                     this.stateTicks = 0;
                     this.state = NoFall.Mode.REPLACE_POSITION;
                  } catch (RuntimeException e) {
                     this.performActionAlt();
                     throw e;
                  }
               }
            } else {
               this.performActionImpl();
            }
         }
      }
   }

   private void performAction(DirectionEvent directionEvent, ClientPlayerEntity player) {
      if (LivingEntity.canGlideWith(player.getEquippedStack(EquipmentSlot.CHEST), EquipmentSlot.CHEST)) {
         this.performActionAlt();
      } else {
         this.state = NoFall.Mode.WAIT_CORRECTION;
         this.stateTicks = 0;

         try {
            PacketUtils.sendPacketDirect(new ClientCommandC2SPacket(player, net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            directionEvent.setCancelled(true);
         } catch (RuntimeException e) {
            this.performActionAlt();
            throw e;
         }
      }
   }

   private void performAction(DirectionEvent directionEvent, PlayerMoveC2SPacket playerMoveC2SPacket, ClientPlayerEntity player) {
      double x = playerMoveC2SPacket.getX(player.getX());
      double d = playerMoveC2SPacket.getY(player.getY()) + 1.0E-8;
      double z = playerMoveC2SPacket.getZ(player.getZ());
      Full full = new Full(x, d, z, playerMoveC2SPacket.getYaw(player.getYaw()), playerMoveC2SPacket.getPitch(player.getPitch()), false, false);

      try {
         PacketUtils.sendPacketDirect(full);
         directionEvent.setCancelled(true);
         player.setPosition(x, d, z);
         player.setOnGround(false);
         player.horizontalCollision = false;
         player.collidedSoftly = false;
         player.verticalCollision = false;
         player.groundCollision = false;
         player.onLanding();
         this.forcePositionUpdate = true;
      } finally {
         this.performActionAlt();
      }
   }

   private static boolean isValid(ClientPlayerEntity player) {
      return !player.isOnGround()
         && !player.hasVehicle()
         && !player.getAbilities().allowFlying
         && !player.getAbilities().flying
         && !player.isGliding()
         && !player.isTouchingWater()
         && !player.isInLava()
         && !player.isClimbing()
         && player.getVelocity().y < 0.0;
   }

   private boolean isValidEx(ClientPlayerEntity player) {
      if (player.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
         return false;
      }

      float f = (float)(player.fallDistance + Math.max(0.0, -player.getVelocity().y));
      if (f < ((Number)this.fallDistance.getValue()).floatValue()) {
         return false;
      }

      float f2 = f - (float)player.getAttributeValue(EntityAttributes.SAFE_FALL_DISTANCE);
      return Math.ceil(f2 * player.getAttributeValue(EntityAttributes.FALL_DAMAGE_MULTIPLIER)) > 0.0;
   }

   private void performActionAlt() {
      this.state = NoFall.Mode.IDLE;
      this.stateTicks = 0;
      this.correctionId = 0;
   }

   private void performActionImpl() {
      this.trackedPlayer = null;
      this.forcePositionUpdate = false;
      this.performActionAlt();
   }

   private enum Mode {
      IDLE,
      WAIT_CONTACT,
      WAIT_CORRECTION,
      WAIT_CONFIRM,
      REPLACE_POSITION;


      // $VF: synthetic method
      private static NoFall.Mode[] createValues() {
         return new NoFall.Mode[]{IDLE, WAIT_CONTACT, WAIT_CORRECTION, WAIT_CONFIRM, REPLACE_POSITION};
      }


   }
}
