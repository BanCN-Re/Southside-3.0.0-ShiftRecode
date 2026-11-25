package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.RenderStateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.NumberValue;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class Item2D extends Module {
   @SerializedName("scale")
   public final NumberValue scale = new NumberValue("Scale", 1.0F, 0.3F, 3.0F, 0.1F);
   @SerializedName("bobbing")
   public final BooleanValue bobbing = new BooleanValue("Bobbing", true);

   public Item2D() {
      super(Category.Render, "Item2D", "Render dropped items as flat 2D sprites facing the camera");
   }

   @EventTarget
   public void onRenderState(RenderStateEvent renderStateEvent) {
      ItemEntityRenderState renderState = renderStateEvent.renderState;
      if (renderState != null && !renderState.itemRenderState.isEmpty()) {
         ItemRenderState itemRenderState = renderState.itemRenderState;
         MatrixStack matrixStack = renderStateEvent.matrixStack;
         Camera camera = mc2.gameRenderer.getCamera();
         matrixStack.push();

         try {
            float f = this.bobbing.getValue() ? MathHelper.sin(renderState.age / 10.0F + renderState.uniqueOffset) * 0.1F + 0.1F : 0.05F;
            matrixStack.translate(0.0F, f + 0.25F * itemRenderState.getTransformation().scale.y(), 0.0F);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - camera.getYaw()));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-camera.getPitch()));
            float f2 = ((Number)this.scale.getValue()).floatValue();
            matrixStack.scale(f2, f2, 0.01F);
            itemRenderState.render(matrixStack, renderStateEvent.vertexConsumerProvider, renderStateEvent.light, OverlayTexture.DEFAULT_UV);
         } finally {
            matrixStack.pop();
         }

         renderStateEvent.cancel();
      }
   }
}
