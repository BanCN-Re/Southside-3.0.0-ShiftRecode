package dev.southside.mixin.network;

import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.EntityEvent;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.InteractionStateFlags;
import dev.southside.utils.IslandVisitorEntity;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {
   @Unique
   private float savedYaw;
   @Unique
   private float savedPitch;
   @Shadow
   @Final
   public MinecraftClient client;

   @Unique
   private void applyInteractionRotation(PlayerEntity player) {
      if (this.client.player != null) {
         this.savedYaw = player.getYaw();
         this.savedPitch = player.getPitch();
         Rotation activeRotation = RotationManager.getActiveRotation();
         player.setYaw(Disabler.normalizeYawDiff(activeRotation.yaw, RotationManager.getServerRotation().yaw));
         Rotation activeRotation2 = RotationManager.getActiveRotation();
         player.setPitch(activeRotation2.pitch);
         Disabler.postDelayPackets(true);
      }
   }

   @Unique
   private void restorePlayerRotation(PlayerEntity player) {
      if (this.client.player != null) {
         player.setYaw(this.savedYaw);
         player.setPitch(this.savedPitch);
         Disabler.postDelayPackets(false);
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onAttackEntityHead(PlayerEntity player, Entity entity, CallbackInfo ci) {
      EntityEvent entityEvent = new EntityEvent(entity, Stage.PRE);
      Southside.INSTANCE.getEventBus().postCancellable(entityEvent);
      if (entityEvent.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V", at = @At("HEAD"), cancellable = true)
   private void onSyncSelectedSlotHead(CallbackInfo ci) {
      if (InteractionStateFlags.active) {
         ci.cancel();
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V",
      at = @At("TAIL")
   )
   private void onAttackEntityTail(PlayerEntity player, Entity entity, CallbackInfo ci) {
      EntityEvent entityEvent = new EntityEvent(entity, Stage.POST);
      Southside.INSTANCE.getEventBus().postCancellable(entityEvent);
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
      at = @At("HEAD")
   )
   private void onInteractItemHead(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (MinecraftClient.getInstance().player == player) {
         this.applyInteractionRotation(player);
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactEntityAtLocation(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onInteractEntityAtLocationHead(PlayerEntity player, Entity entity, EntityHitResult entityHit, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (hand == Hand.MAIN_HAND && entity instanceof IslandVisitorEntity islandVisitorEntity) {
         if (player.isSneaking()) {
            IslandVisitorEntity.performAction(islandVisitorEntity, player);
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
         }
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
      at = @At("HEAD")
   )
   private void onInteractBlockHead(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
      if (MinecraftClient.getInstance().player == player) {
         this.applyInteractionRotation(player);
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
      at = @At("RETURN")
   )
   private void onInteractBlockReturn(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
      if (MinecraftClient.getInstance().player == player) {
         this.restorePlayerRotation(player);
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
      at = @At("RETURN")
   )
   private void onInteractItemReturn(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (MinecraftClient.getInstance().player == player) {
         this.restorePlayerRotation(player);
      }
   }
}
