package dev.southside.utils;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class BlockRaycastHelper {
   private BlockRaycastHelper() {
   }

   public static BlockHitResult raycastHit(ClientPlayerEntity player, ClientWorld world, BlockPos pos, Rotation rotation, boolean flag) {
      if (rotation == null) {
         return null;
      }

      Vec3d eyePos = player.getEyePos();
      Vec3d vec = eyePos.add(Vec3d.fromPolar(rotation.pitch, rotation.yaw).multiply(player.getBlockInteractionRange()));
      return raycast(player, world, pos, eyePos, vec, flag);
   }

   public static BlockHitResult raycastHit(ClientPlayerEntity player, ClientWorld world, BlockPos pos, boolean flag) {
      Vec3d eyePos = player.getEyePos();
      Vec3d vec = eyePos.add(Vec3d.ofCenter(pos).subtract(eyePos).normalize().multiply(player.getBlockInteractionRange()));
      return raycast(player, world, pos, eyePos, vec, flag);
   }

   private static BlockHitResult raycast(ClientPlayerEntity player, ClientWorld world, BlockPos pos, Vec3d vec, Vec3d vec2, boolean flag) {
      if (world.getWorldBorder().contains(pos) && player.canInteractWithBlockAt(pos, 0.0)) {
         BlockHitResult hitResult;
         if (flag) {
            hitResult = world.raycast(new RaycastContext(vec, vec2, ShapeType.OUTLINE, FluidHandling.NONE, player));
         } else {
            BlockState blockState = world.getBlockState(pos);
            hitResult = world.raycastBlock(vec, vec2, pos, blockState.getOutlineShape(world, pos), blockState);
         }

         return hitResult != null && hitResult.getType() == Type.BLOCK && hitResult.getBlockPos().equals(pos) ? hitResult : null;
      } else {
         return null;
      }
   }
}
