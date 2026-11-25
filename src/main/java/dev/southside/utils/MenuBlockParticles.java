package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShapes;
import org.joml.Quaternionfc;

public final class MenuBlockParticles {
   private static final int DEFAULT_INT = 1024;
   private static final long DEFAULT_LONG = 50L;
   @SerializedName("particles")
   private final Deque<MenuBlockParticles.Inner> particles = new ArrayDeque<>();
   @SerializedName("landscape")
   private final MenuIslandLandscape landscape;
   @SerializedName("camera")
   private final Camera camera = new Camera();
   @SerializedName("loadedModels")
   private LoadedEntityModels loadedModels;
   @SerializedName("lastUpdate")
   private long lastUpdate;
   @SerializedName("remainder")
   private long remainder;

   public MenuBlockParticles(MenuIslandLandscape menuIslandLandscape) {
      this.landscape = menuIslandLandscape;
   }

   public void performAction(MenuIslandLandscape.Entry entry, long l) {
      this.update(l);
      BlockState state = entry.getState();
      if (!state.isAir() && state.hasBlockBreakParticles()) {
         BlockPos pos = entry.getPos();
         entry.getOutline()
            .forEachBox(
               (d, d2, d3, d4, d5, d6) -> {
                  double d7 = Math.min(1.0, d4 - d);
                  double d8 = Math.min(1.0, d5 - d2);
                  double d9 = Math.min(1.0, d6 - d3);
                  int n = Math.max(2, MathHelper.ceil(d7 / 0.25));
                  int n2 = Math.max(2, MathHelper.ceil(d8 / 0.25));
                  int n3 = Math.max(2, MathHelper.ceil(d9 / 0.25));

                  for (int i = 0; i < n; i++) {
                     for (int j = 0; j < n2; j++) {
                        for (int k = 0; k < n3; k++) {
                           double d10 = (i + 0.5) / n;
                           double d11 = (j + 0.5) / n2;
                           double d12 = (k + 0.5) / n3;

                           while (this.particles.size() >= 1024) {
                              this.particles.removeFirst();
                           }

                           this.particles
                              .addLast(
                                 new MenuBlockParticles.Inner(
                                    this.landscape,
                                    state,
                                    pos,
                                    pos.getX() + d + d10 * d7,
                                    pos.getY() + d2 + d11 * d8,
                                    pos.getZ() + d3 + d12 * d9,
                                    d10 - 0.5,
                                    d11 - 0.5,
                                    d12 - 0.5
                                 )
                              );
                        }
                     }
                  }
               }
            );
      }
   }

   public void update(long l) {
      LoadedEntityModels loadedEntityModels = MinecraftClient.getInstance().getLoadedEntityModels();
      if (loadedEntityModels != this.loadedModels || MinecraftClient.getInstance().getOverlay() != null) {
         this.clear();
         this.loadedModels = loadedEntityModels;
      }

      if (this.lastUpdate != 0L) {
         this.remainder = this.remainder + Math.max(0L, Math.min(250L, l - this.lastUpdate));
      }

      for (this.lastUpdate = l; this.remainder >= 50L; this.remainder -= 50L) {
         for (MenuBlockParticles.Inner inner : this.particles) {
            inner.tick();
         }

         this.particles.removeIf(inner2 -> !inner2.isAlive());
      }
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Quaternionfc rotation) {
      if (!this.particles.isEmpty()) {
         this.camera.getRotation().set(rotation).conjugate();
         MenuBlockParticles.Entry entry = new MenuBlockParticles.Entry(matrices.peek(), vertexConsumers.getBuffer(MenuRenderLayers.MENU_BLOCK_PARTICLES));
         float f = (float)this.remainder / 50.0F;

         for (MenuBlockParticles.Inner inner : this.particles) {
            inner.render(entry, this.camera, f);
         }
      }
   }

   public void clear() {
      this.particles.clear();
      this.lastUpdate = 0L;
      this.remainder = 0L;
   }

   private record Entry(net.minecraft.client.util.math.MatrixStack.Entry entry, VertexConsumer delegate) implements VertexConsumer {
      public VertexConsumer vertex(float x, float y, float z) {
         this.delegate.vertex(this.entry, x, y, z);
         return this;
      }

      public VertexConsumer color(int n, int n2, int n3, int n4) {
         this.delegate.color(n, n2, n3, n4);
         return this;
      }

      public VertexConsumer texture(float x, float y) {
         this.delegate.texture(x, y);
         return this;
      }

      public VertexConsumer overlay(int n, int n2) {
         this.delegate.overlay(n, n2);
         return this;
      }

      public VertexConsumer light(int n, int n2) {
         this.delegate.light(n, n2);
         return this;
      }

      public VertexConsumer normal(float x, float y, float z) {
         this.delegate.normal(this.entry, x, y, z);
         return this;
      }

      public net.minecraft.client.util.math.MatrixStack.Entry getEntry() {
         return this.entry;
      }

      public VertexConsumer getDelegate() {
         return this.delegate;
      }
   }

   private static final class Inner extends BlockDustParticle {
      @SerializedName("landscape")
      private final MenuIslandLandscape landscape;
      @SerializedName("resting")
      private boolean resting;
      private static final long DEFAULT_LONG = -7691414200866832144L;

      public Inner(MenuIslandLandscape menuIslandLandscape, BlockState state, BlockPos pos, double d, double d2, double d3, double d4, double d5, double d6) {
         super(null, d, d2, d3, d4, d5, d6, state, pos);
         this.landscape = menuIslandLandscape;
         this.collidesWithWorld = false;
      }

      public int getBrightness(float f) {
         return (int)DEFAULT_LONG;
      }

      public void move(double x, double y, double z) {
         if (!this.resting) {
            Box boundingBox = this.getBoundingBox();
            List collisions = this.landscape.getCollisions();
            double d = x;
            double d2 = y;
            double d3 = z;
            y = VoxelShapes.calculateMaxOffset(Axis.Y, boundingBox, collisions, y);
            if (y != 0.0) {
               boundingBox = boundingBox.offset(0.0, y, 0.0);
            }

            boolean flag = Math.abs(x) < Math.abs(z);
            if (flag) {
               z = VoxelShapes.calculateMaxOffset(Axis.Z, boundingBox, collisions, z);
               if (z != 0.0) {
                  boundingBox = boundingBox.offset(0.0, 0.0, z);
               }
            }

            x = VoxelShapes.calculateMaxOffset(Axis.X, boundingBox, collisions, x);
            if (!flag) {
               if (x != 0.0) {
                  boundingBox = boundingBox.offset(x, 0.0, 0.0);
               }

               z = VoxelShapes.calculateMaxOffset(Axis.Z, boundingBox, collisions, z);
            }

            super.move(x, y, z);
            this.resting = Math.abs(d2) >= 1.0E-5 && Math.abs(y) < 1.0E-5;
            this.onGround = d2 != y && d2 < 0.0;
            if (d != x) {
               this.velocityX = 0.0;
            }

            if (d3 != z) {
               this.velocityZ = 0.0;
            }
         }
      }
   }
}
