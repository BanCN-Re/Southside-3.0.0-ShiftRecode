package dev.southside.module.impl.player;

import dev.southside.event.EventTarget;
import dev.southside.event.events.RightClickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.BlockLookUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GhostHand extends Module {
   private static final double DEFAULT_DOUBLE = 4.5;
   private static final double DEFAULT_DOUBLE_VALUE = 0.1;
   private static final long DEFAULT_LONG_VALUE = -318851113560309715L;

   public GhostHand() {
      super(Category.Player, "GhostHand");
   }

   public boolean hasChestCollision(BlockPos pos, Vec3d vec) {
      return pos != null
         && vec != null
         && vec.x >= pos.getX() + 0.1
         && vec.x <= pos.getX() + 0.9
         && vec.y >= pos.getY() + 0.1
         && vec.y <= pos.getY() + 0.9
         && vec.z >= pos.getZ() + 0.1
         && vec.z <= pos.getZ() + 0.9;
   }

   public BlockHitResult raycastForChest() {
      if (mc2.player != null && mc2.world != null) {
         float tickDelta = mc2.getRenderTickCounter().getTickDelta(false);
         Vec3d cameraPosVec = mc2.player.getCameraPosVec(tickDelta);
         Vec3d vec = mc2.player.getRotationVec(tickDelta).multiply(0.1);
         int n = (int)DEFAULT_LONG_VALUE;

         for (int i = 0; i <= n; i++) {
            BlockPos pos = new BlockPos((int)Math.floor(cameraPosVec.x), (int)Math.floor(cameraPosVec.y), (int)Math.floor(cameraPosVec.z));
            if (this.isValidBlock(mc2.world.getBlockState(pos)) && this.hasChestCollision(pos, cameraPosVec)) {
               return BlockLookUtils.raycast(mc2.player.getYaw(), mc2.player.getPitch());
            }

            cameraPosVec = cameraPosVec.add(vec);
         }

         return null;
      } else {
         return null;
      }
   }

   @EventTarget
   public void onCustomEvent2(RightClickEvent rightClickEvent) {
      if (mc2.player != null && mc2.world != null) {
         if (!(
            mc2.crosshairTarget instanceof BlockHitResult hitResult
               && hitResult.getType() == Type.BLOCK
               && this.isValidBlock(mc2.world.getBlockState(hitResult.getBlockPos()))
         )) {
            BlockHitResult hitResult2 = this.raycastForChest();
            if (hitResult2 != null) {
               PacketUtils.sendSequencedPacket(n -> getPlayerInteractBlockC2SPacket(hitResult2, n));
               rightClickEvent.setCancelled(true);
            }
         }
      }
   }

   public static PlayerInteractBlockC2SPacket getPlayerInteractBlockC2SPacket(BlockHitResult hitResult, int n) {
      return new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, n);
   }

   public boolean isValidBlock(BlockState state) {
      return state.getBlock() instanceof ChestBlock || state.getBlock() instanceof EnderChestBlock || state.getBlock() instanceof ShulkerBoxBlock;
   }
}
