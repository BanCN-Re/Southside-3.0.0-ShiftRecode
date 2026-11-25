package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

public class Pathfinder {
   public static final int[][] INT_ARRAY_ARRAY = new int[][]{
      {1, 0, 0},
      {-1, 0, 0},
      {0, 0, 1},
      {0, 0, -1},
      {1, 0, 1},
      {1, 0, -1},
      {-1, 0, 1},
      {-1, 0, -1},
      {0, 1, 0},
      {1, 1, 0},
      {-1, 1, 0},
      {0, 1, 1},
      {0, 1, -1},
      {1, 1, 1},
      {1, 1, -1},
      {-1, 1, 1},
      {-1, 1, -1},
      {0, -1, 0},
      {1, -1, 0},
      {-1, -1, 0},
      {0, -1, 1},
      {0, -1, -1},
      {1, -1, 1},
      {1, -1, -1},
      {-1, -1, 1},
      {-1, -1, -1}
   };
   public static final double DEFAULT_DOUBLE = Math.sqrt(2.0);
   public static final double DEFAULT_DOUBLE_VALUE = Math.sqrt(3.0);

   public static double computeDistance(BlockPos pos, BlockPos pos2) {
      double dx = pos.getX() - pos2.getX();
      double dy = pos.getY() - pos2.getY();
      double dz = pos.getZ() - pos2.getZ();
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   public static BlockPos findBlockPos(World world, BlockPos pos, Pathfinder.Inner2 inner2, Pathfinder.Inner inner, int n) {
      if (isValid(world, pos, inner2, inner)) {
         return pos;
      }

      for (int i = 1; i <= n; i++) {
         BlockPos pos2 = pos.up(i);
         if (isValid(world, pos2, inner2, inner)) {
            return pos2;
         }

         BlockPos pos3 = pos.down(i);
         if (isValid(world, pos3, inner2, inner)) {
            return pos3;
         }
      }

      return null;
   }

   private static boolean isValid(World world, BlockPos pos, Pathfinder.Inner2 inner2, Pathfinder.Inner inner) {
      return (!inner2.requireLoadedChunks || inner.isValid(new ChunkPos(pos)) || inner2.allowUnloadedChunks)
         && isValid(world, pos, inner2)
         && isValidEx(world, pos)
         && (!inner2.avoidHazards || !isValid(world, pos));
   }

   public static boolean isValid(World world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      Fluid fluid = blockState.getFluidState().getFluid();
      return fluid == Fluids.LAVA
         || blockState.isOf(Blocks.FIRE)
         || blockState.isOf(Blocks.SOUL_FIRE)
         || blockState.isOf(Blocks.CACTUS)
         || blockState.isOf(Blocks.SWEET_BERRY_BUSH)
         || blockState.isOf(Blocks.MAGMA_BLOCK);
   }

   public static boolean isValid(World world, BlockPos pos, int[] values, Pathfinder.Inner2 inner2, Pathfinder.Inner inner) {
      int n = values[0];
      int n2 = values[1];
      int n3 = values[2];
      if (Math.abs(n) + Math.abs(n3) >= 2) {
         BlockPos pos2 = pos.add(n, 0, 0);
         BlockPos pos3 = pos.add(0, 0, n3);
         ChunkPos chunkPos = new ChunkPos(pos2);
         ChunkPos chunkPos2 = new ChunkPos(pos3);
         if (!inner2.requireLoadedChunks || inner.isValid(chunkPos) && inner.isValid(chunkPos2)) {
            if (!isValid(world, pos2, inner2) || !isValid(world, pos3, inner2)) {
               return false;
            }

            if (n2 == 0 && !isValidEx(world, pos2) && !isValidEx(world, pos3)) {
               return false;
            }
         } else if (!inner2.allowUnloadedChunks) {
            return false;
         }
      }

      if (Math.abs(n) + Math.abs(n2) + Math.abs(n3) >= 3) {
         BlockPos pos4 = pos.add(n, n2, 0);
         BlockPos pos5 = pos.add(0, n2, n3);
         BlockPos pos6 = pos.add(n, 0, n3);
         if (inner2.requireLoadedChunks
            && (!inner.isValid(new ChunkPos(pos4)) || !inner.isValid(new ChunkPos(pos5)) || !inner.isValid(new ChunkPos(pos6)))
            && !inner2.allowUnloadedChunks) {
            return false;
         }

         if (!isValid(world, pos4, inner2) || !isValid(world, pos5, inner2) || !isValid(world, pos6, inner2)) {
            return false;
         }
      }

      return true;
   }

   public static Pathfinder.Mode getMode(World world, BlockPos pos, BlockPos pos2, Pathfinder.Inner2 inner2, Pathfinder.Inner inner) {
      ChunkPos chunkPos = new ChunkPos(pos2);
      if (!inner.isValid(chunkPos) && !inner2.allowUnloadedChunks) {
         return Pathfinder.Mode.INVALID;
      }

      boolean flag = !inner.isValid(chunkPos);
      int dy = pos2.getY() - pos.getY();
      if (!flag) {
         if (!isValid(world, pos2, inner2)) {
            return Pathfinder.Mode.INVALID;
         }

         if (inner2.avoidHazards && isValid(world, pos2)) {
            return Pathfinder.Mode.INVALID;
         }
      }

      BlockState state = flag ? Blocks.AIR.getDefaultState() : world.getBlockState(pos2);
      boolean flag2 = state.getFluidState().isIn(FluidTags.WATER) || state.getFluidState().isStill();
      boolean flag3 = inner2.allowLadder && !flag && isValidAlt(world, pos2);
      if (flag2) {
         if (!inner2.allowSwim) {
            return Pathfinder.Mode.INVALID;
         }

         if (!flag) {
            int n = computeInt(world, pos2, inner2.maxSwimDepth + 1);
            if (n > inner2.maxSwimDepth) {
               return Pathfinder.Mode.INVALID;
            }
         }

         return Pathfinder.Mode.SWIM;
      } else {
         boolean flag4 = flag || isValidEx(world, pos2);
         if (!flag4 && !flag3) {
            return Pathfinder.Mode.INVALID;
         }

         if (dy > 0) {
            if (dy > inner2.maxJumpHeight) {
               return flag3 ? Pathfinder.Mode.LADDER : Pathfinder.Mode.INVALID;
            } else {
               return !flag && !isValid(world, pos2, inner2) ? Pathfinder.Mode.INVALID : Pathfinder.Mode.JUMP;
            }
         } else if (dy < 0) {
            if (Math.abs(dy) > inner2.maxFallDistance) {
               return Pathfinder.Mode.INVALID;
            } else {
               return !flag && !isValid(world, pos2, inner2) ? Pathfinder.Mode.INVALID : Pathfinder.Mode.FALL;
            }
         } else {
            return Pathfinder.Mode.WALK;
         }
      }
   }

   public static double computeDistance(ChunkPos chunkPos, ChunkPos chunkPos2) {
      int dx = chunkPos.x - chunkPos2.x;
      int dz = chunkPos.z - chunkPos2.z;
      return Math.sqrt(dx * dx + dz * dz);
   }

   public static double computeDouble(int[] values) {
      int n = Math.abs(values[0]) + Math.abs(values[1]) + Math.abs(values[2]);
      if (n == 1) {
         return 1.0;
      } else {
         return n == 2 ? DEFAULT_DOUBLE : DEFAULT_DOUBLE_VALUE;
      }
   }

   public static boolean isValidEx(World world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos.down());
      return blockState.isSolidBlock(world, pos.down()) || blockState.getFluidState().isIn(FluidTags.WATER) || isValidAlt(world, pos.down());
   }

   public static Pathfinder.Inner5 getInner5(World world, BlockPos pos, BlockPos pos2, Pathfinder.Inner2 inner2, Pathfinder.Inner inner, AtomicBoolean atomicBoolean) {
      PriorityQueue<Pathfinder.Inner3> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(inner3 -> inner3.fCost));
      HashMap map = new HashMap();
      HashSet set = new HashSet();
      Pathfinder.Inner3 inner32 = new Pathfinder.Inner3(pos);
      inner32.gCost = 0.0;
      inner32.hCost = computeDistance(pos, pos2);
      inner32.fCost = inner32.hCost;
      priorityQueue.add(inner32);
      map.put(pos, inner32);
      Pathfinder.Inner3 inner33 = inner32;
      double hCost = inner32.hCost;
      int n = 0;

      while (!priorityQueue.isEmpty() && n++ < inner2.maxSearchIterations) {
         if (atomicBoolean.get()) {
            return new Pathfinder.Inner5(collectItems(inner33), false, inner33.blockPos, n);
         }

         Pathfinder.Inner3 inner34 = (Pathfinder.Inner3)priorityQueue.poll();
         if (inner34.hCost < hCost) {
            hCost = inner34.hCost;
            inner33 = inner34;
         }

         if (inner34.blockPos.equals(pos2)) {
            return new Pathfinder.Inner5(collectItems(inner34), true, pos2, n);
         }

         set.add(inner34.blockPos);
         ChunkPos chunkPos = new ChunkPos(inner34.blockPos);
         if (!inner2.requireLoadedChunks || inner.isValid(chunkPos) || inner2.allowUnloadedChunks) {
            for (int[] values : INT_ARRAY_ARRAY) {
               BlockPos pos3 = inner34.blockPos.add(values[0], values[1], values[2]);
               if (!set.contains(pos3)) {
                  ChunkPos chunkPos2 = new ChunkPos(pos3);
                  if (!inner2.requireLoadedChunks || inner.isValid(chunkPos2) || inner2.allowUnloadedChunks) {
                     Pathfinder.Mode mode = getMode(world, inner34.blockPos, pos3, inner2, inner);
                     if (mode != Pathfinder.Mode.INVALID && isValid(world, inner34.blockPos, values, inner2, inner)) {
                        double d = computeDouble(values);
                        if (!inner.isValid(chunkPos2) && inner2.allowUnloadedChunks) {
                           d += inner2.unloadedChunkPenalty;
                        }

                        if (mode == Pathfinder.Mode.JUMP) {
                           d *= 1.4;
                        } else if (mode == Pathfinder.Mode.FALL) {
                           d *= 1.2;
                        } else if (mode == Pathfinder.Mode.SWIM) {
                           d *= 1.8;
                        } else if (mode == Pathfinder.Mode.LADDER) {
                           d *= 1.5;
                        }

                        double d2 = inner34.gCost + d;
                        Pathfinder.Inner3 inner35 = (Pathfinder.Inner3)map.get(pos3);
                        if (inner35 == null) {
                           inner35 = new Pathfinder.Inner3(pos3);
                           map.put(pos3, inner35);
                        }

                        if (!(d2 >= inner35.gCost)) {
                           inner35.gCost = d2;
                           inner35.hCost = computeDistance(pos3, pos2);
                           inner35.fCost = inner35.gCost + inner35.hCost;
                           inner35.parent = inner34;
                           if (!priorityQueue.contains(inner35)) {
                              priorityQueue.add(inner35);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return new Pathfinder.Inner5(collectItems(inner33), false, inner33.blockPos, n);
   }

   public static BlockPos findBlockPosEx(World world, BlockPos pos, Pathfinder.Inner2 inner2, Pathfinder.Inner inner, int n) {
      ArrayDeque arrayDeque = new ArrayDeque();
      HashSet set = new HashSet();
      arrayDeque.add(pos);
      set.add(pos);
      int n2 = 0;

      while (!arrayDeque.isEmpty() && n2++ < n * n * 20) {
         BlockPos pos2 = (BlockPos)arrayDeque.poll();
         if (isValid(world, pos2, inner2, inner)) {
            return pos2;
         }

         for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
               for (int k = -1; k <= 1; k++) {
                  if (i != 0 || j != 0 || k != 0) {
                     BlockPos pos3 = pos2.add(i, j, k);
                     if (set.add(pos3)) {
                        arrayDeque.add(pos3);
                     }
                  }
               }
            }
         }
      }

      return pos;
   }

   public static boolean isValid(World world, BlockPos pos, Pathfinder.Inner2 inner2) {
      int n = (int)Math.ceil(inner2.entityHeight);

      for (int i = 0; i < n; i++) {
         BlockPos pos2 = pos.up(i);
         BlockState blockState = world.getBlockState(pos2);
         if (!blockState.isAir() && !blockState.getFluidState().isStill() && !blockState.getCollisionShape(world, pos2).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public static int computeInt(World world, BlockPos pos, int n) {
      int n2 = 0;
      BlockPos pos2 = pos;

      for (int i = 0; i < n; i++) {
         BlockState blockState = world.getBlockState(pos2);
         if (!blockState.getFluidState().isIn(FluidTags.WATER) && !blockState.getFluidState().isStill()) {
            break;
         }

         n2++;
         pos2 = pos2.down();
      }

      return n2;
   }

   public static boolean isValidAlt(World world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      return blockState.isOf(Blocks.LADDER) || blockState.isOf(Blocks.VINE) || blockState.isOf(Blocks.SCAFFOLDING);
   }

   public static List<BlockPos> collectItems(Pathfinder.Inner3 inner3) {
      ArrayList list = new ArrayList();

      for (Pathfinder.Inner3 i = inner3; i != null; i = i.parent) {
         list.add(i.blockPos);
      }

      Collections.reverse(list);
      return list;
   }

   public static Pathfinder.Inner5 getInner5(World world, BlockPos pos, BlockPos pos2, Pathfinder.Inner2 inner2) {
      Pathfinder.Inner2 inner22 = inner2 == null ? new Pathfinder.Inner2() : inner2;
      Pathfinder.Inner inner = new Pathfinder.Inner(world);
      AtomicBoolean atomicBoolean = new AtomicBoolean(false);
      Pathfinder.Inner5 inner5 = getInner5(world, pos, pos2, inner22, inner, atomicBoolean);
      if (inner5 == null) {
         return new Pathfinder.Inner5(Collections.emptyList(), false, pos, 0);
      }

      if (inner5.reachedTarget && inner5.endPos.equals(pos2)) {
         return inner5;
      }

      BlockPos pos3 = findBlockPosEx(world, pos2, inner22, inner, Math.max(6, inner22.nearestSearchRadius));
      if (pos3 != null && !pos3.equals(pos2)) {
         Pathfinder.Inner5 inner52 = getInner5(world, pos, pos3, inner22, inner, atomicBoolean);
         if (inner52 != null && !inner52.blockPath.isEmpty()) {
            return new Pathfinder.Inner5(inner52.blockPath, false, inner52.endPos, inner52.iterations);
         }
      }

      return inner5;
   }

   public static class Inner {
      @SerializedName("world")
      public final World world;
      @SerializedName("loadedChunks")
      public final Map<ChunkPos, Boolean> loadedChunks = new ConcurrentHashMap<>();

      public boolean isValid(ChunkPos chunkPos) {
         return this.loadedChunks.computeIfAbsent(chunkPos, this::getBoolean_);
      }

      public Boolean getBoolean_(ChunkPos chunkPos) {
         Chunk chunk = this.world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
         return chunk != null;
      }

      public Inner(World world2) {
         this.world = world2;
      }
   }

   public static class Inner2 {
      @SerializedName("maxFallDistance")
      public int maxFallDistance = 3;
      @SerializedName("maxJumpHeight")
      public int maxJumpHeight = 1;
      @SerializedName("allowSwim")
      public boolean allowSwim = true;
      @SerializedName("maxSwimDepth")
      public int maxSwimDepth = 3;
      @SerializedName("allowLadder")
      public boolean allowLadder = true;
      @SerializedName("avoidHazards")
      public boolean avoidHazards = true;
      @SerializedName("entityHeight")
      public double entityHeight = 1.8;
      @SerializedName("headClearance")
      public double headClearance = 0.6;
      @SerializedName("nearestSearchRadius")
      public int nearestSearchRadius = 8;
      @SerializedName("maxSearchIterations")
      public int maxSearchIterations = 20000;
      @SerializedName("requireLoadedChunks")
      public boolean requireLoadedChunks = true;
      @SerializedName("allowUnloadedChunks")
      public boolean allowUnloadedChunks = true;
      @SerializedName("unloadedChunkPenalty")
      public double unloadedChunkPenalty = 8.0;
      @SerializedName("allowDiagonal")
      public boolean allowDiagonal = true;
      @SerializedName("allowParkour")
      public boolean allowParkour = false;
      @SerializedName("allowBreakBlocks")
      public boolean allowBreakBlocks = false;
   }

   public static class Inner3 {
      @SerializedName("blockPos")
      public BlockPos blockPos;
      @SerializedName("parent")
      public Pathfinder.Inner3 parent;
      @SerializedName("gCost")
      public double gCost = Double.MAX_VALUE;
      @SerializedName("hCost")
      public double hCost;
      @SerializedName("fCost")
      public double fCost;

      public Inner3(BlockPos pos) {
         this.blockPos = pos;
      }
   }

   public interface Inner4 {
      void performAction();

      void performActionEx();
   }

   public static class Inner5 {
      @SerializedName("blockPath")
      public final List<BlockPos> blockPath;
      @SerializedName("reachedTarget")
      public final boolean reachedTarget;
      @SerializedName("endPos")
      public final BlockPos endPos;
      @SerializedName("iterations")
      public final int iterations;

      public Inner5(List<BlockPos> list, boolean flag, BlockPos pos, int n) {
         this.blockPath = list;
         this.reachedTarget = flag;
         this.endPos = pos;
         this.iterations = n;
      }
   }

   public static class Inner6 {
      @SerializedName("executorService")
      public final ExecutorService executorService = Executors.newSingleThreadExecutor(Pathfinder.Inner6::getThread);
      @SerializedName("future")
      public Future<?> future;
      @SerializedName("cancelled")
      public final AtomicBoolean cancelled = new AtomicBoolean(false);
      @SerializedName("result")
      public Pathfinder.Inner5 result;
      private static final long DEFAULT_LONG = -3064269920847003642L;

      public void performAction(Pathfinder.Inner2 inner2, World world, BlockPos pos, BlockPos pos2, Pathfinder.Inner4 inner4) {
         try {
            Pathfinder.Inner2 inner22 = inner2 == null ? new Pathfinder.Inner2() : inner2;
            Pathfinder.Inner inner = new Pathfinder.Inner(world);
            AtomicBoolean cancelled2 = this.cancelled;
            Pathfinder.Inner5 inner5 = Pathfinder.getInner5(world, pos, pos2, inner22, inner, cancelled2);
            if (!inner5.reachedTarget) {
               BlockPos pos3 = Pathfinder.findBlockPosEx(world, pos2, inner22, inner, Math.max((int)DEFAULT_LONG, inner22.nearestSearchRadius));
               if (!pos3.equals(pos2)) {
                  Pathfinder.Inner5 inner52 = Pathfinder.getInner5(world, pos, pos3, inner22, inner, cancelled2);
                  if (!inner52.blockPath.isEmpty()) {
                     inner5 = new Pathfinder.Inner5(inner52.blockPath, false, inner52.endPos, inner52.iterations);
                  }
               }
            }

            this.result = inner5;
            if (!this.cancelled.get() && inner4 != null) {
               inner4.performActionEx();
            }
         } catch (Exception e) {
            if (inner4 != null) {
               inner4.performAction();
            }
         }
      }

      public void shutdownNow() {
         this.executorService.shutdownNow();
      }

      public static Thread getThread(Runnable action) {
         Thread thread = new Thread(action, "Pathfinder-Thread");
         thread.setDaemon(true);
         return thread;
      }

      public void performAction() {
         this.cancelled.set(true);
         if (this.future != null) {
            this.future.cancel(true);
         }
      }

      public Pathfinder.Inner5 getResult() {
         return this.result;
      }

      public void performAction(World world, BlockPos pos, BlockPos pos2, Pathfinder.Inner2 inner2, Pathfinder.Inner4 inner4) {
         this.cancelled.set(false);
         this.future = this.executorService.submit(() -> this.performAction(inner2, world, pos, pos2, inner4));
      }

      public boolean get() {
         return this.cancelled.get();
      }
   }

   public enum Mode {
      WALK,
      JUMP,
      FALL,
      SWIM,
      LADDER,
      INVALID;

      private static final long DEFAULT_LONG = 6367992570617790470L;

      // $VF: synthetic method
      private static Pathfinder.Mode[] getModeList() {
         Pathfinder.Mode[] modeArray = new Pathfinder.Mode[(int)DEFAULT_LONG];
         modeArray[0] = WALK;
         modeArray[1] = JUMP;
         modeArray[2] = FALL;
         modeArray[3] = SWIM;
         modeArray[4] = LADDER;
         modeArray[5] = INVALID;
         return modeArray;
      }


   }
}
