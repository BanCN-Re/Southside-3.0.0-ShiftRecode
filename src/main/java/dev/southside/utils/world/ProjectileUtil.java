package dev.southside.utils.world;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.IText;
import dev.southside.utils.Rotation;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class ProjectileUtil {
   @SerializedName("startX")
   public double startX;
   @SerializedName("startY")
   public double startY;
   @SerializedName("startZ")
   public double startZ;
   @SerializedName("minVelocityY")
   public double minVelocityY;
   @SerializedName("maxVelocityY")
   public double maxVelocityY;

   public double computeDouble(BlockPos pos) {
      for (int i = 0; i >= -5; i--) {
         BlockPos pos2 = pos.add(0, i, 0);
         BlockState blockState = IMinecraft.mc.world.getBlockState(pos2);
         if (blockState.isSolidBlock(IMinecraft.mc.world, pos2)) {
            return i == 0 ? 0.4 : 1.0;
         }

         if (blockState.isOpaque()) {
            return 0.99;
         }
      }

      return 0.05;
   }

   public ProjectileUtil.Entry raycast(Rotation rotation, double d) {
      if (rotation.pitch > 90.0F) {
         rotation.pitch = 90.0F;
      }

      if (rotation.pitch < -90.0F) {
         rotation.pitch = -90.0F;
      }

      double d2 = this.startX - MathHelper.cos(rotation.yaw / 180.0F * (float) Math.PI) * 0.16F;
      double d3 = this.startY + IMinecraft.mc.player.getEyeHeight(IMinecraft.mc.player.getPose()) - 0.1F;
      double d4 = this.startZ - MathHelper.sin(rotation.yaw / 180.0F * (float) Math.PI) * 0.16F;
      double d5 = -MathHelper.sin(rotation.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotation.pitch / 180.0F * (float) Math.PI) * 0.4;
      double d6 = -MathHelper.sin(rotation.pitch / 180.0F * (float) Math.PI) * 0.4;
      double d7 = MathHelper.cos(rotation.yaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotation.pitch / 180.0F * (float) Math.PI) * 0.4;
      float dist = MathHelper.sqrt((float)(d5 * d5 + d6 * d6 + d7 * d7));
      d5 /= dist;
      d6 /= dist;
      d7 /= dist;
      d5 *= 1.5;
      d6 *= 1.5;
      d7 *= 1.5;
      d6 += d;
      double d8 = 0.03F;
      double d9 = 0.25;
      double d10 = 0.99F;
      double d11 = d2;
      double d12 = d3;
      double d13 = d4;
      double d14 = d5;
      double d15 = d6;
      double d16 = d7;
      double d17 = d10;
      double d18 = d9;
      double d19 = d8;
      BlockHitResult hitResult = null;
      boolean flag = false;
      boolean flag2 = false;
      int n = 0;

      while (!flag && d12 > -60.0) {
         n++;
         Vec3d vec = new Vec3d(d11, d12, d13);
         Vec3d vec2 = new Vec3d(d11 + d14, d12 + d15, d13 + d16);
         BlockHitResult hitResult2 = IMinecraft.mc.world.raycast(new RaycastContext(vec, vec2, ShapeType.COLLIDER, FluidHandling.ANY, IMinecraft.mc.player));
         if (hitResult2 != null && hitResult2.getType() == Type.BLOCK) {
            flag = true;
            hitResult = hitResult2;
            vec2 = hitResult2.getPos();
         }

         Box box = new Box(d11 - d18, d12 - d18, d13 - d18, d11 + d18, d12 + d18, d13 + d18);

         for (Entity entity : IMinecraft.mc.world.getOtherEntities(null, box.expand(d14, d15, d16).expand(1.0, 1.0, 1.0))) {
            if (entity.canHit() && entity != IMinecraft.mc.player) {
               Box box2 = entity.getBoundingBox().expand(d18, d18, d18);
               Optional hitResult3 = box2.raycast(vec, vec2);
               if (hitResult3.isPresent()) {
                  flag2 = true;
                  flag = true;
                  hitResult = new BlockHitResult((Vec3d)hitResult3.get(), null, BlockPos.ORIGIN, false);
               }
            }
         }

         d11 += d14;
         d12 += d15;
         d13 += d16;
         BlockPos pos = new BlockPos((int)Math.floor(d11), (int)Math.floor(d12), (int)Math.floor(d13));
         BlockState blockState = IMinecraft.mc.world.getBlockState(pos);
         if (blockState.isOf(Blocks.WATER)) {
            d14 *= 0.6;
            d15 *= 0.6;
            d16 *= 0.6;
         } else {
            d14 *= d17;
            d15 *= d17;
            d16 *= d17;
         }

         d15 -= d19;
      }

      return new ProjectileUtil.Entry(d11, d12, d13, flag2, flag, hitResult, n);
   }

   public double assessSingleRotation(Rotation rotation, double d, boolean flag) {
      ProjectileUtil.Entry hitResult = this.raycast(rotation, d);
      if (!hitResult.isHitBlock()) {
         if (flag) {
            applyText(
               String.format(
                  "§7[assess] yaw=%.0f pitch=%.0f → §cNO LANDING§7 endPos=(%.0f, %.0f, %.0f) ticks=%d",
                  rotation.yaw,
                  rotation.pitch,
                  hitResult.getEndX(),
                  hitResult.getEndY(),
                  hitResult.getEndZ(),
                  hitResult.getSteps()
               )
            );
         }

         return 0.05;
      } else {
         BlockHitResult blockHitResult = hitResult.getBlockHitResult();
         if (blockHitResult != null && !hitResult.isHitEntity()) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockPos pos = blockPos.offset(blockHitResult.getSide());
            Direction side = blockHitResult.getSide();
            double d2;
            if (side == Direction.UP) {
               d2 = this.computeDouble(pos) * this.computeDouble(hitResult.steps);
            } else {
               double d3 = this.computeDouble(blockPos, pos);
               d2 = d3 * this.computeDouble(hitResult.steps);
            }

            if (flag) {
               applyText(
                  String.format(
                     "§7[assess] yaw=%.0f pitch=%.0f → §a%s§7 at (%d,%d,%d) ticks=%d finalScore=%.3f",
                     rotation.yaw,
                     rotation.pitch,
                     side == Direction.UP ? "TOP" : side.name(),
                     pos.getX(),
                     pos.getY(),
                     pos.getZ(),
                     hitResult.getSteps(),
                     d2
                  )
               );
            }

            return d2;
         } else {
            if (flag) {
               applyText(String.format("§7[assess] yaw=%.0f pitch=%.0f → §eHIT ENTITY§7 ticks=%d", rotation.yaw, rotation.pitch, hitResult.getSteps()));
            }

            return 0.05 * this.computeDouble(hitResult.steps);
         }
      }
   }

   public double computeDouble(Rotation rotation, double d) {
      return this.assessSingleRotation(rotation, d, false);
   }

   public double computeDouble(double d) {
      double d2 = d / 1000.0;
      return (d2 + 3.0) / (d2 + 2.0) / 1.5;
   }

   public ProjectileUtil(double d, double d2, double d3, double d4, double d5) {
      this.startX = d;
      this.startY = d2;
      this.startZ = d3;
      this.minVelocityY = d4;
      this.maxVelocityY = d5;
   }

   public double computeDouble(BlockPos pos, BlockPos pos2) {
      if (IMinecraft.mc.world.getBlockState(pos).isSolidBlock(IMinecraft.mc.world, pos) && IMinecraft.mc.world.getBlockState(pos.up()).isAir()) {
         return 0.85;
      }

      if (IMinecraft.mc.world.getBlockState(pos).isSolidBlock(IMinecraft.mc.world, pos)) {
         for (int i = 1; i <= 5; i++) {
            BlockPos pos3 = pos.up(i);
            if (IMinecraft.mc.world.getBlockState(pos3).isAir()) {
               return 0.75;
            }
         }

         return 0.6;
      } else {
         for (int j = 0; j >= -5; j--) {
            BlockPos pos4 = pos2.add(0, j, 0);
            if (IMinecraft.mc.world.getBlockState(pos4).isSolidBlock(IMinecraft.mc.world, pos4)) {
               return j == 0 ? 0.3 : 0.7;
            }
         }

         for (int k = -1; k <= 1; k++) {
            for (int m = -1; m <= 1; m++) {
               if (k != 0 || m != 0) {
                  for (int n = -1; n <= 2; n++) {
                     BlockPos pos5 = pos2.add(k, n, m);
                     if (IMinecraft.mc.world.getBlockState(pos5).isSolidBlock(IMinecraft.mc.world, pos5)) {
                        return 0.55;
                     }
                  }
               }
            }
         }

         return 0.4;
      }
   }

   public double computeDouble(Rotation rotation) {
      double d = 0.0;
      int n = 0;

      for (int i = 0; i <= 3; i++) {
         double d2 = i / 3.0;

         for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
               Rotation rotation2 = new Rotation(rotation.yaw + j * 0.5F, rotation.pitch + k * 0.5F);
               double maxVelocityY2 = this.maxVelocityY;
               double minVelocityY2 = this.minVelocityY;
               d += this.computeDouble(rotation2, minVelocityY2 + (maxVelocityY2 - minVelocityY2) * d2);
               n++;
            }
         }

         if (this.minVelocityY == this.maxVelocityY) {
            break;
         }
      }

      return d / n;
   }

   private static void applyText(String text) {
      MinecraftClient.getInstance().execute(() -> IText.applyText(text));
   }

   public record Entry(double endX, double endY, double endZ, boolean hitEntity, boolean hitBlock, BlockHitResult blockHitResult, int steps) {

      public double getEndX() {
         return this.endX;
      }

      public double getEndY() {
         return this.endY;
      }

      public double getEndZ() {
         return this.endZ;
      }

      public boolean isHitEntity() {
         return this.hitEntity;
      }

      public boolean isHitBlock() {
         return this.hitBlock;
      }

      public BlockHitResult getBlockHitResult() {
         return this.blockHitResult;
      }

      public int getSteps() {
         return this.steps;
      }
   }
}
