package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.render.UIMatrixStack;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

public class GLPlatformImpl implements IGLPlatform {
   @SerializedName("textRenderer")
   private final TextRenderer textRenderer;
   private static final long a = 8212855494427017456L;

   public GLPlatformImpl() {
      this.textRenderer = MinecraftAccess.getMc().textRenderer;
   }

   @Override
   public void runTask(Runnable action) {
      action.run();
   }

   @Override
   public float computeFloat(UIMatrixStack matrices, String text, float f, float f2, int n, boolean flag) {
      Matrix4f matrix = matrices == null ? new Matrix4f() : matrices.getModelRecord().getModel();
      RenderDrawBuffer renderDrawBuffer = RenderContextHolder.renderDrawBuffer.getRenderDrawBuffer(matrix);
      Immediate vertexConsumers = renderDrawBuffer.getVertexConsumers();
      int n2 = this.textRenderer.draw(text, f, f2, n, flag, renderDrawBuffer.getMatrix(), vertexConsumers, TextLayerType.NORMAL, 0, (int)a);
      vertexConsumers.draw();
      return n2;
   }

   @Override
   public int getWidth(String text) {
      return MinecraftAccess.getMc().textRenderer.getWidth(text);
   }

   @Override
   public void render(UIMatrixStack matrices, float f, float f2, float f3, float f4, int n) {
      Matrix4f model = matrices.getModelRecord().getModel();
      if (f < f3) {
         float f5 = f;
         f = f3;
         f3 = f5;
      }

      if (f2 < f4) {
         float f6 = f2;
         f2 = f4;
         f4 = f6;
      }

      float f7 = ColorHelper.getAlpha(n) / 255.0F;
      float f8 = ColorHelper.getRed(n) / 255.0F;
      float f9 = ColorHelper.getGreen(n) / 255.0F;
      float f10 = ColorHelper.getBlue(n) / 255.0F;
      Immediate vertexConsumers = RenderContextHolder.renderDrawBufferValue.getRenderDrawBuffer(model).getVertexConsumers();
      VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getGui());
      buffer.vertex(model, f, f2, 0.0F).color(f8, f9, f10, f7);
      buffer.vertex(model, f, f4, 0.0F).color(f8, f9, f10, f7);
      buffer.vertex(model, f3, f4, 0.0F).color(f8, f9, f10, f7);
      buffer.vertex(model, f3, f2, 0.0F).color(f8, f9, f10, f7);
      vertexConsumers.draw();
   }
}
