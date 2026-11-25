package dev.southside.mixin.entity;

import com.mojang.authlib.GameProfile;
import dev.southside.utils.IEntityInterpolation;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OtherClientPlayerEntity.class)
public abstract class MixinOtherClientPlayerEntity extends Entity implements IEntityInterpolation {
   @Unique
   private int interpolationTicks;
   @Unique
   private Vec3d interpolatedPosition;
   @Unique
   private Vec3d targetPosition;

   @Override
   public Vec3d getInterpolatedPosition() {
      return this.interpolatedPosition;
   }

   @Override
   public void setTargetPosition(Vec3d vec) {
      this.targetPosition = vec;
   }

   @Unique
   @Override
   public void setInterpolatedPosition(Vec3d vec) {
      this.interpolatedPosition = vec;
   }

   public MixinOtherClientPlayerEntity() {
      super(null, null);
   }

   @Override
   public Vec3d getTargetPosition() {
      return this.targetPosition;
   }

   @Inject(
      method = "Lnet/minecraft/client/network/OtherClientPlayerEntity;tickMovement()V",
      at = @At(
         value = "FIELD",
         target = "Lnet/minecraft/client/network/OtherClientPlayerEntity;headTrackingIncrements:I",
         opcode = 180,
         ordinal = 0,
         shift = Shift.BEFORE
      )
   )
   private void interpolateBoundingBox(CallbackInfo ci) {
      if (this.interpolationTicks > 0) {
         double d = 1.0 / this.interpolationTicks;
         double interpolated = MathHelper.lerp(d, this.interpolatedPosition.x, this.targetPosition.x);
         double interpolated2 = MathHelper.lerp(d, this.interpolatedPosition.y, this.targetPosition.y);
         double interpolated3 = MathHelper.lerp(d, this.interpolatedPosition.z, this.targetPosition.z);
         this.interpolatedPosition = new Vec3d(interpolated, interpolated2, interpolated3);
         this.setBoundingBox(this.dimensions.getBoxAt(this.interpolatedPosition));
         this.interpolationTicks--;
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/OtherClientPlayerEntity;<init>(Lnet/minecraft/client/world/ClientWorld;Lcom/mojang/authlib/GameProfile;)V",
      at = @At("TAIL")
   )
   private void onInitTail(ClientWorld world, GameProfile gameProfile, CallbackInfo ci) {
      this.interpolatedPosition = this.targetPosition = Vec3d.ZERO;
   }

   @Redirect(
      method = "Lnet/minecraft/client/network/OtherClientPlayerEntity;tickMovement()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/OtherClientPlayerEntity;lerpPosAndRotation(IDDDDD)V")
   )
   private void redirectTickMovement(OtherClientPlayerEntity otherClientPlayerEntity, int n, double d, double d2, double d3, double d4, double d5) {
      double d6 = 1.0 / n;
      double interpolated = MathHelper.lerp(d6, this.getX(), d);
      double interpolated2 = MathHelper.lerp(d6, this.getY(), d2);
      double interpolated3 = MathHelper.lerp(d6, this.getZ(), d3);
      float interpolated4 = (float)MathHelper.lerpAngleDegrees(d6, this.getYaw(), d4);
      float interpolated5 = (float)MathHelper.lerp(d6, this.getPitch(), d5);
      this.setPos(interpolated, interpolated2, interpolated3);
      this.setRotation(interpolated4, interpolated5);
   }

   @Override
   public void setInterpolationTicks(int n) {
      this.interpolationTicks = n;
   }

   @Override
   public int getInterpolationTicks() {
      return this.interpolationTicks;
   }
}
