package dev.southside.utils;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.Nullable;

public final class HudNotification {
   private static final float DEFAULT_FLOAT = 314.0F;
   private static final float DEFAULT_FLOAT_VALUE = 5.0F;
   @SerializedName("title")
   private final String title;
   @SerializedName("message")
   private final String message;
   @SerializedName("icon")
   private final OneConfigIcon icon;
   @SerializedName("animation")
   private final UiAnimation animation;
   @SerializedName("progressBar")
   private final Callable<Float> progressBar;
   @SerializedName("action")
   private final Runnable action;
   @SerializedName("input")
   private final InputHandler input = new InputHandler();
   @SerializedName("backgroundPaint")
   private final ColorAnimation backgroundPaint = new ColorAnimation(new ColorPalette(OneConfigColors.GRAY_850, OneConfigColors.GRAY_700, OneConfigColors.GRAY_900));
   @SerializedName("titlePaint")
   private final ColorAnimation titlePaint = new ColorAnimation(new ColorPalette(OneConfigColors.WHITE_80, OneConfigColors.WHITE, OneConfigColors.WHITE));
   @SerializedName("messagePaint")
   private final ColorAnimation messagePaint = new ColorAnimation(new ColorPalette(OneConfigColors.WHITE_60, OneConfigColors.WHITE_90, OneConfigColors.WHITE_90));
   @SerializedName("hovered")
   private boolean hovered;

   public HudNotification(String text, String text2, @Nullable OneConfigIcon oneConfigIcon, float f, @Nullable Callable<Float> callable, @Nullable Runnable action2) {
      this.title = text;
      this.message = text2;
      this.icon = oneConfigIcon;
      this.animation = new ChainedAnimation(
         new EaseInOutCubicAnimation(250, 0.0F, 330.0F, false),
         callable != null ? new DummyAnimation(330.0F, () -> (Float)callable.call() >= 1.0F) : new DummyAnimation(330.0F, f),
         new EaseInOutCubicAnimation(250, 330.0F, 0.0F, false)
      );
      this.progressBar = callable;
      this.action = action2;
   }

   public float computeFloat(long l, float f, float f2, float f3) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      nvg.scale(l, f2, f2);
      this.input.performAction(f2, f2);
      float f4 = WindowMetrics.getFramebufferWidth() / f2 - this.animation.evaluate(f3);
      float f5 = this.icon == null ? f4 + 16.0F : f4 + 64.0F;
      float f6 = this.icon == null ? 268.0F : 220.0F;
      float wrappedTextBoundsEx = nvg.getWrappedTextBoundsEx(l, this.message, f6, 12.0F, 1.0F, OneConfigFonts.REGULAR);
      float f7 = this.evaluate(wrappedTextBoundsEx);
      float f8 = f - f7;
      this.hovered = this.input.isHovered(f4, f8, 314.0F, f7);
      if (this.hovered && this.input.isValid() && this.action != null) {
         this.action.run();
      }

      int n = this.backgroundPaint.computeInt(this.hovered, this.hovered && this.input.isValidEx());
      int n2 = this.titlePaint.computeInt(this.hovered, this.hovered && this.input.isValidEx());
      int n3 = this.messagePaint.computeInt(this.hovered, this.hovered && this.input.isValidEx());
      nvg.drawRoundedRect(l, f4, f8, 314.0F, f7, n, 8.0F);
      if (this.icon != null) {
         float f9 = f8 + (f7 - (this.progressBar != null ? 5.0F : 0.0F)) / 2.0F - 16.0F;
         this.icon.renderSvg(l, f4 + 16.0F, f9, 32.0F, 32.0F, n2, f2);
      }

      nvg.drawTextEx(l, this.title, f5, f8 + 30.0F, n2, 16.0F, OneConfigFonts.SEMIBOLD);
      nvg.drawWrappedText(l, this.message, f5, f8 + 46.0F, f6, n3, 12.0F, 1.0F, OneConfigFonts.REGULAR);
      if (this.progressBar != null) {
         this.draw(l, nvg, f4, f8, f7);
      }

      nvg.resetTransform(l);
      return f7;
   }

   private void draw(long l, NanoVGHelper nvg, float f, float f2, float f3) {
      try {
         ScissorHelper scissor = ScissorHelper.INSTANCE;
         float f4 = MathClampUtils.clamp01(this.progressBar.call());
         ScissorRect clip = scissor.scissor(l, f + 314.0F * f4, f2 + f3 - 5.0F, 314.0F * (1.0F - f4), 5.0F);
         int n = IMinecraft.mc.world != null ? OneConfigColors.GRAY_400 : OneConfigColors.PRIMARY_700;
         nvg.drawRoundedRect(l, f, f2, 314.0F, f3, n, 8.0F);
         scissor.resetScissor(l, clip);
         float f5 = 314.0F * f4;
         float f6 = !(f5 < 2.5F) && !(311.5F * f4 > 2.5F) ? 2.5F : 0.0F;
         ScissorRect clip2 = scissor.scissor(l, f, f2 + f3 - 5.0F, f5 - f6, 5.0F);
         int n2 = IMinecraft.mc.world != null ? OneConfigColors.WHITE : OneConfigColors.PRIMARY_400;
         nvg.drawRoundedRect(l, f, f2, 314.0F, f3, n2, 8.0F);
         scissor.resetScissor(l, clip2);
         if (f5 >= 2.5F && 311.5F * f4 - 2.5F <= 0.0F) {
            nvg.drawRoundedRect(l, f + 2.5F, f2 + f3 - 5.0F, Math.max(0.0F, f5 - 5.0F), 5.0F, OneConfigColors.PRIMARY_400, 2.5F);
         }
      } catch (Exception e) {
      }
   }

   private float evaluate(float f) {
      float f2 = 68.0F + f;
      if (this.progressBar != null) {
         f2 += 5.0F;
      }

      return f2;
   }

   public boolean isValid() {
      return this.animation.isValid();
   }
}
