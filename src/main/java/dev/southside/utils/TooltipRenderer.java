package dev.southside.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TooltipRenderer {
   private static final NanoVGHelper NANO_VGHELPER = NanoVGHelper.INSTANCE;

   public static int drawAndGetHeight(
      long l,
      int n,
      int n2,
      @NotNull String text,
      @Nullable String text2,
      @NotNull Supplier<UiAnimation> supplier,
      @NotNull Consumer<UiAnimation> consumer,
      @Nullable SvgAsset svg,
      boolean flag,
      @NotNull TooltipRenderer.Mode mode,
      @NotNull InputHandler input
   ) {
      UiAnimation uiAnimation = (UiAnimation)supplier.get();
      EaseOutQuadAnimation easeOutQuadAnimation = null;
      if (uiAnimation.getProgressEx() != 1.0F && flag) {
         easeOutQuadAnimation = new EaseOutQuadAnimation(150, uiAnimation.evaluate(0.0F), 1.0F, false);
      } else if (uiAnimation.getProgressEx() != 0.0F && !flag) {
         easeOutQuadAnimation = new EaseOutQuadAnimation(150, uiAnimation.evaluate(0.0F), 0.0F, false);
      }

      if (easeOutQuadAnimation != null) {
         uiAnimation = easeOutQuadAnimation;
         consumer.accept(easeOutQuadAnimation);
      }

      if (!flag && uiAnimation.isValid()) {
         return -1;
      }

      float[] wrappedTextBounds = NANO_VGHELPER.getWrappedTextBounds(l, text, 400.0F, 16.0F, 1.0F, OneConfigFonts.ONE_CONFIG_FONT);
      float[] floats = new float[0];
      float f;
      float f2;
      if (text2 == null) {
         f = wrappedTextBounds[3] - wrappedTextBounds[1];
         f2 = wrappedTextBounds[2] - wrappedTextBounds[0];
      } else {
         floats = NANO_VGHELPER.getWrappedTextBounds(l, text2, 400.0F, 16.0F, 1.0F, OneConfigFonts.ONE_CONFIG_FONT);
         f = wrappedTextBounds[3] - wrappedTextBounds[1] + (floats[3] - floats[1]);
         f2 = Math.max(wrappedTextBounds[2] - wrappedTextBounds[0], floats[2] - floats[0]);
      }

      NANO_VGHELPER.setAlpha(l, uiAnimation.getProgress());
      switch (mode) {
         case MIDDLE:
            NANO_VGHELPER.translate(l, -(f2 / 2.0F + 34.0F), 0.0F);
            break;
         case RIGHT:
            NANO_VGHELPER.translate(l, -(f2 + 68.0F), 0.0F);
      }

      int n3;
      if (text2 != null) {
         n3 = (int)(91.0F + (f - 32.0F));
         NANO_VGHELPER.drawRoundedRect(l, n - 1.0F, n2 - 42.0F - 47.0F - (f - 32.0F), f2 + 70.0F, n3, OneConfigColors.GRAY_700, 8.0F);
         NANO_VGHELPER.drawRoundedRect(l, n - 1.0F, n2 - 42.0F - 47.0F - (f - 32.0F), f2 + 70.0F, n3, ArgbColorUtils.rgba(204, 204, 204, 25), 8.0F);
         NANO_VGHELPER.drawRoundedRect(l, n, n2 - 42.0F - 46.0F - (f - 32.0F), f2 + 68.0F, n3 - 2.0F, OneConfigColors.GRAY_700, 8.0F);
         NANO_VGHELPER.draw(
            l, OneConfigSvgs.WARNING, n + 16, n2 - 30.0F - (floats[3] - floats[1] - 16.0F), 20.0F, 20.0F, ArgbColorUtils.rgb(223, 51, 39)
         );
         NANO_VGHELPER.drawWrappedText(
            l,
            text2,
            n + 52,
            n2 - 19.0F - (floats[3] - floats[1] - 16.0F),
            400.0F,
            ArgbColorUtils.rgb(223, 51, 39),
            16.0F,
            1.0F,
            OneConfigFonts.ONE_CONFIG_FONT
         );
         NANO_VGHELPER.drawLine(
            l,
            n + 16.0F,
            n2 - 42.0F - 47.0F - (f - 32.0F) + (91.0F + (f - 32.0F)) / 2.0F,
            n + f2 + 68.0F - 16.0F,
            n2 - 42.0F - 47.0F - (f - 32.0F) + (91.0F + (f - 32.0F)) / 2.0F,
            1.0F,
            ArgbColorUtils.rgba(204, 204, 204, 25)
         );
         NANO_VGHELPER.draw(l, OneConfigSvgs.INFO_ARROW, n + 16, n2 - 14.0F - 45.0F - (f - 16.0F), 20.0F, 20.0F, OneConfigColors.WHITE_80);
         NANO_VGHELPER.drawWrappedText(
            l, text, n + 52, n2 - 3.0F - 45.0F - (f - 16.0F), 400.0F, OneConfigColors.WHITE_80, 16.0F, 1.0F, OneConfigFonts.ONE_CONFIG_FONT
         );
      } else {
         n3 = (int)(46.0F + (f - 16.0F));
         NANO_VGHELPER.drawRoundedRect(l, n - 1.0F, n2 - 42.0F - 1.0F - (f - 16.0F), f2 + 70.0F, n3, OneConfigColors.GRAY_700, 8.0F);
         NANO_VGHELPER.drawRoundedRect(l, n - 1.0F, n2 - 42.0F - 1.0F - (f - 16.0F), f2 + 70.0F, n3, ArgbColorUtils.rgba(204, 204, 204, 25), 8.0F);
         NANO_VGHELPER.drawRoundedRect(l, n, n2 - 42.0F - (f - 16.0F), f2 + 68.0F, n3 - 2.0F, OneConfigColors.GRAY_700, 8.0F);
         NANO_VGHELPER.draw(
            l, svg == null ? OneConfigSvgs.INFO_ARROW : svg, n + 16, n2 - 30.0F - (f - 16.0F), 20.0F, 20.0F, OneConfigColors.WHITE_80
         );
         NANO_VGHELPER.drawWrappedText(l, text, n + 52, n2 - 19 - (f - 16.0F), 400.0F, OneConfigColors.WHITE_80, 16.0F, 1.0F, OneConfigFonts.ONE_CONFIG_FONT);
      }

      switch (mode) {
         case MIDDLE:
            NANO_VGHELPER.translate(l, f2 / 2.0F + 34.0F, 0.0F);
            break;
         case RIGHT:
            NANO_VGHELPER.translate(l, f2 + 68.0F, 0.0F);
      }

      NANO_VGHELPER.setAlpha(l, 1.0F);
      return n3;
   }

   public enum Mode {
      LEFT,
      MIDDLE,
      RIGHT;

      private static String[] names;

      // $VF: synthetic method
      private static TooltipRenderer.Mode[] createValues() {
         return new TooltipRenderer.Mode[]{LEFT, MIDDLE, RIGHT};
      }


      public static void setNames(String[] textArray) {
         names = textArray;
      }

   }
}
