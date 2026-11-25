package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.render.LineBreakFormattor;
import dev.southside.module.impl.render.NameTags;
import dev.southside.module.impl.render.NoRender;
import dev.southside.utils.IEntityStateHolder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {
   @Shadow
   @Final
   protected EntityRenderDispatcher dispatcher;

   @Inject(
      method = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRenderLabelIfPresentHead(S val, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, CallbackInfo ci) {
      ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
      Entity entity = null;
      if (val instanceof IEntityStateHolder ientitystateholder) {
         entity = ientitystateholder.getEntity();
      }

      NoRender noRender = moduleManager.getModule(NoRender.class);
      if (entity instanceof ArmorStandEntity && noRender.isEnabled() && noRender.noArmorStands.getValue()) {
         ci.cancel();
      } else {
         NameTags nameTags = moduleManager.getModule(NameTags.class);
         if (nameTags.isEnabled() && nameTags.hideVanillaName.getValue() && entity instanceof PlayerEntity) {
            ci.cancel();
         } else {
            LineBreakFormattor lineBreakFormattor = moduleManager.getModule(LineBreakFormattor.class);
            if (lineBreakFormattor.renderMultilineLabel(val, text, matrices, vertexConsumers, n)) {
               ci.cancel();
            }
         }
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/render/entity/EntityRenderer;updateRenderState(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/entity/state/EntityRenderState;F)V",
      at = @At("TAIL")
   )
   private void onUpdateRenderStateTail(T val, S val2, float f, CallbackInfo ci) {
      if (val2 instanceof IEntityStateHolder ientitystateholder) {
         ientitystateholder.setEntity(val);
      }
   }
}
