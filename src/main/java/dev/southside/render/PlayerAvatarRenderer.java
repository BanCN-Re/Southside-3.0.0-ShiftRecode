package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;

public final class PlayerAvatarRenderer {
   @SerializedName("imageHandle")
   private int imageHandle = -1;
   @SerializedName("cachedTextureId")
   private int cachedTextureId = -1;
   @SerializedName("cachedImageSize")
   private int cachedImageSize = -1;

   public void drawImage(long l, AbstractClientPlayerEntity player, float f, float f2, float f3, float f4) {
      Identifier id = player.getSkinTextures().texture();
      if (id != null) {
         AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(id);
         if (texture != null) {
            int glId = texture.getGlId();
            int n = Math.max(1, Math.round(f3));
            if (this.imageHandle == -1 || this.cachedTextureId != glId || this.cachedImageSize != n) {
               this.imageHandle = NanoVGGL3.nvglCreateImageFromHandle(l, glId, n, -n, 0);
               this.cachedTextureId = glId;
               this.cachedImageSize = n;
            }

            if (this.imageHandle != -1) {
               NVGPaint paint = NVGPaint.calloc();

               try {
                  float f5 = 8.0F;
                  float f6 = f2 - f3 / 4.0F * f5 / 2.0F + 0.1F;
                  float f7 = f3 * f5;
                  NanoVG.nvgImagePattern(l, f - f3 / 4.0F * f5 / 2.0F, f6, f7, f7, 0.0F, this.imageHandle, 1.0F, paint);
                  draw(l, f, f2, f3, f4, paint);
                  NanoVG.nvgImagePattern(l, f - f3 * 3.25F * f5 / 2.0F, f6, f7, f7, 0.0F, this.imageHandle, 1.0F, paint);
                  draw(l, f, f2 - 0.1F, f3, f4, paint);
               } catch (Throwable e) {
                  if (paint != null) {
                     try {
                        paint.close();
                     } catch (Throwable e2) {
                        e.addSuppressed(e2);
                     }
                  }

                  throw e;
               }

               if (paint != null) {
                  paint.close();
               }
            }
         }
      }
   }

   private static void draw(long vg, float x, float y, float width, float height, NVGPaint paint) {
      NanoVG.nvgBeginPath(vg);
      SquirclePath.draw(vg, x, y, width, width, height);
      NanoVG.nvgFillPaint(vg, paint);
      NanoVG.nvgFill(vg);
      NanoVG.nvgClosePath(vg);
   }
}
