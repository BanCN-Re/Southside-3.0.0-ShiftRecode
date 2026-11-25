package dev.southside.render;

import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.utils.IOUtils;
import cc.polyfrost.oneconfig.utils.NetworkUtils;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.PhaseEvent;
import dev.southside.utils.InputHandler;
import dev.southside.utils.OneConfigEventManager;
import dev.southside.utils.OneConfigFont;
import dev.southside.utils.SvgAsset;
import dev.southside.utils.ImageAsset;
import dev.southside.utils.ColorUtils;
import dev.southside.utils.ArgbColorUtils;
import dev.southside.utils.OneConfigColors;
import dev.southside.utils.MinecraftColorTable;
import dev.southside.utils.OneConfigSvgs;
import dev.southside.utils.WindowMetrics;
import dev.southside.utils.OneConfigTextRenderer;
import dev.southside.utils.OneConfigPlatform;
import dev.southside.utils.AssetHelper;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.OneConfigGui;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.function.LongConsumer;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL11C;

public final class NanoVGHelperImpl implements NanoVGHelper {
   @SerializedName("nvgContext")
   public long nvgContext = NanoVGContext.timestamp;
   public static int[] values;
   public static int[] valuesValue = new int[]{0};
   @SerializedName("nvgFrameActive")
   public boolean nvgFrameActive = false;
   @SerializedName("framePendingClose")
   public boolean framePendingClose = false;
   @SerializedName("globalAlpha")
   public float globalAlpha = 1.0F;

   public NanoVGHelperImpl() {
      OneConfigEventManager.INSTANCE.register(new NanoVGHelperImpl.Inner());
   }

   @Override
   public void performAction(LongConsumer callback) {
      this.performAction(false, callback);
   }

   @Override
   public void performAction(boolean flag, LongConsumer callback) {
      this.runTask(() -> this.performActionEx(flag, callback));
   }

   private void runTask(Runnable action) {
      this.nvgFrameActive = true;
      if (this.nvgContext == -1L) {
         this.nvgContext = NanoVGGL3.nvgCreate(1);
         if (this.nvgContext == -1L) {
            throw new RuntimeException("Failed to create nvg context");
         }
      }

      try {
         OneConfigPlatform.getGLPlatform().runTask(action);
      } catch (Exception e) {
         e.printStackTrace();
      }

      this.performAction();
   }

   @Override
   public void performAction(float f, LongConsumer callback) {
      this.runTask(() -> this.performActionEx(f, callback));
   }

   public void performActionEx(boolean flag, LongConsumer callback) {
      if (flag) {
         this.performAction(WindowMetrics.getScaledWidth(), WindowMetrics.getScaledHeight(), WindowMetrics.getScaleFactor(), callback);
      } else {
         this.performAction(WindowMetrics.getFramebufferWidth(), WindowMetrics.getFramebufferHeight(), 1.0F, callback);
      }
   }

   private void performAction(float f, float f2, float f3, LongConsumer callback) {
      long nvgContext2 = this.nvgContext;
      GlStateStack.pushState();

      try {
         NanoVG.nvgSave(nvgContext2);

         try {
            NanoVG.nvgBeginFrame(nvgContext2, f, f2, f3);

            try {
               callback.accept(nvgContext2);
            } finally {
               NanoVG.nvgEndFrame(nvgContext2);
            }
         } finally {
            NanoVG.nvgRestore(nvgContext2);
         }
      } finally {
         GlStateStack.popState();
      }
   }

   public void performActionEx(float f, LongConsumer callback) {
      this.performAction(WindowMetrics.getFramebufferWidth() / f, WindowMetrics.getFramebufferHeight() / f, f, callback);
   }

   private void performAction() {
      if (values != null) {
         int n = values[2] * values[3];
         valuesValue = new int[n];
         ByteBuffer buffer = BufferUtils.createByteBuffer(values[2] * values[3] * 4);
         GL11C.glReadPixels(values[0], values[1], values[2], values[3], 6408, 5121, buffer);

         for (int i = 0; i < n; i++) {
            int[] values2 = valuesValue;
            byte b = buffer.get();
            byte b2 = buffer.get();
            byte b3 = buffer.get();
            byte b4 = buffer.get();
            values2[i] = ArgbColorUtils.rgbaBytes(b, b2, b3, b4);
         }

         values = null;
      }
   }

   @Override
   public void drawRect(long vg, float x, float y, float width, float height, int color) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRect(vg, x, y, width, height);
      NVGColor nvgcolor = this.getNVGColor(vg, color);
      NanoVG.nvgFill(vg);
      nvgcolor.free();
   }

   @Override
   public void draw(long vg, double x, double y, double width, double height, int color) {
      this.drawRect(vg, (float)x, (float)y, (float)width, (float)height, color);
   }

   @Override
   public void drawRoundedRect(long vg, float x, float y, float width, float height, int color, float radius) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, width, height, radius);
      NVGColor nvgcolor = this.getNVGColor(vg, color);
      NanoVG.nvgFill(vg);
      nvgcolor.free();
   }

   @Override
   public void draw(long vg, float x, float y, float width, float height, int color, float radius) {
      this.draw(vg, x, y, width, height, radius, radius, radius, radius, color);
   }

   @Override
   public void drawRoundedRectVaried(long vg, float x, float y, float width, float height, int color, float radius, float f, float f2, float f3) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRectVarying(vg, x, y, width, height, radius, f, f2, f3);
      NVGColor nvgcolor = this.getNVGColor(vg, color);
      NanoVG.nvgFill(vg);
      nvgcolor.free();
   }

   @Override
   public void draw(long vg, float x, float y, float width, float height, float f, float radius, float f2, float f3, int n) {
      NVGColor nvgcolor = NanoVGHelper.INSTANCE.getNVGColor(vg, n);

      try {
         float f4 = 0.625F;
         float f5 = 1.25F;
         NanoVG.nvgBeginPath(vg);
         float f6 = Math.min(width, height) / (2.0F * f5);
         float f7 = Math.min(f, f6);
         float f8 = Math.min(radius, f6);
         float f9 = Math.min(f2, f6);
         float f10 = Math.min(f3, f6);
         if (f7 > 0.0F) {
            float f11 = f7 * f5;
            NanoVG.nvgMoveTo(vg, x, y + f11);
            NanoVG.nvgBezierTo(vg, x, y + f11 * (1.0F - f4), x + f11 * (1.0F - f4), y, x + f11, y);
         } else {
            NanoVG.nvgMoveTo(vg, x, y);
         }

         if (f8 > 0.0F) {
            float f12 = f8 * f5;
            NanoVG.nvgLineTo(vg, x + width - f12, y);
            NanoVG.nvgBezierTo(vg, x + width - f12 * (1.0F - f4), y, x + width, y + f12 * (1.0F - f4), x + width, y + f12);
         } else {
            NanoVG.nvgLineTo(vg, x + width, y);
         }

         if (f9 > 0.0F) {
            float f13 = f9 * f5;
            NanoVG.nvgLineTo(vg, x + width, y + height - f13);
            NanoVG.nvgBezierTo(
               vg, x + width, y + height - f13 * (1.0F - f4), x + width - f13 * (1.0F - f4), y + height, x + width - f13, y + height
            );
         } else {
            NanoVG.nvgLineTo(vg, x + width, y + height);
         }

         if (f10 > 0.0F) {
            float f14 = f10 * f5;
            NanoVG.nvgLineTo(vg, x + f14, y + height);
            NanoVG.nvgBezierTo(vg, x + f14 * (1.0F - f4), y + height, x, y + height - f14 * (1.0F - f4), x, y + height - f14);
         } else {
            NanoVG.nvgLineTo(vg, x, y + height);
         }

         NanoVG.nvgClosePath(vg);
         NanoVG.nvgFillColor(vg, nvgcolor);
         NanoVG.nvgFill(vg);
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

   @Override
   public void drawRoundedRect(long vg, float x, float y, float width, float height, int color, float radius, float f) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x + f, y + f, width - f, height - f, radius);
      NanoVG.nvgStrokeWidth(vg, f + 0.5F);
      NanoVG.nvgPathWinding(vg, 2);
      NVGColor nvgcolor = this.getNVGColor(vg, color);
      NanoVG.nvgStrokeColor(vg, nvgcolor);
      NanoVG.nvgStroke(vg);
      nvgcolor.free();
   }

   @Override
   public void draw(long vg, double x, double y, double width, double height, double d, int n) {
      this.drawEx(vg, x, y, x + width, y + d, n);
      this.drawEx(vg, x, y, x + d, y + height, n);
      this.drawEx(vg, x, y + height - d, x + width, y + height, n);
      this.drawEx(vg, x + width - d, y, x + width, y + height, n);
   }

   @Override
   public void draw(long vg, float x, float y, float width, float height, float f, int n) {
      this.drawEx(vg, (double)x, (double)y, (double)(x + width), (double)(y + f), n);
      this.drawEx(vg, (double)x, (double)y, (double)(x + f), (double)(y + height), n);
      this.drawEx(vg, (double)x, (double)(y + height - f), (double)(x + width), (double)(y + height), n);
      this.drawEx(vg, (double)(x + width - f), (double)y, (double)(x + width), (double)(y + height), n);
   }

   @Override
   public void drawGradientRect(long vg, float x, float y, float width, float height, int color, int n, NanoVGHelper.Mode mode) {
      NVGPaint paint = NVGPaint.create();
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRect(vg, x, y, width, height);
      NVGColor nvgcolor = this.getNVGColor(vg, color);
      NVGColor nvgcolor2 = this.getNVGColor(vg, n);
      float[] floatList = NanoVGHelper.Mode.getFloatList(x, y, width, height, mode);
      NanoVG.nvgFillPaint(vg, NanoVG.nvgLinearGradient(vg, floatList[0], floatList[1], floatList[2], floatList[3], nvgcolor, nvgcolor2, paint));
      NanoVG.nvgFillPaint(vg, paint);
      NanoVG.nvgFill(vg);
      nvgcolor.free();
      nvgcolor2.free();
   }

   @Override
   public void drawRoundedRect(long vg, float x, float y, float width, float height, int color, int n, float f, NanoVGHelper.Mode mode) {
      NVGPaint paint = NVGPaint.create();
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, width, height, f);
      NVGColor nvgcolor = this.getNVGColor(vg, color);
      NVGColor nvgcolor2 = this.getNVGColor(vg, n);
      float[] floatList = NanoVGHelper.Mode.getFloatList(x, y, width, height, mode);
      NanoVG.nvgFillPaint(vg, NanoVG.nvgLinearGradient(vg, floatList[0], floatList[1], floatList[2], floatList[3], nvgcolor, nvgcolor2, paint));
      NanoVG.nvgFill(vg);
      nvgcolor.free();
      nvgcolor2.free();
   }

   @Override
   public void drawRoundedRect(long vg, float x, float y, float width, float height, int color) {
      this.drawRoundedRect(vg, x, y, width, height, color, 8.0F);
      NVGPaint paint = NVGPaint.create();
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, width, height, 8.0F);
      NVGColor nvgcolor = this.getNVGColor(vg, -1);
      NVGColor nvgcolor2 = this.getNVGColor(vg, 0);
      NanoVG.nvgFillPaint(vg, NanoVG.nvgLinearGradient(vg, x, y, x + width, y, nvgcolor, nvgcolor2, paint));
      NanoVG.nvgFill(vg);
      nvgcolor.free();
      nvgcolor2.free();
      NVGPaint paint2 = NVGPaint.create();
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, width, height, 8.0F);
      NVGColor nvgcolor3 = this.getNVGColor(vg, 0);
      NVGColor nvgcolor4 = this.getNVGColor(vg, -16777216);
      NanoVG.nvgFillPaint(vg, NanoVG.nvgLinearGradient(vg, x, y, x, y + height, nvgcolor3, nvgcolor4, paint2));
      NanoVG.nvgFill(vg);
      nvgcolor3.free();
      nvgcolor4.free();
   }

   @Override
   public void drawCircle(long l, float f, float f2, float f3, int n) {
      NanoVG.nvgBeginPath(l);
      NanoVG.nvgCircle(l, f, f2, f3);
      NVGColor nvgcolor = this.getNVGColor(l, n);
      NanoVG.nvgFill(l);
      nvgcolor.free();
   }

   @Override
   public void draw(long vg, float x, float y, float width, float height, int color) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgEllipse(vg, x, y, width, height);
      NVGColor nvgcolor = this.getNVGColor(vg, color);
      NanoVG.nvgFill(vg);
      nvgcolor.free();
   }

   @Override
   public void drawEx(long vg, float x, float y, float width, float height, int color, float radius) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgEllipse(vg, x, y, width, height);
      NanoVG.nvgStrokeWidth(vg, radius + 0.5F);
      NanoVG.nvgPathWinding(vg, 2);
      NVGColor nvgcolor = this.getNVGColor(vg, color);
      NanoVG.nvgStrokeColor(vg, nvgcolor);
      NanoVG.nvgStroke(vg);
      nvgcolor.free();
   }

   @Override
   public void draw(long vg, float x, float y, float width, float height, float f, float radius) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgArc(vg, x, y, width, height, f, 2);
      NanoVG.nvgStrokeWidth(vg, radius + 0.5F);
      NVGColor nvgcolor = this.getNVGColor(vg, -1);
      NanoVG.nvgStrokeColor(vg, nvgcolor);
      NanoVG.nvgStroke(vg);
      nvgcolor.free();
   }

   @Override
   public void draw(long vg, String text, float x, float y, int n, float f, OneConfigFont font) {
      float f2 = 0.0F;
      int n2 = n;

      for (int i = 0; i < text.length(); i++) {
         String text2 = String.valueOf(text.charAt(i));
         if (text2.equals("§") && i + 1 < text.length()) {
            int n3 = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
            if (n3 != -1) {
               switch (n3) {
                  case 0:
                  case 1:
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                  case 7:
                  case 8:
                  case 9:
                  case 10:
                  case 11:
                  case 12:
                  case 13:
                  case 14:
                  case 15:
                     n2 = ColorUtils.computeIntEx(MinecraftColorTable.DEFAULT_VALUES[n3], 255);
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                  case 20:
                  default:
                     break;
                  case 21:
                     n2 = n;
               }

               i++;
               continue;
            }
         }

         this.drawTextEx(vg, text2, x + f2, y, n2, f, font);
         f2 += this.getTextWidth(vg, text2, f, font);
      }
   }

   @Override
   public void drawEx(long vg, String text, float x, float y, int n, float f, OneConfigFont font) {
      float f2 = 0.0F;
      int n2 = n;

      for (int i = 0; i < text.length(); i++) {
         String text2 = String.valueOf(text.charAt(i));
         if (text2.equals("§") && i + 1 < text.length()) {
            int n3 = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
            if (n3 != -1) {
               switch (n3) {
                  case 0:
                  case 1:
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                  case 7:
                  case 8:
                  case 9:
                  case 10:
                  case 11:
                  case 12:
                  case 13:
                  case 14:
                  case 15:
                     n2 = ColorUtils.computeIntEx(MinecraftColorTable.DEFAULT_VALUES[n3], 255);
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                  case 20:
                  default:
                     break;
                  case 21:
                     n2 = n;
               }

               i++;
               continue;
            }
         }

         this.drawText(vg, text2, x + f2, y, n2, f, font);
         f2 += this.getTextWidth(vg, text2, f, font);
      }
   }

   @Override
   public void drawText(long vg, String text, float x, float y, int n, float f, OneConfigFont font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, f);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextAlign(vg, 9);
      NVGColor nvgcolor = this.getNVGColor(vg, n);
      NanoVG.nvgText(vg, x, y, text);
      nvgcolor.free();
   }

   @Override
   public void draw(long vg, String text, float x, float y, float f, int n, float f2, OneConfigFont font) {
      if (text != null && !text.isEmpty() && !(f <= 0.0F)) {
         String text2 = text;
         if (this.getTextWidth(vg, text2, f2, font) > f) {
            while (!text2.isEmpty() && this.getTextWidth(vg, text2 + "...", f2, font) > f) {
               text2 = text2.substring(0, text2.offsetByCodePoints(text2.length(), -1));
            }

            text2 = text2 + "...";
         }

         this.drawTextEx(vg, text2, x, y, n, f2, font);
      }
   }

   @Override
   public void draw(long vg, float x, float y, float width, float height) {
      float f = (float)(System.nanoTime() % 1000000000L) / 1.0E9F * 6.2832F;
      this.draw(vg, x, y, width, f, f + 4.7124F, height);
   }

   @Override
   public void drawTextEx(long vg, String text, float x, float y, int n, float f, OneConfigFont font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, f);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextAlign(vg, 17);
      NVGColor nvgcolor = this.getNVGColor(vg, n);
      NanoVG.nvgText(vg, x, y, text);
      nvgcolor.free();
   }

   @Override
   public void drawTextAlt(long vg, String text, float x, float y, int n, float f, OneConfigFont font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, f);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextAlign(vg, 18);
      NVGColor nvgcolor = this.getNVGColor(vg, n);
      NanoVG.nvgText(vg, x, y, text);
      nvgcolor.free();
   }

   @Override
   public void drawWrappedText(long vg, String text, float x, float y, float f, int n, float f2, OneConfigFont font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, f2);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextAlign(vg, 17);
      NVGColor nvgcolor = this.getNVGColor(vg, n);
      NanoVG.nvgTextBox(vg, x, y, f, text);
      nvgcolor.free();
   }

   @Override
   public void drawWrappedText(long vg, String text, float x, float y, float f, int n, float f2, float f3, OneConfigFont font) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgFontSize(vg, f2);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextLineHeight(vg, f3);
      NanoVG.nvgTextAlign(vg, 17);
      NVGColor nvgcolor = this.getNVGColor(vg, n);
      NanoVG.nvgTextBox(vg, x, y, f, text);
      nvgcolor.free();
   }

   @Override
   public float[] getWrappedTextBounds(long vg, String text, float x, float y, OneConfigFont font) {
      float[] floats = new float[4];
      NanoVG.nvgFontSize(vg, y);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, x, text, floats);
      return floats;
   }

   @Override
   public float[] getWrappedTextBounds(long vg, String text, float x, float y, float f, OneConfigFont font) {
      float[] floats = new float[4];
      NanoVG.nvgFontSize(vg, y);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextLineHeight(vg, f);
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, x, text, floats);
      return floats;
   }

   @Override
   public float getWrappedTextBoundsEx(long vg, String text, float x, float y, float f, OneConfigFont font) {
      float[] floats = new float[4];
      NanoVG.nvgFontSize(vg, y);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextLineHeight(vg, f);
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, x, text, floats);
      return floats[3] - floats[1];
   }

   @Override
   public float getWrappedTextBoundsEx(long vg, String text, float x, float y, OneConfigFont font) {
      float[] floats = new float[4];
      NanoVG.nvgFontSize(vg, y);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, x, text, floats);
      return floats[2] - floats[0];
   }

   @Override
   public float getWrappedTextBoundsAlt(long vg, String text, float x, float y, float f, OneConfigFont font) {
      float[] floats = new float[4];
      NanoVG.nvgFontSize(vg, y);
      NanoVG.nvgFontFace(vg, font.getName());
      NanoVG.nvgTextLineHeight(vg, f);
      NanoVG.nvgTextBoxBounds(vg, 0.0F, 0.0F, x, text, floats);
      return floats[2] - floats[0];
   }

   @Override
   public void draw(long l, float f, float f2, float f3, OneConfigFont font, InputHandler input) {
      String text = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
      this.drawTextEx(l, text, f, f2, -15112193, f3, font);
      float textWidth = this.getTextWidth(l, text, f3, font);
      this.drawRect(l, f, f2 + f3 / 2.0F, textWidth, 1.0F, -15112193);
      float f4 = (int)(f - 2.0F);
      float f5 = (int)(f2 - 1.0F);
      float f6 = (int)(textWidth + 4.0F);
      float f7 = (int)(f3 / 2.0F + 3.0F);
      if (input.isHoveredEx(f4, f5, f6, f7)) {
         NetworkUtils.applyText("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
      }
   }

   @Deprecated
   @Override
   public void draw(long vg, String text, float x, float y, float f, float f2) {
      this.drawImage(vg, text, x, y, f, f2, IOUtils.class);
   }

   @Override
   public void drawImage(long vg, String text, float x, float y, float f, float f2, Class<?> clazz) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.isValid(vg, text, clazz)) {
         NVGPaint paint = NVGPaint.calloc();
         int image = assetHelper.getImage(text);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, f, f2, 0.0F, image, 1.0F, paint);
         NanoVG.nvgRect(vg, x, y, f, f2);
         NanoVG.nvgFillPaint(vg, paint);
         NanoVG.nvgFill(vg);
         paint.free();
      }
   }

   @Override
   public void drawImage(long vg, String text, float x, float y, float f, float f2, int n, Class<?> clazz) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.isValid(vg, text, clazz)) {
         NVGPaint paint = NVGPaint.calloc();
         int image = assetHelper.getImage(text);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, f, f2, 0.0F, image, 1.0F, paint);
         this.drawRect(vg, x, y, f, f2, n, paint);
         paint.free();
      }
   }

   private void drawRect(long vg, float x, float y, float width, float height, int color, NVGPaint paint) {
      NanoVG.nvgRGBA((byte)(color >> 16 & 0xFF), (byte)(color >> 8 & 0xFF), (byte)(color & 0xFF), (byte)(color >> 24 & 0xFF), paint.innerColor());
      NanoVG.nvgRect(vg, x, y, width, height);
      NanoVG.nvgFillPaint(vg, paint);
      NanoVG.nvgFill(vg);
   }

   @Override
   public void draw(long l, ImageAsset image, float f, float f2, float f3, float f4) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.isValid(l, image)) {
         this.drawImage(l, image.filePath, f, f2, f3, f4, image.getClass());
      }
   }

   @Override
   public void draw(long l, ImageAsset image, float f, float f2, float f3, float f4, int n) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.isValid(l, image)) {
         this.drawImage(l, image.filePath, f, f2, f3, f4, n, image.getClass());
      }
   }

   @Deprecated
   @Override
   public void draw(long vg, String text, float x, float y, float f, float f2, float f3) {
      this.drawRoundedRect(vg, text, x, y, f, f2, f3, IOUtils.class);
   }

   @Override
   public void drawRoundedRect(long vg, String text, float x, float y, float f, float f2, float f3, Class<?> clazz) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.isValid(vg, text, clazz)) {
         NVGPaint paint = NVGPaint.calloc();
         int image = assetHelper.getImage(text);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, f, f2, 0.0F, image, 1.0F, paint);
         NanoVG.nvgRoundedRect(vg, x, y, f, f2, f3);
         NanoVG.nvgFillPaint(vg, paint);
         NanoVG.nvgFill(vg);
         paint.free();
      }
   }

   @Deprecated
   @Override
   public void draw(long l, ImageAsset image, float f, float f2, float f3, float f4, float f5) {
      if (AssetHelper.INSTANCE.isValid(l, image)) {
         this.drawRoundedRect(l, image.filePath, f, f2, f3, f4, f5, image.getClass());
      }
   }

   @Override
   public float getTextWidth(long l, String text, float f, OneConfigFont font) {
      for (int i = 0; i < text.length(); i++) {
         String text2 = String.valueOf(text.charAt(i));
         if (text2.equals("§") && i + 1 < text.length()) {
            text = text.replace(text2 + text.charAt(i + 1), "");
         }
      }

      float[] floats = new float[4];
      NanoVG.nvgFontSize(l, f);
      NanoVG.nvgFontFace(l, font.getName());
      return NanoVG.nvgTextBounds(l, 0.0F, 0.0F, text, floats);
   }

   @Override
   public float getTextHeight(long l, float f, OneConfigFont font) {
      NanoVG.nvgFontSize(l, f);
      float[] floats = new float[1];
      float[] floats2 = new float[1];
      float[] floats3 = new float[1];
      NanoVG.nvgFontFace(l, font.getName());
      NanoVG.nvgTextMetrics(l, floats, floats2, floats3);
      return floats3[0];
   }

   @Override
   public void drawLine(long vg, float x, float y, float width, float height, float f, int n) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgMoveTo(vg, x, y);
      NanoVG.nvgLineTo(vg, width, height);
      NVGColor nvgcolor = this.getNVGColor(vg, n);
      NanoVG.nvgStrokeColor(vg, nvgcolor);
      NanoVG.nvgStrokeWidth(vg, f);
      NanoVG.nvgStroke(vg);
      nvgcolor.free();
   }

   @Override
   public void drawEx(long vg, double x, double y, double width, double height, int color) {
      this.draw(vg, (float)x, (float)y, (float)width - x, (float)height - y, color);
   }

   @Override
   public void draw(long vg, float x, float y, float width, float height, float f, float radius, float f2) {
      this.drawRoundedRect(vg, x, y, width, height, f, radius, f2, new Color(0, 0, 0, 127));
   }

   @Override
   public void drawRoundedRect(long vg, float x, float y, float width, float height, float f, float radius, float f2, Color color) {
      NVGPaint paint = NVGPaint.calloc();

      try {
         NVGColor nvgColor = NVGColor.calloc();

         try {
            NVGColor nvgColor2 = NVGColor.calloc();

            try {
               this.performAction(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F, nvgColor);
               this.performAction(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 0.0F, nvgColor2);
               NanoVG.nvgBoxGradient(vg, x - radius, y - radius, width + 2.0F * radius, height + 2.0F * radius, f2 + radius, f, nvgColor, nvgColor2, paint);
               NanoVG.nvgBeginPath(vg);
               NanoVG.nvgRoundedRect(
                  vg, x - radius - f, y - radius - f, width + 2.0F * radius + 2.0F * f, height + 2.0F * radius + 2.0F * f, f2 + radius
               );
               NanoVG.nvgRoundedRect(vg, x, y, width, height, f2);
               NanoVG.nvgPathWinding(vg, 2);
               NanoVG.nvgFillPaint(vg, paint);
               NanoVG.nvgFill(vg);
            } catch (Throwable e) {
               if (nvgColor2 != null) {
                  try {
                     nvgColor2.close();
                  } catch (Throwable e2) {
                     e.addSuppressed(e2);
                  }
               }

               throw e;
            }

            if (nvgColor2 != null) {
               nvgColor2.close();
            }
         } catch (Throwable e3) {
            if (nvgColor != null) {
               try {
                  nvgColor.close();
               } catch (Throwable e4) {
                  e3.addSuppressed(e4);
               }
            }

            throw e3;
         }

         if (nvgColor != null) {
            nvgColor.close();
         }
      } catch (Throwable e5) {
         if (paint != null) {
            try {
               paint.close();
            } catch (Throwable e6) {
               e5.addSuppressed(e6);
            }
         }

         throw e5;
      }

      if (paint != null) {
         paint.close();
      }
   }

   @Override
   public void performAction(float f, float f2, float f3, float f4, NVGColor nvgColor) {
      nvgColor.r(f);
      nvgColor.g(f2);
      nvgColor.b(f3);
      nvgColor.a(f4);
   }

   @Override
   public NVGColor getNVGColor(long l, int n) {
      NVGColor nvgColor = NVGColor.calloc();
      NanoVG.nvgRGBA((byte)(n >> 16 & 0xFF), (byte)(n >> 8 & 0xFF), (byte)(n & 0xFF), (byte)(n >> 24 & 0xFF), nvgColor);
      NanoVG.nvgFillColor(l, nvgColor);
      return nvgColor;
   }

   @Override
   public void scale(long l, float f, float f2) {
      NanoVG.nvgScale(l, f, f2);
   }

   @Override
   public void translate(long l, float f, float f2) {
      NanoVG.nvgTranslate(l, f, f2);
   }

   @Override
   public void rotate(long l, double d) {
      NanoVG.nvgRotate(l, (float)Math.toRadians(d));
   }

   @Override
   public void resetTransform(long l) {
      NanoVG.nvgResetTransform(l);
   }

   @Override
   public void save(long l) {
      NanoVG.nvgSave(l);
   }

   @Override
   public void restore(long l) {
      NanoVG.nvgRestore(l);
   }

   @Override
   public void setAlpha(long l, float f) {
      float f2 = f;
      if (OneConfigGui.INSTANCE != null && OneConfigGui.INSTANCE.isIsDrawing() && f > OneConfigGui.INSTANCE.transparencyFactor) {
         f2 = OneConfigGui.INSTANCE.transparencyFactor;
      }

      this.globalAlpha = f2;
      NanoVG.nvgGlobalAlpha(l, f2);
   }

   @Override
   public float getGlobalAlpha() {
      return this.globalAlpha;
   }

   @Deprecated
   @Override
   public void drawEx(long vg, String text, float x, float y, float f, float f2, float f3) {
      this.drawImage(vg, text, x, y, f, f2, f3, IOUtils.class);
   }

   @Override
   public void drawImage(long vg, String text, float x, float y, float f, float f2, float f3, Class<?> clazz) {
      float f4 = f * f3;
      float f5 = f2 * f3;
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.isValid(vg, text, f4, f5, clazz)) {
         NVGPaint paint = NVGPaint.calloc();
         int n = assetHelper.computeInt(text, f4, f5);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, f, f2, 0.0F, n, 1.0F, paint);
         NanoVG.nvgRect(vg, x, y, f, f2);
         NanoVG.nvgFillPaint(vg, paint);
         NanoVG.nvgFill(vg);
         paint.free();
      }
   }

   @Deprecated
   @Override
   public void drawEx(long vg, String text, float x, float y, float f, float f2) {
      this.draw(vg, text, x, y, f, f2, IOUtils.class);
   }

   @Override
   public void draw(long vg, String text, float x, float y, float f, float f2, Class<?> clazz) {
      if (OneConfigGui.isOneConfigGui()) {
         this.drawImage(vg, text, x, y, f, f2, OneConfigGui.getProgress(), clazz);
      } else {
         this.drawImage(vg, text, x, y, f, f2, 1.0F, clazz);
      }
   }

   @Override
   public void draw(long vg, String text, float x, float y, float f, float f2, int n, float f3) {
      this.drawImage(vg, text, x, y, f, f2, n, f3, IOUtils.class);
   }

   @Override
   public void drawImage(long vg, String text, float x, float y, float f, float f2, int n, float f3, Class<?> clazz) {
      float f4 = f * f3;
      float f5 = f2 * f3;
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.isValid(vg, text, f4, f5, clazz)) {
         NVGPaint paint = NVGPaint.calloc();
         int n2 = assetHelper.computeInt(text, f4, f5);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgImagePattern(vg, x, y, f, f2, 0.0F, n2, 1.0F, paint);
         this.drawRect(vg, x, y, f, f2, n, paint);
         paint.free();
      }
   }

   @Override
   public void draw(long vg, String text, float x, float y, float f, float f2, int n) {
      this.draw(vg, text, x, y, f, f2, n, IOUtils.class);
   }

   @Override
   public void draw(long vg, String text, float x, float y, float f, float f2, int n, Class<?> clazz) {
      if (OneConfigGui.isOneConfigGui()) {
         this.drawImage(vg, text, x, y, f, f2, n, OneConfigGui.getProgress(), clazz);
      } else {
         this.drawImage(vg, text, x, y, f, f2, n, 1.0F, clazz);
      }
   }

   @Override
   public void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4, float f5) {
      this.drawImage(l, svg.filePath, f, f2, f3, f4, f5, svg.getClass());
   }

   @Override
   public void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4) {
      this.draw(l, svg.filePath, f, f2, f3, f4, svg.getClass());
   }

   @Override
   public void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4, int n, float f5) {
      this.drawImage(l, svg.filePath, f, f2, f3, f4, n, f5, svg.getClass());
   }

   @Override
   public void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4, int n) {
      if (OneConfigGui.isOneConfigGui()) {
         this.draw(l, svg, f, f2, f3, f4, n, OneConfigGui.getProgress());
      } else {
         this.draw(l, svg, f, f2, f3, f4, n, 1.0F);
      }
   }

   @Override
   public void draw(long l, InfoType infoType, float f, float f2, float f3) {
      SvgAsset svg = null;
      int n = 0;
      int n2 = 0;
      switch (infoType) {
         case INFO:
            svg = OneConfigSvgs.INFO_CIRCLE;
            n = OneConfigColors.GRAY_400;
            n2 = OneConfigColors.GRAY_300;
            break;
         case SUCCESS:
            svg = OneConfigSvgs.CHECK_CIRCLE;
            n = OneConfigColors.SUCCESS_700;
            n2 = OneConfigColors.SUCCESS_600;
            break;
         case WARNING:
            svg = OneConfigSvgs.WARNING;
            n = OneConfigColors.WARNING_600;
            n2 = OneConfigColors.WARNING_500;
            break;
         case ERROR:
            svg = OneConfigSvgs.ERROR;
            n = OneConfigColors.ERROR_700;
            n2 = OneConfigColors.ERROR_600;
      }

      float f4 = f + f3 / 2.0F;
      float f5 = f2 + f3 / 2.0F;
      this.drawCircle(l, f4, f5, f3 / 2.0F, n);
      this.drawCircle(l, f4, f5, f3 / 2.0F - f3 / 12.0F, n2);
      float f6 = f3 / 1.75F;
      this.draw(l, svg, f4 - f6 / 2.0F, f5 - f6 / 2.0F, f6, f6);
   }

   @Override
   public int[] getIntList(int n, int n2, int n3, int n4) {
      values = new int[]{n, n2, n3, n4};
      int[] values2 = valuesValue;
      valuesValue = new int[]{0};
      return values2;
   }

   @Override
   public boolean isNvgFrameActive() {
      return this.nvgFrameActive;
   }

   @Deprecated
   @Override
   public int computeInt(String text, float f, float f2, int n, int n2) {
      return OneConfigTextRenderer.computeInt(text, f, f2, n, n2);
   }

   @Deprecated
   @Override
   public void applyText(String text, float f, float f2, int n, NanoVGHelper.Mode2 mode2, float f3) {
      OneConfigTextRenderer.applyText(text, f, f2, n, OneConfigTextRenderer.Mode.fromOrdinal(mode2.ordinal()), f3);
   }

   public class Inner {
      @EventTarget
      public void onPhase(PhaseEvent phaseEvent) {
         if (phaseEvent.phase == Stage.POST && NanoVGHelperImpl.this.nvgFrameActive) {
            if (NanoVGHelperImpl.this.framePendingClose) {
               NanoVGHelperImpl.this.nvgFrameActive = false;
               NanoVGHelperImpl.this.framePendingClose = false;
            } else {
               NanoVGHelperImpl.this.framePendingClose = true;
            }
         }
      }
   }
}
