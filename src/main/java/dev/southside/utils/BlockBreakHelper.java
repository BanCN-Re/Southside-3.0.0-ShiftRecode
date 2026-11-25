package dev.southside.utils;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class BlockBreakHelper implements IMinecraft {
   private static BlockPos targetPos;
   private static Direction direction;
   private static boolean active;
   private static float factor;

   private BlockBreakHelper() {
   }

   private static void sendSequencedPacket(Action action, BlockPos pos, Direction direction2) {
      if (mc.interactionManager != null && mc.world != null) {
         mc.interactionManager.sendSequencedPacket(mc.world, n -> new PlayerActionC2SPacket(action, pos, direction2, n));
      }
   }

   public static void performAction(BlockHitResult hitResult) {
      if (hitResult != null && mc.player != null && mc.interactionManager != null && mc.world != null) {
         BlockPos blockPos = hitResult.getBlockPos();
         Direction side = hitResult.getSide();
         BlockState blockState = mc.world.getBlockState(blockPos);
         if (!blockState.isAir() && !(blockState.getHardness(mc.world, blockPos) < 0.0F)) {
            if (targetPos == null || !targetPos.equals(blockPos) || direction != side) {
               performActionEx();
               targetPos = blockPos.toImmutable();
               direction = side;
               factor = 0.0F;
            }

            if (!active) {
               mc.player.swingHand(Hand.MAIN_HAND);
               sendSequencedPacket(Action.START_DESTROY_BLOCK, targetPos, direction);
               active = true;
            }
         }
      }
   }

   public static void performActionEx(BlockHitResult hitResult) {
      if (hitResult != null && mc.player != null && mc.interactionManager != null && mc.world != null) {
         BlockPos blockPos = hitResult.getBlockPos();
         Direction side = hitResult.getSide();
         BlockState blockState = mc.world.getBlockState(blockPos);
         if (!blockState.isAir() && !(blockState.getHardness(mc.world, blockPos) < 0.0F)) {
            if (targetPos == null || !targetPos.equals(blockPos) || direction != side) {
               targetPos = blockPos.toImmutable();
               direction = side;
            }

            mc.player.swingHand(Hand.MAIN_HAND);
            sendSequencedPacket(Action.STOP_DESTROY_BLOCK, targetPos, direction);
            mc.interactionManager.breakBlock(targetPos);
            mc.world.setBlockBreakingInfo(mc.player.getId(), targetPos, -1);
            performAction();
         } else {
            performAction();
         }
      }
   }

   public static void performAction(BlockHitResult hitResult, boolean flag) {
      if (hitResult != null && mc.player != null && mc.interactionManager != null && mc.world != null) {
         BlockPos blockPos = hitResult.getBlockPos();
         Direction side = hitResult.getSide();
         BlockState blockState = mc.world.getBlockState(blockPos);
         if (!blockState.isAir() && !(blockState.getHardness(mc.world, blockPos) < 0.0F)) {
            if (targetPos == null || !targetPos.equals(blockPos) || direction != side) {
               performActionEx();
               targetPos = blockPos.toImmutable();
               direction = side;
               active = false;
               factor = 0.0F;
            }

            if (!flag && !mc.player.getAbilities().creativeMode) {
               if (!active) {
                  sendSequencedPacket(Action.START_DESTROY_BLOCK, targetPos, direction);
                  active = true;
               }

               float f = blockState.calcBlockBreakingDelta(mc.player, mc.player.clientWorld, targetPos);
               if (f <= 0.0F) {
                  return;
               }

               mc.player.swingHand(Hand.MAIN_HAND);
               factor += f;
               mc.world.setBlockBreakingInfo(mc.player.getId(), targetPos, Math.max(0, (int)(factor * 10.0F) - 1));
               if (factor >= 1.0F) {
                  sendSequencedPacket(Action.STOP_DESTROY_BLOCK, targetPos, direction);
                  mc.interactionManager.breakBlock(targetPos);
                  mc.world.setBlockBreakingInfo(mc.player.getId(), targetPos, -1);
                  performAction();
               }
            } else {
               mc.player.swingHand(Hand.MAIN_HAND);
               sendSequencedPacket(Action.START_DESTROY_BLOCK, blockPos, side);
               sendSequencedPacket(Action.STOP_DESTROY_BLOCK, blockPos, side);
               mc.interactionManager.breakBlock(blockPos);
               mc.world.setBlockBreakingInfo(mc.player.getId(), blockPos, -1);
               performAction();
            }
         }
      }
   }

   public static void performAction() {
      targetPos = null;
      direction = null;
      active = false;
      factor = 0.0F;
   }

   public static void performActionEx() {
      if (mc.player != null && mc.interactionManager != null && mc.world != null && active && targetPos != null && !mc.world.getBlockState(targetPos).isAir()) {
         sendSequencedPacket(Action.ABORT_DESTROY_BLOCK, targetPos, direction != null ? direction : Direction.UP);
         mc.world.setBlockBreakingInfo(mc.player.getId(), targetPos, -1);
      }

      performAction();
   }
}
