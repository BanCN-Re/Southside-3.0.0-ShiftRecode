package dev.southside.mixin.misc;

import dev.southside.module.impl.render.BrokenClouds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.CloudRenderer;
import net.minecraft.client.render.CloudRenderer.CloudCells;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public abstract class MixinCloudRenderer {
   @Shadow
   private CloudCells cells;

   @Shadow
   private static int unpackColor(long l) {
      throw new AssertionError();
   }

   @Inject(method = "buildCloudCells", at = @At("HEAD"), cancellable = true)
   private void buildBrokenClouds(
      @Coerce Object obj, BufferBuilder bufferBuilder, int n, int n2, int n3, int n4, int n5, int n6, boolean flag, CallbackInfo ci
   ) {
      if (!flag && this.cells != null && BrokenClouds.isValidEx()) {
         float f = 12.0F;
         float f2 = 4.0F * f;
         float farPlaneDistance = MinecraftClient.getInstance().gameRenderer.getFarPlaneDistance();
         int n7 = Math.max(32, MathHelper.ceil(farPlaneDistance / f));
         float f3 = f2 * MathHelper.ceil(2.0F * farPlaneDistance / f2);
         f3 = Math.max(f3, n7 * f);
         long[] longArray = this.cells.cells();
         int n8 = this.cells.width();
         int n9 = this.cells.height();

         for (int i = -n7; i <= n7; i++) {
            long l = longArray[Math.floorMod(n2 + i, n9) * n8];
            if (l != 0L) {
               int n10 = ColorHelper.mix(n4, unpackColor(l));
               float f4 = i * f;
               float f5 = f4 + f;

               for (float j = -f3; j < f3; j += f2) {
                  float f6 = Math.min(j + f2, f3);
                  bufferBuilder.vertex(j, 0.0F, f4).color(n10);
                  bufferBuilder.vertex(j, 0.0F, f5).color(n10);
                  bufferBuilder.vertex(f6, 0.0F, f5).color(n10);
                  bufferBuilder.vertex(f6, 0.0F, f4).color(n10);
               }
            }
         }

         ci.cancel();
      }
   }
}
