package dev.southside.utils;

import dev.southside.utils.misc.PosRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockPlaceFinder implements IMinecraft {
   public boolean isValid(Vec3d vec, BlockPos pos, BlockPos pos2) {
      return this.computeFloat(vec, pos2) > mc.player.getBlockInteractionRange() || pos2.getY() >= pos.getY();
   }

   public boolean isValidBlock(BlockPos pos) {
      Block block = mc.world.getBlockState(pos).getBlock();
      return !Arrays.asList(
            Blocks.ANVIL,
            Blocks.AIR,
            Blocks.WATER,
            Blocks.FIRE,
            Blocks.LAVA,
            Blocks.SKELETON_SKULL,
            Blocks.OAK_SIGN,
            Blocks.TRAPPED_CHEST,
            Blocks.CHEST,
            Blocks.ENCHANTING_TABLE,
            Blocks.ENDER_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.COBWEB
         )
         .contains(block);
   }

   public PosRecord getPosRecord(BlockPos pos, Vec3d vec) {
      PosRecord posRecord = this.getPosRecord(pos);
      if (posRecord == null) {
         BlockPos pos2 = this.findBlockPos(vec);
         if (pos2 == null) {
            return null;
         }

         Direction direction = this.getDirection(pos2, vec);
         if (direction == null) {
            return null;
         }

         posRecord = new PosRecord(pos2, direction);
      }

      return this.isAir(posRecord.getPos().offset(posRecord.getFacing())) ? posRecord : null;
   }

   public static double getSquaredDistance(BlockPos pos, BlockPos pos2) {
      return pos2.getSquaredDistance(pos);
   }

   public PosRecord getPosRecord(Vec3d vec, float f) {
      BlockPos pos = BlockPos.ofFloored(vec);
      if (!this.isAir(pos)) {
         return null;
      }

      BlockPos pos2 = new BlockPos(pos.getX(), (int)f, pos.getZ());
      return this.getPosRecord(pos2, vec);
   }

   public static boolean isAir(PosRecord posRecord) {
      return !mc.world.getBlockState(posRecord.getPos().offset(posRecord.getFacing())).isAir();
   }

   public float computeFloat(Vec3d vec, BlockPos pos) {
      float f = (float)(vec.getX() - pos.getX());
      float f2 = (float)(vec.getY() - pos.getY());
      float f3 = (float)(vec.getZ() - pos.getZ());
      return MathHelper.sqrt(f * f + f2 * f2 + f3 * f3);
   }

   public Map<BlockPos, Block> buildMap(Vec3d vec) {
      HashMap map = new HashMap();
      if (vec == null) {
         return map;
      }

      for (int i = 5; i >= -4; i--) {
         for (int j = 5; j >= -4; j--) {
            for (int k = 5; k >= -4; k--) {
               BlockPos pos = new BlockPos((int)vec.getX() + i, (int)vec.getY() + j, (int)vec.getZ() + k);
               Block block = this.getBlock(pos);
               if (block != null) {
                  map.put(pos, block);
               }
            }
         }
      }

      return map;
   }

   public static double getSquaredDistance(BlockPos pos, PosRecord posRecord) {
      return posRecord.getPos().getSquaredDistance(pos);
   }

   public PosRecord getPosRecord(BlockPos pos) {
      if (this.isValidBlock(pos.add(-1, 0, 0))) {
         return new PosRecord(pos.add(-1, 0, 0), Direction.EAST);
      } else if (this.isValidBlock(pos.add(1, 0, 0))) {
         return new PosRecord(pos.add(1, 0, 0), Direction.WEST);
      } else if (this.isValidBlock(pos.add(0, 0, 1))) {
         return new PosRecord(pos.add(0, 0, 1), Direction.NORTH);
      } else if (this.isValidBlock(pos.add(0, 0, -1))) {
         return new PosRecord(pos.add(0, 0, -1), Direction.SOUTH);
      } else {
         return this.isValidBlock(pos.add(0, -1, 0)) ? new PosRecord(pos.add(0, -1, 0), Direction.UP) : null;
      }
   }

   public BlockPos findBlockPos(Vec3d vec) {
      BlockPos pos = BlockPos.ofFloored(vec);
      ArrayList<BlockPos> list = new ArrayList<>();

      for (BlockPos pos2 : this.buildMap(vec).keySet()) {
         if (this.isValidBlock(pos2)) {
            list.add(pos2);
         }
      }

      list.removeIf(pos3 -> this.isValid(vec, pos, pos3));
      list.sort(Comparator.comparingDouble(pos4 -> getSquaredDistance(pos, pos4)));
      return list.isEmpty() ? null : (BlockPos)list.getFirst();
   }

   public boolean isAir(BlockPos pos) {
      return mc.world != null && mc.world.getBlockState(pos).isAir();
   }

   public Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }

   public Direction getDirection(BlockPos pos, Vec3d vec) {
      ArrayList<PosRecord> list = new ArrayList<>();
      BlockPos pos2 = new BlockPos((int)Math.floor(vec.getX()), (int)Math.floor(vec.getY()), (int)Math.floor(vec.getZ()));
      if (this.isAir(pos.east()) && !pos.east().equals(pos2)) {
         list.add(new PosRecord(pos.east(), Direction.EAST));
      }

      if (this.isAir(pos.north()) && !pos.north().equals(pos2)) {
         list.add(new PosRecord(pos.north(), Direction.NORTH));
      }

      if (this.isAir(pos.south()) && !pos.south().equals(pos2)) {
         list.add(new PosRecord(pos.south(), Direction.SOUTH));
      }

      if (this.isAir(pos.west()) && !pos.west().equals(pos2)) {
         list.add(new PosRecord(pos.west(), Direction.WEST));
      }

      list.sort(Comparator.comparingDouble(posRecord -> getSquaredDistance(pos2, posRecord)));
      list.removeIf(BlockPlaceFinder::isAir);
      return list.isEmpty() ? null : ((PosRecord)list.getFirst()).getFacing();
   }
}
