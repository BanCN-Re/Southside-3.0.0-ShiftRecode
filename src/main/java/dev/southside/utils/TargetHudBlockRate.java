package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;

public class TargetHudBlockRate {
   private static final int DEFAULT_INT = 20;
   private static final long DEFAULT_LONG = 1048575L;
   @SerializedName("offsetAnimation")
   public final SmoothFloat offsetAnimation = new SmoothFloat(3.0F, 0.0F);
   @SerializedName("healthAnimation")
   public final SmoothFloat healthAnimation = new SmoothFloat(3.0F, 1.0F);
   @SerializedName("player")
   public AbstractClientPlayerEntity player;
   @SerializedName("blockHistory")
   private long blockHistory;

   public TargetHudBlockRate(AbstractClientPlayerEntity player2) {
      this.player = player2;
   }

   private boolean isEmpty(ItemStack stack) {
      return stack != null && !stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.isOf(Items.SHIELD));
   }

   private void performAction() {
      boolean flag = this.player.isUsingItem() && this.isEmpty(this.player.getActiveItem());
      this.blockHistory = (this.blockHistory << 1 | (flag ? 1L : 0L)) & 1048575L;
   }

   private int computeInt() {
      return Math.min(100, Math.round(Long.bitCount(this.blockHistory) / 20.0F * 100.0F));
   }

   public void render(DrawContext context, float f, float f2, float f3) {
      float f4 = this.offsetAnimation.getValue();
      if (!(f4 < -(50.0F * f))) {
         float f5 = f3 + f4;
         MatrixStack matrices = context.getMatrices();
         matrices.push();
         matrices.translate(f2 + 5.0F * f, f5 + 2.0F * f, 0.0F);
         int n = Math.max(1, Math.round(24.0F * f));
         PlayerSkinDrawer.draw(context, this.player.getSkinTextures(), 0, 0, n);
         matrices.pop();
      }
   }

   public void draw(long l, float f, float f2, float f3) {
      float f4 = this.offsetAnimation.getValue();
      if (!(f4 < -(50.0F * f))) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         float f5 = f3 + f4;
         float f6 = 120.0F * f;
         float f7 = 28.0F * f;
         float f8 = 2.0F * f;
         float f9 = 12.0F * f;
         float f10 = f5 + f7 + f8;
         this.performAction();
         float f11 = 3.0F * f;
         nvg.drawRoundedRect(l, f2, f5, f6, f7, -1778384896, f11);
         nvg.drawRoundedRect(l, f2, f10, f6, f9, -1778384896, f11);
         float f12 = 6.0F * f;
         float f13 = f2 + 5.0F * f;
         float f14 = f13 + 24.0F * f + f12;
         String string = this.player.getDisplayName().getString();
         float f15 = 9.0F * f;
         float f16 = f2 + f6 - f14 - f12;
         string = formatText(nvg, l, string, f16, f15, OneConfigFonts.SEMIBOLD);
         float f17 = f5 + 4.0F * f + 5.5F * f;
         nvg.drawTextEx(l, string, f14, f17, -1, f15, OneConfigFonts.SEMIBOLD);
         float f18 = Math.max(this.player.getMaxHealth(), 1.0F);
         float f19 = this.player.getHealth() / f18;
         this.healthAnimation.setTarget(f19 < 0.0F ? 0.0F : Math.min(f19, 1.0F));
         float f20 = this.healthAnimation.getValue();
         float f21 = 2.5F * f;
         float f22 = f5 + 19.0F * f;
         float f23 = Math.max(1.0F, f2 + f6 - f14 - f12);
         nvg.drawRoundedRect(l, f14, f22, f23, f21, -936892376, f21 / 2.0F);
         if (f20 > 0.02F) {
            nvg.drawRoundedRect(l, f14, f22, f23 * f20, f21, -1, f21 / 2.0F);
         }

         String formatted = String.format("Health: %.1f  Block Rate: %d%%", this.player.getHealth(), this.computeInt());
         float f24 = 8.0F * f;
         float f25 = f10 + (f9 - f24) / 2.0F + 5.0F * f;
         nvg.drawTextEx(l, formatted, f2 + f12, f25, -2302756, f24, OneConfigFonts.REGULAR);
      }
   }

   private static String formatText(NanoVGHelper nvg, long l, String text, float f, float f2, OneConfigFont font) {
      if (text == null) {
         return "";
      }

      if (nvg.getTextWidth(l, text, f2, font) <= f) {
         return text;
      }

      if (nvg.getTextWidth(l, "...", f2, font) > f) {
         return "";
      }

      int n = 0;
      int len = text.length();

      while (n < len) {
         int n2 = n + len + 1 >>> 1;
         String text2 = text.substring(0, n2) + "...";
         if (nvg.getTextWidth(l, text2, f2, font) <= f) {
            n = n2;
         } else {
            len = n2 - 1;
         }
      }

      return text.substring(0, n) + "...";
   }
}
