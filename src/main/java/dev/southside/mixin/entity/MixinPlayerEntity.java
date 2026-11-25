package dev.southside.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.southside.Southside;
import dev.southside.event.events.PlayerEntityEvent;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.movement.KeepSprint;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends Entity {
   @Inject(method = "Lnet/minecraft/entity/player/PlayerEntity;getAttackCooldownProgressPerTick()F", at = @At("HEAD"), cancellable = true)
   private void onGetAttackCooldownProgressPerTickHead(CallbackInfoReturnable<Float> cir) {
      if (Southside.INSTANCE.getModuleManager().getModule(OldHitting.class).isEnabled()) {
         cir.setReturnValue(1.0F);
      }
   }

   @ModifyArgs(
      method = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V")
   )
   private void preserveAttackVelocity(Args args) {
      if ((Object)this == MinecraftClient.getInstance().player && KeepSprint.canKeepSprint()) {
         args.set(0, this.getVelocity());
      }
   }

   @ModifyExpressionValue(
      method = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F")
   )
   private float modifyAttackYaw(float f) {
      if ((Object)this == MinecraftClient.getInstance().player && RotationManager.isValid()) {
         Rotation currentRotation = RotationManager.getCurrentRotation();
         return currentRotation == null ? f : currentRotation.yaw;
      } else {
         return f;
      }
   }

   public MixinPlayerEntity(EntityType<?> entityType, World world) {
      super(entityType, world);
   }

   @ModifyArgs(
      method = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V")
   )
   private void preserveSprintState(Args args) {
      if ((Object)this == MinecraftClient.getInstance().player && (KeepSprint.canKeepSprint() || KillAura.keepSprint.getValue())) {
         args.set(0, true);
      }
   }

   @Inject(method = "Lnet/minecraft/entity/player/PlayerEntity;getAttackCooldownProgress(F)F", at = @At("HEAD"), cancellable = true)
   private void onGetAttackCooldownProgressHead(float f, CallbackInfoReturnable<Float> cir) {
      if (Southside.INSTANCE.getModuleManager().getModule(OldHitting.class).isEnabled()) {
         cir.setReturnValue(1.0F);
      }
   }

   @Inject(method = "Lnet/minecraft/entity/player/PlayerEntity;tick()V", at = @At("HEAD"), cancellable = true)
   private void onTickHead(CallbackInfo ci) {
      PlayerEntityEvent playerEntityEvent = new PlayerEntityEvent();
      Southside.INSTANCE.getEventBus().postCancellable(playerEntityEvent);
      if (playerEntityEvent.isCancelled()) {
         ci.cancel();
      }
   }

   @WrapWithCondition(
      method = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", ordinal = 0)
   )
   private boolean shouldSetSprintingAfterAttack(PlayerEntity player, boolean flag) {
      return (Object)this != MinecraftClient.getInstance().player || !KeepSprint.canKeepSprint() || flag;
   }

   @WrapWithCondition(
      method = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 0)
   )
   private boolean shouldApplyAttackVelocity(PlayerEntity player, Vec3d vec) {
      return (Object)this != MinecraftClient.getInstance().player || !KeepSprint.canKeepSprint();
   }
}
