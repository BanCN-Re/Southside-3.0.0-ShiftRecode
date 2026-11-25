package dev.southside.nanovg;

import com.google.gson.annotations.SerializedName;
import dev.southside.render.NanoVGContext;
import dev.southside.utils.FontResourceLoader;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryUtil;

public class NanoFontRenderer {
   @SerializedName("fontKey")
   public final String fontKey;
   @SerializedName("fontHandle")
   public int fontHandle;
   @SerializedName("fontSize")
   public float fontSize;

   public void applyText(String text, float f, float f2, float f3, Color color) {
      NanoVG.nvgTextAlign(NanoVGContext.timestamp, 9);
      NVGColor nvgColor = NVGColor.calloc();
      nvgColor.r(color.getRed() / 255.0F).g(color.getGreen() / 255.0F).b(color.getBlue() / 255.0F).a(color.getAlpha() / 255.0F);
      NanoVG.nvgFillColor(NanoVGContext.timestamp, nvgColor);
      nvgColor.free();
      this.drawText(text, f, f2 + 1.0F, f3);
   }

   public void applyText(String text, float f, float f2, float f3, float f4, int n, Color color) {
      NanoVG.nvgTextAlign(NanoVGContext.timestamp, n);
      NVGColor nvgColor = NVGColor.calloc();
      nvgColor.r(color.getRed() / 255.0F).g(color.getGreen() / 255.0F).b(color.getBlue() / 255.0F).a(color.getAlpha() / 255.0F);
      NanoVG.nvgFillColor(NanoVGContext.timestamp, nvgColor);
      nvgColor.free();
      NanoVG.nvgFontBlur(NanoVGContext.timestamp, f4);
      this.drawText(text, f, f2 + 1.0F, f3);
   }

   public void drawGradientRect(
      String text, float f, float f2, float f3, float f4, float f5, float f6, float f7, int n, Color color, Color color2
   ) {
      NanoVG.nvgTextAlign(NanoVGContext.timestamp, n);
      float textWidth = this.getTextWidth(text, f7);
      f5 = f + textWidth;
      f4 = f2 - f7 / 2.0F;
      Color white = Color.WHITE;
      Color yellow = Color.YELLOW;
      long timestamp = NanoVGContext.timestamp;
      NVGPaint paint = NVGPaint.calloc();

      try {
         NVGColor nvgColor = NVGColor.calloc();

         try {
            NVGColor nvgColor2 = NVGColor.calloc();
            nvgColor2.r(white.getRed() / 255.0F).g(white.getGreen() / 255.0F).b(white.getBlue() / 255.0F).a(white.getAlpha() / 255.0F);
            nvgColor.r(yellow.getRed() / 255.0F).g(yellow.getGreen() / 255.0F).b(yellow.getBlue() / 255.0F).a(yellow.getAlpha() / 255.0F);
            NanoVG.nvgLinearGradient(timestamp, f, f4, f5, f4, nvgColor2, nvgColor, paint);
            NanoVG.nvgFillPaint(NanoVGContext.timestamp, paint);
            NanoVG.nvgRect(NanoVGContext.timestamp, f, f2, textWidth, f7);
            this.drawText(text, f, f2, f7);
            NanoVG.nvgFill(NanoVGContext.timestamp);
         } catch (Throwable e) {
            if (nvgColor != null) {
               try {
                  nvgColor.close();
               } catch (Throwable e2) {
                  e.addSuppressed(e2);
               }
            }

            throw e;
         }

         if (nvgColor != null) {
            nvgColor.close();
         }
      } catch (Throwable e3) {
         if (paint != null) {
            try {
               paint.close();
            } catch (Throwable e4) {
               e3.addSuppressed(e4);
            }
         }

         throw e3;
      }

      if (paint != null) {
         paint.close();
      }
   }

   public void applyText(String text, float f, float f2, Color color, boolean flag) {
      this.applyText(text, f, f2, this.fontSize, color, flag);
   }

   public void applyText(String text, float f, float f2, Color color) {
      this.applyText(text, f, f2, this.fontSize, color);
   }

   public float getTextWidth(String text, float f) {
      if (text == null) {
         return 0.0F;
      }

      NanoVG.nvgFontFaceId(NanoVGContext.timestamp, this.fontHandle);
      NanoVG.nvgFontSize(NanoVGContext.timestamp, f);
      float[] floats = new float[4];
      NanoVG.nvgTextBounds(NanoVGContext.timestamp, 0.0F, 0.0F, text, floats);
      return (floats[2] - floats[0]) / 2.0F;
   }

   public void applyText(String text, float f, float f2, float f3, float f4, int n, Color color, boolean flag) {
      if (flag) {
         this.applyText(text, f + 0.5F, f2 + 0.5F, f3, f4, n, new Color(20, 20, 20, color.getAlpha()));
      }

      this.applyText(text, f, f2, f3, f4, n, color);
   }

   public String getFontKey() {
      return this.fontKey;
   }

   public float getTextWidth() {
      NanoVG.nvgFontFaceId(NanoVGContext.timestamp, this.fontHandle);
      NanoVG.nvgFontSize(NanoVGContext.timestamp, this.fontSize);
      float[] floats = new float[4];
      NanoVG.nvgTextBounds(NanoVGContext.timestamp, 0.0F, 0.0F, "AaBbCcDdEeFfGgJjYy", floats);
      return (floats[3] - floats[1]) / 2.0F;
   }

   public String formatText(String text, float f, float f2, boolean flag, boolean flag2) {
      if (text == null) {
         return "";
      }

      StringBuilder sb = new StringBuilder();
      int n = flag ? text.length() - 1 : 0;
      byte b;
      if (flag) {
         b = -1;
      } else {
         b = 1;
      }

      byte b2 = b;
      Serializable serializable = "";

      for (int i = n; i <= text.length() - 1 && i >= 0 && this.getTextWidth(sb.toString() + serializable, f2) <= f; i += b2) {
         sb.append(text.charAt(i));
         String text2;
         if (flag) {
            if (i == 0) {
               text2 = "";
            } else {
               text2 = String.valueOf(text.charAt(i + b2));
            }
         } else if (i == text.length() - 1) {
            text2 = "";
         } else {
            text2 = String.valueOf(text.charAt(i + b2));
         }

         serializable = text2;
      }

      if (flag) {
         sb.reverse();
      }

      String text3 = sb.toString();
      if (flag2 && !text.equals(text3)) {
         String text4 = flag ? "..." + text3 : text3 + "...";
         text3 = text4;
      }

      return text3;
   }

   public void applyTextEx(String text, float f, float f2, float f3, Color color) {
      this.applyText(text, f, f2, f3, color);
   }

   public void applyTextAlt(String text, float f, float f2, float f3, Color color) {
      float f4 = f - this.getTextWidth(text, f3) / 2.0F;
      float f5 = f2 - this.getTextWidth(f3);
      int rgb = color.getRGB();
      rgb = (rgb & 16579836) >> 2 | rgb & 0xFF000000;
      this.applyText(text, f4 + 0.5F, f5 + 0.5F, f3, new Color(rgb));
      this.applyText(text, f4, f5, f3, color);
   }

   public void applyTextEx(String text, float f, float f2, Color color, boolean flag) {
      this.applyText(text, f, f2, this.fontSize, color, flag);
   }

   public float computeFloat(String text, float f, int n, float f2, float f3) {
      float f4 = 0.0F;

      for (int i = 1; i <= n; i++) {
         String text2 = this.formatText(text, f, f3, false, i == n);
         text = text.replace(text2.replace("...", ""), "");
         float f5 = !text2.isEmpty() ? this.getTextWidthEx(text2, f3) + f2 : 0.0F;
         f4 += f5;
      }

      return f4;
   }

   public String formatText(String text, float f, float f2) {
      return this.formatText(text, f, f2, false, false);
   }

   public void applyTextEx(String text, float f, float f2, Color color) {
      this.applyText(text, f, f2, this.fontSize, color);
   }

   public void applyText(String text, float f, float f2, float f3, int n, Color color, Color color2) {
      this.drawGradientRect(text, f, f2, f, f2, f + this.getTextWidth(text, f3), f2 + this.getTextWidthEx(text, f3), f3, n, color, color2);
   }

   public void drawText(String text, float f, float f2, float f3) {
      NanoVG.nvgFontFaceId(NanoVGContext.timestamp, this.fontHandle);
      NanoVG.nvgFontSize(NanoVGContext.timestamp, f3 / 2.0F);
      NanoVG.nvgText(NanoVGContext.timestamp, f, f2, text);
   }

   public void applyTextImpl(String text, float f, float f2, float f3, Color color) {
      this.applyText(text, f, f2, f3, 3.0F, color, color);
   }

   public float computeFloat(String text, float f, float f2, float f3, int n, float f4, float f5, Color color) {
      float f6 = f2;

      for (int i = 1; i <= n; i++) {
         String text2 = this.formatText(text, f3, f5, false, i == n);
         text = text.replace(text2.replace("...", ""), "");
         this.applyText(text2, f, f6, f5, color);
         float f7 = !text2.isEmpty() ? this.getTextWidthEx(text2, f5) + f4 : 0.0F;
         f6 += f7;
      }

      return f6 - f2;
   }

   public void textLetterSpacing(float f) {
      NanoVG.nvgTextLetterSpacing(NanoVGContext.timestamp, f);
   }

   public void drawTrimBlurString(String text, float f, float f2, float f3, int n, float f4, float f5, float f6, int n2, Color color) {
      float f7 = f2;

      for (int i = 1; i <= n; i++) {
         float f8 = f3 + 2.0F;
         String text2 = this.formatText(text, f8, f5, false, i == n);
         text = text.replace(text2.replace("...", ""), "");
         this.applyText(text2, f, f7, f5, f6, n2, color);
         float f9 = !text2.isEmpty() ? this.getTextWidthEx(text2, f5) + f4 : 0.0F;
         f7 += f9;
      }
   }

   public void applyText(String text, float f, float f2, float f3, float f4, Color color) {
      this.applyText(text, f, f2, f3, f4, color, color);
   }

   public void applyTextDirect(String text, float f, float f2, float f3, Color color) {
      this.applyText(text, f, f2, f3, 3.0F, color, color);
   }

   public String formatText(String text, float f, boolean flag) {
      return this.formatText(text, f, this.fontSize, flag, false);
   }

   public float computeFloat(String text) {
      return this.getTextWidth(text, this.fontSize);
   }

   public void applyTextSafe(String text, float f, float f2, float f3, Color color) {
      this.applyText(text, f, f2, f3, color);
   }

   public void applyTextAlt(String text, float f, float f2, Color color) {
      float f3 = f - this.getTextWidth(text, this.fontSize) / 2.0F;
      float f4 = f2 - this.getTextWidth(this.fontSize);
      float fontSize2 = this.fontSize;
      int rgb = color.getRGB();
      rgb = (rgb & 16579836) >> 2 | rgb & 0xFF000000;
      this.applyText(text, f3 + 0.5F, f4 + 0.5F, fontSize2, new Color(rgb));
      this.applyText(text, f3, f4, fontSize2, color);
   }

   public void setFontSize(float f) {
      this.fontSize = f;
   }

   public void applyText(String text, float f, float f2, float f3, Color color, Color color2) {
      this.applyText(text, f, f2, f3, 3.0F, color, color2);
   }

   public NanoFontRenderer(String text, String text2) {
      this.fontKey = text;

      try (InputStream inputStream = FontResourceLoader.getInputStream(text2 + ".ttf")) {
         byte[] bytes = inputStream.readAllBytes();
         ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
         buffer.put(bytes).flip();
         this.fontHandle = NanoVG.nvgCreateFontMem(NanoVGContext.timestamp, text, buffer, false);
         this.fontSize = 16.0F;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public void applyText(String text, float f, float f2, float f3, Color color, boolean flag) {
      if (flag) {
         this.applyText(text, f + 0.5F, f2 + 0.5F, f3, new Color(20, 20, 20, color.getAlpha()));
      }

      this.applyText(text, f, f2, f3, color);
   }

   public void applyTextAlt(String text, float f, float f2, Color color, boolean flag) {
      int rgb = color.getRGB();
      if (flag) {
         rgb = (rgb & 16579836) >> 2 | rgb & 0xFF000000;
      }

      this.applyText(text, f + 0.5F, f2 + 0.5F, this.fontSize, new Color(rgb));
      this.applyTextImpl(text, f, f2, color);
   }

   public void applyText(String text, float f, float f2, float f3, float f4, float f5, float f6, float f7, int n, Color color, Color color2) {
      this.drawGradientRect(text, f, f2, f3, f4, f5, f6, f7, n, color, color2);
   }

   public float getTextWidth(float f) {
      NanoVG.nvgFontFaceId(NanoVGContext.timestamp, this.fontHandle);
      NanoVG.nvgFontSize(NanoVGContext.timestamp, f);
      float[] floats = new float[4];
      NanoVG.nvgTextBounds(NanoVGContext.timestamp, 0.0F, 0.0F, "AaBbCcDdEeFfGgJjYy", floats);
      return (floats[3] - floats[1]) / 2.0F;
   }

   public String formatText(String text, float f) {
      return this.formatText(text, f, this.fontSize, false, false);
   }

   public void applyTextEx(String text, float f, float f2, float f3, Color color, Color color2) {
      this.applyText(text, f, f2, f3, 3.0F, color, color2);
   }

   public float getTextWidthEx(String text, float f) {
      if (text == null) {
         return 0.0F;
      }

      if (text.isEmpty()) {
         return this.getTextWidth(f);
      }

      NanoVG.nvgFontFaceId(NanoVGContext.timestamp, this.fontHandle);
      NanoVG.nvgFontSize(NanoVGContext.timestamp, f);
      float[] floats = new float[4];
      NanoVG.nvgTextBounds(NanoVGContext.timestamp, 0.0F, 0.0F, text, floats);
      return (floats[3] - floats[1]) / 2.0F;
   }

   public void applyText(String text, float f, float f2, float f3, float f4, Color color, Color color2) {
      NanoVG.nvgTextAlign(NanoVGContext.timestamp, 9);
      NVGColor nvgColor = NVGColor.calloc();
      nvgColor.r(color2.getRed() / 255.0F).g(color2.getGreen() / 255.0F).b(color2.getBlue() / 255.0F).a(color2.getAlpha() / 255.0F);
      NanoVG.nvgFillColor(NanoVGContext.timestamp, nvgColor);
      nvgColor.free();
      NanoVG.nvgFontBlur(NanoVGContext.timestamp, f4);
      this.drawText(text, f, f2 + 1.0F, f3);
      nvgColor = NVGColor.calloc();
      nvgColor.r(color.getRed() / 255.0F).g(color.getGreen() / 255.0F).b(color.getBlue() / 255.0F).a(color.getAlpha() / 255.0F);
      NanoVG.nvgFillColor(NanoVGContext.timestamp, nvgColor);
      nvgColor.free();
      NanoVG.nvgFontBlur(NanoVGContext.timestamp, 0.0F);
      this.drawText(text, f, f2 + 1.0F, f3);
   }

   public void applyTextImpl(String text, float f, float f2, Color color) {
      this.applyText(text, f, f2, this.fontSize, 3.0F, color, color);
   }
}
