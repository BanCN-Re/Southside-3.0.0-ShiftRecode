package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.impl.world.sca.Line;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockLineTracker {
   public static final int DEFAULT_INT;
   @SerializedName("recentPositions")
   public final Deque<BlockPos> recentPositions = new ArrayDeque<>(4);
   @SerializedName("lastTargetPos")
   public BlockPos lastTargetPos = null;

   public void updateBlock(BlockPos pos) {
      this.recentPositions.add(pos);

      while (this.recentPositions.size() > 4) {
         this.recentPositions.pollFirst();
      }
   }

   public Line getLine() {
      ArrayList list = new ArrayList<>(this.recentPositions);
      int size = list.size();
      if (size < 2) {
         return null;
      }

      BlockPos pos = (BlockPos)list.get(size - 2);
      BlockPos pos2 = (BlockPos)list.get(size - 1);
      Vec3d vec = new Vec3d((pos.getX() + pos2.getX()) / 2.0, (pos.getY() + pos2.getY()) / 2.0, (pos.getZ() + pos2.getZ()) / 2.0);
      Vec3d dir = new Vec3d(pos2.getX() - pos.getX(), pos2.getY() - pos.getY(), pos2.getZ() - pos.getZ()).normalize();
      return new Line(vec, dir);
   }

   public Line getLine(MovementInputState movementInputState) {
      Vec3d vec = this.computePosition(MovementMathUtils.computeFloat(IMinecraft.mc.player.getYaw(), movementInputState));
      BlockPos pos = this.findBlockPos();
      if (pos == null) {
         return null;
      }

      Line line = this.getLine();
      Vec3d vec2 = line != null && !this.isValid(line, vec) ? line.origin : Vec3d.ofBottomCenter(pos);
      return new Line(new Vec3d(vec2.x + 0.5, IMinecraft.mc.player.getY(), vec2.z + 0.5), vec);
   }

   public void clear() {
      this.lastTargetPos = null;
      this.recentPositions.clear();
   }

   public BlockPos findBlockPos() {
      double[] coords = new double[]{0.301, 0.0, -0.301};
      ArrayList list = new ArrayList();

      for (double d : coords) {
         for (double d2 : coords) {
            BlockPos pos = new BlockPos(
               (int)Math.floor(IMinecraft.mc.player.getX() + d),
               (int)Math.floor(IMinecraft.mc.player.getY() - 1.0),
               (int)Math.floor(IMinecraft.mc.player.getZ() + d2)
            );
            if (!IMinecraft.mc.world.getBlockState(pos).getCollisionShape(IMinecraft.mc.world, pos).isEmpty()) {
               list.add(pos);
            }
         }
      }

      BlockPos pos2 = this.recentPositions.peekLast();
      if (pos2 != null && list.contains(pos2)) {
         return pos2;
      } else if (this.lastTargetPos != null && list.contains(this.lastTargetPos)) {
         return this.lastTargetPos;
      } else if (!list.isEmpty()) {
         this.lastTargetPos = (BlockPos)list.get(0);
         return this.lastTargetPos;
      } else {
         return null;
      }
   }

   public Vec3d computePosition(float f) {
      double d = f / 180.0 * 4.0 + 4.0;
      double d2 = Math.round(d);
      double d3 = Math.toRadians(MathHelper.wrapDegrees((d2 - 4.0) / 4.0 * 180.0 + 90.0));
      return new Vec3d(Math.cos(d3), 0.0, Math.sin(d3));
   }

   public boolean isValid(Line line, Vec3d vec) {
      double d = line.direction.dotProduct(vec);
      double d2 = Math.acos(MathHelper.clamp(d, -1.0, 1.0));
      double d3 = Math.toDegrees(d2);
      return Math.abs(d3) > 50.0;
   }

   static {
      long l = -4258561022895325180L;
      DEFAULT_INT = (int)l;
   }
}
