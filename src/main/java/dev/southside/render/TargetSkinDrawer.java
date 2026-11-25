package dev.southside.render;

import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;

public class TargetSkinDrawer {
   public static LivingEntity livingEntity;
   public static LivingEntity livingEntityValue;
   public static int count = -1;

   public static void drawImage(long l, Identifier id, float f, float f2, float f3, float f4, float f5) {
      MinecraftClient instance = MinecraftClient.getInstance();
      if (id != null && instance.getTextureManager().getTexture(id) != null) {
         int glId = instance.getTextureManager().getTexture(id).getGlId();
         if (count == -1) {
            count = NanoVGGL3.nvglCreateImageFromHandle(l, glId, (int)f3, -((int)f4), 0);
         } else {
            float f6 = f5 - 1.0F;
            int n = count;
            NanoVG.nvgImageSize(l, n, new int[]{(int)f3}, new int[]{-((int)f4)});
            NVGPaint paint = NVGPaint.calloc();
            float f7 = 8.0F;
            float f8 = f2 - f4 / 4.0F * f7 / 2.0F + 0.1F;
            float f9 = f3 * f7;
            float f10 = f4 * f7;
            NanoVG.nvgImagePattern(l, f - f3 / 4.0F * f7 / 2.0F, f8, f9, f10, 0.0F, n, 1.0F, paint);
            NanoVG.nvgBeginPath(l);
            SquirclePath.draw(l, f, f2, f3, f4, f6);
            NanoVG.nvgFillPaint(l, paint);
            NanoVG.nvgFill(l);
            NanoVG.nvgClosePath(l);
            NanoVG.nvgImagePattern(l, f - f3 * 3.25F * f7 / 2.0F, f8, f9, f10, 0.0F, n, 1.0F, paint);
            NanoVG.nvgBeginPath(l);
            SquirclePath.draw(l, f, f2 - 0.1F, f3, f4, f6);
            NanoVG.nvgFillPaint(l, paint);
            NanoVG.nvgFill(l);
            NanoVG.nvgClosePath(l);
            paint.free();
         }
      }
   }

   public static void setLivingEntityValue(LivingEntity living) {
      livingEntityValue = living;
   }

   public static LivingEntity getLivingEntity() {
      return livingEntity;
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (livingEntityValue != null) {
         if (livingEntity != livingEntityValue) {
            count = -1;
            livingEntity = livingEntityValue;
         }
      } else {
         count = -1;
      }
   }

   public static LivingEntity getLivingEntity2() {
      return livingEntityValue;
   }
}
