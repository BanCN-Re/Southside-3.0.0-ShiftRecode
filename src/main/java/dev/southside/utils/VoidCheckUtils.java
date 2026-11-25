package dev.southside.utils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class VoidCheckUtils {
   public static boolean isValid(double d) {
      for (double i = 0.0; i < d; i++) {
         BlockPos pos = BlockPos.ofFloored(IMinecraft.mc.player.getX(), IMinecraft.mc.player.getY() - i, IMinecraft.mc.player.getZ());
         BlockState blockState = IMinecraft.mc.world.getBlockState(pos);
         if (!blockState.isAir()
            && !IMinecraft.mc
               .world
               .getBlockCollisions(IMinecraft.mc.player, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1))
               .iterator()
               .hasNext()) {
            return true;
         }
      }

      return false;
   }

   public static boolean isValid() {
      if (IMinecraft.mc.player != null && IMinecraft.mc.world != null) {
         Vec3d pos = IMinecraft.mc.player.getPos();
         BlockPos pos2 = new BlockPos(
            (int)Math.floor(pos.x + Math.sin(Math.toRadians(IMinecraft.mc.player.getYaw())) * 0.8),
            (int)Math.floor(pos.y),
            (int)Math.floor(pos.z + -Math.cos(Math.toRadians(IMinecraft.mc.player.getYaw())) * 0.8)
         );
         if (IMinecraft.mc.world.getBlockState(pos2).getBlock() == Blocks.LAVA) {
            return false;
         }

         BlockPos pos3 = pos2.down();
         BlockState blockState = IMinecraft.mc.world.getBlockState(pos3);
         if (blockState.getBlock() == Blocks.LAVA) {
            return false;
         }

         if (blockState.isAir()) {
            if (pos3.getY() < IMinecraft.mc.world.getBottomY()) {
               return false;
            }

            if (IMinecraft.mc.world.getBlockState(pos3.down()).isAir()) {
               return pos3.down().getY() >= IMinecraft.mc.world.getBottomY() && !IMinecraft.mc.world.getBlockState(pos3.down().down()).isAir();
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static boolean isValid(Vec3d vec) {
      return isValidBlock(IMinecraft.mc.world.getBlockState(new BlockPos((int)vec.x, (int)vec.y, (int)vec.z)).getBlock());
   }

   public static List<ValuePair<BlockPos, Block>> collectItems(int n) {
      ArrayList list = new ArrayList();
      if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
         for (int i = -n; i <= n; i++) {
            for (int j = -n; j <= n; j++) {
               for (int k = -n; k <= n; k++) {
                  double x = MinecraftClient.getInstance().player.getX();
                  double y = MinecraftClient.getInstance().player.getY();
                  double z = MinecraftClient.getInstance().player.getZ();
                  BlockPos pos = new BlockPos((int)(i + x), (int)(j + y), (int)(k + z));
                  list.add(new ValuePair<>(pos, MinecraftClient.getInstance().world.getBlockState(pos).getBlock()));
               }
            }
         }

         return list;
      } else {
         return null;
      }
   }

   public static boolean isValidBlock(Block block) {
      return !(block instanceof FluidBlock)
         && !(block instanceof AirBlock)
         && !(block instanceof ChestBlock)
         && !(block instanceof FurnaceBlock)
         && !(block instanceof LadderBlock)
         && !(block instanceof TntBlock);
   }
}
