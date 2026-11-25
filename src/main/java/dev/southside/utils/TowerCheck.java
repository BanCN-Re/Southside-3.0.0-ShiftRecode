package dev.southside.utils;

import dev.southside.anticheat.player.PlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class TowerCheck implements IHackerPlayerCheck {
   private static final int DEFAULT_INT = 50;
   private static final int DEFAULT_INT_VALUE = 40;
   private static final int DEFAULT_INT_DATA = 18;
   private static final double DEFAULT_DOUBLE = 0.05;
   private static final double DEFAULT_DOUBLE_VALUE = 0.2;

   @Override
   public void checkPlayer(PlayerData data) {
      PlayerEntity player = data.getPlayer();
      int age = player.age;
      if (data.getTargetBlockPos() != null && !data.getPlacementTimestamps().isEmpty()) {
         if (data.getLastTargetBlockY() != -1 && data.getTargetBlockPos().getY() > data.getLastTargetBlockY()) {
            data.setLastTowerRiseTick(age);
         }

         Vec3d velocity = player.getVelocity();
         double dist = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
         boolean flag = dist > 0.05;
         boolean flag2 = !player.isOnGround() && velocity.y > 0.2;
         boolean flag3 = age - data.getLastTowerRiseTick() > 50;
         if (flag && flag2 && flag3) {
            int n = data.getTowerJumpCount() + 1;
            data.setTowerJumpCount(n);
            if (n >= 3 && age - data.getLastTowerFlagTick() > 40) {
               data.addVl(4.0F, "Scaffold (Jumping)");
               data.setLastTowerFlagTick(age);
               data.setTowerJumpCount(0);
            }
         } else {
            int n2 = data.getTowerDecayTicks() + 1;
            data.setTowerDecayTicks(n2);
            if (n2 >= 18) {
               if (data.getTowerJumpCount() > 0) {
                  data.setTowerJumpCount(data.getTowerJumpCount() - 1);
               }

               data.setTowerDecayTicks(0);
            }
         }
      }
   }
}
