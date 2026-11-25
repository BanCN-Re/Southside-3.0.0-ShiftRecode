package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.impl.client.Theme;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class TargetHudPill {
   @SerializedName("offsetAnimation")
   public final SmoothFloat offsetAnimation = new SmoothFloat(3.0F, 0.0F);
   @SerializedName("player")
   public AbstractClientPlayerEntity player;

   public TargetHudPill(AbstractClientPlayerEntity player2) {
      this.player = player2;
   }

   public void draw(long l, float f, float f2, float f3) {
      if (!(this.offsetAnimation.getValue() < -(40.0F * f))) {
         float f4 = this.offsetAnimation.getValue() + f3;
         double health = this.player.getHealth();
         String string = this.player.getDisplayName().getString();
         double d = new BigDecimal(health).setScale(1, RoundingMode.HALF_UP).doubleValue();
         String text = string + " " + d + " HP";
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         float f5 = 28.0F * f;
         float f6 = 8.0F * f;
         float textWidth = nvg.getTextWidth(l, text, f5, OneConfigFonts.SEMIBOLD);
         float textHeight = nvg.getTextHeight(l, f5, OneConfigFonts.SEMIBOLD);
         float f7 = f6 * 2.0F + textWidth;
         float f8 = textHeight + f6 * 2.0F;
         nvg.drawRoundedRect(l, f2, f4, f7, f8, new Color(0, 0, 0, 100).getRGB(), 8.0F * f);
         nvg.drawRoundedRect(l, f2, f4, f7, f8, 10.0F, 0.01F, 8.0F * f, new Color(0, 0, 0, 80));
         float f9 = f2 + f6;
         float f10 = f4 + f6 + textHeight / 2.0F;
         nvg.drawTextEx(l, text, f9, f10, Theme.blendColor(0).getRGB(), f5, OneConfigFonts.SEMIBOLD);
      }
   }
}
