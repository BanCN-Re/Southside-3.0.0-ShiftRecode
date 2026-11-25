package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.impl.client.Theme;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

public class TargetHudCompact {
   @SerializedName("offsetAnimation")
   public final SmoothFloat offsetAnimation = new SmoothFloat(3.0F, 0.0F);
   @SerializedName("healthAnimation")
   public final SmoothFloat healthAnimation = new SmoothFloat(3.0F, 1.0F);
   @SerializedName("player")
   public AbstractClientPlayerEntity player;

   public TargetHudCompact(AbstractClientPlayerEntity player2) {
      this.player = player2;
   }

   public void render(DrawContext context, float f, float f2, float f3) {
      MatrixStack matrices = context.getMatrices();
      matrices.push();
      matrices.translate(f2, f3 + this.offsetAnimation.getValue(), 0.0F);
      float f4 = 32.0F * f;
      float f5 = 8.0F * f;
      float f6 = (40.0F * f - f4) / 2.0F;
      PlayerSkinDrawer.draw(context, this.player.getSkinTextures(), (int)f5, (int)f6, (int)f4);
      matrices.pop();
   }

   public void fromHsb(long l, float f, float f2, float f3) {
      if (!(this.offsetAnimation.getValue() < -(40.0F * f))) {
         float f4 = this.offsetAnimation.getValue() + f3;
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         float f5 = 20.0F * f;
         float f6 = 6.0F * f;
         double health = this.player.getHealth();
         String string = this.player.getDisplayName().getString();
         double d = new BigDecimal(health).setScale(1, RoundingMode.HALF_UP).doubleValue();
         String text = string + " " + d + " HP";
         float textWidth = nvg.getTextWidth(l, text, f5, OneConfigFonts.SEMIBOLD);
         float textHeight = nvg.getTextHeight(l, f5, OneConfigFonts.SEMIBOLD);
         float f7 = f6 * 2.0F + textWidth + 40.0F * f;
         float f8 = 40.0F * f;
         nvg.drawRoundedRect(l, f2, f4, f7, f8, new Color(0, 0, 0, 100).getRGB(), 8.0F * f);
         nvg.drawRoundedRect(l, f2, f4, f7, f8, 10.0F, 0.01F, 8.0F * f, new Color(0, 0, 0, 80));
         float f9 = f2 + f6 + 40.0F * f;
         float f10 = f4 + f6 + textHeight / 2.0F;
         nvg.drawTextEx(l, text, f9, f10, Theme.blendColor(0).getRGB(), f5, OneConfigFonts.SEMIBOLD);
         float f11 = f2 + f6 + 40.0F * f;
         float f12 = f4 + f8 - f6 - 6.0F * f;
         float f13 = 6.0F * f;
         nvg.drawRoundedRect(l, f11, f12, textWidth, f13, new Color(0, 0, 0, 80).getRGB(), 3.0F * f);
         float f14 = this.player.getHealth() / this.player.getMaxHealth();
         this.healthAnimation.setTarget(f14);
         float f15 = textWidth * this.healthAnimation.getValue();
         int n = Color.HSBtoRGB(Math.max(0.0F, f14) / 3.0F, 1.0F, 1.0F);
         nvg.drawRoundedRect(l, f11, f12, f15, f13, n, 3.0F * f);
      }
   }
}
