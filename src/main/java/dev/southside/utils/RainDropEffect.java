package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class RainDropEffect {
   private static final long DEFAULT_LONG = 60000L;
   private static final double DEFAULT_DOUBLE = 0.055;
   private static final float DEFAULT_FLOAT = 0.032F;
   private static final float DEFAULT_FLOAT_VALUE = 0.055F;
   private static final int DEFAULT_INT = 235;
   private static final int DEFAULT_INT_VALUE = 190;
   private static final List<RainDropEffect.Inner> inners = new ArrayList<>();

   private RainDropEffect() {
   }

   public static void onMouseClick(double x, double y, double z) {
      inners.add(new RainDropEffect.Inner(x, y, z));
   }

   public static void clear() {
      inners.clear();
   }

   private static void performAction() {
      if (!inners.isEmpty()) {
         ClientWorld world = MinecraftClient.getInstance().world;
         long now = System.currentTimeMillis();
         Iterator it = inners.iterator();

         while (it.hasNext()) {
            RainDropEffect.Inner inner = (RainDropEffect.Inner)it.next();
            if (inner.resting) {
               if (now >= inner.expiresAt) {
                  it.remove();
               }
            } else if (world == null) {
               it.remove();
            } else {
               inner.y -= 0.055;
               BlockPos pos = BlockPos.ofFloored(inner.x, inner.y - 0.001, inner.z);
               BlockState blockState = world.getBlockState(pos);
               VoxelShape collisionShape = blockState.getCollisionShape(world, pos);
               if (!collisionShape.isEmpty()) {
                  inner.y = pos.getY() + collisionShape.getMax(Axis.Y) + 0.012;
                  inner.resting = true;
                  inner.expiresAt = now + 60000L;
               } else if (inner.y < world.getBottomY() - 1.0) {
                  it.remove();
               }
            }
         }
      }
   }

   private static void render(WorldRenderContext renderContext) {
      if (!inners.isEmpty()) {
         VertexConsumerProvider vertexConsumers = renderContext.consumers();
         if (vertexConsumers != null) {
            Camera camera = renderContext.camera();
            Vec3d pos = camera.getPos();
            Quaternionf rotation = camera.getRotation();
            MatrixStack matrices = renderContext.matrixStack();
            if (matrices == null) {
               matrices = new MatrixStack();
            }

            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            boolean flag = Math.abs(positionMatrix.m30() + pos.x) < 0.5 && Math.abs(positionMatrix.m31() + pos.y) < 0.5 && Math.abs(positionMatrix.m32() + pos.z) < 0.5;
            double d = flag ? 0.0 : pos.x;
            double d2 = flag ? 0.0 : pos.y;
            double d3 = flag ? 0.0 : pos.z;

            for (RainDropEffect.Inner inner : inners) {
               matrices.push();
               matrices.translate(inner.x - d, inner.y - d2, inner.z - d3);
               if (inner.resting) {
                  performActionEx(buffer, matrices.peek(), 0.055F, 190);
               } else {
                  matrices.multiply(rotation);
                  performAction(buffer, matrices.peek(), 0.032F, 235);
               }

               matrices.pop();
            }
         }
      }
   }

   private static void performAction(VertexConsumer consumer, Entry entry, float f, int n) {
      color(consumer, entry, -f, -f, 0.0F, n);
      color(consumer, entry, f, -f, 0.0F, n);
      color(consumer, entry, f, f, 0.0F, n);
      color(consumer, entry, -f, f, 0.0F, n);
   }

   private static void performActionEx(VertexConsumer consumer, Entry entry, float f, int n) {
      color(consumer, entry, -f, 0.0F, -f, n);
      color(consumer, entry, f, 0.0F, -f, n);
      color(consumer, entry, f, 0.0F, f, n);
      color(consumer, entry, -f, 0.0F, f, n);
   }

   private static void color(VertexConsumer consumer, Entry entry, float f, float f2, float f3, int n) {
      consumer.vertex(entry, f, f2, f3).color(255, 255, 255, n);
   }

   static {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> performAction());
      WorldRenderEvents.AFTER_ENTITIES.register(RainDropEffect::render);
   }

   private static final class Inner {
      @SerializedName("x")
      public final double x;
      @SerializedName("y")
      public double y;
      @SerializedName("z")
      public final double z;
      @SerializedName("resting")
      public boolean resting;
      @SerializedName("expiresAt")
      public long expiresAt;

      public Inner(double x2, double y2, double z2) {
         this.x = x2;
         this.y = y2;
         this.z = z2;
      }
   }
}
