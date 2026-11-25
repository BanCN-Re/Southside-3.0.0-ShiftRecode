package dev.southside.utils;

import dev.southside.anticheat.player.PlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class BlinkStutterCheck implements IHackerPlayerCheck {
   private static final double DEFAULT_DOUBLE = 0.01;
   private static final int DEFAULT_INT = 5;
   private static final double DEFAULT_DOUBLE_VALUE = 1.5;
   private static final int DEFAULT_INT_VALUE = 30;

   @Override
   public void checkPlayer(PlayerData data) {
      PlayerEntity player = data.getPlayer();
      int age = player.age;
      Vec3d pos = player.getPos();
      Vec3d lastPosition = data.getLastPosition();
      if (lastPosition == null) {
         data.setLastPosition(pos);
      } else {
         double dx = pos.x - lastPosition.x;
         double dz = pos.z - lastPosition.z;
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (dist < 0.01 && player.isAlive() && !player.hasVehicle()) {
            data.setBlinkCounter(data.getBlinkCounter() + 1);
         } else if (data.getBlinkCounter() >= 5) {
            if (dist > 1.5 && age - data.getLastBlinkTick() > 30) {
               float f = Math.min(data.getBlinkCounter() / 10.0F, 3.0F);
               float f2 = Math.min((float)(dist / 1.5), 3.0F);
               data.addVl(1.0F + f + f2, "Blink");
               data.setLastBlinkTick(age);
               data.setStutterCounter(0);
            }

            data.setBlinkCounter(0);
         } else {
            if (data.getBlinkCounter() >= 2 && dist > 0.5) {
               data.setStutterCounter(data.getStutterCounter() + 1);
               if (data.getStutterCounter() >= 4 && age - data.getLastBlinkTick() > 30) {
                  data.addVl(2.0F, "Blink (Stutter)");
                  data.setLastBlinkTick(age);
                  data.setStutterCounter(0);
               }
            } else if (dist > 0.01 && data.getStutterCounter() > 0) {
               data.setStutterCounter(data.getStutterCounter() - 1);
            }

            data.setBlinkCounter(0);
         }

         data.setLastPosition(pos);
      }
   }
}
