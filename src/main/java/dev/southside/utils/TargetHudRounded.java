package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

public class TargetHudRounded {
   private static final float DEFAULT_FLOAT = 160.0F;
   private static final float DEFAULT_FLOAT_VALUE = 40.0F;
   @SerializedName("offsetAnimation")
   public final SmoothFloat offsetAnimation = new SmoothFloat(3.0F, 0.0F);
   @SerializedName("healthAnimation")
   public final SmoothFloat healthAnimation = new SmoothFloat(3.0F, 1.0F);
   @SerializedName("player")
   public AbstractClientPlayerEntity player;

   public TargetHudRounded(AbstractClientPlayerEntity player2) {
      this.player = player2;
   }

   public void fromHsb(long l, float f, float f2, float f3) {
      if (!(this.offsetAnimation.getValue() < -(40.0F * f))) {
         float f4 = this.offsetAnimation.getValue() + f3;
         float f5 = 160.0F * f;
         float f6 = 40.0F * f;
         float f7 = 8.0F * f;
         float f8 = f;
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.drawRoundedRect(l, f2, f4, f5, f6, 855638015, f7);
         nvg.drawRoundedRect(l, f2 + f8, f4 + f8, f5 - f8 * 2.0F, f6 - f8 * 2.0F, 1679037460, f7 - f8);
         nvg.draw(l, this.player.getDisplayName().getString(), f2 + 44.0F * f, f4 + 11.0F * f, -1, 10.0F * f, OneConfigFonts.REGULAR);
         float f9 = f2 + 44.0F * f;
         float f10 = f4 + 25.0F * f;
         float f11 = f5 - 54.0F * f;
         float f12 = 6.0F * f;
         nvg.drawRoundedRect(l, f9, f10, f11, f12, 1342177280, 3.0F * f);
         float f13 = this.player.getHealth() / this.player.getMaxHealth();
         this.healthAnimation.setTarget(f13);
         float f14 = f11 * this.healthAnimation.getValue();
         int n = Color.HSBtoRGB(Math.max(0.0F, f13) / 3.0F, 1.0F, 1.0F);
         nvg.drawRoundedRect(l, f9, f10, f14, f12, n, 3.0F * f);
      }
   }

   public void render(DrawContext context, float f, float f2, float f3) {
      MatrixStack matrices = context.getMatrices();
      matrices.push();
      matrices.translate(f2, f3 + this.offsetAnimation.getValue(), 0.0F);
      float f4 = 28.0F * f;
      float f5 = 8.0F * f;
      float f6 = (40.0F * f - f4) / 2.0F;
      PlayerSkinDrawer.draw(context, this.player.getSkinTextures(), (int)f5, (int)f6, (int)f4);
      matrices.pop();
   }
}
