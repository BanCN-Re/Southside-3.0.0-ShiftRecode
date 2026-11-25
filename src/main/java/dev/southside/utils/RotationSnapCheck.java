package dev.southside.utils;

import dev.southside.anticheat.player.PlayerData;
import java.util.LinkedList;
import net.minecraft.entity.player.PlayerEntity;

public class RotationSnapCheck implements IHackerPlayerCheck {
   private static final double DEFAULT_DOUBLE = 35.0;
   private static final double DEFAULT_DOUBLE_VALUE = 360.0;
   private static final double DEFAULT_DOUBLE_DATA = 70.0;
   private static final double DEFAULT_DOUBLE_REF = 20.0;
   private static final int DEFAULT_INT = 10;
   private static final int DEFAULT_INT_VALUE = 40;
   public static boolean active = true;

   public boolean isEmpty(PlayerData data) {
      return data.getTargetBlockPos() != null && !data.getPlacementTimestamps().isEmpty();
   }

   @Override
   public void checkPlayer(PlayerData data) {
      PlayerEntity player = data.getPlayer();
      int age = player.age;
      float yaw = player.getYaw();
      LinkedList yawHistory = data.getYawHistory();
      yawHistory.addLast(yaw);
      if (yawHistory.size() > 10) {
         yawHistory.removeFirst();
      }

      if (this.isEmpty(data) && yawHistory.size() >= 4) {
         if (age - data.getLastTowerFlagTick() >= 40 || age - data.getLastScaffoldSpeedFlagTick() >= 40) {
            double[] coords = new double[yawHistory.size() - 1];

            for (int i = 0; i < coords.length; i++) {
               coords[i] = computeDouble((Float)yawHistory.get(i + 1) - (Float)yawHistory.get(i));
            }

            boolean flag = false;

            for (int j = Math.max(0, coords.length - 2); j < coords.length; j++) {
               if (Math.abs(coords[j]) > 35.0) {
                  flag = true;
                  break;
               }
            }

            if (flag) {
               data.setRotationSnapCounter(data.getRotationSnapCounter() + 1);
               if (data.getRotationSnapCounter() >= 3) {
                  data.addVl(3.0F, "Scaffold (Rotation Snap)");
                  data.setRotationSnapCounter(0);
               }
            } else if (data.getRotationSnapCounter() > 0) {
               data.setRotationSnapCounter(data.getRotationSnapCounter() - 1);
            }

            double d = 0.0;

            for (double d2 : coords) {
               d += Math.abs(d2);
            }

            if (d > 360.0) {
               data.setRotationSumFlags(data.getRotationSumFlags() + 1);
               if (data.getRotationSumFlags() >= 2) {
                  data.addVl(2.0F, "Scaffold (Rotation Sum)");
                  data.setRotationSumFlags(0);
               }
            } else if (data.getRotationSumFlags() > 0) {
               data.setRotationSumFlags(data.getRotationSumFlags() - 1);
            }

            double d3 = d / coords.length;
            double d4 = 0.0;

            for (double d5 : coords) {
               double d6 = Math.abs(d5) - d3;
               d4 += d6 * d6;
            }

            double d7 = d4 / coords.length;
            if (d7 > 70.0) {
               data.setRotationVarianceFlags(data.getRotationVarianceFlags() + 1);
               if (data.getRotationVarianceFlags() >= 3) {
                  data.addVl(1.5F, "Scaffold (Rotation Variance)");
                  data.setRotationVarianceFlags(0);
               }
            } else if (data.getRotationVarianceFlags() > 0) {
               data.setRotationVarianceFlags(data.getRotationVarianceFlags() - 1);
            }

            int n = 0;

            for (double d8 : coords) {
               if (Math.abs(d8) > 20.0) {
                  n++;
               }
            }

            if (n > 4) {
               data.setRotationSwitchCounter(data.getRotationSwitchCounter() + 1);
               if (data.getRotationSwitchCounter() >= 2) {
                  data.addVl(1.5F, "Scaffold (Rotation Switch)");
                  data.setRotationSwitchCounter(0);
               }
            } else if (data.getRotationSwitchCounter() > 0) {
               data.setRotationSwitchCounter(data.getRotationSwitchCounter() - 1);
            }
         }
      }
   }

   public static void setActive(boolean flag) {
      active = flag;
   }

   public static double computeDouble(double d) {
      double d2 = d % 360.0;
      if (d2 > 180.0) {
         d2 -= 360.0;
      }

      if (d2 < -180.0) {
         d2 += 360.0;
      }

      return d2;
   }
}
