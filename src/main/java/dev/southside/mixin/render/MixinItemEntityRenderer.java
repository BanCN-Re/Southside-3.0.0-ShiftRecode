package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.event.events.RenderStateEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer {
   @Inject(
      method = "Lnet/minecraft/client/render/entity/ItemEntityRenderer;render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRenderHead(ItemEntityRenderState itemEntityRenderState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, CallbackInfo ci) {
      RenderStateEvent renderStateEvent = (RenderStateEvent)Southside.INSTANCE.getEventBus().postCancellableEvent(RenderStateEvent.getRenderStateEvent(itemEntityRenderState, matrices, vertexConsumers, n));
      if (renderStateEvent.isCancelled()) {
         ci.cancel();
      }
   }
}
