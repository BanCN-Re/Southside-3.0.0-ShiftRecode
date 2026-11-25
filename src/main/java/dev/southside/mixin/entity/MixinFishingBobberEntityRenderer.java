package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.render.entity.state.FishingBobberEntityState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntityRenderer.class)
public class MixinFishingBobberEntityRenderer {
   @Inject(
      method = "Lnet/minecraft/client/render/entity/FishingBobberEntityRenderer;render(Lnet/minecraft/client/render/entity/state/FishingBobberEntityState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRenderHead(FishingBobberEntityState fishingBobberEntityState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, CallbackInfo ci) {
      NoRender noRender = Southside.INSTANCE.getModuleManager().getModule(NoRender.class);
      if (noRender.fishingHook.getValue()) {
         ci.cancel();
      }
   }
}
