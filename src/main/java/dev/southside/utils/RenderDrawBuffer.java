package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class RenderDrawBuffer {
   @SerializedName("vertexConsumers")
   public final Immediate vertexConsumers;
   @SerializedName("matrix")
   public Matrix4f matrix = new Matrix4f().identity();

   public Matrix4f getMatrix() {
      return this.matrix;
   }

   public RenderDrawBuffer getRenderDrawBuffer(MatrixStack matrices) {
      return this.getRenderDrawBuffer(matrices.peek().getPositionMatrix());
   }

   public RenderDrawBuffer getRenderDrawBuffer(Matrix4f matrix2) {
      this.matrix = matrix2;
      return this;
   }

   public RenderDrawBuffer(int n) {
      BufferAllocator bufferAllocator = new BufferAllocator(n);
      this.vertexConsumers = VertexConsumerProvider.immediate(bufferAllocator);
   }

   public Immediate getVertexConsumers() {
      return this.vertexConsumers;
   }
}
