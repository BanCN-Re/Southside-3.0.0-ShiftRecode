package dev.southside.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockRotationHelper {
   public static final MinecraftClient mc = MinecraftClient.getInstance();
   public static final float DEFAULT_FLOAT = 0.15F;
   public static final float DEFAULT_FLOAT_VALUE = 0.1F;
   public static Rotation rotation = null;
   public static Rotation rotationValue = null;
   public static boolean active = false;

   public static Rotation computeRotation(BlockPos pos, Direction direction) {
      if (mc.player == null) {
         return null;
      }

      Vec3d vec = Vec3d.ofCenter(pos).add(direction.getOffsetX() * 0.5, direction.getOffsetY() * 0.5, direction.getOffsetZ() * 0.5);
      Vec3d eyePos = mc.player.getEyePos();
      Vec3d delta = vec.subtract(eyePos);
      float yaw = mc.player.getYaw();
      double d = Math.toRadians(yaw);
      double d2 = delta.x * -Math.sin(d) + delta.z * Math.cos(d);
      float f = (float)Math.toDegrees(-Math.atan2(delta.y, Math.abs(d2)));
      return new Rotation(yaw, f);
   }

   public static void performAction() {
      active = false;
      rotation = null;
   }

   public static Rotation computeRotation(BlockPos pos, Direction direction, Rotation rotation2, float f) {
      if (mc.player == null) {
         return null;
      }

      Rotation rotation3 = computeRotationAlt(pos, direction);
      return rotation3 == null ? null : computeRotation(rotation2, rotation3, f);
   }

   public static float evaluate(float f) {
      float f2 = f % 360.0F;
      if (f2 >= 180.0F) {
         f2 -= 360.0F;
      }

      if (f2 < -180.0F) {
         f2 += 360.0F;
      }

      return f2;
   }

   public static Rotation computeRotation(BlockPos pos, Direction direction, float f, float f2) {
      if (mc.player == null) {
         return null;
      }

      Rotation rotation2 = computeRotationAlt(pos, direction);
      if (rotation2 == null) {
         return null;
      }

      Rotation rotation3 = new Rotation(mc.player.getYaw(), mc.player.getPitch());
      float f3 = evaluate(rotation2.getYaw() - rotation3.getYaw());
      float f4 = rotation2.getPitch() - rotation3.getPitch();
      f3 = MathHelper.clamp(f3, -f, f);
      f4 = MathHelper.clamp(f4, -f2, f2);
      float f5 = rotation3.getYaw() + f3;
      float clamped = MathHelper.clamp(rotation3.getPitch() + f4, -90.0F, 90.0F);
      return new Rotation(f5, clamped);
   }

   public static void updateBlock(BlockPos pos, Direction direction) {
      if (mc.player != null) {
         rotation = computeRotationAlt(pos, direction);
         rotationValue = new Rotation(mc.player.getYaw(), mc.player.getPitch());
         active = true;
      }
   }

   public static Rotation computeRotation(BlockPos pos, Direction direction, Rotation rotation2) {
      if (mc.player == null) {
         return null;
      }

      Rotation rotation3 = computeRotationAlt(pos, direction);
      return rotation3 == null ? null : computeRotation(rotation2, rotation3, 0.15F);
   }

   public static Rotation computeRotation(Rotation rotation2) {
      if (active && mc.player != null && rotation != null) {
         rotationValue = rotation2;
         Rotation rotation3 = computeRotation(rotationValue, rotation, 0.15F);
         Rotation rotation4 = rotation;
         float pitch = rotation4.pitch;
         float yaw = rotation3.yaw;
         if ((float)Math.hypot(((yaw - pitch) % 360.0F + 540.0F) % 360.0F - 180.0F, rotation3.pitch - rotation4.pitch) < 0.1F) {
            active = false;
            Rotation rotation5 = rotation;
            return new Rotation(rotation5.yaw, rotation5.pitch, rotation5.targetPosition, rotation5.sourcePosition);
         } else {
            return rotation3;
         }
      } else {
         return null;
      }
   }

   public static boolean isActive() {
      return active;
   }

   public static Rotation computeRotationEx(BlockPos pos, Direction direction, Rotation rotation2, float f) {
      if (mc.player == null) {
         return null;
      }

      Rotation rotation3 = computeRotation(pos, direction);
      return rotation3 == null ? null : computeRotation(rotation2, rotation3, f);
   }

   public static Rotation computeRotationAlt(BlockPos pos, Direction direction, Rotation rotation2, float f) {
      if (mc.player == null) {
         return null;
      }

      Rotation rotation3 = computeRotationEx(pos, direction);
      return rotation3 == null ? null : computeRotation(rotation2, rotation3, f);
   }

   public static Rotation computeRotationEx(BlockPos pos, Direction direction) {
      if (mc.player == null) {
         return null;
      }

      Vec3d vec = Vec3d.ofCenter(pos).add(direction.getOffsetX() * 0.501, direction.getOffsetY() * 0.301, direction.getOffsetZ() * 0.501);
      Vec3d eyePos = mc.player.getEyePos();
      double dx = vec.x - eyePos.x;
      double dy = vec.y - eyePos.y;
      double dz = vec.z - eyePos.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float f = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float f2 = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return new Rotation(f, f2, vec, eyePos);
   }

   public static Rotation computeRotation(Rotation rotation2, Rotation rotation3, float f) {
      float f2 = evaluate(rotation3.getYaw() - rotation2.getYaw());
      float f3 = rotation3.getPitch() - rotation2.getPitch();
      if (Math.abs(f2) < 0.1F && Math.abs(f3) < 0.1F) {
         return new Rotation(rotation3.yaw, rotation3.pitch, rotation3.targetPosition, rotation3.sourcePosition);
      }

      float f4 = rotation2.getYaw() + f2 * f;
      float f5 = rotation2.getPitch() + f3 * f;
      f5 = MathHelper.clamp(f5, -90.0F, 90.0F);
      return new Rotation(f4, f5);
   }

   public static Rotation computeRotationAlt(BlockPos pos, Direction direction) {
      if (mc.player == null) {
         return null;
      }

      Vec3d vec = Vec3d.ofCenter(pos).add(direction.getOffsetX() * 0.501, direction.getOffsetY() * 0.501, direction.getOffsetZ() * 0.501);
      Vec3d eyePos = mc.player.getEyePos();
      double dx = vec.x - eyePos.x;
      double dy = vec.y - eyePos.y;
      double dz = vec.z - eyePos.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float f = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float f2 = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return new Rotation(f, f2, vec, eyePos);
   }
}
