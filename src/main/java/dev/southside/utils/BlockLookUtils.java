package dev.southside.utils;

import cc.polyfrost.oneconfig.config.Config;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.ShortPlantBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class BlockLookUtils {
   private static final double DEFAULT_DOUBLE = 1.0E-7;
   public static Vec3d vector = null;
   private static MinecraftClient mc = MinecraftClient.getInstance();
   private static Config[] configCache;

   public static boolean isValid(Rotation rotation, BlockPos pos, Direction direction, boolean flag) {
      return isValid(mc.player, rotation.yaw, rotation.pitch, pos, direction, flag, BlockLookUtils::isValidBlock);
   }

   public static boolean isValid(PlayerEntity player, float f, float f2, BlockPos pos, Direction direction, boolean flag) {
      return isValid(player, f, f2, pos, direction, flag, BlockLookUtils::isValidBlock);
   }

   public static boolean isValid(Rotation rotation, BlockPos pos, Direction direction, boolean flag, Predicate<BlockState> predicate) {
      return isValid(mc.player, rotation.yaw, rotation.pitch, pos, direction, flag, predicate);
   }

   public static boolean isValid(PlayerEntity player, float f, float f2, BlockPos pos, Direction direction, boolean flag, Predicate<BlockState> predicate) {
      if (player != null && direction != null) {
         BlockHitResult hitResult = raycast(f, f2, predicate);
         return hitResult != null
            && pos.getX() == hitResult.getBlockPos().getX()
            && pos.getY() == hitResult.getBlockPos().getY()
            && pos.getZ() == hitResult.getBlockPos().getZ()
            && (direction == hitResult.getSide() || !flag);
      } else {
         return false;
      }
   }

   public static void performAction() {
      if (mc.player != null) {
         vector = mc.player.getEyePos();
      }
   }

   @Nullable
   private static Direction getDirection(Vec3d vec, Box box) {
      if (Math.abs(vec.x - box.minX) < 1.0E-7) {
         return Direction.WEST;
      } else if (Math.abs(vec.x - box.maxX) < 1.0E-7) {
         return Direction.EAST;
      } else if (Math.abs(vec.y - box.minY) < 1.0E-7) {
         return Direction.DOWN;
      } else if (Math.abs(vec.y - box.maxY) < 1.0E-7) {
         return Direction.UP;
      } else if (Math.abs(vec.z - box.minZ) < 1.0E-7) {
         return Direction.NORTH;
      } else {
         return Math.abs(vec.z - box.maxZ) < 1.0E-7 ? Direction.SOUTH : null;
      }
   }

   public static BlockHitResult raycastHit(float x, float y) {
      return raycast(x, y, BlockLookUtils::isValidBlock);
   }

   public static BlockHitResult raycast(float f, float f2, Predicate<BlockState> predicate) {
      MinecraftClient instance = MinecraftClient.getInstance();
      double blockInteractionRange = instance.player.getBlockInteractionRange();
      if (f == 0.0F && f2 == 0.0F) {
         return null;
      }

      Vec3d vec = vector;
      Vec3d vec2 = Vec3d.fromPolar(f2, f);
      Vec3d vec3 = vec.add(vec2.multiply(blockInteractionRange));
      if (vec2.x == 0.0) {
         vec2 = new Vec3d(1.0E-7, vec2.y, vec2.z);
      }

      if (vec2.y == 0.0) {
         vec2 = new Vec3d(vec2.x, 1.0E-7, vec2.z);
      }

      if (vec2.z == 0.0) {
         vec2 = new Vec3d(vec2.x, vec2.y, 1.0E-7);
      }

      BlockPos pos = BlockPos.ofFloored(vec);
      int n = (int)Math.signum(vec2.x);
      int n2 = (int)Math.signum(vec2.y);
      int n3 = (int)Math.signum(vec2.z);
      double d = n > 0 ? pos.getX() + 1 : pos.getX();
      double d2 = n2 > 0 ? pos.getY() + 1 : pos.getY();
      double d3 = n3 > 0 ? pos.getZ() + 1 : pos.getZ();
      double d4 = (d - vec.x) / vec2.x;
      double d5 = (d2 - vec.y) / vec2.y;
      double d6 = (d3 - vec.z) / vec2.z;
      double d7 = n / vec2.x;
      double d8 = n2 / vec2.y;
      double d9 = n3 / vec2.z;
      World world = mc.player.getWorld();

      while (vec.distanceTo(pos.toCenterPos()) <= blockInteractionRange) {
         if (!world.isAir(pos)) {
            BlockState blockState = world.getBlockState(pos);
            if (!predicate.test(blockState)) {
               FluidState fluidState = world.getFluidState(pos);
               VoxelShape shape;
               if (fluidState != null && fluidState.isStill() && fluidState.getFluid() == Fluids.WATER) {
                  shape = VoxelShapes.fullCube();
               } else {
                  shape = blockState.getCollisionShape(world, pos, ShapeContext.of(instance.player));
               }

               if (!shape.isEmpty()) {
                  for (Box box : shape.getBoundingBoxes()) {
                     Box box2 = box.offset(pos);
                     Optional hitResult = box2.raycast(vec, vec3);
                     if (hitResult.isPresent()) {
                        Vec3d vec4 = (Vec3d)hitResult.get();
                        Direction direction2 = getDirection2(vec4, box2);
                        boolean flag = box2.contains(vec);
                        return new BlockHitResult(vec4, direction2, pos, flag);
                     }
                  }
               }
            }
         }

         if (d4 < d5) {
            if (d4 < d6) {
               pos = pos.add(n, 0, 0);
               d4 += d7;
            } else {
               pos = pos.add(0, 0, n3);
               d6 += d9;
            }
         } else if (d5 < d6) {
            pos = pos.add(0, n2, 0);
            d5 += d8;
         } else {
            pos = pos.add(0, 0, n3);
            d6 += d9;
         }
      }

      return null;
   }

   public static boolean isValidBlock(BlockState state) {
      Block block = state.getBlock();
      return block instanceof PlantBlock
         || block instanceof SnowBlock
         || block instanceof AirBlock
         || block instanceof ShortPlantBlock
         || block instanceof FluidBlock;
   }

   private static Direction getDirection2(Vec3d vec, Box box) {
      double d = 1.0E-7;
      if (Math.abs(vec.x - box.minX) <= 1.0E-7) {
         return Direction.WEST;
      }

      if (Math.abs(vec.x - box.maxX) <= 1.0E-7) {
         return Direction.EAST;
      }

      if (Math.abs(vec.y - box.minY) <= 1.0E-7) {
         return Direction.DOWN;
      }

      if (Math.abs(vec.y - box.maxY) <= 1.0E-7) {
         return Direction.UP;
      }

      if (Math.abs(vec.z - box.minZ) <= 1.0E-7) {
         return Direction.NORTH;
      }

      if (Math.abs(vec.z - box.maxZ) <= 1.0E-7) {
         return Direction.SOUTH;
      }

      double d2 = Math.abs(vec.x - box.minX);
      double d3 = Math.abs(vec.x - box.maxX);
      double d4 = Math.abs(vec.y - box.minY);
      double d5 = Math.abs(vec.y - box.maxY);
      double d6 = Math.abs(vec.z - box.minZ);
      double d7 = Math.abs(vec.z - box.maxZ);
      double d8 = d2;
      Direction west = Direction.WEST;
      if (d3 < d8) {
         d8 = d3;
         west = Direction.EAST;
      }

      if (d4 < d8) {
         d8 = d4;
         west = Direction.DOWN;
      }

      if (d5 < d8) {
         d8 = d5;
         west = Direction.UP;
      }

      if (d6 < d8) {
         d8 = d6;
         west = Direction.NORTH;
      }

      if (d7 < d8) {
         west = Direction.SOUTH;
      }

      return west;
   }

   public static BlockHitResult raycast(float x, float y) {
      MinecraftClient instance = MinecraftClient.getInstance();
      double blockInteractionRange = instance.player.getBlockInteractionRange();
      Vec3d vec = vector;
      Vec3d vec2 = Vec3d.fromPolar(y, x);
      Vec3d vec3 = vec.add(vec2.multiply(blockInteractionRange));
      if (vec2.x == 0.0) {
         vec2 = new Vec3d(1.0E-7, vec2.y, vec2.z);
      }

      if (vec2.y == 0.0) {
         vec2 = new Vec3d(vec2.x, 1.0E-7, vec2.z);
      }

      if (vec2.z == 0.0) {
         vec2 = new Vec3d(vec2.x, vec2.y, 1.0E-7);
      }

      BlockPos pos = BlockPos.ofFloored(vec);
      int n = (int)Math.signum(vec2.x);
      int n2 = (int)Math.signum(vec2.y);
      int n3 = (int)Math.signum(vec2.z);
      double d = n > 0 ? pos.getX() + 1 : pos.getX();
      double d2 = n2 > 0 ? pos.getY() + 1 : pos.getY();
      double d3 = n3 > 0 ? pos.getZ() + 1 : pos.getZ();
      double d4 = (d - vec.x) / vec2.x;
      double d5 = (d2 - vec.y) / vec2.y;
      double d6 = (d3 - vec.z) / vec2.z;
      double d7 = n / vec2.x;
      double d8 = n2 / vec2.y;
      double d9 = n3 / vec2.z;

      while (vec.distanceTo(pos.toCenterPos()) <= blockInteractionRange) {
         if (!mc.player.getWorld().isAir(pos) && isValidBlockEx(mc.player.getWorld().getBlockState(pos))) {
            Box box = new Box(pos);
            Optional hitResult = box.raycast(vec, vec3);
            if (hitResult.isPresent()) {
               Vec3d vec4 = (Vec3d)hitResult.get();
               Direction direction = getDirection(vec4, box);
               boolean flag = box.contains(vec);
               return new BlockHitResult(vec4, direction, pos, flag);
            }
         }

         if (d4 < d5) {
            if (d4 < d6) {
               pos = pos.add(n, 0, 0);
               d4 += d7;
            } else {
               pos = pos.add(0, 0, n3);
               d6 += d9;
            }
         } else if (d5 < d6) {
            pos = pos.add(0, n2, 0);
            d5 += d8;
         } else {
            pos = pos.add(0, 0, n3);
            d6 += d9;
         }
      }

      return null;
   }

   private static boolean isValidBlockEx(BlockState state) {
      return state.getBlock() instanceof ChestBlock || state.getBlock() instanceof EnderChestBlock || state.getBlock() instanceof ShulkerBoxBlock;
   }

   public static double computeDouble(float f, float f2, Entity entity) {
      Vec3d dir = Vec3d.fromPolar(f2, f).normalize();
      Box boundingBox = entity.getBoundingBox();
      return computeDouble(vector, dir, boundingBox);
   }

   public static boolean isHovered(float f, float f2, double d, Entity entity) {
      return isHovered(f, f2, d, entity.getBoundingBox());
   }

   public static boolean isHovered(float f, float f2, double d, Box box) {
      if (box == null) {
         return false;
      }

      Vec3d vec = vector;
      ClientWorld world = MinecraftClient.getInstance().world;
      Vec3d dir = Vec3d.fromPolar(f2, f).normalize();
      if (dir.lengthSquared() < 1.0E-12) {
         return false;
      }

      double d2 = computeDouble(vec, dir, box);
      return !Double.isFinite(d2) || d2 < 0.0 || d2 > d ? false : !isValid(world, vec, dir, d2, d, mc.player);
   }

   private static boolean isValid(World world, Vec3d vec, Vec3d vec2, double d, double d2, Entity entity) {
      int n = MathHelper.floor(vec.x);
      int n2 = MathHelper.floor(vec.y);
      int n3 = MathHelper.floor(vec.z);
      int n4 = vec2.x > 0.0 ? 1 : (vec2.x < 0.0 ? -1 : 0);
      int n5 = vec2.y > 0.0 ? 1 : (vec2.y < 0.0 ? -1 : 0);
      int n6 = vec2.z > 0.0 ? 1 : (vec2.z < 0.0 ? -1 : 0);
      double d3 = computeDouble(vec.x, vec2.x, n, n4);
      double d4 = computeDouble(vec.y, vec2.y, n2, n5);
      double d5 = computeDouble(vec.z, vec2.z, n3, n6);
      double d6 = n4 != 0 ? 1.0 / Math.abs(vec2.x) : Double.POSITIVE_INFINITY;
      double d7 = n5 != 0 ? 1.0 / Math.abs(vec2.y) : Double.POSITIVE_INFINITY;
      double d8 = n6 != 0 ? 1.0 / Math.abs(vec2.z) : Double.POSITIVE_INFINITY;
      double d9 = Math.min(d, d2);
      if (isValid(world, vec, vec2, new BlockPos(n, n2, n3), d, entity)) {
         return true;
      }

      while (true) {
         if (d3 < d4) {
            if (d3 < d5) {
               if (d3 > d9) {
                  break;
               }

               n += n4;
               if (isValid(world, vec, vec2, new BlockPos(n, n2, n3), d, entity)) {
                  return true;
               }

               d3 += d6;
            } else {
               if (d5 > d9) {
                  break;
               }

               n3 += n6;
               if (isValid(world, vec, vec2, new BlockPos(n, n2, n3), d, entity)) {
                  return true;
               }

               d5 += d8;
            }
         } else if (d4 < d5) {
            if (d4 > d9) {
               break;
            }

            n2 += n5;
            if (isValid(world, vec, vec2, new BlockPos(n, n2, n3), d, entity)) {
               return true;
            }

            d4 += d7;
         } else {
            if (d5 > d9) {
               break;
            }

            n3 += n6;
            if (isValid(world, vec, vec2, new BlockPos(n, n2, n3), d, entity)) {
               return true;
            }

            d5 += d8;
         }
      }

      return false;
   }

   private static double computeDouble(double d, double d2, int n, int n2) {
      if (n2 == 0) {
         return Double.POSITIVE_INFINITY;
      }

      double d3 = n2 > 0 ? n + 1 : n;
      return (d3 - d) / d2;
   }

   private static boolean isValid(World world, Vec3d vec, Vec3d vec2, BlockPos pos, double d, Entity entity) {
      BlockState blockState = world.getBlockState(pos);
      if (blockState.isAir()) {
         return false;
      }

      VoxelShape outlineShape = blockState.getOutlineShape(world, pos, entity != null ? ShapeContext.of(entity) : ShapeContext.absent());
      if (outlineShape.isEmpty()) {
         return false;
      }

      for (Box box : outlineShape.getBoundingBoxes()) {
         Box box2 = box.offset(pos);
         double d2 = computeDouble(vec, vec2, box2);
         if (Double.isFinite(d2) && d2 >= 0.0 && d2 < d) {
            return true;
         }
      }

      return false;
   }

   public static double computeDouble(Vec3d vec, Vec3d vec2, Box box) {
      double d = 0.0;
      double positive_infinity = Double.POSITIVE_INFINITY;
      if (!isHovered(vec.x, vec2.x, box.minX, box.maxX, BlockLookUtils.Inner.DEFAULT_DOUBLES)) {
         return Double.POSITIVE_INFINITY;
      }

      d = Math.max(d, BlockLookUtils.Inner.DEFAULT_DOUBLES[0]);
      positive_infinity = Math.min(positive_infinity, BlockLookUtils.Inner.DEFAULT_DOUBLES[1]);
      if (positive_infinity < d) {
         return Double.POSITIVE_INFINITY;
      }

      if (!isHovered(vec.y, vec2.y, box.minY, box.maxY, BlockLookUtils.Inner.DEFAULT_DOUBLES)) {
         return Double.POSITIVE_INFINITY;
      }

      d = Math.max(d, BlockLookUtils.Inner.DEFAULT_DOUBLES[0]);
      positive_infinity = Math.min(positive_infinity, BlockLookUtils.Inner.DEFAULT_DOUBLES[1]);
      if (positive_infinity < d) {
         return Double.POSITIVE_INFINITY;
      }

      if (!isHovered(vec.z, vec2.z, box.minZ, box.maxZ, BlockLookUtils.Inner.DEFAULT_DOUBLES)) {
         return Double.POSITIVE_INFINITY;
      }

      d = Math.max(d, BlockLookUtils.Inner.DEFAULT_DOUBLES[0]);
      positive_infinity = Math.min(positive_infinity, BlockLookUtils.Inner.DEFAULT_DOUBLES[1]);
      return positive_infinity < d ? Double.POSITIVE_INFINITY : d;
   }

   private static boolean isHovered(double d, double d2, double d3, double d4, double[] coords) {
      if (Math.abs(d2) < 1.0E-12) {
         if (!(d < d3) && !(d > d4)) {
            coords[0] = Double.NEGATIVE_INFINITY;
            coords[1] = Double.POSITIVE_INFINITY;
            return true;
         } else {
            return false;
         }
      } else {
         double d5 = 1.0 / d2;
         double d6 = (d3 - d) * d5;
         double d7 = (d4 - d) * d5;
         if (d6 > d7) {
            double d8 = d6;
            d6 = d7;
            d7 = d8;
         }

         coords[0] = d6;
         coords[1] = d7;
         return true;
      }
   }

   static {
      setConfigCache(null);
   }

   public static void setConfigCache(Config[] configArray) {
      configCache = configArray;
   }

   private static final class Inner {
      public static final double[] DEFAULT_DOUBLES = new double[2];
   }
}
