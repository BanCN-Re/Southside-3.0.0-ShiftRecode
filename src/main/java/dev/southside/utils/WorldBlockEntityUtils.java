package dev.southside.utils;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class WorldBlockEntityUtils {
   public static Iterable<BlockEntity> getIterable() {
      return LoadedBlockEntityIterator::new;
   }

   public static boolean isValidBlock(BlockPos pos) {
      return IMinecraft.mc.player != null && IMinecraft.mc.world != null ? IMinecraft.mc.world.getBlockState(pos).isAir() : false;
   }
}
