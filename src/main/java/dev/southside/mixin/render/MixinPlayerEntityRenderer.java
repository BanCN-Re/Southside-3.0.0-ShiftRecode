package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.RainLove;
import dev.southside.utils.IslandVisitorEntity;
import dev.southside.utils.WingsFeatureRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
   @Inject(
      method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",
      at = @At("RETURN")
   )
   private void animatePlayerScale(PlayerEntityRenderState renderState, MatrixStack matrices, CallbackInfo ci) {
      Southside southside = Southside.INSTANCE;
      if (southside != null && southside.getModuleManager() != null) {
         RainLove rainLove = southside.getModuleManager().getModule(RainLove.class);
         if (rainLove.isEnabled()) {
            rainLove.render(renderState, matrices);
         }
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Z)V",
      at = @At("RETURN")
   )
   private void onInitReturn(Context context, boolean flag, CallbackInfo ci) {
      PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer)(Object)this;
      playerEntityRenderer.addFeature(new WingsFeatureRenderer((FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>)this));
   }

   @Inject(
      method = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRenderLabel(PlayerEntityRenderState renderState, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, CallbackInfo ci) {
      if (IslandVisitorEntity.isKeyDown(renderState.id)) {
         ci.cancel();
      }
   }
}
