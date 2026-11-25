package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class TargetHudLarge {
   private static final float DEFAULT_FLOAT = 280.0F;
   private static final float DEFAULT_FLOAT_VALUE = 88.0F;
   @SerializedName("offsetAnimation")
   public final SmoothFloat offsetAnimation = new SmoothFloat(3.0F, 0.0F);
   @SerializedName("healthAnimation")
   public final SmoothFloat healthAnimation = new SmoothFloat(4.0F, 1.0F);
   @SerializedName("player")
   public AbstractClientPlayerEntity player;

   public TargetHudLarge(AbstractClientPlayerEntity player2) {
      this.player = player2;
   }

   public void draw(long l, float f, float f2, float f3) {
      if (!(this.offsetAnimation.getValue() < -(88.0F * f))) {
         float f4 = f3 + this.offsetAnimation.getValue();
         float f5 = 280.0F * f;
         float f6 = 88.0F * f;
         float f7 = 16.0F * f;
         float f8 = 18.0F * f;
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.drawRoundedRect(l, f2, f4, f5, f6, 855638016, f7);
         nvg.drawRoundedRect(l, f2 + f, f4 + f, f5 - 2.0F * f, f6 - 2.0F * f, 755501326, f7 - f);
         float f9 = Math.max(this.player.getMaxHealth(), 1.0F);
         float f10 = Math.max(0.0F, Math.min(this.player.getHealth() / f9, 1.0F));
         this.healthAnimation.setTarget(f10);
         float f11 = this.healthAnimation.getValue();
         int n = Math.round(f11 * 100.0F);
         String string = this.player.getDisplayName().getString();
         float f12 = 18.0F * f;
         float f13 = 15.0F * f;
         float f14 = f4 + f8 + nvg.getTextHeight(l, f12, OneConfigFonts.SEMIBOLD) / 2.0F;
         nvg.drawTextEx(l, string, f2 + f8, f14, -1, f12, OneConfigFonts.SEMIBOLD);
         String text = n + "%";
         float textWidth = nvg.getTextWidth(l, text, f13, OneConfigFonts.SEMIBOLD);
         nvg.drawTextEx(l, text, f2 + f5 - f8 - textWidth, f14, -1, f13, OneConfigFonts.SEMIBOLD);
         float f15 = f5 - f8 * 2.0F;
         float f16 = 8.0F * f;
         float f17 = f2 + f8;
         float f18 = f4 + f6 - f8 - f16;
         nvg.drawRoundedRect(l, f17, f18, f15, f16, -1273883620, f16 / 2.0F);
         nvg.drawRoundedRect(l, f17, f18, f15 * f11, f16, -9399041, f16 / 2.0F);
         nvg.drawRoundedRect(l, f17, f18, f15, f16 / 2.0F, 436207615, f16 / 2.0F);
      }
   }
}
