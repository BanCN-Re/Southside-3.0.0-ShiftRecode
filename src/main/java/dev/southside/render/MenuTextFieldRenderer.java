package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.mixin.misc.MixinTextFieldWidget;
import dev.southside.utils.OneConfigColors;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.IImeTextTarget;
import dev.southside.utils.NanoVGHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Util;
import org.lwjgl.nanovg.NanoVG;

public final class MenuTextFieldRenderer {
   @SerializedName("advances")
   private float[] advances = new float[]{0.0F};
   @SerializedName("measuredText")
   private String measuredText;
   @SerializedName("offset")
   private float offset;

   public void render(DrawContext context, TextFieldWidget textField) {
      MenuOverlayRenderer.render(
         context,
         l -> {
            NanoVGHelper nvg = NanoVGHelper.INSTANCE;
            String text = textField.getText();
            if (!text.equals(this.measuredText)) {
               this.measuredText = text;
               this.advances = new float[text.length() + 1];
               int n = 0;

               while (n < text.length()) {
                  int n2 = text.offsetByCodePoints(n, 1);
                  float textWidth = nvg.getTextWidth(l, text.substring(0, n2), 13.0F, OneConfigFonts.WQY_MICROHEI);

                  for (int i = n + 1; i <= n2; i++) {
                     this.advances[i] = textWidth;
                  }

                  n = n2;
               }
            }

            int n3 = Math.min(textField.getCursor(), text.length());
            int n4 = Math.min(((MixinTextFieldWidget)textField).selectionEnd(), text.length());
            float f = textField.getWidth() - 20;
            if (this.advances[n3] - this.offset > f) {
               this.offset = this.advances[n3] - f;
            }

            if (this.advances[n3] < this.offset) {
               this.offset = this.advances[n3];
            }

            this.offset = Math.max(0.0F, Math.min(this.offset, Math.max(0.0F, this.advances[text.length()] - f)));
            float x = textField.getX();
            float y = textField.getY();
            float height = textField.getHeight();
            nvg.drawRoundedRect(l, x, y, textField.getWidth(), height, OneConfigColors.GRAY_850_95, 7.0F);
            if (textField.isFocused()) {
               nvg.drawRoundedRect(l, x, y, textField.getWidth(), height, OneConfigColors.PRIMARY_400, 7.0F, 1.0F);
            }

            NanoVG.nvgIntersectScissor(l, x + 8.0F, y + 2.0F, textField.getWidth() - 16, height - 4.0F);
            float f2 = x + 10.0F - this.offset;
            if (textField.isFocused() && n3 != n4) {
               nvg.drawRect(
                  l,
                  f2 + Math.min(this.advances[n3], this.advances[n4]),
                  y + 7.0F,
                  Math.abs(this.advances[n3] - this.advances[n4]),
                  height - 14.0F,
                  OneConfigColors.PRIMARY_500
               );
            }

            nvg.drawTextEx(l, text, f2, y + height / 2.0F, OneConfigColors.WHITE_90, 13.0F, OneConfigFonts.WQY_MICROHEI);
            if (textField.isFocused() && ((MixinTextFieldWidget)textField).editable()) {
               float f3 = f2 + this.advances[n3];
               if (Util.getMeasuringTimeMs() / 500L % 2L == 0L) {
                  nvg.drawRect(l, f3, y + 8.0F, 1.0F, height - 16.0F, OneConfigColors.WHITE);
               }

               ImeOverlayManager.INSTANCE.performAction((IImeTextTarget)textField, context, f3, y + 6.0F, f3 + 1.0F, y + height - 6.0F);
            }
         }
      );
   }

   public void performAction(TextFieldWidget textField, double d, boolean flag) {
      double d2 = d - textField.getX() - 10.0 + this.offset;
      int n = 0;
      String text = textField.getText();

      while (n < text.length()) {
         int n2 = text.offsetByCodePoints(n, 1);
         if (n2 >= this.advances.length || d2 < (this.advances[n] + this.advances[n2]) * 0.5) {
            break;
         }

         n = n2;
      }

      textField.setCursor(n, flag || Screen.hasShiftDown());
   }
}
