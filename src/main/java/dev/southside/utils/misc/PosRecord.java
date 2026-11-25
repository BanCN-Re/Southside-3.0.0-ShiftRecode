package dev.southside.utils.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record PosRecord(BlockPos pos, Direction facing) {
   public BlockPos getPos() {
      return this.pos;
   }

   public Direction getFacing() {
      return this.facing;
   }
}
