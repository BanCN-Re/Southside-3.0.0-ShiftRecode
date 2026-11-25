package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class CustomParticleRenderer {
   private static final int[][] INT_ARRAY_ARRAY = new int[][]{{20, 24}, {32, 24}};
   private static final int DEFAULT_INT = 8;
   private static final float DEFAULT_FLOAT = 64.0F;
   private static final List<CustomParticleRenderer.Inner> inners = new ArrayList<>();
   private static Identifier textureId;

   private CustomParticleRenderer() {
   }

   public static void performAction(Identifier id, double d, double d2, double d3) {
      textureId = id;
      ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

      for (int i = 0; i < 12; i++) {
         int[] values = INT_ARRAY_ARRAY[threadLocalRandom.nextInt(INT_ARRAY_ARRAY.length)];
         inners.add(
            new CustomParticleRenderer.Inner(
               d + (threadLocalRandom.nextDouble() - 0.5) * 0.36,
               d2 + (threadLocalRandom.nextDouble() - 0.5) * 0.26,
               d3 + (threadLocalRandom.nextDouble() - 0.5) * 0.36,
               (threadLocalRandom.nextDouble() - 0.5) * 0.12,
               threadLocalRandom.nextDouble() * 0.12 + 0.03,
               (threadLocalRandom.nextDouble() - 0.5) * 0.12,
               14 + threadLocalRandom.nextInt(12),
               0.03F + threadLocalRandom.nextFloat() * 0.022F,
               values[0] + threadLocalRandom.nextInt(6),
               values[1] + threadLocalRandom.nextInt(6)
            )
         );
      }
   }

   private static void performAction() {
      if (!inners.isEmpty()) {
         Iterator it = inners.iterator();

         while (it.hasNext()) {
            CustomParticleRenderer.Inner inner = (CustomParticleRenderer.Inner)it.next();
            inner.x = inner.x + inner.vx;
            inner.y = inner.y + inner.vy;
            inner.z = inner.z + inner.vz;
            inner.vy -= 0.014;
            if (--inner.life <= 0) {
               it.remove();
            }
         }

         if (inners.isEmpty()) {
            textureId = null;
         }
      }
   }

   private static void render(WorldRenderContext renderContext) {
      if (!inners.isEmpty() && textureId != null) {
         VertexConsumerProvider vertexConsumers = renderContext.consumers();
         if (vertexConsumers != null) {
            Camera camera = renderContext.camera();
            Vec3d pos = camera.getPos();
            Quaternionf rotation = camera.getRotation();
            MatrixStack matrices = renderContext.matrixStack();
            if (matrices == null) {
               matrices = new MatrixStack();
            }

            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(textureId));
            int n = 15728880;
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            boolean flag = Math.abs(positionMatrix.m30() + pos.x) < 0.5 && Math.abs(positionMatrix.m31() + pos.y) < 0.5 && Math.abs(positionMatrix.m32() + pos.z) < 0.5;
            double d = flag ? 0.0 : pos.x;
            double d2 = flag ? 0.0 : pos.y;
            double d3 = flag ? 0.0 : pos.z;

            for (CustomParticleRenderer.Inner inner : inners) {
               matrices.push();
               matrices.translate(inner.x - d, inner.y - d2, inner.z - d3);
               matrices.multiply(rotation);
               Entry entry = matrices.peek();
               float size = inner.size;
               float f = inner.u / 64.0F;
               float f2 = (inner.u + 2) / 64.0F;
               float f3 = inner.v / 64.0F;
               float f4 = (inner.v + 2) / 64.0F;
               performAction(buffer, entry, size, f, f3, f2, f4, n);
               matrices.pop();
            }
         }
      }
   }

   private static void performAction(VertexConsumer consumer, Entry entry, float f, float f2, float f3, float f4, float f5, int n) {
      normal(consumer, entry, -f, -f, f2, f5, n);
      normal(consumer, entry, f, -f, f4, f5, n);
      normal(consumer, entry, f, f, f4, f3, n);
      normal(consumer, entry, -f, f, f2, f3, n);
   }

   private static void normal(VertexConsumer consumer, Entry entry, float f, float f2, float f3, float f4, int n) {
      consumer.vertex(entry, f, f2, 0.0F)
         .color(255, 255, 255, 255)
         .texture(f3, f4)
         .overlay(OverlayTexture.DEFAULT_UV)
         .light(n)
         .normal(entry, 0.0F, 0.0F, 1.0F);
   }

   static {
      WorldRenderEvents.AFTER_ENTITIES.register(CustomParticleRenderer::render);
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> performAction());
   }

   private static final class Inner {
      @SerializedName("x")
      public double x;
      @SerializedName("y")
      public double y;
      @SerializedName("z")
      public double z;
      @SerializedName("vx")
      public double vx;
      @SerializedName("vy")
      public double vy;
      @SerializedName("vz")
      public double vz;
      @SerializedName("life")
      public int life;
      @SerializedName("size")
      public final float size;
      @SerializedName("u")
      public final int u;
      @SerializedName("v")
      public final int v;

      public Inner(double d, double d2, double d3, double d4, double d5, double d6, int n, float f, int n2, int n3) {
         this.x = d;
         this.y = d2;
         this.z = d3;
         this.vx = d4;
         this.vy = d5;
         this.vz = d6;
         this.life = n;
         this.size = f;
         this.u = n2;
         this.v = n3;
      }
   }
}
