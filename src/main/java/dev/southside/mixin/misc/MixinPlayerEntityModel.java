package dev.southside.mixin.misc;

import dev.southside.utils.IslandVisitorEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class MixinPlayerEntityModel {
   @Unique
   private static final float SOUTHSIDE_SEATED_LEAN = -0.16F;
   @Unique
   private static final float SOUTHSIDE_RESTING_ARM_PITCH = 0.28F;
   @Unique
   private static final float SOUTHSIDE_SEATED_LEG_PITCH = -0.7F;
   @Unique
   private static final float SOUTHSIDE_LEAN_SIN = (float)Math.sin(-0.16F);
   @Unique
   private static final float SOUTHSIDE_LEAN_COS = (float)Math.cos(-0.16F);

   @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
   private void southside$seatMenuVisitor(PlayerEntityRenderState renderState, CallbackInfo ci) {
      if (IslandVisitorEntity.isKeyDown(renderState.id)) {
         PlayerEntityModel playerEntityModel = (PlayerEntityModel)(Object)this;
         southside$leanUpperPart(playerEntityModel.body);
         southside$leanUpperPart(playerEntityModel.head);
         southside$leanUpperPart(playerEntityModel.leftArm);
         southside$leanUpperPart(playerEntityModel.rightArm);
         playerEntityModel.body.pitch = -0.16F;
         playerEntityModel.leftArm.roll = -0.08F;
         playerEntityModel.rightArm.roll = 0.08F;
         playerEntityModel.leftArm.pitch = 0.28F;
         playerEntityModel.rightArm.pitch = 0.28F;
         float f = (float)Math.sin((float)(System.currentTimeMillis() % 40000L) * 0.0016F) * 0.035F;
         playerEntityModel.leftLeg.pitch = -0.7F + f;
         playerEntityModel.rightLeg.pitch = -0.7F - f;
         playerEntityModel.leftLeg.yaw = (float) (-Math.PI / 10);
         playerEntityModel.rightLeg.yaw = (float) (Math.PI / 10);
         playerEntityModel.leftLeg.roll = -0.07853982F;
         playerEntityModel.rightLeg.roll = 0.07853982F;
      }
   }

   @Unique
   private static void southside$leanUpperPart(ModelPart modelPart) {
      float f = modelPart.pivotY - 12.0F;
      float pivotZ = modelPart.pivotZ;
      modelPart.pivotY = 12.0F + f * SOUTHSIDE_LEAN_COS - pivotZ * SOUTHSIDE_LEAN_SIN;
      modelPart.pivotZ = f * SOUTHSIDE_LEAN_SIN + pivotZ * SOUTHSIDE_LEAN_COS;
   }
}
