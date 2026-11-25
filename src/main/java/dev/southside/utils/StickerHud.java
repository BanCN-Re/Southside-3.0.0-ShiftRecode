package dev.southside.utils;

import cc.polyfrost.oneconfig.hud.Hud;
import dev.southside.Southside;
import dev.southside.manager.StickerManager;
import dev.southside.render.UIMatrixStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;

public class StickerHud extends Hud {
   private static final float DEFAULT_FLOAT = 64.0F;
   private static final long DEFAULT_LONG = 3887975315653263359L;

   @Override
   public float setScale(float f, boolean flag) {
      StickerManager.Entry2 entry2 = this.getEntry2();
      return (entry2 == null ? 64.0F : entry2.getHeight()) * f;
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      StickerManager.Entry2 entry2 = this.getEntry2();
      return (entry2 == null ? 64.0F : entry2.getWidth()) * f;
   }

   private StickerManager.Entry2 getEntry2() {
      StickerManager stickerManager = Southside.INSTANCE.getStickerManager();
      return stickerManager == null ? null : stickerManager.getEntry2();
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      StickerManager stickerManager = Southside.INSTANCE.getStickerManager();
      if (stickerManager != null) {
         StickerManager.Entry2 entry2 = stickerManager.getEntry2();
         if (entry2 == null) {
            if (flag) {
               context.fill((int)f, (int)f2, (int)(f + 64.0F * f3), (int)(f2 + 64.0F * f3), (int)DEFAULT_LONG);
            }
         } else {
            int n = (int)(entry2.getWidth() * f3);
            int n2 = (int)(entry2.getHeight() * f3);
            context.drawTexture(RenderLayer::getGuiTextured, entry2.getIdentifier(), (int)f, (int)f2, 0.0F, 0.0F, n, n2, n, n2);
         }
      }
   }
}
