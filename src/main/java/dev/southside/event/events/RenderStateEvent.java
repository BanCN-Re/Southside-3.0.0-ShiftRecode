package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

public class RenderStateEvent extends CancellableEvent {
   private static final RenderStateEvent INSTANCE = new RenderStateEvent();
   @SerializedName("renderState")
   public ItemEntityRenderState renderState;
   @SerializedName("matrixStack")
   public MatrixStack matrixStack;
   @SerializedName("vertexConsumerProvider")
   public VertexConsumerProvider vertexConsumerProvider;
   @SerializedName("light")
   public int light;

   public static RenderStateEvent getRenderStateEvent(ItemEntityRenderState itemEntityRenderState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n) {
      INSTANCE.setCancelled(false);
      INSTANCE.renderState = itemEntityRenderState;
      INSTANCE.matrixStack = matrices;
      INSTANCE.vertexConsumerProvider = vertexConsumers;
      INSTANCE.light = n;
      return INSTANCE;
   }
}
