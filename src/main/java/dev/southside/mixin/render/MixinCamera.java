package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.MotionCamera;
import dev.southside.module.impl.render.ViewClip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class MixinCamera {
   @Unique
   private float southside$motionDistance = -1.0F;

   @Shadow
   protected abstract void setPos(Vec3d vec);

   @Shadow
   protected abstract void moveBy(float x, float y, float z);

   @Shadow
   public abstract Vec3d getPos();

   @Shadow
   private float clipToSpace(float f) {
      throw new AssertionError();
   }

   @Inject(
      method = "update",
      at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = Shift.AFTER),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(Lnet/minecraft/util/math/Vec3d;)V", shift = Shift.AFTER)
      },
      require = 2
   )
   private void updateMotionCamera(BlockView blockView, Entity entity, boolean flag, boolean flag2, float f, CallbackInfo ci) {
      this.southside$motionDistance = -1.0F;
      MinecraftClient instance = MinecraftClient.getInstance();
      if (instance.gameRenderer != null && (Object)this == instance.gameRenderer.getCamera()) {
         MotionCamera motionCamera = southside$motionCamera();
         if (motionCamera != null && motionCamera.isEnabled()) {
            if (blockView == instance.world && entity instanceof ClientPlayerEntity player && player == instance.player && !player.isSleeping()) {
               Vec3d vec = motionCamera.computePosition(blockView, entity, this.getPos(), flag);
               this.southside$motionDistance = motionCamera.getCurrentDistance();
               if (flag) {
                  this.setPos(vec);
               } else if (this.southside$motionDistance > 0.0F) {
                  this.moveBy(-this.clipToSpace(this.southside$motionDistance), 0.0F, 0.0F);
               }
            } else {
               motionCamera.resetToDefaults();
            }
         }
      }
   }

   @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F"))
   private float applyMotionDistance(float f) {
      return this.southside$motionDistance >= 0.0F ? this.southside$motionDistance : f;
   }

   @Inject(method = "reset", at = @At("RETURN"))
   private void resetMotionCamera(CallbackInfo ci) {
      this.southside$motionDistance = -1.0F;
      MinecraftClient instance = MinecraftClient.getInstance();
      if (instance.gameRenderer != null && (Object)this == instance.gameRenderer.getCamera()) {
         MotionCamera motionCamera = southside$motionCamera();
         if (motionCamera != null) {
            motionCamera.resetToDefaults();
         }
      }
   }

   @Unique
   private static MotionCamera southside$motionCamera() {
      Southside southside = Southside.INSTANCE;
      return southside != null && southside.getModuleManager() != null ? southside.getModuleManager().getModule(MotionCamera.class) : null;
   }

   @Inject(method = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F", at = @At("HEAD"), cancellable = true)
   private void onClipToSpaceHead(float f, CallbackInfoReturnable<Float> cir) {
      ViewClip viewClip = Southside.INSTANCE.getModuleManager().getModule(ViewClip.class);
      if (viewClip.isEnabled()) {
         float f2 = f * viewClip.scale.getValue().floatValue();
         cir.setReturnValue(f2);
         cir.cancel();
      }
   }
}
