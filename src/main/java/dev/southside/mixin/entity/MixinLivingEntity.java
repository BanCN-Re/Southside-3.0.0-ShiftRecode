package dev.southside.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.southside.Southside;
import dev.southside.event.events.YawEvent;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.IEntityInterpolation;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
   @Unique
   public boolean wasDesyncing = false;
   @Unique
   private TrackedPosition trackedPositionSnapshot;
   @Unique
   public int remainingInterpolationTicks = 0;
   @Shadow
   protected double serverX;
   @Shadow
   protected double serverY;
   @Shadow
   protected double serverZ;
   @Shadow
   protected double serverYaw;
   @Shadow
   protected double serverPitch;
   @Shadow
   protected int bodyTrackingIncrements;
   @Unique
   private YawEvent jumpEvent;

   @Inject(method = "Lnet/minecraft/entity/LivingEntity;updateTrackedPositionAndAngles(DDDFFI)V", at = @At("TAIL"))
   public void onUpdateTrackedPositionAndAnglesTail(double d, double d2, double d3, float f, float f2, int n, CallbackInfo ci) {
      if ((Object)this instanceof OtherClientPlayerEntity otherClientPlayerEntity) {
         IEntityInterpolation ientityinterpolation = (IEntityInterpolation)otherClientPlayerEntity;
         if (ServerPacketManager.active) {
            synchronized (ServerPacketManager.linkedBlockingQueue) {
               ServerPacketManager.linkedBlockingQueue.add((Runnable)() -> {
                  ientityinterpolation.setTargetPosition(new Vec3d(d, d2, d3));
                  ientityinterpolation.setInterpolationTicks(n);
               });
            }
         } else {
            ientityinterpolation.setTargetPosition(new Vec3d(d, d2, d3));
            ientityinterpolation.setInterpolationTicks(n);
         }
      }
   }

   @Inject(method = "Lnet/minecraft/entity/LivingEntity;getHandSwingDuration()I", at = @At("RETURN"), cancellable = true)
   private void onGetHandSwingDurationReturn(CallbackInfoReturnable<Integer> cir) {
      if ((Object)this == MinecraftClient.getInstance().player) {
         OldHitting oldHitting = Southside.INSTANCE.getModuleManager().getModule(OldHitting.class);
         if (oldHitting.isEnabled()) {
            double currentValue = oldHitting.speed.getCurrentValue();
            if (currentValue != 0.0) {
               cir.setReturnValue((int)Math.max(1.0, ((Integer)cir.getReturnValue()).intValue() / Math.abs(currentValue)));
            }
         }
      }
   }

   @Inject(method = "Lnet/minecraft/entity/LivingEntity;jump()V", at = @At("HEAD"))
   private void onJumpHead(CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null && (LivingEntity)(Object)this == MinecraftClient.getInstance().player) {
         this.jumpEvent = new YawEvent(this.getYaw());
         Southside.INSTANCE.getEventBus().postEvent(this.jumpEvent);
      }
   }

   @ModifyExpressionValue(
      method = "Lnet/minecraft/entity/LivingEntity;jump()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F")
   )
   private float modifyJumpYaw(float f) {
      return this.jumpEvent == null ? f : this.jumpEvent.getYaw();
   }

   @ModifyExpressionValue(
      method = "Lnet/minecraft/entity/LivingEntity;calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPitch()F")
   )
   private float modifyGlidingPitch(float f) {
      if ((Object)this == MinecraftClient.getInstance().player && RotationManager.isValid()) {
         Rotation currentRotation = RotationManager.getCurrentRotation();
         return currentRotation == null ? f : currentRotation.pitch;
      } else {
         return f;
      }
   }

   @Inject(method = "Lnet/minecraft/entity/LivingEntity;resetLerp()V", at = @At("HEAD"))
   private void onResetLerpHead(CallbackInfo ci) {
      if ((Object)this instanceof OtherClientPlayerEntity otherClientPlayerEntity) {
         IEntityInterpolation ientityinterpolation = (IEntityInterpolation)otherClientPlayerEntity;
         if (ServerPacketManager.active) {
            synchronized (ServerPacketManager.linkedBlockingQueue) {
               ServerPacketManager.linkedBlockingQueue.add((Runnable)() -> ientityinterpolation.setInterpolationTicks(0));
            }
         } else {
            ientityinterpolation.setInterpolationTicks(0);
         }
      }
   }

   public MixinLivingEntity() {
      super(null, null);
   }

   @Inject(method = "Lnet/minecraft/entity/LivingEntity;tickMovement()V", at = @At("HEAD"))
   public void onTickMovementHead(CallbackInfo ci) {
      if (this.trackedPositionSnapshot == null) {
         this.trackedPositionSnapshot = new TrackedPosition();
      }

      this.trackedPositionSnapshot.setPos(this.getTrackedPosition().getPos());
      if (ServerPacketManager.active && !this.wasDesyncing) {
         this.remainingInterpolationTicks = this.bodyTrackingIncrements;
      }

      if (ServerPacketManager.active && this.remainingInterpolationTicks > 0) {
         this.remainingInterpolationTicks--;
      }

      this.wasDesyncing = ServerPacketManager.active;
   }

   @ModifyExpressionValue(
      method = "Lnet/minecraft/entity/LivingEntity;tick()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"),
      slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F", ordinal = 1))
   )
   private float modifyTickYaw(float f) {
      if ((Object)this != MinecraftClient.getInstance().player) {
         return f;
      }

      Rotation currentRotation = RotationManager.getCurrentRotation();
      return currentRotation == null ? f : currentRotation.yaw;
   }

   @Shadow
   public abstract float getYaw(float f);

   @ModifyExpressionValue(
      method = "Lnet/minecraft/entity/LivingEntity;turnHead(FF)F",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F")
   )
   private float modifyHeadYaw(float f) {
      if ((Object)this != MinecraftClient.getInstance().player) {
         return f;
      }

      Rotation currentRotation = RotationManager.getCurrentRotation();
      return currentRotation == null ? f : currentRotation.yaw;
   }

   @ModifyExpressionValue(
      method = "Lnet/minecraft/entity/LivingEntity;calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;")
   )
   private Vec3d modifyGlidingDirection(Vec3d vec) {
      if ((Object)this == MinecraftClient.getInstance().player && RotationManager.isValid()) {
         Rotation currentRotation = RotationManager.getCurrentRotation();
         if (currentRotation == null) {
            return vec;
         }

         float f = MathHelper.cos(-currentRotation.yaw * (float) (Math.PI / 180.0));
         float f2 = MathHelper.sin(-currentRotation.yaw * (float) (Math.PI / 180.0));
         float f3 = MathHelper.cos(currentRotation.pitch * (float) (Math.PI / 180.0));
         float f4 = MathHelper.sin(currentRotation.pitch * (float) (Math.PI / 180.0));
         return new Vec3d(f2 * f3, -f4, f * f3);
      } else {
         return vec;
      }
   }
}
