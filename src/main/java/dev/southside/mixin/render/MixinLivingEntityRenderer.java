package dev.southside.mixin.render;

import dev.southside.utils.IMinecraft;
import dev.southside.utils.RotationManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
   extends EntityRenderer<T, S> {
   protected MixinLivingEntityRenderer(Context context) {
      super(context);
   }

   @Inject(
      method = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
      at = @At("TAIL")
   )
   private void onUpdateRenderStateTail(T val, S val2, float f, CallbackInfo ci) {
      boolean flag = IMinecraft.mc.player != null && val == IMinecraft.mc.player;
      if (flag) {
         RotationManager.performActionAlt((PlayerEntityRenderState)val2, f);
         RotationManager.performAction((PlayerEntityRenderState)val2, f);
         RotationManager.performActionEx((PlayerEntityRenderState)val2, f);
      }
   }
}
