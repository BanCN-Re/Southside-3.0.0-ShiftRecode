package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class HeartParticleEffect {
   private static final Identifier HEART_ID;
   private static final int DEFAULT_INT;
   @SerializedName("particles")
   private final HeartParticleEffect.Entry[] particles = new HeartParticleEffect.Entry[24];
   @SerializedName("next")
   private int next;
   private static long timestamp;

   public void performAction(long l) {
      for (int i = 0; i < 5; i++) {
         this.particles[this.next] = new HeartParticleEffect.Entry(
            l + i * timestamp, 1250 + i * 75, (i - 2) * 0.14F, 0.9F + i % 3 * 0.18F, i * 1.7F, 0.85F + i % 2 * 0.15F
         );
         this.next = (this.next + 1) % this.particles.length;
      }
   }

   public void render(DrawContext context, long l, float f, float f2, float f3) {
      boolean flag = false;

      for (int i = 0; i < this.particles.length; i++) {
         HeartParticleEffect.Entry entry = this.particles[i];
         if (entry != null) {
            long l2 = l - entry.started;
            if (l2 >= entry.lifetime) {
               this.particles[i] = null;
            } else if (l2 >= 0L) {
               flag = true;
            }
         }
      }

      if (flag) {
         context.draw();

         for (HeartParticleEffect.Entry entry2 : this.particles) {
            if (entry2 != null) {
               long l3 = l - entry2.started;
               if (l3 >= 0L) {
                  float f4 = (float)l3 / entry2.lifetime;
                  float clamped = MathHelper.clamp((float)l3 / 120.0F, 0.0F, 1.0F);
                  float f5 = 1.0F - MathHelper.clamp((f4 - 0.55F) / 0.45F, 0.0F, 1.0F);
                  int n = Math.round(255.0F * clamped * f5);
                  if (n != 0) {
                     float f6 = f + f3 * (entry2.spread * (0.5F + f4) + (float)Math.sin(f4 * 5.0F + entry2.phase) * 0.055F);
                     float f7 = f2 - f3 * (0.12F + f4 * entry2.rise);
                     float f8 = MathHelper.clamp(f3 * 0.13F, 3.0F, 10.0F) * entry2.size * (0.6F + clamped * 0.4F);
                     drawTexture(context, f6, f7, f8, n << 24 | 16777215);
                  }
               }
            }
         }

         context.draw();
      }
   }

   public void performAction() {
      Arrays.fill(this.particles, null);
      this.next = 0;
   }

   private static void drawTexture(DrawContext context, float f, float f2, float f3, int n) {
      MatrixStack matrices = context.getMatrices();
      matrices.push();

      try {
         matrices.translate(f - f3, f2 - f3, 0.0F);
         matrices.scale(f3 / 4.0F, f3 / 4.0F, 1.0F);
         context.drawTexture(RenderLayer::getGuiTextured, HEART_ID, 0, 0, 0.0F, 0.0F, 8, 8, 8, 8, n);
      } finally {
         matrices.pop();
      }
   }

   static {
      DEFAULT_INT = 5;
      timestamp = 65L;
      HEART_ID = Identifier.ofVanilla("textures/particle/heart.png");
   }


   private record Entry(long started, int lifetime, float spread, float rise, float phase, float size) {

      public long getStarted() {
         return this.started;
      }

      public int getLifetime() {
         return this.lifetime;
      }

      public float getSpread() {
         return this.spread;
      }

      public float getRise() {
         return this.rise;
      }

      public float getPhase() {
         return this.phase;
      }

      public float getSize() {
         return this.size;
      }
   }
}
