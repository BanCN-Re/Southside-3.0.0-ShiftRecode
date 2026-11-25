package dev.southside.utils;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BoxRaytraceUtils {
   public static double computeDouble(double x, double y, double z) {
      return x <= y ? y : Math.min(x, z);
   }

   public static Vec3d computePosition(Vec3d vec, Vec3d vec2, double d) {
      double dx = vec2.x - vec.x;
      double dy = vec2.y - vec.y;
      double dz = vec2.z - vec.z;
      if (dz * dz < 1.0E-7F) {
         return null;
      }

      double d2 = (d - vec.z) / dz;
      return d2 >= 0.0 && d2 <= 1.0 ? new Vec3d(vec.x + dx * d2, vec.y + dy * d2, vec.z + dz * d2) : null;
   }

   public static Vec3d computePositionEx(Vec3d vec, Vec3d vec2, double d) {
      double dx = vec2.x - vec.x;
      double dy = vec2.y - vec.y;
      double dz = vec2.z - vec.z;
      if (dy * dy < 1.0E-7F) {
         return null;
      }

      double d2 = (d - vec.y) / dy;
      return d2 >= 0.0 && d2 <= 1.0 ? new Vec3d(vec.x + dx * d2, vec.y + dy * d2, vec.z + dz * d2) : null;
   }

   public static BlockHitResult raycastHit(Box box, Vec3d vec, Rotation rotation, double d) {
      float f = MathHelper.cos(-rotation.yaw * (float) (Math.PI / 180.0));
      float f2 = MathHelper.sin(-rotation.yaw * (float) (Math.PI / 180.0));
      float f3 = MathHelper.cos(rotation.pitch * (float) (Math.PI / 180.0));
      float f4 = MathHelper.sin(rotation.pitch * (float) (Math.PI / 180.0));
      Vec3d vec2 = new Vec3d(f2 * f3, -f4, f * f3);
      Vec3d vec3 = vec.add(vec2.x * d, vec2.y * d, vec2.z * d);
      Vec3d vec4 = computePositionAlt(vec, vec3, box.minX);
      Vec3d vec5 = computePositionAlt(vec, vec3, box.maxX);
      Vec3d vec6 = computePositionEx(vec, vec3, box.minY);
      Vec3d vec7 = computePositionEx(vec, vec3, box.maxY);
      Vec3d vec8 = computePosition(vec, vec3, box.minZ);
      Vec3d vec9 = computePosition(vec, vec3, box.maxZ);
      if (!isValidAlt(box, vec4)) {
         vec4 = null;
      }

      if (!isValidAlt(box, vec5)) {
         vec5 = null;
      }

      if (!isValidEx(box, vec6)) {
         vec6 = null;
      }

      if (!isValidEx(box, vec7)) {
         vec7 = null;
      }

      if (!isValid(box, vec8)) {
         vec8 = null;
      }

      if (!isValid(box, vec9)) {
         vec9 = null;
      }

      Vec3d vec10 = vec4;
      if (vec5 != null && (vec10 == null || vec.squaredDistanceTo(vec5) < vec.squaredDistanceTo(vec10))) {
         vec10 = vec5;
      }

      if (vec6 != null && (vec10 == null || vec.squaredDistanceTo(vec6) < vec.squaredDistanceTo(vec10))) {
         vec10 = vec6;
      }

      if (vec7 != null && (vec10 == null || vec.squaredDistanceTo(vec7) < vec.squaredDistanceTo(vec10))) {
         vec10 = vec7;
      }

      if (vec8 != null && (vec10 == null || vec.squaredDistanceTo(vec8) < vec.squaredDistanceTo(vec10))) {
         vec10 = vec8;
      }

      if (vec9 != null && (vec10 == null || vec.squaredDistanceTo(vec9) < vec.squaredDistanceTo(vec10))) {
         vec10 = vec9;
      }

      if (vec10 == null) {
         return null;
      }

      Direction direction;
      if (vec10 == vec4) {
         direction = Direction.WEST;
      } else if (vec10 == vec5) {
         direction = Direction.EAST;
      } else if (vec10 == vec6) {
         direction = Direction.DOWN;
      } else if (vec10 == vec7) {
         direction = Direction.UP;
      } else if (vec10 == vec8) {
         direction = Direction.NORTH;
      } else {
         direction = Direction.SOUTH;
      }

      return new BlockHitResult(vec10, direction, BlockPos.ORIGIN, false);
   }

   public static boolean isValid(Box box, Vec3d vec) {
      return vec != null && vec.x >= box.minX && vec.x <= box.maxX && vec.y >= box.minY && vec.y <= box.maxY;
   }

   public static boolean isValidEx(Box box, Vec3d vec) {
      return vec != null && vec.x >= box.minX && vec.x <= box.maxX && vec.z >= box.minZ && vec.z <= box.maxZ;
   }

   public static Vec3d computePosition(Vec3d vec, Box box) {
      double clamped = MathHelper.clamp(vec.x, box.minX, box.maxX);
      double clamped2 = MathHelper.clamp(vec.y, box.minY, box.maxY);
      double clamped3 = MathHelper.clamp(vec.z, box.minZ, box.maxZ);
      return new Vec3d(clamped, clamped2, clamped3);
   }

   public static boolean isValidAlt(Box box, Vec3d vec) {
      return vec != null && vec.y >= box.minY && vec.y <= box.maxY && vec.z >= box.minZ && vec.z <= box.maxZ;
   }

   public static Vec3d computePositionAlt(Vec3d vec, Vec3d vec2, double d) {
      double dx = vec2.x - vec.x;
      double dy = vec2.y - vec.y;
      double dz = vec2.z - vec.z;
      if (dx * dx < 1.0E-7F) {
         return null;
      }

      double d2 = (d - vec.x) / dx;
      return d2 >= 0.0 && d2 <= 1.0 ? new Vec3d(vec.x + dx * d2, vec.y + dy * d2, vec.z + dz * d2) : null;
   }
}
