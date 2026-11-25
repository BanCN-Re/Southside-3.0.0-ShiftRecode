package dev.southside.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class EntityDistanceUtils {
   public static double computeDistance(PlayerEntity player, Entity entity) {
      Vec3d eyePos = player.getEyePos();
      Box boundingBox = entity.getBoundingBox();
      double x = eyePos.x;
      double y = eyePos.y;
      double z = eyePos.z;
      double minX = boundingBox.minX;
      double minY = boundingBox.minY;
      double minZ = boundingBox.minZ;
      double maxX = boundingBox.maxX;
      double maxY = boundingBox.maxY;
      double maxZ = boundingBox.maxZ;
      double d = Math.max(minX, Math.min(x, maxX));
      double d2 = Math.max(minY, Math.min(y, maxY));
      double d3 = Math.max(minZ, Math.min(z, maxZ));
      Vec3d vec = new Vec3d(d, d2, d3);
      return eyePos.distanceTo(vec);
   }
}
