package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerMoveSimulator {
   @SerializedName("x")
   private double x;
   @SerializedName("y")
   private double y;
   @SerializedName("z")
   private double z;
   @SerializedName("motion")
   private Vec3d motion;
   @SerializedName("eyePos")
   private Vec3d eyePos;
   @SerializedName("yaw")
   private float yaw;
   @SerializedName("strafe")
   private float strafe;
   @SerializedName("forward")
   private float forward;
   @SerializedName("jumpMovementFactor")
   private float jumpMovementFactor;
   @SerializedName("movementSpeed")
   private float movementSpeed;
   @SerializedName("onGround")
   private boolean onGround;
   @SerializedName("mc")
   private final MinecraftClient mc = MinecraftClient.getInstance();

   public PlayerMoveSimulator(PlayerEntity player) {
      this.x = player.getX();
      this.y = player.getY();
      this.z = player.getZ();
      this.motion = player.getVelocity();
      this.yaw = RotationManager.getActiveRotation().yaw;
      this.strafe = this.mc.player.input.movementSideways;
      this.forward = this.mc.player.input.movementForward;
      this.movementSpeed = player.getMovementSpeed();
      this.onGround = player.isOnGround();
      this.jumpMovementFactor = player.isSprinting() ? 0.026F : 0.02F;
      this.eyePos = this.mc.player.getEyePos();
   }

   private void performAction() {
      float f = 0.91F;
      float f2 = f;
      float f3 = 0.98F;
      float jumpMovementFactor2 = this.jumpMovementFactor;
      this.performAction(jumpMovementFactor2, new Vec3d(this.strafe, 0.0, this.forward));
      this.x = this.x + this.motion.x;
      this.y = this.y + this.motion.y;
      this.z = this.z + this.motion.z;
      this.performActionEx();
      double d = 0.08;
      this.motion = this.motion.add(0.0, -d, 0.0);
      this.eyePos = new Vec3d(this.x, this.y + this.mc.player.dimensions.eyeHeight(), this.z);
      this.motion = new Vec3d(this.motion.x * f, this.motion.y * f3, this.motion.z * f2);
   }

   private void performActionEx() {
      Vec3d vec = new Vec3d(this.x, this.y, this.z);
      Vec3d vec2 = vec.add(0.0, -0.2, 0.0);
      BlockHitResult hitResult = this.raycastHit(vec, vec2);
      if (hitResult != null && hitResult.getType() == Type.BLOCK && hitResult.getSide() == Direction.UP) {
         this.onGround = true;
      } else {
         this.onGround = false;
      }
   }

   private void performAction(float f, Vec3d vec) {
      double d = vec.lengthSquared();
      if (!(d < 1.0E-7)) {
         Vec3d vec2 = (d > 1.0 ? vec.normalize() : vec).multiply(f);
         float f2 = MathHelper.sin(this.yaw * (float) (Math.PI / 180.0));
         float f3 = MathHelper.cos(this.yaw * (float) (Math.PI / 180.0));
         double d2 = vec2.x * f3 - vec2.z * f2;
         double d3 = vec2.z * f3 + vec2.x * f2;
         this.motion = this.motion.add(d2, 0.0, d3);
      }
   }

   public void onKeyTyped(int n) {
      for (int i = 0; i < n; i++) {
         this.performAction();
      }
   }

   public BlockPos findBlockPos(int n) {
      float f = this.mc.player != null ? this.mc.player.getWidth() / 2.0F : 0.3F;

      for (int i = 0; i < n; i++) {
         Vec3d vec = new Vec3d(this.x, this.y, this.z);
         this.performAction();
         Vec3d vec2 = new Vec3d(this.x, this.y, this.z);
         BlockPos pos;
         if ((pos = this.findBlockPos(vec, vec2)) != null) {
            return pos;
         }

         if ((pos = this.findBlockPos(vec.add(f, 0.0, f), vec2.add(f, 0.0, f))) != null) {
            return pos;
         }

         if ((pos = this.findBlockPos(vec.add(-f, 0.0, f), vec2.add(-f, 0.0, f))) != null) {
            return pos;
         }

         if ((pos = this.findBlockPos(vec.add(f, 0.0, -f), vec2.add(f, 0.0, -f))) != null) {
            return pos;
         }

         if ((pos = this.findBlockPos(vec.add(-f, 0.0, -f), vec2.add(-f, 0.0, -f))) != null) {
            return pos;
         }

         if ((pos = this.findBlockPos(vec.add(f, 0.0, 0.0), vec2.add(f, 0.0, 0.0))) != null) {
            return pos;
         }

         if ((pos = this.findBlockPos(vec.add(-f, 0.0, 0.0), vec2.add(-f, 0.0, 0.0))) != null) {
            return pos;
         }

         if ((pos = this.findBlockPos(vec.add(0.0, 0.0, f), vec2.add(0.0, 0.0, f))) != null) {
            return pos;
         }

         if ((pos = this.findBlockPos(vec.add(0.0, 0.0, -f), vec2.add(0.0, 0.0, -f))) != null) {
            return pos;
         }
      }

      return null;
   }

   private BlockHitResult raycastHit(Vec3d vec, Vec3d vec2) {
      double dist = vec.distanceTo(vec2);
      return dist < 1.0E-7 ? null : RaytraceUtils.raycast(dist, RotationManager.computeRotation(vec, vec2, false), false, 1.0F);
   }

   private BlockPos findBlockPos(Vec3d vec, Vec3d vec2) {
      BlockHitResult hitResult = this.raycastHit(vec, vec2);
      return hitResult != null && hitResult.getType() == Type.BLOCK && hitResult.getSide() == Direction.UP ? hitResult.getBlockPos() : null;
   }

   public Vec3d computePosition() {
      return this.eyePos.add(0.0);
   }

   public Vec3d computePositionEx() {
      return new Vec3d(this.x, this.y, this.z);
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public Vec3d getMotion() {
      return this.motion;
   }
}
