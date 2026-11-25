package dev.southside.utils;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class EntityPositionHistory implements IMinecraft {
   private static final int DEFAULT_INT;
   private static final double DEFAULT_DOUBLE = 0.91;
   private static final double DEFAULT_DOUBLE_VALUE = 0.08;
   private static final Map<UUID, ArrayDeque<Vec3d>> arrayDequeMap = new HashMap<>();

   private EntityPositionHistory() {
   }

   public static void clear() {
      arrayDequeMap.clear();
   }

   public static Rotation computeRotation(Vec3d vec) {
      double x = mc.player.getX();
      double d = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
      double z = mc.player.getZ();
      double d2 = vec.x - x;
      double d3 = vec.z - z;
      double d4 = vec.y + 1.0 - d;
      double dist = Math.sqrt(d2 * d2 + d3 * d3);
      float f = (float)(Math.atan2(d3, d2) * 180.0 / 3.141592653589793) - 90.0F;
      float f2 = (float)dist;
      float f3 = (float)d4;
      float f4 = 1.0F;
      float f5 = 0.006F;
      float f6 = f4 * f4 * f4 * f4 - f5 * (f5 * f2 * f2 + 2.0F * f3 * f4 * f4);
      float f7 = -((float)Math.toDegrees(Math.atan((f4 * f4 - Math.sqrt(f6)) / (f5 * f2))));
      if (Float.isNaN(f7)) {
         f7 = (float)(-Math.atan2(d4, dist) * 180.0 / 3.141592653589793);
      }

      return new Rotation(f, f7);
   }

   public static void remove(UUID uuid) {
      arrayDequeMap.remove(uuid);
   }

   public static Vec3d computePosition(LivingEntity living) {
      ArrayDeque arrayDeque = arrayDequeMap.get(living.getUuid());
      if (arrayDeque != null && arrayDeque.size() >= 2) {
         Vec3d vec = (Vec3d)arrayDeque.peekFirst();
         Vec3d vec2 = (Vec3d)arrayDeque.peekLast();
         int n = arrayDeque.size() - 1;
         return new Vec3d((vec2.x - vec.x) / n, (vec2.y - vec.y) / n, (vec2.z - vec.z) / n);
      } else {
         return new Vec3d(living.getX() - living.prevX, living.getY() - living.prevY, living.getZ() - living.prevZ);
      }
   }

   public static Vec3d computePosition(LivingEntity living, int n, boolean flag) {
      Vec3d vec = computePosition(living);
      double x = living.getX();
      double y = living.getY();
      double z = living.getZ();
      double x2 = vec.x;
      double y2 = vec.y;
      double z2 = vec.z;
      boolean isOnGround = living.isOnGround();

      for (int i = 0; i < n; i++) {
         x += x2;
         z += z2;
         if (isOnGround) {
            x2 *= 0.91;
            z2 *= 0.91;
            if (y2 <= 0.0) {
               y2 = 0.0;
            }
         }

         y += y2;
         if (!isOnGround) {
            y2 -= 0.08;
         }
      }

      Vec3d vec2 = new Vec3d(x, y, z);
      if (flag && mc.world != null) {
         Vec3d vec3 = living.getPos().add(0.0, living.getHeight() / 2.0, 0.0);
         Vec3d vec4 = vec2.add(0.0, living.getHeight() / 2.0, 0.0);
         BlockHitResult hitResult = mc.world.raycast(new RaycastContext(vec3, vec4, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
         if (hitResult.getType() == Type.BLOCK) {
            vec2 = new Vec3d(hitResult.getPos().x, y, hitResult.getPos().z);
         }
      }

      return vec2;
   }

   public static void updateEntity(LivingEntity living) {
      ArrayDeque arrayDeque = arrayDequeMap.computeIfAbsent(living.getUuid(), uuid -> new ArrayDeque<>());
      arrayDeque.addLast(living.getPos());

      while (arrayDeque.size() > 5) {
         arrayDeque.removeFirst();
      }
   }

   static {
      long l = 5862999001997539785L;
      long l2 = -1780966474668017204L ^ l;
      byte b = -1;
      long l3 = l2;
      DEFAULT_INT = (int)l3;
   }
}
