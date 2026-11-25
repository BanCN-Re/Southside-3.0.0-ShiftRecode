package dev.southside.utils.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.utils.ValuePair;
import dev.southside.utils.Pathfinder;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.IText;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import dev.southside.utils.rotation.MoveFixMode;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AIWalkController {
   public static final MinecraftClient mc = MinecraftClient.getInstance();
   public static final LinkedList<BlockPos> path = new LinkedList<>();
   public static volatile boolean active = false;
   public static final AIWalkController INSTANCE = new AIWalkController();
   public static boolean activeValue = false;
   public static boolean activeData = false;
   public static double value = 2.0;
   public static long timestamp = 1500L;
   public static long timestampValue = 1000L;
   public static boolean activeRef = true;
   public static int count = 8;
   public static long timestampData = 120L;
   public static int countValue = 3;
   @SerializedName("executorService")
   public final ExecutorService executorService = Executors.newSingleThreadExecutor(AIWalkController::walkTo);
   @SerializedName("cancelled")
   public final AtomicBoolean cancelled = new AtomicBoolean(false);
   @SerializedName("targetPos")
   public volatile BlockPos targetPos = null;
   @SerializedName("pathTask")
   public volatile Future<?> pathTask = null;
   @SerializedName("lastPosition")
   public Vec3d lastPosition = null;
   @SerializedName("lastMovedTime")
   public long lastMovedTime = 0L;
   @SerializedName("lastRepathTime")
   public long lastRepathTime = 0L;
   @SerializedName("followInProgress")
   public final AtomicBoolean followInProgress = new AtomicBoolean(false);

   public void performAction() {
      this.cancelled.set(true);
      if (this.pathTask != null) {
         this.pathTask.cancel(true);
      }

      synchronized (path) {
         path.clear();
      }

      active = false;
      if (mc.player != null) {
         mc.options.forwardKey.setPressed(false);
         mc.options.jumpKey.setPressed(false);
      }
   }

   private static void applyText(String text, Formatting formatting) {
      if (mc.inGameHud != null) {
         mc.inGameHud.getChatHud().addMessage(Text.empty().append(IText.TEXT).append(Text.literal(text).setStyle(Style.EMPTY.withColor(formatting))));
      }
   }

   public void performActionEx() {
      if (mc.player != null) {
         applyText("[PathFinder] 最终拼接失败，尝试重新开始寻路", Formatting.RED);
      }

      walkTo(this.targetPos);
   }

   public static void stopWalking() {
      if (mc.player != null) {
         applyText("[PathFinder] 无法找到到达目标的路径", Formatting.RED);
      }
   }

   public static void performAction(Exception e) {
      if (mc.player != null) {
         applyText("[PathFinder] 寻路异常: " + e.getMessage(), Formatting.RED);
      }
   }

   public static void performActionAlt() {
      if (mc.player != null) {
         applyText("[PathFinder] 重新规划未找到路径", Formatting.RED);
      }
   }

   public boolean isFrontBlocked(Vec3d vec, double d, double d2) {
      double dist = Math.sqrt(d * d + d2 * d2);
      if (dist < 1.0E-6) {
         return false;
      }

      double d3 = d / dist;
      double d4 = d2 / dist;
      BlockPos pos = new BlockPos((int)Math.floor(vec.x + d3 * 0.6), (int)Math.floor(vec.y), (int)Math.floor(vec.z + d4 * 0.6));
      BlockPos pos2 = pos.up(1);
      boolean flag = !mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).isEmpty();
      boolean flag2 = !mc.world.getBlockState(pos2).getCollisionShape(mc.world, pos2).isEmpty();
      return flag || flag2;
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (active && mc.player != null && mc.world != null && mc.currentScreen == null) {
         Vec3d pos = mc.player.getPos();
         BlockPos pos2 = null;
         double max_value = Double.MAX_VALUE;
         synchronized (path) {
            if (path.isEmpty()) {
               performActionSafe();
               return;
            }

            for (BlockPos pos3 : path) {
               double dist = pos.squaredDistanceTo(pos3.toCenterPos());
               if (dist < max_value) {
                  max_value = dist;
                  pos2 = pos3;
               }
            }

            while (!path.isEmpty() && pos.squaredDistanceTo(path.peek().toCenterPos()) < 0.2) {
               path.poll();
            }
         }

         if (pos2 != null) {
            double d = pos2.getX() + 0.5 - pos.x;
            double d2 = pos2.getZ() + 0.5 - pos.z;
            double dy = pos2.getY() - pos.y;
            double dist2 = Math.sqrt(d * d + d2 * d2);
            if (this.lastPosition == null) {
               this.lastPosition = pos;
               this.lastMovedTime = System.currentTimeMillis();
            } else if (pos.squaredDistanceTo(this.lastPosition) > 5.0E-4) {
               this.lastPosition = pos;
               this.lastMovedTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - this.lastMovedTime > 1000L) {
               this.performActionImpl();
               return;
            }

            if (dist2 > value && System.currentTimeMillis() - this.lastRepathTime > 1500L) {
               this.lastRepathTime = System.currentTimeMillis();
               this.performActionImpl();
            } else if (dist2 < 0.35 && Math.abs(dy) < 1.2) {
               synchronized (path) {
                  path.poll();
                  if (!path.isEmpty()) {
                     mc.options.jumpKey.setPressed(false);
                     return;
                  }
               }

               if (this.isValidBlock(this.targetPos)) {
                  active = false;
                  short s = -9228;
                  mc.options.forwardKey.setPressed(false);
                  short s2 = 27283;
                  mc.options.jumpKey.setPressed(false);
                  applyText("[PathFinder] 到达目标", Formatting.GRAY);
               } else {
                  this.performActionWithOffset();
                  mc.options.forwardKey.setPressed(false);
               }
            } else {
               float f = (float)Math.toDegrees(Math.atan2(-d, d2));
               if (Southside.INSTANCE.getModuleManager().getModule(StrafeFix.class).isEnabled()) {
                  RotationManager.rotationRequests.add(new ValuePair<>(new Rotation(f, mc.player.getPitch()), 0));
                  RotationManager.moveFixMode = MoveFixMode.Strict;
               } else {
                  mc.player.setYaw(f);
               }

               if (this.isFrontBlocked(pos, d, d2)) {
                  this.performActionImpl();
               } else {
                  if (dy > 0.5 && mc.player.isOnGround()) {
                     mc.player.jump();
                  }

                  mc.options.forwardKey.setPressed(true);
               }
            }
         }
      }
   }

   public static void walkTo(List<BlockPos> list) {
      synchronized (path) {
         path.clear();
         path.addAll(list);
         active = true;
      }
   }

   public void performActionImpl() {
      if (System.currentTimeMillis() - this.lastRepathTime >= 1500L) {
         this.lastRepathTime = System.currentTimeMillis();
         if (this.targetPos != null) {
            this.performAction();
            this.cancelled.set(false);
            this.pathTask = this.executorService.submit((Runnable)this::performActionCustom);
         }
      }
   }

   public boolean isValidBlock(BlockPos pos) {
      if (pos == null) {
         return true;
      }

      Vec3d pos2 = mc.player.getPos();
      double d = pos2.x - (pos.getX() + 0.5);
      double d2 = pos2.z - (pos.getZ() + 0.5);
      double dist = Math.sqrt(d * d + d2 * d2);
      double d3 = Math.abs(pos2.y - pos.getY());
      return dist <= 0.7 && d3 <= 0.9;
   }

   public List<BlockPos> buildFullPathByStitching(World world, BlockPos pos, BlockPos pos2, Pathfinder.Inner2 inner2) {
      if (world != null && pos != null && pos2 != null) {
         LinkedList linkedList = new LinkedList();
         BlockPos pos3 = pos;
         int n = 0;
         int n2 = 0;

         while (n < 8 && !this.cancelled.get()) {
            Pathfinder.Inner5 inner5 = Pathfinder.getInner5(world, pos3, pos2, inner2);
            if (inner5 != null && inner5.blockPath != null && !inner5.blockPath.isEmpty()) {
               if (linkedList.isEmpty()) {
                  linkedList.addAll(inner5.blockPath);
               } else if (((BlockPos)linkedList.getLast()).equals(inner5.blockPath.getFirst())) {
                  for (int i = 1; i < inner5.blockPath.size(); i++) {
                     linkedList.add(inner5.blockPath.get(i));
                  }
               } else {
                  linkedList.addAll(inner5.blockPath);
               }

               n++;
               if (inner5.reachedTarget && inner5.endPos.equals(pos2)) {
                  return linkedList;
               }

               if (inner5.endPos.equals(pos3)) {
                  if (++n2 > 3) {
                     break;
                  }

                  inner2.allowUnloadedChunks = true;
                  inner2.unloadedChunkPenalty = Math.max(1.0, inner2.unloadedChunkPenalty / 2.0);
               } else {
                  pos3 = inner5.endPos;
               }
            } else if (++n2 > 3) {
               return linkedList.isEmpty() ? null : linkedList;
            }

            performActionDirect();
         }

         return linkedList.isEmpty() ? null : linkedList;
      } else {
         return null;
      }
   }

   private static void performActionDirect() {
      try {
         Thread.sleep(120L);
      } catch (InterruptedException e) {
      }
   }

   public static void performActionSafe() {
      INSTANCE.performAction();
   }

   public static void walkTo(BlockPos pos) {
      INSTANCE.updateBlock(pos);
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      LinkedList linkedList;
      synchronized (path) {
         linkedList = new LinkedList<>(path);
      }

      if (!linkedList.isEmpty()) {
         for (Object obj : linkedList) {
            BlockPos pos = (BlockPos)obj;
            Box box = new Box(pos);
            BoxMeshRenderer.renderBox(box, new Color(0, 200, 0, 120));
            BoxMeshRenderer.renderBoxEx(box, new Color(0, 200, 0, 60));
         }

         Box box2 = new Box((BlockPos)linkedList.getLast());
         BoxMeshRenderer.renderBox(box2, new Color(255, 0, 0, 150));
         BoxMeshRenderer.renderBoxEx(box2, new Color(255, 0, 0, 100));
      }
   }

   public void updateBlock(BlockPos pos) {
      if (mc.player != null && mc.world != null) {
         performActionSafe();
         this.targetPos = pos;
         this.cancelled.set(false);
         this.pathTask = this.executorService.submit(() -> this.updateBlockEx(pos));
      }
   }

   public void performActionCustom() {
      try {
         Pathfinder.Inner2 inner2 = new Pathfinder.Inner2();
         inner2.allowUnloadedChunks = true;
         inner2.unloadedChunkPenalty = 6.0;
         inner2.nearestSearchRadius = Math.max(inner2.nearestSearchRadius, 12);
         List list = this.buildFullPathByStitching(mc.world, mc.player.getBlockPos(), this.targetPos, inner2);
         if (list != null && !list.isEmpty()) {
            mc.execute(() -> walkTo(list));
         } else {
            mc.execute(AIWalkController::performActionAlt);
         }
      } catch (Exception e) {
         Southside.LOGGER.error("Unexpected error in PathFinder", e);
      }
   }

   public void performActionWithOffset() {
      if (this.pathTask == null || this.pathTask.isDone()) {
         this.pathTask = this.executorService.submit(this::performActionByIndex);
      }
   }

   public static Thread walkTo(Runnable action) {
      Thread thread = new Thread(action, "AIWalk-Stitcher");
      thread.setDaemon(true);
      return thread;
   }

   public void performActionByIndex() {
      try {
         Pathfinder.Inner2 inner2 = new Pathfinder.Inner2();
         inner2.allowUnloadedChunks = true;
         inner2.unloadedChunkPenalty = 4.0;
         inner2.nearestSearchRadius = Math.max(inner2.nearestSearchRadius, 12);
         List list = this.buildFullPathByStitching(mc.world, mc.player.getBlockPos(), this.targetPos, inner2);
         if (list != null && !list.isEmpty()) {
            mc.execute(() -> walkTo(list));
         } else {
            mc.execute(this::performActionEx);
         }
      } catch (Exception e) {
         Southside.LOGGER.error("Unexpected error in PathFinder", e);
      }
   }

   public void updateBlockEx(BlockPos pos) {
      try {
         Pathfinder.Inner2 inner2 = new Pathfinder.Inner2();
         inner2.allowUnloadedChunks = true;
         inner2.unloadedChunkPenalty = 6.0;
         List list = this.buildFullPathByStitching(mc.world, mc.player.getBlockPos(), pos, inner2);
         if (list == null || list.isEmpty()) {
            mc.execute(AIWalkController::stopWalking);
            return;
         }

         mc.execute(() -> walkTo(list));
      } catch (Exception e) {
         Southside.LOGGER.error("Unexpected error in PathFinder", e);
         mc.execute(() -> performAction(e));
      }
   }

   static {
      short s = 17491;
      long l = 6672740303634150440L;
      s = 2817;
      l = 28884988577119094L;
   }
}
