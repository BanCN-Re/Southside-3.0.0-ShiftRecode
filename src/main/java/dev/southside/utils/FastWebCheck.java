package dev.southside.utils;

import dev.southside.anticheat.player.PlayerData;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class FastWebCheck implements IHackerPlayerCheck {
   private static final double DEFAULT_DOUBLE = 0.04;
   private static final int DEFAULT_INT = 20;

   @Override
   public void checkPlayer(PlayerData data) {
      PlayerEntity player = data.getPlayer();
      int age = player.age;
      if (IMinecraft.mc.world != null && player.isAlive()) {
         if (this.isValidTarget(player)) {
            double x = player.getVelocity().x;
            double z = player.getVelocity().z;
            double dist = Math.sqrt(x * x + z * z);
            if (dist > 0.04) {
               data.setFastWebCounter(data.getFastWebCounter() + 1);
               if (data.getFastWebCounter() >= 5) {
                  if (age - data.getLastFastWebFlagTick() > 20) {
                     data.addVl(Math.min((float)(dist / 0.04), 5.0F), "FastWeb");
                     data.setLastFastWebFlagTick(age);
                  }

                  data.setFastWebCounter(0);
               }
            }
         } else if (data.getFastWebCounter() > 0) {
            data.setFastWebCounter(data.getFastWebCounter() - 1);
         }
      }
   }

   public boolean isValidTarget(PlayerEntity player) {
      BlockPos blockPos = player.getBlockPos();

      for (int i = -1; i <= 1; i++) {
         for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
               if (IMinecraft.mc.world.getBlockState(blockPos.add(i, j, k)).getBlock() == Blocks.COBWEB) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
