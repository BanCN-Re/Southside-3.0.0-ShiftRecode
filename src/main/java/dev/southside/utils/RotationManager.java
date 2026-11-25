package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ConnectionEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.event.events.XEvent;
import dev.southside.event.events.YawEvent;
import dev.southside.event.events.JumpYawEvent;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.PositionRecord;
import dev.southside.utils.rotation.MoveFixMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

public class RotationManager implements IMinecraft {
   public static Rotation currentRotation;
   public static Rotation prevRotation;
   public static Rotation serverRotation;
   public static MoveFixMode moveFixMode;
   public static List<ValuePair<Rotation, Integer>> rotationRequests = new ArrayList<>();
   public static DisableRotationEvent disableRotationEvent = new DisableRotationEvent(false);
   public static ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

   public static void updateRotations() {
      if (mc.player != null) {
         if (prevRotation == null) {
            prevRotation = new Rotation(mc.player.getYaw(), mc.player.getPitch());
         }

         if (serverRotation == null) {
            serverRotation = new Rotation(mc.player.getYaw(), mc.player.getPitch());
         }

         disableRotationEvent.setDisableRotation(false);
         Southside.INSTANCE.getEventBus().post(disableRotationEvent);
         if (rotationRequests.isEmpty()) {
            setRotation((Rotation)null);
         }

         currentRotation = disableRotationEvent.isDisableRotation() ? null : computeRotation(rotationRequests);
         rotationRequests.clear();
      }
   }

   public static boolean canOverridePriority(int n) {
      return n >= rotationRequests.stream().mapToInt(ValuePair::getSecond).max().orElse(Integer.MIN_VALUE);
   }

   public static void requestRotation(Rotation rotation, int n) {
      rotationRequests.add(new ValuePair<>(rotation, n));
   }

   public static void requestRotationWithMoveFix(Rotation rotation, int n, MoveFixMode moveFixMode2) {
      requestRotation(rotation, n);
      moveFixMode = moveFixMode2;
   }

   public static void setRotation(Rotation rotation) {
      requestRotation(rotation, 0);
      moveFixMode = Southside.INSTANCE.getModuleManager().getModule(StrafeFix.class).getMoveFixMode();
   }

   public static void setRotationWithMoveFix(Rotation rotation, MoveFixMode moveFixMode2) {
      currentRotation = rotation;
      moveFixMode = moveFixMode2;
   }

   public static double getAngleDelta(float yaw, float pitch) {
      double d = yaw - pitch;
      return ((d + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
   }

   public static double getAngleDifference(float yaw, float pitch) {
      return Math.abs(getAngleDelta(yaw, pitch));
   }

   public static float computeFloat(float x, float y) {
      return computeFloatEx(x, y);
   }

   public static void performAction(PlayerEntityRenderState renderState, float f) {
      performActionAlt(renderState, f);
   }

   public static void performAction(Args args) {
      performActionAlt(args);
   }

   public static boolean isValidTarget(Entity entity) {
      return !entity.isSpectator() && entity.isCollidable();
   }

   public static List<Vec3d> collectItems(Box box) {
      ArrayList list = new ArrayList();
      list.add(box.getCenter());
      list.add(new Vec3d(box.getCenter().x, box.maxY, box.getCenter().z));
      list.add(new Vec3d(box.getCenter().x, box.minY, box.getCenter().z));

      for (double d : new double[]{box.minX, box.maxX}) {
         for (double d2 : new double[]{box.minY, box.maxY}) {
            for (double d3 : new double[]{box.minZ, box.maxZ}) {
               list.add(new Vec3d(d, d2, d3));
            }
         }
      }

      for (int i = 0; i < 10; i++) {
         double d4 = box.minX + Math.random() * (box.maxX - box.minX);
         double d5 = box.minY + Math.random() * (box.maxY - box.minY);
         double d6 = box.minZ + Math.random() * (box.maxZ - box.minZ);
         list.add(new Vec3d(d4, d5, d6));
      }

      return list;
   }

   public static PositionRecord getPositionRecord(BlockPos pos) {
      if (pos == null) {
         return null;
      }

      PositionRecord positionRecord = null;

      for (double i = 0.1; i < 0.9; i += 0.1) {
         for (double j = 0.1; j < 0.9; j += 0.1) {
            for (double k = 0.1; k < 0.9; k += 0.1) {
               Vec3d vec = new Vec3d(mc.player.getX(), mc.player.getBoundingBox().minY + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
               Vec3d vec2 = new Vec3d(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
               double dist = vec.distanceTo(vec2);
               double dx = vec2.x - vec.x;
               double dy = vec2.y - vec.y;
               double dz = vec2.z - vec.z;
               double dist2 = Math.sqrt(dx * dx + dz * dz);
               Rotation rotation = new Rotation(
                  MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F),
                  MathHelper.wrapDegrees((float)(-Math.toDegrees(Math.atan2(dy, dist2))))
               );
               Rotation rotation2 = rotation;
               float pitch = rotation2.pitch;
               float yaw = rotation2.yaw;
               float f = pitch * (float) (Math.PI / 180.0);
               float f2 = -yaw * (float) (Math.PI / 180.0);
               float f3 = MathHelper.cos(f2);
               float f4 = MathHelper.sin(f2);
               float f5 = MathHelper.cos(f);
               float f6 = MathHelper.sin(f);
               Vec3d vec3 = new Vec3d(f4 * f5, -f6, f3 * f5);
               Vec3d vec4 = vec.add(vec3.x * dist, vec3.y * dist, vec3.z * dist);
               BlockHitResult hitResult = mc.world.raycast(new RaycastContext(vec, vec4, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
               if (hitResult.getType() == Type.BLOCK) {
                  PositionRecord positionRecord2 = new PositionRecord(vec2, rotation);
                  if (positionRecord != null) {
                     Rotation rotation3 = positionRecord2.getRotation();
                     rotation2 = rotation3;
                     double d;
                     if (prevRotation == null) {
                        d = 0.0;
                     } else {
                        Rotation rotation4 = prevRotation;
                        Rotation rotation5 = rotation2;
                        float yaw2 = rotation5.yaw;
                        float pitch2 = rotation4.getPitch();
                        float f7 = yaw2;
                        d = Math.hypot(((f7 - pitch2) % 360.0F + 540.0F) % 360.0F - 180.0F, rotation5.pitch - rotation4.getYaw());
                     }

                     Rotation rotation6 = positionRecord.getRotation();
                     Double val = d;
                     rotation2 = rotation6;
                     double d2;
                     if (prevRotation == null) {
                        d2 = 0.0;
                     } else {
                        Rotation rotation7 = prevRotation;
                        Rotation rotation8 = rotation2;
                        float yaw3 = rotation8.yaw;
                        float pitch3 = rotation7.getPitch();
                        float f8 = yaw3;
                        d2 = Math.hypot(((f8 - pitch3) % 360.0F + 540.0F) % 360.0F - 180.0F, rotation8.pitch - rotation7.getYaw());
                     }

                     Double val2 = d2;
                     if (!(val < val2)) {
                        continue;
                     }
                  }

                  positionRecord = positionRecord2;
               }
            }
         }
      }

      return positionRecord;
   }

   public static Rotation getRotationToBlockFace(BlockPos pos, Direction direction) {
      double d = pos.getX() + 0.5 + direction.getOffsetX() * 0.5;
      double d2 = pos.getY() + 0.5 + direction.getOffsetY() * 0.5;
      double d3 = pos.getZ() + 0.5 + direction.getOffsetZ() * 0.5;
      double x = mc.player.getX();
      double d4 = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
      double z = mc.player.getZ();
      double d5 = d - x;
      double d6 = d2 - d4;
      double d7 = d3 - z;
      double dist = Math.sqrt(d5 * d5 + d7 * d7);
      float f = (float)(Math.toDegrees(Math.atan2(d7, d5)) - 90.0);
      float f2 = (float)(-Math.toDegrees(Math.atan2(d6, dist)));
      return new Rotation(f, f2);
   }

   public static boolean isValid(int n, ValuePair<Rotation, Integer> valuePair) {
      return (Integer)valuePair.getSecond() == n;
   }

   public static Rotation getActiveRotation() {
      if (currentRotation != null) {
         return currentRotation;
      }

      ClientPlayerEntity player = mc.player;
      return player == null ? new Rotation(0.0F, 0.0F) : new Rotation(player.getYaw(), player.getPitch());
   }

   public static Rotation getRotationToPos(Vec3d vec) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         return new Rotation(0.0F, 0.0F);
      }

      Vec3d vec2 = new Vec3d(player.getX(), player.getEyeY(), player.getZ());
      return getRotationFromDelta(vec.subtract(vec2));
   }

   private static Rotation getRotationFromDelta(Vec3d vec) {
      double horizDist = MathHelper.hypot(vec.x, vec.z);
      float f = (float)(MathHelper.atan2(vec.z, vec.x) * (180.0 / Math.PI)) - 90.0F;
      float f2 = (float)(-(MathHelper.atan2(vec.y, horizDist) * (180.0 / Math.PI)));
      return new Rotation(f, f2);
   }

   public static Rotation getRotationToEntity(Entity entity) {
      if (entity != null && mc.player != null) {
         double dx = entity.getX() - mc.player.getX();
         double dz = entity.getZ() - mc.player.getZ();
         double eyeHeight = entity.getEyeHeight(entity.getPose());
         double eyeHeight2 = mc.player.getEyeHeight(mc.player.getPose());
         double d = entity.getY() + eyeHeight * 0.9 - (mc.player.getY() + eyeHeight2);
         double dist = Math.sqrt(dx * dx + dz * dz);
         float f = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
         float f2 = (float)(-Math.toDegrees(Math.atan2(d, dist)));
         return new Rotation(
            mc.player.getYaw() + MathHelper.wrapDegrees(f - mc.player.getYaw()),
            mc.player.getPitch() + MathHelper.wrapDegrees(f2 - mc.player.getPitch())
         );
      } else {
         return null;
      }
   }

   public static Rotation getBowRotationToEntity(Entity entity) {
      double dist = mc.player.distanceTo(entity);
      double d = entity.getX() + (entity.getX() - entity.lastRenderX) * dist * 0.8;
      double d2 = entity.getZ() + (entity.getZ() - entity.lastRenderZ) * dist * 0.8;
      double d3 = d - mc.player.getX();
      double d4 = d2 - mc.player.getZ();
      double d5 = entity.getY() + 1.0 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
      double dist2 = Math.sqrt(d3 * d3 + d4 * d4);
      float f = (float)(Math.atan2(d4, d3) * 180.0 / Math.PI) - 90.0F;
      float f2 = (float)dist2;
      float f3 = (float)d5;
      float f4 = 1.0F;
      float f5 = f3;
      float f6 = f2;
      float f7 = 0.006F;
      float f8 = f4 * f4 * f4 * f4 - f7 * (f7 * f6 * f6 + 2.0F * f5 * f4 * f4);
      float f9 = -((float)Math.toDegrees(Math.atan((f4 * f4 - Math.sqrt(f8)) / (f7 * f6))));
      return new Rotation(f, f9);
   }

   public static double computeDistance(Box box) {
      Vec3d eyePos = mc.player.getEyePos();
      Vec3d vec = null;

      for (double i = 0.0; i <= 1.0; i += 0.05) {
         for (double j = 0.0; j < 1.0; j += 0.05) {
            for (double k = 0.0; k <= 1.0; k += 0.05) {
               Vec3d vec2 = new Vec3d(
                  box.minX + (box.maxX - box.minX) * i, box.minY + (box.maxY - box.minY) * j, box.minZ + (box.maxZ - box.minZ) * k
               );
               double dist = eyePos.squaredDistanceTo(vec2);
               if (vec == null || eyePos.squaredDistanceTo(vec) > dist) {
                  vec = vec2;
               }
            }
         }
      }

      return vec.distanceTo(eyePos);
   }

   public static void setMoveFixMode(MoveFixMode moveFixMode2) {
      moveFixMode = moveFixMode2;
   }

   public static Rotation computeRotation(MutableVec3d mutableVec3d, Direction direction) {
      MutableVec3d mutableVec3d2 = new MutableVec3d(
         mutableVec3d.getX() + 0.5 + direction.getOffsetX() * 0.5, mutableVec3d.getY() + 0.5 + direction.getOffsetY() * 0.5, mutableVec3d.getZ() + 0.5 + direction.getOffsetZ() * 0.5
      );
      Vec3d pos = mc.player.getPos();
      Double val = 0.0;
      float f = 1.62F;
      if (mc.player.isSleeping()) {
         f = 0.2F;
      }

      if (mc.player.isSneaking()) {
         f -= 0.08F;
      }

      Float val2 = f;
      Vec3d vec = pos.add(val, val2.floatValue(), 0.0);
      MutableVec3d mutableVec3d3 = mutableVec3d2.getMutableVec3d(new MutableVec3d(vec.x, vec.y, vec.z));
      return new Rotation(
         (float)(MathHelper.atan2(mutableVec3d3.getZ(), mutableVec3d3.getX()) * 180.0 / (float) Math.PI) - 90.0F,
         (float)(-(MathHelper.atan2(mutableVec3d3.getY(), Math.hypot(mutableVec3d3.getX(), mutableVec3d3.getZ())) * 180.0 / (float) Math.PI))
      );
   }

   public static double computeDouble(float x, double y, double z) {
      float f = x;
      if (y < 0.0) {
         f = x + 180.0F;
      }

      float f2 = 1.0F;
      if (y < 0.0) {
         f2 = -0.5F;
      } else if (y > 0.0) {
         f2 = 0.5F;
      }

      if (z > 0.0) {
         f -= 90.0F * f2;
      }

      if (z < 0.0) {
         f += 90.0F * f2;
      }

      return Math.toRadians(f);
   }

   public static float[] getFloatList(Entity entity) {
      double x = mc.player.getX();
      double d = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
      double z = mc.player.getZ();
      double x2 = entity.getX();
      double d2 = entity.getY() + entity.getHeight() / 2.0F;
      double z2 = entity.getZ();
      double d3 = x - x2;
      double d4 = d - d2;
      double d5 = z - z2;
      double dist = Math.sqrt(Math.pow(d3, 2.0) + Math.pow(d5, 2.0));
      double d6 = Math.toDegrees(Math.atan2(d5, d3)) + 90.0;
      double d7 = Math.toDegrees(Math.atan2(dist, d4));
      return new float[]{(float)d6, (float)(90.0 - d7)};
   }

   public static Vec2fMath getVec2fMath(Vec3d vec, Vec3d vec2) {
      double dx = vec2.x - vec.x;
      double dy = vec2.y - vec.y;
      double dz = vec2.z - vec.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float f = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float f2 = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return new Vec2fMath(MathHelper.wrapDegrees(f), MathHelper.wrapDegrees(f2));
   }

   public static void performActionEx(PlayerEntityRenderState renderState, float f) {
      if (currentRotation != null && prevRotation != null) {
         renderState.pitch = MathHelper.lerpAngleDegrees(f, prevRotation.pitch, currentRotation.pitch);
      }
   }

   public static Vec3d computePosition(BlockPos pos, Direction direction) {
      World world = mc.player.getWorld();
      BlockState blockState = world.getBlockState(pos);
      ShapeContext shapeContext = ShapeContext.of(mc.player);
      VoxelShape outlineShape = blockState.getOutlineShape(world, pos, shapeContext);
      if (outlineShape.isEmpty()) {
         outlineShape = blockState.getCollisionShape(world, pos, shapeContext);
      }

      if (outlineShape.isEmpty()) {
         outlineShape = VoxelShapes.fullCube();
      }

      ArrayList list = new ArrayList(outlineShape.getBoundingBoxes());
      double positive_infinity = Double.POSITIVE_INFINITY;
      double negative_infinity = Double.NEGATIVE_INFINITY;
      double positive_infinity2 = Double.POSITIVE_INFINITY;
      double negative_infinity2 = Double.NEGATIVE_INFINITY;
      double positive_infinity3 = Double.POSITIVE_INFINITY;
      double negative_infinity3 = Double.NEGATIVE_INFINITY;

      for (Object obj : list) {
         Box box = (Box)obj;
         if (box.minX < positive_infinity) {
            positive_infinity = box.minX;
         }

         if (box.maxX > negative_infinity) {
            negative_infinity = box.maxX;
         }

         if (box.minY < positive_infinity2) {
            positive_infinity2 = box.minY;
         }

         if (box.maxY > negative_infinity2) {
            negative_infinity2 = box.maxY;
         }

         if (box.minZ < positive_infinity3) {
            positive_infinity3 = box.minZ;
         }

         if (box.maxZ > negative_infinity3) {
            negative_infinity3 = box.maxZ;
         }
      }

      ArrayList list2 = new ArrayList();

      for (Object obj2 : list) {
         Box box2 = (Box)obj2;
         switch (direction) {
            case EAST:
               double maxX = box2.maxX;
               boolean flag = Math.abs(maxX - negative_infinity) <= 1.0E-7;
               if (flag) {
                  double d = (box2.maxY - box2.minY) * (box2.maxZ - box2.minZ);
                  if (d > 0.0) {
                     list2.add(new RotationManager.Entry(box2, d));
                  }
               }
               break;
            case WEST:
               double minX = box2.minX;
               boolean flag2 = Math.abs(minX - positive_infinity) <= 1.0E-7;
               if (flag2) {
                  double d2 = (box2.maxY - box2.minY) * (box2.maxZ - box2.minZ);
                  if (d2 > 0.0) {
                     list2.add(new RotationManager.Entry(box2, d2));
                  }
               }
               break;
            case UP:
               double maxY = box2.maxY;
               boolean flag3 = Math.abs(maxY - negative_infinity2) <= 1.0E-7;
               if (flag3) {
                  double d3 = (box2.maxX - box2.minX) * (box2.maxZ - box2.minZ);
                  if (d3 > 0.0) {
                     list2.add(new RotationManager.Entry(box2, d3));
                  }
               }
               break;
            case DOWN:
               double minY = box2.minY;
               boolean flag4 = Math.abs(minY - positive_infinity2) <= 1.0E-7;
               if (flag4) {
                  double d4 = (box2.maxX - box2.minX) * (box2.maxZ - box2.minZ);
                  if (d4 > 0.0) {
                     list2.add(new RotationManager.Entry(box2, d4));
                  }
               }
               break;
            case SOUTH:
               double maxZ = box2.maxZ;
               boolean flag5 = Math.abs(maxZ - negative_infinity3) <= 1.0E-7;
               if (flag5) {
                  double d5 = (box2.maxX - box2.minX) * (box2.maxY - box2.minY);
                  if (d5 > 0.0) {
                     list2.add(new RotationManager.Entry(box2, d5));
                  }
               }
               break;
            case NORTH:
               double minZ = box2.minZ;
               boolean flag6 = Math.abs(minZ - positive_infinity3) <= 1.0E-7;
               if (flag6) {
                  double d6 = (box2.maxX - box2.minX) * (box2.maxY - box2.minY);
                  if (d6 > 0.0) {
                     list2.add(new RotationManager.Entry(box2, d6));
                  }
               }
         }
      }

      if (list2.isEmpty()) {
         for (Object obj3 : list) {
            Box box3 = (Box)obj3;
            double d7 = switch (direction.getAxis()) {
               case X -> (box3.maxY - box3.minY) * (box3.maxZ - box3.minZ);
               case Y -> (box3.maxX - box3.minX) * (box3.maxZ - box3.minZ);
               default -> (box3.maxX - box3.minX) * (box3.maxY - box3.minY);
            };
            if (d7 > 0.0) {
               list2.add(new RotationManager.Entry(box3, d7));
            }
         }
      }

      double d8 = 0.0;

      for (Object obj4 : list2) {
         RotationManager.Entry entry = (RotationManager.Entry)obj4;
         d8 += entry.weight;
      }

      double d9 = threadLocalRandom.nextDouble() * d8;
      Box box4 = null;

      for (Object obj5 : list2) {
         RotationManager.Entry entry2 = (RotationManager.Entry)obj5;
         d9 -= entry2.weight;
         if (d9 <= 0.0) {
            box4 = entry2.box;
            break;
         }
      }

      if (box4 == null) {
         box4 = ((RotationManager.Entry)list2.get(list2.size() - 1)).box;
      }

      double x = pos.getX();
      double y = pos.getY();
      double z = pos.getZ();
      switch (direction) {
         case EAST:
            double d10 = box4.minY + threadLocalRandom.nextDouble() * (box4.maxY - box4.minY);
            double d11 = box4.minZ + threadLocalRandom.nextDouble() * (box4.maxZ - box4.minZ);
            x += box4.maxX;
            y += d10;
            z += d11;
            break;
         case WEST:
            double d12 = box4.minY + threadLocalRandom.nextDouble() * (box4.maxY - box4.minY);
            double d13 = box4.minZ + threadLocalRandom.nextDouble() * (box4.maxZ - box4.minZ);
            x += box4.minX;
            y += d12;
            z += d13;
            break;
         case UP:
            double d14 = box4.minX + threadLocalRandom.nextDouble() * (box4.maxX - box4.minX);
            double d15 = box4.minZ + threadLocalRandom.nextDouble() * (box4.maxZ - box4.minZ);
            x += d14;
            y += box4.maxY;
            z += d15;
            break;
         case DOWN:
            double d16 = box4.minX + threadLocalRandom.nextDouble() * (box4.maxX - box4.minX);
            double d17 = box4.minZ + threadLocalRandom.nextDouble() * (box4.maxZ - box4.minZ);
            x += d16;
            y += box4.minY;
            z += d17;
            break;
         case SOUTH:
            double d18 = box4.minX + threadLocalRandom.nextDouble() * (box4.maxX - box4.minX);
            double d19 = box4.minY + threadLocalRandom.nextDouble() * (box4.maxY - box4.minY);
            x += d18;
            y += d19;
            z += box4.maxZ;
            break;
         case NORTH:
            double d20 = box4.minX + threadLocalRandom.nextDouble() * (box4.maxX - box4.minX);
            double d21 = box4.minY + threadLocalRandom.nextDouble() * (box4.maxY - box4.minY);
            x += d20;
            y += d21;
            z += box4.minZ;
      }

      return new Vec3d(x, y, z);
   }

   public static List<ValuePair<Rotation, Integer>> getRotationRequests() {
      return rotationRequests;
   }

   public static float[] getFloatList(float x, float y, float width, float height) {
      MinecraftClient instance = MinecraftClient.getInstance();
      float f = ((Double)instance.options.getMouseSensitivity().getValue()).floatValue();
      float f2 = f * 0.6F + 0.2F;
      float f3 = f2 * f2 * f2 * 1.2F;
      float f4 = x - width;
      float f5 = y - height;
      f4 -= f4 % f3;
      f5 -= f5 % f3;
      float f6 = width + f4;
      float f7 = height + f5;
      if (f7 > 90.0F) {
         f7 = 90.0F;
      }

      if (f7 < -90.0F) {
         f7 = -90.0F;
      }

      return new float[]{f6, f7};
   }

   @EventTarget(priority = -1145144)
   public void onConnection(ConnectionEvent connectionEvent) {
      Packet packet = connectionEvent.packet;
      if (packet instanceof PlayerMoveC2SPacket playerMoveC2SPacket && playerMoveC2SPacket.changeLook) {
         serverRotation = new Rotation(playerMoveC2SPacket.getYaw(mc.player.getYaw()), playerMoveC2SPacket.getPitch(mc.player.getPitch()));
      }

      if (packet instanceof PlayerPositionLookS2CPacket playerPositionLookS2CPacket) {
         serverRotation = new Rotation(playerPositionLookS2CPacket.change().yaw(), playerPositionLookS2CPacket.change().pitch());
      }
   }

   public static double computeDouble(double x, double y, double z) {
      return Math.max(y, Math.min(z, x));
   }

   public static void performActionAlt(PlayerEntityRenderState renderState, float f) {
      if (currentRotation != null && prevRotation != null) {
         float interpolated = MathHelper.lerpAngleDegrees(f, prevRotation.yaw, currentRotation.yaw);
         ClientPlayerEntity player = mc.player;
         ClientPlayerEntity player2 = player;
         float f2;
         if (player2.getVehicle() instanceof LivingEntity living) {
            float interpolated2 = MathHelper.lerpAngleDegrees(f, living.prevBodyYaw, living.bodyYaw);
            float clamped = MathHelper.clamp(MathHelper.wrapDegrees(interpolated - interpolated2), -85.0F, 85.0F);
            interpolated2 = interpolated - clamped;
            if (Math.abs(clamped) > 50.0F) {
               interpolated2 += clamped * 0.2F;
            }

            f2 = interpolated2;
         } else {
            f2 = MathHelper.lerpAngleDegrees(f, player2.prevBodyYaw, player2.bodyYaw);
         }

         Float val = f2;
         renderState.bodyYaw = val;
         renderState.yawDegrees = MathHelper.wrapDegrees(interpolated - renderState.bodyYaw);
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.state.equals(Stage.PRE) && currentRotation != null) {
         Rotation rotation = currentRotation;
         prevRotation = new Rotation(rotation.yaw, rotation.pitch, rotation.targetPosition, rotation.sourcePosition);
      }
   }

   @EventTarget
   public void onYaw(YawEvent yawEvent) {
      if (isValid() || isValidEx()) {
         yawEvent.setYaw(currentRotation.getYaw());
      }
   }

   public static Rotation computeRotation(Vec3d vec, Vec3d vec2, boolean flag) {
      Vec3d delta = vec2.subtract(vec);
      float wrapped = MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F);
      float wrapped2 = MathHelper.wrapDegrees((float)(-Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)))));
      float f = flag ? (float)ThreadLocalRandom.current().nextGaussian() * 5.0F : 0.0F;
      float f2 = wrapped + f;
      float f3 = flag ? (float)ThreadLocalRandom.current().nextGaussian() * 2.0F : 0.0F;
      float f4 = wrapped2 + f3;
      return new Rotation(f2, f4);
   }

   public static float getMovementYaw() {
      float yaw = mc.player.getYaw();
      float forwardSpeed = mc.player.forwardSpeed;
      float sidewaysSpeed = mc.player.sidewaysSpeed;
      float f = yaw;
      float f2 = f;
      if (forwardSpeed < 0.0F) {
         f2 = f + 180.0F;
      }

      float f3 = 1.0F;
      if (forwardSpeed < 0.0F) {
         f3 = -0.5F;
      } else if (forwardSpeed > 0.0F) {
         f3 = 0.5F;
      }

      if (sidewaysSpeed > 0.0F) {
         f2 -= 90.0F * f3;
      }

      if (sidewaysSpeed < 0.0F) {
         f2 += 90.0F * f3;
      }

      return f2;
   }

   public static Vec3d computePosition(Box box, Direction direction, Vec3d vec, float f, double d) {
      double d2;
      if (direction.getAxis() == Axis.X) {
         if (direction == Direction.EAST) {
            d2 = box.maxX;
         } else {
            d2 = box.minX;
         }
      } else {
         d2 = (box.minX + box.maxX) * 0.5;
      }

      double d3;
      if (direction.getAxis() == Axis.Y) {
         if (direction == Direction.UP) {
            d3 = box.maxY;
         } else {
            d3 = box.minY;
         }
      } else {
         d3 = (box.minY + box.maxY) * 0.5;
      }

      double d4;
      if (direction.getAxis() == Axis.Z) {
         if (direction == Direction.SOUTH) {
            d4 = box.maxZ;
         } else {
            d4 = box.minZ;
         }
      } else {
         d4 = (box.minZ + box.maxZ) * 0.5;
      }

      Vec3d vec2 = new Vec3d(d2, d3, d4);
      Vec3d vec3;
      Vec3d vec4;
      switch (direction.getAxis()) {
         case X:
            vec3 = new Vec3d(0.0, 1.0, 0.0);
            vec4 = new Vec3d(0.0, 0.0, 1.0);
            break;
         case Y:
            vec3 = new Vec3d(1.0, 0.0, 0.0);
            vec4 = new Vec3d(0.0, 0.0, 1.0);
            break;
         default:
            vec3 = new Vec3d(1.0, 0.0, 0.0);
            vec4 = new Vec3d(0.0, 1.0, 0.0);
      }

      double d5;
      if (vec3.x != 0.0) {
         d5 = box.maxX - box.minX;
      } else if (vec3.y != 0.0) {
         d5 = box.maxY - box.minY;
      } else {
         d5 = box.maxZ - box.minZ;
      }

      double d6 = d5 * 0.5;
      double d7;
      if (vec4.x != 0.0) {
         d7 = box.maxX - box.minX;
      } else if (vec4.y != 0.0) {
         d7 = box.maxY - box.minY;
      } else {
         d7 = box.maxZ - box.minZ;
      }

      double d8 = d7 * 0.5;
      double d9 = Math.toRadians(f + 90.0F);
      Vec3d vec5 = new Vec3d(Math.cos(d9), 0.0, Math.sin(d9));
      Vec3d vec6 = new Vec3d(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
      double d10 = vec6.dotProduct(vec5);
      if (Math.abs(d10) > 1.0E-6) {
         double d11 = vec6.dotProduct(vec2.subtract(vec)) / d10;
         if (d11 > 0.0) {
            Vec3d vec7 = vec.add(vec5.multiply(d11));
            double d12 = vec7.subtract(vec2).dotProduct(vec3);
            double d13 = vec7.subtract(vec2).dotProduct(vec4);
            if (Math.abs(d12) <= d6 && Math.abs(d13) <= d8) {
               Vec3d vec8 = vec3;
               Vec3d vec9 = vec4;
               double d14 = d12;
               double d15 = d13;
               if (Math.abs(d12) >= d6 - 1.0E-4) {
                  d14 = Math.copySign(d6 - d, d12);
               }

               if (Math.abs(d13) >= d8 - 1.0E-4) {
                  d15 = Math.copySign(d8 - d, d13);
               }

               return vec2.add(vec8.multiply(d14)).add(vec9.multiply(d15));
            }
         }
      }

      Vec3d vec10 = vec3;
      Vec3d vec11 = vec4;
      Vec3d vec12 = vec.add(vec5.multiply(100.0));
      double d16 = vec12.subtract(vec2).dotProduct(vec10);
      double d17 = vec12.subtract(vec2).dotProduct(vec11);
      d16 = Math.max(-d6, Math.min(d6, d16));
      d17 = Math.max(-d8, Math.min(d8, d17));
      double d18 = d16;
      double d19 = d17;
      if (Math.abs(d16) >= d6 - 1.0E-4) {
         d18 = Math.copySign(d6 - d, d16);
      }

      if (Math.abs(d17) >= d8 - 1.0E-4) {
         d19 = Math.copySign(d8 - d, d17);
      }

      return vec2.add(vec10.multiply(d18)).add(vec11.multiply(d19));
   }

   public static Rotation computeRotation(List<ValuePair<Rotation, Integer>> list) {
      int n = list.stream().mapToInt(ValuePair::getSecond).max().orElse(-1);
      List list2 = rotationRequests.stream().filter(valuePair -> isValid(n, (ValuePair<Rotation, Integer>)valuePair)).toList();
      if (list2.size() > 1) {
         Random random = new Random();
         return (Rotation)((ValuePair)list2.get(random.nextInt(list2.size()))).getFirst();
      } else {
         return list2.size() == 1 ? (Rotation)((ValuePair)list2.getFirst()).getFirst() : null;
      }
   }

   public static float computeFloatEx(float x, float y) {
      if (Math.abs(x) < y) {
         return x;
      } else if (x > 0.0F) {
         return y;
      } else {
         return x < 0.0F ? -y : 0.0F;
      }
   }

   public static float computeFloat(Entity entity) {
      return Math.abs(getFloatList(entity)[0] - mc.player.getYaw()) % 360.0F > 180.0F
         ? 360.0F - Math.abs(getFloatList(entity)[0] - mc.player.getYaw()) % 360.0F
         : Math.abs(getFloatList(entity)[0] - mc.player.getYaw()) % 360.0F;
   }

   public static Rotation raycast(Entity entity, Entity entity2, double d) {
      MinecraftClient instance = MinecraftClient.getInstance();
      if (instance.world != null && entity != null && entity2 != null) {
         Vec3d eyePos = entity.getEyePos();
         List list = collectItems(entity2.getBoundingBox());
         Rotation rotation = null;
         double max_value = Double.MAX_VALUE;

         for (Object obj : list) {
            Vec3d vec = (Vec3d)obj;
            double dist = eyePos.distanceTo(vec);
            if (!(dist > d)) {
               MinecraftClient instance2 = MinecraftClient.getInstance();
               BlockHitResult hitResult = instance2.world.raycast(new RaycastContext(eyePos, vec, ShapeType.COLLIDER, FluidHandling.NONE, instance2.player));
               if (hitResult.getType() != Type.MISS) {
                  Vec3d pos = hitResult.getPos();
                  double dx = eyePos.x - pos.getX();
                  double dy = eyePos.y - pos.getY();
                  double dz = eyePos.z - pos.getZ();
                  if (!(dx * dx + dy * dy + dz * dz >= eyePos.squaredDistanceTo(vec) - 0.01)) {
                     continue;
                  }
               }

               if (dist < max_value) {
                  max_value = dist;
                  double dx2 = vec.x - eyePos.x;
                  double dy2 = vec.y - eyePos.y;
                  double dz2 = vec.z - eyePos.z;
                  double dist2 = Math.sqrt(dx2 * dx2 + dz2 * dz2);
                  float f = (float)Math.toDegrees(Math.atan2(dz2, dx2)) - 90.0F;
                  float f2 = (float)(-Math.toDegrees(Math.atan2(dy2, dist2)));
                  rotation = new Rotation(f, f2, vec, eyePos);
               }
            }
         }

         return rotation;
      } else {
         return null;
      }
   }

   public static Rotation raycast(Entity entity, double d, double d2, float f) {
      if (mc.player == null) {
         return null;
      }

      Vec3d eyePos = entity.getEyePos();
      Vec3d eyePos2 = mc.player.getEyePos();
      double y = mc.player.getEyePos().y;
      double minY = entity.getBoundingBox().minY;
      double maxY = entity.getBoundingBox().maxY;
      double d3 = y < minY ? minY : Math.min(y, maxY);
      Rotation rotation = computeRotation(eyePos2, new Vec3d(eyePos.x, d3, eyePos.z), false);
      Entity cameraEntity = RaytraceUtils.mc.cameraEntity;
      EntityHitResult entityHit;
      if (cameraEntity == null) {
         entityHit = null;
      } else {
         Vec3d eyePos3 = cameraEntity.getEyePos();
         float f2 = MathHelper.cos(-rotation.yaw * (float) (Math.PI / 180.0));
         float f3 = MathHelper.sin(-rotation.yaw * (float) (Math.PI / 180.0));
         float f4 = MathHelper.cos(rotation.pitch * (float) (Math.PI / 180.0));
         float f5 = MathHelper.sin(rotation.pitch * (float) (Math.PI / 180.0));
         Vec3d vec = new Vec3d(f3 * f4, -f5, f2 * f4);
         Vec3d vec2 = eyePos3.add(vec.x * d, vec.y * d, vec.z * d);
         Box box = cameraEntity.getBoundingBox().stretch(vec.multiply(d)).expand(1.0, 1.0, 1.0);
         EntityHitResult hitResult = ProjectileUtil.raycast(cameraEntity, eyePos3, vec2, box, RaytraceUtils::isValidTarget, d * d);
         entityHit = hitResult;
      }

      return entityHit != null && entityHit.getType() == Type.ENTITY ? rotation : raycastEx(entity, d, d2, f);
   }

   public static Rotation computeRotation(Vec3d vec, Vec3d vec2) {
      double dx = vec2.x - vec.x;
      double dy = vec2.y - vec.y;
      double dz = vec2.z - vec.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float f = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float f2 = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      float f3 = f % 360.0F;
      if (f3 >= 180.0F) {
         f3 -= 360.0F;
      }

      if (f3 < -180.0F) {
         f3 += 360.0F;
      }

      f2 = MathHelper.clamp(f2, -90.0F, 90.0F);
      return new Rotation(f3, f2);
   }

   public RotationManager() {
      Southside.INSTANCE.getEventBus().subscribe(this);
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (xevent.getPhase().equals(Stage.PRE) && currentRotation != null) {
         xevent.setProgress(currentRotation.getYaw());
         xevent.setPitch(currentRotation.getPitch());
      }
   }

   public static Rotation getCurrentRotation() {
      return currentRotation;
   }

   @EventTarget
   public void onYawEvent2(JumpYawEvent jumpYawEvent) {
      if (isValid() || isValidEx()) {
         jumpYawEvent.setYaw(currentRotation.getYaw());
      }
   }

   public static boolean isValid(Vec3d vec, HitResult hitResult, Entity entity) {
      if (hitResult.getType() == Type.ENTITY && ((EntityHitResult)hitResult).getEntity() == entity) {
         Vec3d pos = hitResult.getPos();
         Box boundingBox = entity.getBoundingBox();
         boolean flag = vec.x > boundingBox.minX && vec.x < boundingBox.maxX && vec.y > boundingBox.minY && vec.y < boundingBox.maxY && vec.z > boundingBox.minZ && vec.z < boundingBox.maxZ;
         return flag || pos.distanceTo(vec) <= 3.0;
      } else {
         return false;
      }
   }

   public static HitResult raycast(Vec2fMath vec2fMath) {
      double blockInteractionRange = mc.player.getBlockInteractionRange();
      BlockHitResult hitResult = null;
      Entity cameraEntity = RaytraceUtils.mc.getCameraEntity();
      if (cameraEntity != null && RaytraceUtils.mc.world != null) {
         float x = vec2fMath.getX();
         float y = vec2fMath.getY();
         Vec3d vec = new Vec3d(RaytraceUtils.mc.player.getX(), RaytraceUtils.mc.player.getEyeY(), RaytraceUtils.mc.player.getZ());
         Vec3d vec2 = RaytraceUtils.computePosition(y, x);
         Vec3d vec3 = vec.add(vec2.x * blockInteractionRange, vec2.y * blockInteractionRange, vec2.z * blockInteractionRange);
         ShapeType outline = ShapeType.OUTLINE;
         ClientWorld world = RaytraceUtils.mc.world;
         FluidHandling none = FluidHandling.NONE;
         ClientPlayerEntity player = RaytraceUtils.mc.player;
         hitResult = world.raycast(new RaycastContext(vec, vec3, outline, none, player));
      }

      Object obj = hitResult;
      Vec3d cameraPosVec = mc.player.getCameraPosVec(1.0F);
      boolean flag = blockInteractionRange > 3.0;
      double d = blockInteractionRange * blockInteractionRange;
      if (hitResult != null) {
         d = hitResult.getPos().squaredDistanceTo(cameraPosVec);
      }

      Vec3d vec4 = computePosition(vec2fMath.x, vec2fMath.y);
      Vec3d vec5 = cameraPosVec.add(vec4.x * blockInteractionRange, vec4.y * blockInteractionRange, vec4.z * blockInteractionRange);
      Box box = mc.player.getBoundingBox().stretch(vec4.multiply(blockInteractionRange)).expand(1.0);
      EntityHitResult hitResult2 = ProjectileUtil.raycast(mc.player, cameraPosVec, vec5, box, RotationManager::isValidTarget, d);
      if (hitResult2 != null) {
         Vec3d pos = hitResult2.getPos();
         double dist = cameraPosVec.squaredDistanceTo(pos);
         if (flag && dist > 9.0) {
            obj = BlockHitResult.createMissed(pos, Direction.getFacing(vec4.x, vec4.y, vec4.z), BlockPos.ofFloored(pos));
         } else if (dist < d || hitResult == null) {
            obj = hitResult2;
         }
      }

      return (HitResult)obj;
   }

   public static MoveFixMode getMoveFixMode() {
      return moveFixMode;
   }

   public static boolean isValid() {
      return isValid(MoveFixMode.Strict);
   }

   public static boolean isValidEx() {
      return isValid(MoveFixMode.Silent);
   }

   private static boolean isValid(MoveFixMode moveFixMode2) {
      return Southside.INSTANCE.getModuleManager().getModule(StrafeFix.class).isEnabled()
         && getMoveFixMode() != MoveFixMode.Off
         && getMoveFixMode() != moveFixMode2
         && getCurrentRotation() != null;
   }

   public static Rotation computeRotation(BlockPos pos, Direction direction, float f, float f2) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         return new Rotation(f, 80.5F);
      }

      Rotation rotation = new Rotation(f, f2);
      if (BlockLookUtils.isValid(MinecraftClient.getInstance().player, rotation.yaw, rotation.pitch, pos, direction, true)) {
         return rotation;
      }

      BlockState blockState = player.getWorld().getBlockState(pos);
      VoxelShape outlineShape = blockState.getOutlineShape(player.getWorld(), pos);
      if (outlineShape.isEmpty()) {
         outlineShape = VoxelShapes.fullCube();
      }

      List boundingBoxes = outlineShape.getBoundingBoxes();
      Rotation rotation2 = null;
      Rotation rotation3 = null;
      double max_value = Double.MAX_VALUE;
      Vec3d vector = BlockLookUtils.vector;

      for (Object obj : boundingBoxes) {
         Box box = (Box)obj;
         Box box2 = box.offset(pos);
         Vec3d vec = null;
         ArrayList list = new ArrayList();
         List list2 = collectItems(box2, direction, vector, f);
         double max_value2 = Double.MAX_VALUE;

         for (Object obj2 : list2) {
            Vec3d vec2 = (Vec3d)obj2;
            Rotation rotationFromDelta = getRotationFromDelta(vec2.subtract(vector));
            if (Math.abs(rotationFromDelta.pitch) > 89.9F) {
               rotationFromDelta.yaw = f;
            }

            if (BlockLookUtils.isValid(MinecraftClient.getInstance().player, rotationFromDelta.yaw, rotationFromDelta.pitch, pos, direction, true)) {
               double d = rotationFromDelta.yaw - f;
               double d2 = Math.abs(((d + 180.0) % 360.0 + 360.0) % 360.0 - 180.0);
               if (d2 < max_value2) {
                  max_value2 = d2;
                  rotation2 = rotationFromDelta;
                  vec = vec2;
               }
            }
         }

         if (vec != null) {
            list.add(vec);
         }

         Vec3d vec3 = computePosition(box2, direction, vector, f, RandomUtils.computeFloat(0.05F, 0.1F));
         rotation3 = getRotationFromDelta(vec3.subtract(vector));
         Rotation rotationFromDelta2 = getRotationFromDelta(vec3.subtract(vector));
         if (BlockLookUtils.isValid(MinecraftClient.getInstance().player, rotationFromDelta2.yaw, rotationFromDelta2.pitch, pos, direction, true)) {
            list.add(vec3);
         }

         for (Object obj3 : list) {
            Vec3d vec4 = (Vec3d)obj3;
            Rotation rotationFromDelta3 = getRotationFromDelta(vec4.subtract(vector));
            if (Math.abs(rotationFromDelta3.pitch) > 89.9F) {
               rotationFromDelta3.yaw = f;
            }

            double d3 = rotationFromDelta3.yaw - f;
            double d4 = Math.abs(((d3 + 180.0) % 360.0 + 360.0) % 360.0 - 180.0);
            if (d4 < max_value) {
               max_value = d4;
               rotation2 = rotationFromDelta3;
            }
         }
      }

      return rotation2 != null ? rotation2 : rotation3;
   }

   public static void performAction(Rotation rotation, MoveFixMode moveFixMode2) {
      currentRotation = rotation;
      moveFixMode = moveFixMode2;
   }

   public static void performActionEx(Args args) {
      if (currentRotation != null && prevRotation != null) {
         args.set(1, prevRotation.pitch);
         args.set(2, currentRotation.pitch);
      }
   }

   public static Rotation getServerRotation() {
      return serverRotation;
   }

   public static Rotation computeRotation(Vec3d vec, float f) {
      Vec3d vec2 = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ())
         .add(mc.player.getVelocity().multiply(f));
      Vec3d delta = vec.subtract(vec2);
      double x = delta.x;
      double z = delta.z;
      double dist = Math.sqrt(x * x + z * z);
      float f2 = (float)Math.toDegrees(Math.atan2(z, x)) - 90.0F;
      float f3 = (float)(-Math.toDegrees(Math.atan2(delta.y, dist)));
      return new Rotation(f2, f3);
   }

   public static Rotation computeRotation(Vec3d vec, Direction direction) {
      double d = vec.x + 0.5;
      double d2 = vec.y + 0.5;
      double d3 = vec.z + 0.5;
      Vec3i vector = direction.getVector();
      d += vector.getX() * 0.5;
      d2 += vector.getY() * 0.5;
      d3 += vector.getZ() * 0.5;
      Vec3d vec2 = new Vec3d(d, d2, d3);
      MinecraftClient instance = MinecraftClient.getInstance();
      ClientPlayerEntity player = instance.player;
      Rotation rotation;
      if (player == null) {
         rotation = new Rotation(0.0F, 0.0F);
      } else {
         Vec3d vec3 = new Vec3d(player.getX(), player.getEyeY(), player.getZ());
         Vec3d delta = vec2.subtract(vec3);
         double horizDist = MathHelper.hypot(delta.x, delta.z);
         float f = (float)(MathHelper.atan2(delta.z, delta.x) * (180.0 / Math.PI)) - 90.0F;
         float f2 = (float)(-(MathHelper.atan2(delta.y, horizDist) * (180.0 / Math.PI)));
         rotation = new Rotation(f, f2);
      }

      return rotation;
   }

   public static double raycast(Entity entity) {
      Box boundingBox = entity.getBoundingBox();
      if (entity instanceof EndCrystalEntity) {
         Vec3d pos = entity.getPos();
         boundingBox = new Box(pos.x - 1.0, pos.y, pos.z - 1.0, pos.x + 1.0, pos.y + 2.0, pos.z + 1.0);
      }

      boundingBox = boundingBox.expand(0.03);
      double d = 6.0;
      float yaw = getRotationToPos(entity.getPos()).yaw;
      float pitch = getRotationToPos(entity.getPos()).pitch;
      double d2 = Math.toRadians(-yaw);
      double d3 = Math.toRadians(-pitch);
      double d4 = Math.cos(d3) * Math.sin(d2);
      double d5 = Math.sin(d3);
      double d6 = Math.cos(d3) * Math.cos(d2);
      Vec3d vec = new Vec3d(d4, d5, d6);
      double eyeHeight = mc.player.getEyeHeight(mc.player.getPose());
      Vec3d vec2 = new Vec3d(mc.player.getX(), mc.player.getY() + eyeHeight, mc.player.getZ());
      Vec3d vec3 = vec2.add(vec.multiply(d));
      if (boundingBox.contains(vec2)) {
         return 0.0;
      }

      Optional hitResult = boundingBox.raycast(vec2, vec3);
      if (hitResult.isPresent()) {
         double dist = vec2.distanceTo((Vec3d)hitResult.get());
         if (dist < Double.MAX_VALUE) {
            return dist;
         }
      }

      return Double.MAX_VALUE;
   }

   public static Rotation raycastEx(Entity entity, double d, double d2, float f) {
      Box boundingBox = entity.getBoundingBox();
      if (f != 0.0F) {
         boundingBox = boundingBox.offset((entity.getX() - entity.lastRenderX) * f, (entity.getY() - entity.lastRenderY) * f, (entity.getZ() - entity.lastRenderZ) * f);
      }

      Vec3d vec = null;
      if (mc.player != null) {
         vec = mc.player.getEyePos();
      }

      Vec3d vec2 = null;
      if (vec != null) {
         double d3 = vec.x < boundingBox.minX ? boundingBox.minX : Math.min(vec.x, boundingBox.maxX);
         double d4 = vec.y < boundingBox.minY ? boundingBox.minY : Math.min(vec.y, boundingBox.maxY);
         double d5 = vec.z < boundingBox.minZ ? boundingBox.minZ : Math.min(vec.z, boundingBox.maxZ);
         vec2 = new Vec3d(d3, d4, d5);
      }

      Rotation rotation = null;
      if (vec2 != null) {
         Vec3d eyePos = mc.player.getEyePos();
         rotation = computeRotation(eyePos, vec2, false);
      }

      if (vec2 != null && vec2.subtract(vec).lengthSquared() <= d2 * d2) {
         return rotation;
      }

      Rotation rotation2 = rotation;
      Entity cameraEntity = RaytraceUtils.mc.cameraEntity;
      EntityHitResult entityHit;
      if (cameraEntity == null) {
         entityHit = null;
      } else {
         Vec3d eyePos2 = cameraEntity.getEyePos();
         float f2 = MathHelper.cos(-rotation2.yaw * (float) (Math.PI / 180.0));
         float f3 = MathHelper.sin(-rotation2.yaw * (float) (Math.PI / 180.0));
         float f4 = MathHelper.cos(rotation2.pitch * (float) (Math.PI / 180.0));
         float f5 = MathHelper.sin(rotation2.pitch * (float) (Math.PI / 180.0));
         Vec3d vec3 = new Vec3d(f3 * f4, -f5, f2 * f4);
         Vec3d vec4 = eyePos2.add(vec3.x * d, vec3.y * d, vec3.z * d);
         Box box = cameraEntity.getBoundingBox().stretch(vec3.multiply(d)).expand(1.0, 1.0, 1.0);
         EntityHitResult hitResult = ProjectileUtil.raycast(cameraEntity, eyePos2, vec4, box, RaytraceUtils::isValidTarget, d * d);
         entityHit = hitResult;
      }

      double d6 = Math.max(d2, d);
      return entityHit != null && entityHit.getEntity() == entity && entityHit.getPos().subtract(vec).lengthSquared() <= d6 * d6 ? rotation : null;
   }

   public static float[] getFloatList(double x, double y, double z) {
      double d = x - mc.player.getX();
      double d2 = y - mc.player.getY() - mc.player.getEyeHeight(mc.player.getPose());
      double d3 = z - mc.player.getZ();
      double dist = Math.sqrt(d * d + d3 * d3);
      float f = (float)Math.toDegrees(-Math.atan2(d, d3));
      float f2 = (float)Math.toDegrees(-Math.atan2(d2, dist));
      return new float[]{f, f2};
   }

   public static void performActionAlt(Args args) {
      if (currentRotation != null && prevRotation != null) {
         args.set(1, prevRotation.yaw);
         args.set(2, currentRotation.yaw);
      }
   }

   public static List<Vec3d> collectItems(Box box, Direction direction, Vec3d vec, float f) {
      ArrayList list = new ArrayList();
      double d = RandomUtils.computeFloat(0.1F, 0.15F);
      if (direction.getAxis() == Axis.Y) {
         double d2 = direction == Direction.UP ? box.maxY : box.minY;
         double clamped = MathHelper.clamp(vec.x, box.minX + d, box.maxX - d);
         double clamped2 = MathHelper.clamp(vec.z, box.minZ + d, box.maxZ - d);
         list.add(new Vec3d(clamped, d2, clamped2));
         double d3 = (box.minX + box.maxX) * 0.5;
         double d4 = (box.minZ + box.maxZ) * 0.5;
         list.add(new Vec3d(d3, d2, d4));
      } else {
         double d5;
         if (direction.getAxis() == Axis.X) {
            if (direction == Direction.EAST) {
               d5 = box.maxX;
            } else {
               d5 = box.minX;
            }
         } else {
            d5 = (box.minX + box.maxX) * 0.5;
         }

         double d6 = (box.minY + box.maxY) * 0.5;
         double d7;
         if (direction.getAxis() == Axis.Z) {
            if (direction == Direction.SOUTH) {
               d7 = box.maxZ;
            } else {
               d7 = box.minZ;
            }
         } else {
            d7 = (box.minZ + box.maxZ) * 0.5;
         }

         list.add(new Vec3d(d5, d6, d7));
         double d8 = Math.toRadians(f);
         double d9 = -Math.sin(d8);
         double d10 = Math.cos(d8);
         double d11 = d5;
         double d12 = d7;
         if (direction.getAxis() == Axis.Z) {
            double d13 = direction == Direction.SOUTH ? box.maxZ : box.minZ;
            if (Math.abs(d10) > 1.0E-4) {
               double d14 = (d13 - vec.z) / d10;
               if (d14 > 0.0) {
                  double d15 = vec.x + d14 * d9;
                  d11 = MathHelper.clamp(d15, box.minX + d, box.maxX - d);
                  d12 = d13;
               }
            }
         } else if (direction.getAxis() == Axis.X) {
            double d16 = direction == Direction.EAST ? box.maxX : box.minX;
            if (Math.abs(d9) > 1.0E-4) {
               double d17 = (d16 - vec.x) / d9;
               if (d17 > 0.0) {
                  double d18 = vec.z + d17 * d10;
                  d12 = MathHelper.clamp(d18, box.minZ + d, box.maxZ - d);
                  d11 = d16;
               }
            }
         }

         for (double i = box.minY + 0.1; i < box.maxY; i += 0.4) {
            list.add(new Vec3d(d11, i, d12));
         }

         list.add(new Vec3d(d11, box.maxY - 0.1, d12));
         list.add(new Vec3d(d11, box.minY + 0.1, d12));
      }

      return list;
   }

   public static Rotation getPrevRotation() {
      return prevRotation;
   }

   public static Vec3d computePosition(float x, float y) {
      float f = MathHelper.cos(-x * (float) (Math.PI / 180.0) - (float) Math.PI);
      float f2 = MathHelper.sin(-x * (float) (Math.PI / 180.0) - (float) Math.PI);
      float f3 = -MathHelper.cos(-y * (float) (Math.PI / 180.0));
      float f4 = MathHelper.sin(-y * (float) (Math.PI / 180.0));
      return new Vec3d(f2 * f3, f4, f * f3);
   }

   public static Vec3d computePosition(Box box, Direction direction) {
      double d = (box.minX + box.maxX) * 0.5;
      double d2 = (box.minY + box.maxY) * 0.5;
      double d3 = (box.minZ + box.maxZ) * 0.5;
      switch (direction) {
         case EAST:
            d = box.maxX;
            break;
         case WEST:
            d = box.minX;
            break;
         case UP:
            d2 = box.maxY;
            break;
         case DOWN:
            d2 = box.minY;
            break;
         case SOUTH:
            d3 = box.maxZ;
            break;
         case NORTH:
            d3 = box.minZ;
      }

      return new Vec3d(d, d2, d3);
   }

   public record Entry(Box box, double weight) {

      public Box getBox() {
         return this.box;
      }

      public double getWeight() {
         return this.weight;
      }
   }
}
