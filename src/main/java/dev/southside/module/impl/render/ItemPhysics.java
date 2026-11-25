package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.RenderStateEvent;
import dev.southside.event.events.TransformationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.IEntityStateHolder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class ItemPhysics extends Module {
   @SerializedName("randomRotation")
   public final BooleanValue randomRotation = new BooleanValue("RandomRotation", true);
   @SerializedName("random")
   private final Random random = Random.createLocal();
   @SerializedName("cancelVanillaTransform")
   private boolean cancelVanillaTransform;

   public ItemPhysics() {
      super(Category.Render, "ItemPhysics");
   }

   @EventTarget
   public void onTransformation(TransformationEvent transformationEvent) {
      if (this.cancelVanillaTransform) {
         transformationEvent.cancel();
      }
   }

   @EventTarget
   public void onRenderState(RenderStateEvent renderStateEvent) {
      ItemEntityRenderState renderState = renderStateEvent.renderState;
      if (renderState != null && !renderState.itemRenderState.isEmpty()) {
         ItemRenderState itemRenderState = renderState.itemRenderState;
         MatrixStack matrixStack = renderStateEvent.matrixStack;
         boolean flag = !itemRenderState.hasDepth();
         boolean cancelVanillaTransform2 = this.cancelVanillaTransform;
         matrixStack.push();

         try {
            this.random.setSeed(renderState.seed);
            Transformation transformation = itemRenderState.getTransformation();
            this.render(matrixStack, transformation);
            matrixStack.translate(0.0F, 0.25F * transformation.scale.y(), 0.0F);
            if (renderState instanceof IEntityStateHolder ientitystateholder && ientitystateholder.getEntity() instanceof ItemEntity itemEntity && itemEntity.isTouchingWater()) {
               matrixStack.translate(0.0F, 0.333F, 0.0F);
            }

            matrixStack.translate(0.0F, 0.001F, 0.0F);
            if (flag) {
               matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            }

            if ((Boolean)this.randomRotation.getValue()) {
               RotationAxis rotationAxis = flag ? RotationAxis.POSITIVE_Z : RotationAxis.POSITIVE_Y;
               matrixStack.multiply(rotationAxis.rotationDegrees((this.random.nextFloat() * 2.0F - 1.0F) * 90.0F));
            }

            this.cancelVanillaTransform = true;

            for (int i = 0; i < renderState.renderedAmount; i++) {
               matrixStack.push();

               try {
                  if (i > 0) {
                     float f = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                     float f2 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                     matrixStack.translate(f, flag ? f2 : 0.0F, flag ? 0.0F : f2);
                  }

                  itemRenderState.render(matrixStack, renderStateEvent.vertexConsumerProvider, renderStateEvent.light, OverlayTexture.DEFAULT_UV);
               } finally {
                  matrixStack.pop();
               }

               float f3 = Math.max(this.random.nextFloat() * 0.0625F, 0.03125F);
               matrixStack.translate(0.0F, flag ? 0.0F : f3, flag ? -f3 : 0.0F);
            }
         } finally {
            this.cancelVanillaTransform = cancelVanillaTransform2;
            matrixStack.pop();
         }

         renderStateEvent.cancel();
      }
   }

   private void render(MatrixStack matrices, Transformation transformation) {
      float otherTranslationY = transformation.translation.y;

      try {
         transformation.translation.y = 0.0F;
         transformation.apply(false, matrices);
      } finally {
         transformation.translation.y = otherTranslationY;
      }
   }
}
