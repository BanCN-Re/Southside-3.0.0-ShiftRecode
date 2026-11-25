package dev.southside.utils;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class RaytraceUtils implements IMinecraft {
   public static BlockHitResult raycast(double d, Rotation rotation, boolean flag, float f) {
      Entity cameraEntity = mc.cameraEntity;
      if (cameraEntity != null && mc.player != null && mc.world != null) {
         Vec3d cameraPosVec = mc.player.getCameraPosVec(f);
         Vec3d vec = computePosition(rotation.pitch, rotation.yaw);
         Vec3d vec2 = cameraPosVec.add(vec.x * d, vec.y * d, vec.z * d);
         return mc.world.raycast(new RaycastContext(cameraPosVec, vec2, ShapeType.OUTLINE, flag ? FluidHandling.ANY : FluidHandling.NONE, cameraEntity));
      } else {
         return null;
      }
   }

   public static boolean isBlockVisible(World world, PlayerEntity player, Rotation rotation, Direction direction, BlockPos pos, boolean flag) {
      Vec3d cameraPosVec = player.getCameraPosVec(1.0F);
      float pitch = rotation.getPitch();
      float yaw = rotation.getYaw();
      float f = (float)Math.toRadians(-yaw - 180.0F);
      float f2 = (float)Math.toRadians(-pitch);
      float f3 = MathHelper.cos(f2) * MathHelper.sin(f);
      float f4 = MathHelper.sin(f2);
      float f5 = MathHelper.cos(f2) * MathHelper.cos(f);
      Vec3d dir = new Vec3d(f3, f4, f5).normalize();
      Vec3d vec = cameraPosVec.add(dir.multiply(4.5));
      BlockHitResult hitResult = world.raycast(new RaycastContext(cameraPosVec, vec, ShapeType.OUTLINE, FluidHandling.NONE, player));
      if (hitResult == null || hitResult.getType() != Type.BLOCK) {
         return false;
      } else {
         return !hitResult.getBlockPos().equals(pos) ? false : !flag || hitResult.getSide().equals(direction);
      }
   }

   public static HitResult raycast(Rotation rotation, double d, float f, Entity entity, boolean flag, float f2, float f3) {
      if (entity != null && mc.world != null) {
         float pitch = rotation.getPitch();
         float yaw = rotation.getYaw();
         Vec3d cameraPosVec = entity.getCameraPosVec(f3);
         float f4 = yaw * (float) (Math.PI / 180.0);
         float f5 = -pitch * (float) (Math.PI / 180.0);
         float f6 = MathHelper.cos(f5);
         float f7 = MathHelper.sin(f5);
         float f8 = MathHelper.cos(f4);
         float f9 = MathHelper.sin(f4);
         Vec3d vec = new Vec3d(f7 * f8, -f9, f6 * f8);
         Vec3d vec2 = cameraPosVec.add(vec.x * d, vec.y * d, vec.z * d);
         HitResult hitResult = entity.getWorld().raycast(new RaycastContext(cameraPosVec, vec2, ShapeType.OUTLINE, FluidHandling.NONE, entity));
         double d2 = d;
         Vec3d cameraPosVec2 = entity.getCameraPosVec(f3);
         if (hitResult != null && hitResult.getType() == Type.BLOCK && !flag) {
            d2 = hitResult.getPos().distanceTo(cameraPosVec2);
         }

         float yaw2 = rotation.getYaw();
         float pitch2 = rotation.getPitch();
         float f10 = yaw2 * (float) (Math.PI / 180.0);
         float f11 = -pitch2 * (float) (Math.PI / 180.0);
         float f12 = MathHelper.cos(f11);
         float f13 = MathHelper.sin(f11);
         float f14 = MathHelper.cos(f10);
         float f15 = MathHelper.sin(f10);
         Vec3d vec3 = new Vec3d(f13 * f14, -f15, f12 * f14);
         Vec3d vec4 = cameraPosVec2.add(vec3.x * d, vec3.y * d, vec3.z * d);
         Entity entity2 = null;
         Vec3d vec5 = null;
         List otherEntities = mc.world
            .getOtherEntities(
               entity,
               entity.getBoundingBox().stretch(vec3.x * d, vec3.y * d, vec3.z * d).expand(1.0),
               EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(Entity::canHit)
            );
         double d3 = d2;

         for (Object obj : otherEntities) {
            Entity entity3 = (Entity)obj;
            if (!entity3.getUuid().equals(mc.player.getUuid())) {
               float f16 = entity3.getTargetingMargin() + f;
               Box box = entity3.getBoundingBox().expand(f16);
               if (f2 != 0.0F) {
                  box = box.offset((entity3.getX() - entity3.prevX) * f2, (entity3.getY() - entity3.prevY) * f2, (entity3.getZ() - entity3.prevZ) * f2);
               }

               Optional hitResult2 = box.raycast(cameraPosVec2, vec4);
               BlockHitResult hitResult3 = null;
               if (hitResult2.isPresent()) {
                  Vec3d vec6 = (Vec3d)hitResult2.get();
                  hitResult3 = new BlockHitResult(vec6, Direction.getFacing(vec3.x, vec3.y, vec3.z), BlockPos.ofFloored(vec6), false);
               }

               if (box.contains(cameraPosVec2)) {
                  if (d3 >= 0.0) {
                     entity2 = entity3;
                     Vec3d vec7 = hitResult3 == null ? cameraPosVec2 : hitResult3.getPos();
                     vec5 = vec7;
                     d3 = 0.0;
                  }
               } else if (hitResult3 != null) {
                  double dist = cameraPosVec2.distanceTo(hitResult3.getPos());
                  if (dist < d3 || d3 == 0.0) {
                     entity2 = entity3;
                     vec5 = hitResult3.getPos();
                     d3 = dist;
                  }
               }
            }
         }

         if (entity2 != null && (d3 < d2 || hitResult == null)) {
            hitResult = new EntityHitResult(entity2, vec5);
         }

         return (HitResult)hitResult;
      } else {
         return null;
      }
   }

   public static BlockHitResult raycast(double d, Rotation rotation, BlockPos pos, BlockState state) {
      Entity cameraEntity = mc.cameraEntity;
      if (cameraEntity == null) {
         return null;
      }

      Vec3d eyePos = cameraEntity.getEyePos();
      float f = MathHelper.cos(-rotation.yaw * (float) (Math.PI / 180.0));
      float f2 = MathHelper.sin(-rotation.yaw * (float) (Math.PI / 180.0));
      float f3 = MathHelper.cos(rotation.pitch * (float) (Math.PI / 180.0));
      float f4 = MathHelper.sin(rotation.pitch * (float) (Math.PI / 180.0));
      Vec3d vec = new Vec3d(f2 * f3, -f4, f * f3);
      Vec3d vec2 = eyePos.add(vec.x * d, vec.y * d, vec.z * d);
      return mc.world.raycastBlock(eyePos, vec2, pos, state.getOutlineShape(mc.world, pos, ShapeContext.of(mc.player)), state);
   }

   public static boolean isValid(Entity entity, Entity entity2) {
      return entity2 != entity && EntityPredicates.EXCEPT_SPECTATOR.test(entity2) && entity2.canHit();
   }

   public static HitResult raycastHit(Rotation rotation, double d) {
      float f = 0.0F;
      return raycast(rotation, d, f, mc.player);
   }

   public static boolean isBlockVisible(Entity entity, Entity entity2, Rotation rotation, double d, double d2) {
      Vec3d eyePos = entity.getEyePos();
      float f = MathHelper.cos(-rotation.yaw * (float) (Math.PI / 180.0));
      float f2 = MathHelper.sin(-rotation.yaw * (float) (Math.PI / 180.0));
      float f3 = MathHelper.cos(rotation.pitch * (float) (Math.PI / 180.0));
      float f4 = MathHelper.sin(rotation.pitch * (float) (Math.PI / 180.0));
      Vec3d vec = new Vec3d(f2 * f3, -f4, f * f3);
      double d3 = d * d;
      double d4 = d2 * d2;
      Vec3d vec2 = eyePos.add(vec.x * d, vec.y * d, vec.z * d);
      Box box = entity.getBoundingBox().stretch(vec.multiply(d)).expand(1.0, 1.0, 1.0);
      EntityHitResult hitResult = ProjectileUtil.raycast(entity, eyePos, vec2, box, entity3 -> isValidEx(entity2, entity3), d3);
      if (hitResult == null) {
         return false;
      }

      double dist = eyePos.squaredDistanceTo(hitResult.getPos());
      if (dist <= d3) {
         Vec3d pos = hitResult.getPos();
         boolean flag = mc.world.raycast(new RaycastContext(eyePos, pos, ShapeType.OUTLINE, FluidHandling.NONE, mc.player)).getType() == Type.MISS;
         if (flag) {
            return true;
         }
      }

      return dist <= d4;
   }

   public static HitResult raycast(double x, float y, float z) {
      if (mc.player != null && mc.world != null) {
         Vec3d cameraPosVec = mc.player.getCameraPosVec(1.0F);
         Rotation rotation = new Rotation(y, z);
         float f = (float)Math.cos(-rotation.getYaw() * (float) (Math.PI / 180.0) - (float) Math.PI);
         float f2 = (float)Math.sin(-rotation.getYaw() * (float) (Math.PI / 180.0) - (float) Math.PI);
         float f3 = (float)(-Math.cos(-rotation.getPitch() * (float) (Math.PI / 180.0)));
         float f4 = (float)Math.sin(-rotation.getPitch() * (float) (Math.PI / 180.0));
         Vec3d vec = new Vec3d(f2 * f3, f4, f * f3);
         Vec3d vec2 = cameraPosVec.add(vec.x * x, vec.y * x, vec.z * x);
         return mc.world.raycast(new RaycastContext(cameraPosVec, vec2, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
      } else {
         return null;
      }
   }

   public static BlockHitResult raycast(Vec3d vec, Vec3d vec2) {
      BlockHitResult hitResult = mc.world.raycast(new RaycastContext(vec, vec2, ShapeType.COLLIDER, FluidHandling.ANY, mc.player));
      return hitResult != null && hitResult.getType() == Type.BLOCK ? hitResult : null;
   }

   public static HitResult raycast(Rotation rotation, double d, float f, Entity entity) {
      if (entity != null && mc.world != null) {
         float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
         Vec3d cameraPosVec = entity.getCameraPosVec(tickDelta);
         float f2 = (float)Math.cos(-rotation.getYaw() * (float) (Math.PI / 180.0) - (float) Math.PI);
         float f3 = (float)Math.sin(-rotation.getYaw() * (float) (Math.PI / 180.0) - (float) Math.PI);
         float f4 = (float)(-Math.cos(-rotation.getPitch() * (float) (Math.PI / 180.0)));
         float f5 = (float)Math.sin(-rotation.getPitch() * (float) (Math.PI / 180.0));
         Vec3d vec = new Vec3d(f3 * f4, f5, f2 * f4);
         Vec3d vec2 = cameraPosVec.add(vec.x * d, vec.y * d, vec.z * d);
         double d2 = d + f;
         Box box = new Box(cameraPosVec.x - d2, cameraPosVec.y - d2, cameraPosVec.z - d2, cameraPosVec.x + d2, cameraPosVec.y + d2, cameraPosVec.z + d2);
         List entitiesByClass = mc.world.getEntitiesByClass(Entity.class, box, entity2 -> isValid(entity, entity2));
         Entity entity3 = null;
         Vec3d vec3 = null;
         double d3 = d * d;

         for (Object obj : entitiesByClass) {
            Entity entity4 = (Entity)obj;
            Box box2 = entity4.getBoundingBox().expand(f);
            Optional hitResult = box2.raycast(cameraPosVec, vec2);
            if (hitResult.isPresent()) {
               Vec3d vec4 = (Vec3d)hitResult.get();
               double dist = cameraPosVec.squaredDistanceTo(vec4);
               if (dist < d3) {
                  d3 = dist;
                  entity3 = entity4;
                  vec3 = vec4;
               }
            }
         }

         return (HitResult)(entity3 != null
            ? new EntityHitResult(entity3, vec3)
            : mc.world.raycast(new RaycastContext(cameraPosVec, vec2, ShapeType.OUTLINE, FluidHandling.NONE, entity)));
      } else {
         return null;
      }
   }

   public static boolean isValidEx(Entity entity, Entity entity2) {
      return !entity2.isSpectator() && entity2.canHit() && entity2 == entity;
   }

   public static Vec3d computePosition(float x, float y) {
      float f = x * (float) (Math.PI / 180.0);
      float f2 = -y * (float) (Math.PI / 180.0);
      float f3 = MathHelper.cos(f2);
      float f4 = MathHelper.sin(f2);
      float f5 = MathHelper.cos(f);
      float f6 = MathHelper.sin(f);
      return new Vec3d(f4 * f5, -f6, f3 * f5);
   }

   public static boolean isBlockVisible(Vec3d vec, Vec3d vec2, BlockPos pos, Direction direction, Double val) {
      BlockHitResult hitResult = mc.world.raycast(new RaycastContext(vec, vec2, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
      if (hitResult.getType() == Type.BLOCK && (direction == null || hitResult.getSide() == direction)) {
         return val != null && hitResult.getPos().squaredDistanceTo(vec) > val * val ? false : hitResult.getBlockPos().equals(pos);
      } else {
         return false;
      }
   }

   public static boolean isValidTarget(Entity entity) {
      return !entity.isSpectator() && entity.canHit();
   }

   @FunctionalInterface
   public interface Inner {
      boolean isValid();
   }
}
