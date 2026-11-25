package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

public class TargetHudSimple {
   private static final float DEFAULT_FLOAT = 170.0F;
   private static final float DEFAULT_FLOAT_VALUE = 44.0F;
   private static final float DEFAULT_FLOAT_DATA = 32.0F;
   private static final int DEFAULT_INT = -1275068416;
   private static final int DEFAULT_INT_VALUE = 520093695;
   private static final int DEFAULT_INT_DATA = -587202561;
   private static final int DEFAULT_INT_REF = -4934476;
   @SerializedName("offsetAnimation")
   public final SmoothFloat offsetAnimation = new SmoothFloat(3.0F, 0.0F);
   @SerializedName("healthAnimation")
   public final SmoothFloat healthAnimation = new SmoothFloat(3.0F, 1.0F);
   @SerializedName("player")
   public AbstractClientPlayerEntity player;

   public TargetHudSimple(AbstractClientPlayerEntity player2) {
      this.player = player2;
   }

   public void render(DrawContext context, float f, float f2, float f3) {
      MatrixStack matrices = context.getMatrices();
      matrices.push();
      matrices.translate(f2, f3 + this.offsetAnimation.getValue(), 0.0F);
      float f4 = 32.0F * f;
      float f5 = (44.0F * f - f4) / 2.0F;
      PlayerSkinDrawer.draw(context, this.player.getSkinTextures(), (int)f5, (int)f5, (int)f4);
      matrices.pop();
   }

   public void draw(long l, float f, float f2, float f3) {
      if (!(this.offsetAnimation.getValue() < -(44.0F * f))) {
         float f4 = this.offsetAnimation.getValue() + f3;
         float f5 = 170.0F * f;
         float f6 = 44.0F * f;
         float f7 = 32.0F * f;
         float f8 = f4 + (f6 - f7) / 2.0F;
         float f9 = f8 + f7;
         float f10 = f2 + 44.0F * f;
         float f11 = f5 - 6.0F * f - 44.0F * f;
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.drawRect(l, f2, f4, f5, f6, -1275068416);
         float f12 = 3.0F * f;
         float f13 = f9 - f12 - f;
         float f14 = 10.0F * f;
         float f15 = f8 + nvg.getTextHeight(l, f14, OneConfigFonts.SEMIBOLD) / 2.0F;
         nvg.draw(l, this.player.getDisplayName().getString(), f10, f15, -1, f14, OneConfigFonts.SEMIBOLD);
         float f16 = this.player.getHealth() / this.player.getMaxHealth();
         this.healthAnimation.setTarget(f16);
         String formatted = String.format("%.1f HP", this.player.getHealth());
         nvg.drawTextEx(l, formatted, f10, f8 + f7 * 0.5F, -4934476, 8.0F * f, OneConfigFonts.REGULAR);
         nvg.drawRect(l, f10, f13, f11, f12, 520093695);
         float f17 = f11 * this.healthAnimation.getValue();
         if (f17 > 0.5F) {
            nvg.drawRect(l, f10, f13, f17, f12, -587202561);
         }
      }
   }
}
