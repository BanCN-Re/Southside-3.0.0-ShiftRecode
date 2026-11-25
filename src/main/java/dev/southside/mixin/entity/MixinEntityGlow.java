package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.event.events.EntityGlowEvent;
import dev.southside.event.events.PlayerPreMoveEvent;
import dev.southside.event.events.JumpYawEvent;
import dev.southside.module.impl.combat.HitBox;
import dev.southside.module.impl.movement.NoPush;
import dev.southside.utils.FakePlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.entity.EntityChangeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
public abstract class MixinEntityGlow {
   @Shadow
   private Vec3d pos;
   @Shadow
   private Box boundingBox;
   @Shadow
   private Vec3d velocity;
   @Shadow
   private int id;
   @Shadow
   private EntityChangeListener changeListener;

   @Shadow
   protected static Vec3d movementInputToVelocity(Vec3d vec, float f, float f2) {
      return null;
   }

   @Shadow
   public abstract void setBoundingBox(Box box);

   @Shadow
   public abstract Text getName();

   @Shadow
   public abstract boolean isFireImmune();

   @Redirect(
      method = "Lnet/minecraft/entity/Entity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isControlledByPlayer()Z")
   )
   public boolean redirectMove(Entity entity) {
      return !(entity instanceof ClientPlayerEntity) && entity.isControlledByPlayer();
   }

   @Inject(method = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/Box;", at = @At("HEAD"), cancellable = true)
   public final void onGetBoundingBoxHead(CallbackInfoReturnable<Box> cir) {
      HitBox hitBox = Southside.INSTANCE.getModuleManager().getModule(HitBox.class);
      if (hitBox.isEnabled()
         && MinecraftClient.getInstance() != null
         && MinecraftClient.getInstance().player != null
         && this.getId() != MinecraftClient.getInstance().player.getId()) {
         cir.setReturnValue(hitBox.getBox(this.boundingBox));
      }
   }

   @Shadow
   protected abstract Box calculateDefaultBoundingBox(Vec3d vec);

   @Shadow
   public abstract Vec3d getVelocity();

   @Inject(method = "Lnet/minecraft/entity/Entity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V", at = @At("TAIL"))
   public void onUpdateVelocityTail(float f, Vec3d vec, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().postEvent(new EntityGlowEvent());
   }

   @Shadow
   public abstract Box calculateBoundingBox();

   @ModifyArgs(
      method = "Lnet/minecraft/entity/Entity;pushAwayFrom(Lnet/minecraft/entity/Entity;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V")
   )
   private void modifyPushAwayFrom(Args args, Entity entity) {
      NoPush noPush = Southside.INSTANCE.getModuleManager().getModule(NoPush.class);
      if (noPush.isEnabled()) {
         args.set(0, 0.0);
         args.set(2, 0.0);
      }

      if (entity instanceof FakePlayerEntity fakePlayerEntity && fakePlayerEntity.spawned) {
         args.set(0, 0.0);
         args.set(2, 0.0);
      }
   }

   @Shadow
   public abstract int getId();

   @Inject(
      method = "Lnet/minecraft/entity/Entity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   public void onMoveHead(MovementType movementType, Vec3d vec, CallbackInfo ci) {
      PlayerPreMoveEvent playerPreMoveEvent = new PlayerPreMoveEvent();
      Southside.INSTANCE.getEventBus().postCancellable(playerPreMoveEvent);
      if (playerPreMoveEvent.isCancelled()) {
         ci.cancel();
      }
   }

   @Shadow
   public abstract void setPos(double x, double y, double z);

   @Shadow
   protected abstract void setRotation(float yaw, float pitch);

   @Shadow
   public abstract float getYaw();

   @Shadow
   public abstract boolean isFrozen();

   @Redirect(
      method = "Lnet/minecraft/entity/Entity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"
      )
   )
   public Vec3d redirectUpdateVelocity(Vec3d vec, float f, float f2) {
      if ((Entity)(Object)this == MinecraftClient.getInstance().player) {
         JumpYawEvent jumpYawEvent = (JumpYawEvent)Southside.INSTANCE.getEventBus().postEvent(new JumpYawEvent(f2));
         return movementInputToVelocity(vec, f, jumpYawEvent.getYaw());
      } else {
         return movementInputToVelocity(vec, f, f2);
      }
   }

   @Shadow
   protected abstract void updatePrevPosition();
}
