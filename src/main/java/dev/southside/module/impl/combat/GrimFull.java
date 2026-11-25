package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.ForwardEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.AutoGApple;
import dev.southside.utils.ValuePair;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.RotationManager;
import dev.southside.utils.MovementFreezeManager;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class GrimFull extends Module {
   @SerializedName("knockbackPending")
   private boolean knockbackPending;
   @SerializedName("supportFound")
   private boolean supportFound;
   @SerializedName("discardQueuedVelocity")
   private boolean discardQueuedVelocity;
   private static final long DEFAULT_LONG_VALUE = 7116577687098884095L;

   public GrimFull() {
      super(Category.Combat, "GrimFull");
   }

   private boolean isValid(EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket) {
      return !AutoGApple.activeValue && new Vec3d(entityVelocityUpdateS2CPacket.getVelocityX(), 0.0, entityVelocityUpdateS2CPacket.getVelocityZ()).length() >= 0.15;
   }

   private boolean isValidBlock(BlockPos pos) {
      return pos != null && new Box(pos.up()).intersects(Module.mc.player.getBoundingBox());
   }

   private BlockHitResult raycastHit() {
      return BlockLookUtils.raycast(Module.mc.player.getYaw(), 90.0F, BlockLookUtils::isValidBlock);
   }

   @EventTarget
   public void onForward(ForwardEvent forwardEvent) {
      if (this.knockbackPending && MovementFreezeManager.active) {
         forwardEvent.setSneak(true);
      }
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (this.knockbackPending && this.supportFound && Module.mc.player != null && Module.mc.world != null && Module.mc.interactionManager != null) {
         this.applyTextDirect("send");
         this.supportFound = false;
         BlockHitResult hitResult = this.raycastHit();
         if (hitResult != null) {
            Module.mc.interactionManager.interactBlock(Module.mc.player, Hand.MAIN_HAND, hitResult);
         }
      }
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (this.discardQueuedVelocity) {
         this.performActionImpl();
      }

      if (this.knockbackPending && Module.mc.player != null && Module.mc.world != null) {
         BlockHitResult hitResult = this.raycastHit();
         if (hitResult != null && this.isValidBlock(hitResult.getBlockPos()) && Module.mc.player.isOnGround()) {
            if (!this.supportFound) {
               this.applyTextDirect("found");
               this.supportFound = true;
               Rotation rotation = new Rotation(Module.mc.player.getYaw(), 90.0F);
               RotationManager.rotationRequests.add(new ValuePair<>(rotation, (int)DEFAULT_LONG_VALUE));
            }

            this.performActionAlt();
         }
      }
   }

   private void performActionAlt() {
      if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         if (MovementFreezeManager.mc.player.getVelocity() != null && !MovementFreezeManager.activeValue && MovementFreezeManager.mc.player != null && !MovementFreezeManager.active) {
            MovementFreezeManager.active = true;
            MovementFreezeManager.vector = MovementFreezeManager.mc.player.getVelocity();
            MovementFreezeManager.factor = MovementFreezeManager.mc.player.fallDistance;
         }
      }
   }

   private void performActionImpl() {
      if (ServerPacketManager.mc.getNetworkHandler() == null) {
         ServerPacketManager.clear();
      } else {
         while (true) {
            Packet packet;
            synchronized (ServerPacketManager.linkedBlockingQueue) {
               Object obj = ServerPacketManager.linkedBlockingQueue.poll();
               if (!(obj instanceof Packet packet2)) {
                  if (!(obj instanceof Runnable action)) {
                     ServerPacketManager.active = false;
                     ServerPacketManager.count = 0;
                     return;
                  }

                  action.run();
                  continue;
               }

               packet = packet2;
            }

            if (packet instanceof KeepAliveC2SPacket) {
               ServerPacketManager.count = Math.max(0, ServerPacketManager.count - 1);
            } else if (!(packet instanceof EntityVelocityUpdateS2CPacket)) {
               MovementFreezeManager.handlePacket(packet);

               try {
                  ServerPacketManager.handlePacket(packet);
               } catch (Exception e) {
                  Southside.LOGGER.error("Unexpected error in GrimFull", e);
               }
            }
         }
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (Module.mc.player != null && Module.mc.world != null) {
         Packet packet = directionEvent.getPacket();
         if (!directionEvent.isCancelled()
            && packet instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket
            && entityVelocityUpdateS2CPacket.getEntityId() == Module.mc.player.getId()
            && this.isValid(entityVelocityUpdateS2CPacket)) {
            BlockHitResult hitResult = this.raycastHit();
            if (hitResult != null && this.isValidBlock(hitResult.getBlockPos())) {
               this.knockbackPending = true;
               this.applyTextDirect("kb");
            }
         }

         if (packet instanceof BlockUpdateS2CPacket blockUpdateS2CPacket && blockUpdateS2CPacket.getPos() != null && Module.mc.player.isOnGround() && this.isValidBlock(blockUpdateS2CPacket.getPos())) {
            this.applyTextDirect("received");
            this.discardQueuedVelocity = true;
            this.knockbackPending = false;
         }
      }
   }
}
