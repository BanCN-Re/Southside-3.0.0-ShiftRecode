package dev.southside.anticheat.check.impl;

import dev.southside.anticheat.player.PlayerData;
import dev.southside.utils.IHackerPlayerCheck;
import dev.southside.utils.IMinecraft;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ScaffoldBatchCheck implements IHackerPlayerCheck {
   private static final int DEFAULT_INT = 6;
   private static final int DEFAULT_INT_VALUE = 60;
   private static final double DEFAULT_DOUBLE = 4.0;
   private static final long e = 3000L;

   public double calculateExpectedDelay(boolean flag, boolean flag2, boolean flag3, float f) {
      if (!flag && flag2 && flag3) {
         double d = 110.0 * Math.abs(Math.cos(2.0 * Math.toRadians(f)));
         return 328.0 + d;
      } else {
         return 238.0;
      }
   }

   public boolean isValid(PlayerEntity player, BlockPos pos) {
      Vec3d pos2 = player.getPos();
      double d = pos2.x - (pos.getX() + 0.5);
      double d2 = pos2.y - (pos.getY() + 0.5);
      double d3 = pos2.z - (pos.getZ() + 0.5);
      return Math.sqrt(d * d + d2 * d2 + d3 * d3) <= 4.0;
   }

   public int computeInt(BlockPos pos) {
      int n = 0;

      for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
         BlockPos pos2 = pos.offset(direction);
         if (IMinecraft.mc.world.getBlockState(pos2).isSolidBlock(IMinecraft.mc.world, pos2)) {
            n++;
         }
      }

      return n;
   }

   public void checkPlayer(PlayerData data, int n) {
      if (n - data.getLastScaffoldSpeedFlagTick() >= 60) {
         List placementTimestamps = data.getPlacementTimestamps();
         List<Boolean> placementSneaking = data.getPlacementSneaking();
         if (placementTimestamps.size() >= 2) {
            long l = placementSneaking.stream().filter(val -> !val).count();
            boolean flag = l >= placementSneaking.size() / 2;
            double d = 0.0;
            double d2 = 0.0;
            int n2 = 0;

            for (int i = 1; i < placementTimestamps.size(); i++) {
               long l2 = (Long)placementTimestamps.get(i) - (Long)placementTimestamps.get(i - 1);
               boolean flag2 = (Boolean)placementSneaking.get(i);
               boolean flag3 = (Boolean)placementSneaking.get(i - 1);
               double d3 = this.calculateExpectedDelay(flag, flag2, flag3, data.getPlayer().getYaw());
               d += l2;
               d2 += d3;
               n2++;
            }

            if (n2 != 0) {
               double d4 = d / n2;
               double d5 = d2 / n2;
               if (d4 < d5) {
                  double d6 = d5 - d4;
                  float f = Math.min(5.0F, (float)(1.1 * d6 / 50.0 + 1.0));
                  data.addVl(f, "Scaffold (Speed: " + String.format("%.0f", d4) + "ms<" + String.format("%.0f", d5) + "ms)");
                  data.setLastScaffoldSpeedFlagTick(n);
               }
            }
         }
      }
   }

   @Override
   public void checkPlayer(PlayerData data) {
      PlayerEntity player = data.getPlayer();
      int age = player.age;
      if (IMinecraft.mc.world != null && player.isAlive()) {
         BlockPos pos = player.getBlockPos().down();
         BlockPos pos2 = null;

         for (Direction direction : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos pos3 = direction == Direction.DOWN ? pos : pos.offset(direction);
            BlockState blockState = IMinecraft.mc.world.getBlockState(pos3);
            if (blockState.isSolidBlock(IMinecraft.mc.world, pos3) && player.getY() > pos3.getY() && this.isValid(player, pos3)) {
               int n = this.computeInt(pos3);
               if (n == 1 && (data.getTargetBlockPos() == null || !data.getTargetBlockPos().equals(pos3))) {
                  pos2 = pos3;
                  break;
               }
            }
         }

         if (pos2 != null) {
            long now = System.currentTimeMillis();
            List placementTimestamps = data.getPlacementTimestamps();
            List placementSneaking = data.getPlacementSneaking();
            if (data.getTargetBlockPos() != null) {
               data.setLastTargetBlockY(data.getTargetBlockPos().getY());
            }

            placementTimestamps.add(now);
            placementSneaking.add(player.isSneaking());
            data.setTargetBlockPos(pos2);
            if (placementTimestamps.size() >= 6) {
               this.checkPlayer(data, age);
               long last = (Long)placementTimestamps.getLast();
               boolean last2 = (Boolean)placementSneaking.getLast();
               placementTimestamps.clear();
               placementSneaking.clear();
               placementTimestamps.add(last);
               placementSneaking.add(last2);
            }
         }

         if (!data.getPlacementTimestamps().isEmpty()) {
            long last3 = data.getPlacementTimestamps().getLast();
            if (System.currentTimeMillis() - last3 > e) {
               data.getPlacementTimestamps().clear();
               data.getPlacementSneaking().clear();
               data.setTargetBlockPos(null);
               data.setLastTargetBlockY(-1);
            }
         }
      }
   }
}
