package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.render.PlayerAvatarRenderer;
import java.awt.Color;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class TargetHudModern {
   @SerializedName("offsetAnimation")
   public final SmoothFloat offsetAnimation = new SmoothFloat(3.0F, 0.0F);
   @SerializedName("healthAnimation")
   public final SmoothFloat healthAnimation = new SmoothFloat(3.0F, 1.0F);
   @SerializedName("highlightAnimation")
   public final SmoothFloat highlightAnimation = new SmoothFloat(10.0F, 0.0F);
   @SerializedName("player")
   public AbstractClientPlayerEntity player;
   @SerializedName("skinRenderer")
   private final PlayerAvatarRenderer skinRenderer = new PlayerAvatarRenderer();

   public TargetHudModern(AbstractClientPlayerEntity player2) {
      this.player = player2;
   }

   public void draw(long l, float f, float f2, float f3) {
      if (!(this.offsetAnimation.getValue() < -30.0F * f)) {
         float f4 = this.offsetAnimation.getValue() + f3;
         String formatted = String.format("Health: %.1f /  %.1f", this.player.getHealth(), this.player.getMaxHealth());
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         ScissorHelper scissor = ScissorHelper.INSTANCE;
         float f5 = Math.max(
            Math.max(
               nvg.getTextWidth(l, formatted, 14.0F * f, OneConfigFonts.REGULAR) + 80.0F * f,
               nvg.getTextWidth(l, this.player.getDisplayName().getString(), 18.0F * f, OneConfigFonts.SEMIBOLD) + 80.0F * f
            ),
            197.5F * f
         );
         float f6 = f5 * this.healthAnimation.getValue();
         float textHeight = nvg.getTextHeight(l, 13.0F * f, OneConfigFonts.SEMIBOLD);
         float f7 = (62.5F * f - 30.0F * f) / 2.0F + textHeight / 2.0F;
         boolean flag = this.player == KillAura.target;
         this.highlightAnimation.setTarget(flag ? 1.0F : 0.0F);
         if (flag) {
            this.highlightAnimation.setImmediate(1.0F);
         }

         ScissorRect clip = scissor.scissor(l, f2 + f6, f4, f5 - f6, 62.5F * f);
         this.draw(l, nvg, f2, f4, f, f5, f7, textHeight, formatted, -1088281818, -1);
         scissor.resetScissor(l, clip);
         ScissorRect clip2 = scissor.scissor(l, f2, f4, f6, 62.5F * f);
         int n = flag ? -922746881 : new Color(255, 255, 255, (int)ColorUtils.lerpFloat(140.0F, 200.0F, this.highlightAnimation.getValue())).getRGB();
         this.draw(l, nvg, f2, f4, f, f5, f7, textHeight, formatted, n, -16777216);
         scissor.resetScissor(l, clip2);
         this.healthAnimation.setTarget(this.player.getHealth() / this.player.getMaxHealth());
      }
   }

   private void draw(long l, NanoVGHelper nvg, float f, float f2, float f3, float f4, float f5, float f6, String text, int n, int n2) {
      nvg.drawRoundedRect(l, f, f2, f4, 62.5F * f3, n, 9.0F * f3);
      nvg.draw(l, this.player.getDisplayName().getString(), (int)(f + 60.0F * f3), f2 + f5, n2, 18.0F * f3, OneConfigFonts.SEMIBOLD);
      nvg.draw(l, text, f + 60.0F * f3, f2 + f5 + f6 + 7.5F * f3, n2, 14.0F * f3, OneConfigFonts.REGULAR);
   }

   public void performAction(float x, float y, float z) {
      float f = 40.0F * x;
      float f2 = y + 12.0F * x;
      float f3 = z + this.offsetAnimation.getValue() + (61.25F * x - f) / 2.0F;
      float f4 = 3.0F * x;
      ScissorHelper scissor = ScissorHelper.INSTANCE;
      NanoVGHelper.INSTANCE.performAction(true, l -> {
         ScissorRect clip = scissor.scissor(l, y - 5.0F * x, z + 1.0F, WindowMetrics.getScaledWidth(), WindowMetrics.getScaledHeight());
         this.skinRenderer.drawImage(l, this.player, f2, f3, f, f4);
         scissor.resetScissor(l, clip);
      });
   }
}
