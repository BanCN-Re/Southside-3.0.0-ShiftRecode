package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.utils.ColorUtils;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.ScissorHelper;
import dev.southside.utils.ScissorRect;
import dev.southside.utils.SmoothFloat;
import dev.southside.utils.WindowMetrics;
import java.awt.Color;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

public class TargetHudCard {
   public static final float DEFAULT_FLOAT = 197.5F;
   public static final float DEFAULT_FLOAT_VALUE = 68.0F;
   private static final float DEFAULT_FLOAT_DATA = 11.25F;
   private static final float DEFAULT_FLOAT_REF = 58.0F;
   private static final float DEFAULT_FLOAT_PRIMARY = 12.0F;
   private static final int DEFAULT_INT = 20;
   private static final int DEFAULT_INT_VALUE = 1090519039;
   private static final int DEFAULT_INT_DATA = -8278273;
   private static final int DEFAULT_INT_REF = -755840;
   @SerializedName("slideOffset")
   public final SmoothFloat slideOffset = new SmoothFloat(3.0F, 0.0F);
   @SerializedName("armorAnim")
   private final SmoothFloat armorAnim;
   @SerializedName("healthAnim")
   private final SmoothFloat healthAnim;
   @SerializedName("highlightAnim")
   public final SmoothFloat highlightAnim = new SmoothFloat(10.0F, 0.0F);
   @SerializedName("player")
   public AbstractClientPlayerEntity player;
   @SerializedName("skinRenderer")
   private final PlayerAvatarRenderer skinRenderer = new PlayerAvatarRenderer();

   public TargetHudCard(AbstractClientPlayerEntity player2) {
      this.player = player2;
      this.armorAnim = new SmoothFloat(3.0F, clamp(player2.getArmor(), 20.0F));
      this.healthAnim = new SmoothFloat(3.0F, clamp(player2.getHealth(), player2.getMaxHealth()));
   }

   public void draw(long l, float f, float f2, float f3) {
      float f4 = this.slideOffset.getValue();
      if (!(f4 <= -68.0F * f)) {
         float f5 = f3 + f4;
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         ScissorHelper scissor = ScissorHelper.INSTANCE;
         float f6 = 7.5F * f;
         float f7 = 197.5F * f;
         float f8 = 68.0F * f;
         nvg.drawRoundedRect(l, f2, f5, f7, f8, -1088281818, f6);
         ScissorRect clip = scissor.scissor(l, f2, f5, 4.0F * f, f8);
         if (this.player == KillAura.target) {
            this.highlightAnim.setTarget(1.0F);
            this.highlightAnim.setImmediate(1.0F);
            nvg.drawRoundedRect(l, f2, f5, f7, f8, -1, f6);
            scissor.resetScissor(l, clip);
            nvg.drawRoundedRect(l, f2 + 3.0F * f, f5, f, f8, 8.5F, 0.01F, f6, new Color(255, 255, 255, 150));
         } else {
            this.highlightAnim.setTarget(0.0F);
            int n = (int)ColorUtils.lerpFloat(0.0F, 150.0F, this.highlightAnim.getValue());
            nvg.drawRoundedRect(l, f2, f5, f7, f8, new Color(255, 255, 255, n).getRGB(), f6);
            scissor.resetScissor(l, clip);
            int n2 = (int)ColorUtils.lerpFloat(0.0F, 150.0F, this.highlightAnim.getValue());
            nvg.drawRoundedRect(l, f2 + 3.0F * f, f5, f, f8, 8.5F, 0.01F, f6, new Color(255, 255, 255, n2));
         }

         float f9 = f2 + 58.0F * f;
         float f10 = 13.0F * f;
         float textHeight = nvg.getTextHeight(l, f10, OneConfigFonts.SEMIBOLD);
         float f11 = f5 + 11.25F * f + textHeight / 2.0F;
         nvg.draw(l, this.player.getDisplayName().getString(), (int)f9, f11, -1, f10, OneConfigFonts.SEMIBOLD);
         int armor = this.player.getArmor();
         float health = this.player.getHealth();
         float maxHealth = this.player.getMaxHealth();
         this.armorAnim.setTarget(clamp(armor, 20.0F));
         this.healthAnim.setTarget(clamp(health, maxHealth));
         float f12 = 127.5F * f;
         float f13 = f11 + textHeight / 2.0F + 7.0F * f;
         performAction(nvg, l, f9, f13, f12, f, "Armor", armor + " / 20", this.armorAnim.getValue(), -8278273);
         performAction(
            nvg, l, f9, f13 + 16.0F * f, f12, f, "Health", evaluate(health) + " / " + evaluate(maxHealth), this.healthAnim.getValue(), -755840
         );
      }
   }

   private static float clamp(float x, float y) {
      return y > 0.0F ? MathHelper.clamp(x / y, 0.0F, 1.0F) : 0.0F;
   }

   private static float evaluate(float f) {
      return Math.round(f * 10.0F) / 10.0F;
   }

   private static void performAction(
      NanoVGHelper nvg, long l, float f, float f2, float f3, float f4, String text, String text2, float f5, int n
   ) {
      float f6 = 8.5F * f4;
      nvg.drawTextEx(l, text, f, f2, -2696993, f6, OneConfigFonts.REGULAR);
      float textWidth = nvg.getTextWidth(l, text2, f6, OneConfigFonts.SEMIBOLD);
      nvg.drawTextEx(l, text2, f + f3 - textWidth, f2, n, f6, OneConfigFonts.SEMIBOLD);
      float f7 = f2 + 5.5F * f4;
      float f8 = 4.0F * f4;
      performAction(nvg, l, f, f7, f3, f8, 1090519039);
      float f9 = f3 * f5;
      if (f9 > 0.0F) {
         performAction(nvg, l, f, f7, f9, f8, n);
      }
   }

   private static void performAction(NanoVGHelper nvg, long l, float f, float f2, float f3, float f4, int n) {
      NanoVG.nvgBeginPath(l);
      SquirclePath.draw(l, f, f2, f3, f4, f4 * 0.5F);
      NVGColor nvgcolor = nvg.getNVGColor(l, n);

      try {
         NanoVG.nvgFill(l);
      } catch (Throwable e) {
         if (nvgcolor != null) {
            try {
               nvgcolor.close();
            } catch (Throwable e2) {
               e.addSuppressed(e2);
            }
         }

         throw e;
      }

      if (nvgcolor != null) {
         nvgcolor.close();
      }
   }

   public void performAction(float x, float y, float z) {
      float f = 30.0F * x;
      float f2 = y + 16.0F * x;
      float f3 = z + this.slideOffset.getValue() + 11.25F * x;
      float f4 = 6.0F * x;
      ScissorHelper scissor = ScissorHelper.INSTANCE;
      NanoVGHelper.INSTANCE.performAction(true, l -> {
         ScissorRect clip = scissor.scissor(l, y - 5.0F * x, z, WindowMetrics.getScaledWidth(), WindowMetrics.getScaledHeight());
         this.skinRenderer.drawImage(l, this.player, f2, f3, f, f4);
         scissor.resetScissor(l, clip);
      });
   }
}
