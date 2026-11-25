package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public final class PacketBlockBreaker implements IMinecraft {
   @SerializedName("targetPos")
   private BlockPos targetPos;
   @SerializedName("targetFace")
   private Direction targetFace = Direction.UP;
   @SerializedName("progress")
   private float progress;
   @SerializedName("breaking")
   private boolean breaking;
   @SerializedName("cooldownMillis")
   private long cooldownMillis;
   @SerializedName("nextBreakTime")
   private long nextBreakTime;
   @SerializedName("listener")
   private PacketBlockBreaker.Inner listener;
   @SerializedName("minimumCooldownMillis")
   private long minimumCooldownMillis;
   @SerializedName("swung")
   private boolean swung;

   private static boolean isValid() {
      return MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null;
   }

   private void sendSequencedPacket(Action action, BlockPos pos, Direction direction) {
      if (mc.player != null && mc.interactionManager != null && mc.world != null) {
         mc.interactionManager.sendSequencedPacket(mc.world, n -> new PlayerActionC2SPacket(action, pos, direction, n));
      }
   }

   private void reset() {
      this.nextBreakTime = System.currentTimeMillis() + Math.max(this.cooldownMillis, this.minimumCooldownMillis);
   }

   public boolean isValid(BlockPos pos, Direction direction, long l, @Nullable PacketBlockBreaker.Inner inner) {
      if (isValid() && mc.interactionManager != null && pos != null && direction != null) {
         if (System.currentTimeMillis() < this.nextBreakTime) {
            return false;
         }

         ClientPlayerEntity player = mc.player;
         if (player == null) {
            return false;
         }

         BlockState blockState = mc.world.getBlockState(pos);
         if (blockState.isAir()) {
            return false;
         }

         if (this.breaking && pos.equals(this.targetPos) && direction == this.targetFace) {
            return true;
         }

         if (this.breaking) {
            this.performAction();
         }

         this.targetPos = pos.toImmutable();
         this.targetFace = direction;
         this.progress = 0.0F;
         this.breaking = true;
         this.cooldownMillis = Math.max(0L, l);
         this.listener = inner;
         this.minimumCooldownMillis = getDurationMs(player);
         this.swung = false;
         this.sendSequencedPacket(Action.START_DESTROY_BLOCK, this.targetPos, this.targetFace);
         float f = blockState.calcBlockBreakingDelta(player, player.clientWorld, this.targetPos);
         if (player.getAbilities().creativeMode || f >= 1.0F) {
            player.swingHand(Hand.MAIN_HAND);
            this.swung = true;
            this.progress = 1.0F;
            this.performAction(blockState);
         }

         return true;
      } else {
         return false;
      }
   }

   public void performAction() {
      if (this.breaking && this.targetPos != null && this.targetFace != null && mc.player != null) {
         if (!this.swung && mc.player != null) {
            mc.player.swingHand(Hand.MAIN_HAND);
         }

         this.sendSequencedPacket(Action.ABORT_DESTROY_BLOCK, this.targetPos, this.targetFace);
         mc.world.setBlockBreakingInfo(mc.player.getId(), this.targetPos, -1);
         this.reset();
      }

      this.performActionAlt();
   }

   public BlockPos getTargetPos() {
      return this.targetPos;
   }

   public boolean isValidBlock(BlockPos pos) {
      return this.breaking && this.targetPos != null && this.targetPos.equals(pos);
   }

   public boolean isBreaking() {
      return this.breaking;
   }

   private static long getDurationMs(ClientPlayerEntity player) {
      return player.getAbilities().creativeMode ? 50L : 250L;
   }

   public void performActionEx() {
      if (this.breaking && isValid() && this.targetPos != null && this.targetFace != null) {
         BlockState blockState = mc.world.getBlockState(this.targetPos);
         ClientPlayerEntity player = mc.player;
         if (player == null || mc.interactionManager == null) {
            this.performAction();
         } else if (blockState.isAir()) {
            this.performAction();
         } else {
            float f = blockState.calcBlockBreakingDelta(player, player.clientWorld, this.targetPos);
            if (!(f <= 0.0F)) {
               player.swingHand(Hand.MAIN_HAND);
               this.swung = true;
               this.progress += f;
               mc.world.setBlockBreakingInfo(player.getId(), this.targetPos, (int)(this.progress * 10.0F) - 1);
               if (this.progress >= 1.0F) {
                  this.performAction(blockState);
               }
            }
         }
      }
   }

   private void performAction(BlockState state) {
      if (mc.player != null && mc.interactionManager != null && this.targetPos != null && this.targetFace != null) {
         if (!this.swung && mc.player != null) {
            mc.player.swingHand(Hand.MAIN_HAND);
            this.swung = true;
         }

         this.sendSequencedPacket(Action.STOP_DESTROY_BLOCK, this.targetPos, this.targetFace);
         mc.interactionManager.breakBlock(this.targetPos);
         mc.world.setBlockBreakingInfo(mc.player.getId(), this.targetPos, -1);
         this.reset();
         if (this.listener != null) {
            this.listener.updateBlock(this.targetPos, state);
         }
      }

      this.performActionAlt();
   }

   public void performActionAlt() {
      this.breaking = false;
      this.progress = 0.0F;
      this.targetPos = null;
      this.targetFace = Direction.UP;
      this.listener = null;
      this.minimumCooldownMillis = 0L;
      this.swung = false;
   }

   public interface Inner {
      void updateBlock(BlockPos pos, BlockState state);
   }
}
