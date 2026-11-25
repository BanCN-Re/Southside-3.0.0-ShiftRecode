package dev.southside.render;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import dev.southside.gui.SouthsideTitleScreen;
import dev.southside.utils.OneConfigFont;
import dev.southside.utils.OneConfigColors;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.NanoVGHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import org.joml.Matrix4f;
import org.lwjgl.nanovg.NanoVG;

public final class MenuOverlayRenderer {
   private static final int DEFAULT_INT = -14271925;
   private static final int DEFAULT_INT_VALUE = -12363413;
   private static final int DEFAULT_INT_DATA = 922155775;
   private static final int DEFAULT_INT_REF = -1963525377;
   private static final List<MenuOverlayRenderer.Entry> entries = new ArrayList<>();
   private static ScreenRect screenRect;

   private MenuOverlayRenderer() {
   }

   public static void clear() {
      entries.clear();
      screenRect = null;
   }

   public static void setScreenRect(ScreenRect screenRect2) {
      screenRect = screenRect2;
   }

   public static void render(DrawContext context, LongConsumer callback) {
      entries.add(new MenuOverlayRenderer.Entry(new Matrix4f(context.getMatrices().peek().getPositionMatrix()), screenRect, callback));
   }

   public static void save(DrawContext context) {
      if (!entries.isEmpty()) {
         context.draw();

         try {
            NanoVGHelper.INSTANCE.performAction(true, l -> {
               for (MenuOverlayRenderer.Entry entry : entries) {
                  NanoVG.nvgSave(l);

                  try {
                     NanoVG.nvgResetTransform(l);
                     if (entry.clip != null) {
                        NanoVG.nvgScissor(l, entry.clip.getLeft(), entry.clip.getTop(), entry.clip.width(), entry.clip.height());
                     }

                     Matrix4f matrix = entry.matrix;
                     NanoVG.nvgTransform(l, matrix.m00(), matrix.m01(), matrix.m10(), matrix.m11(), matrix.m30(), matrix.m31());
                     entry.painter.accept(l);
                  } finally {
                     NanoVG.nvgRestore(l);
                  }
               }
            });
         } finally {
            entries.clear();
         }
      }
   }

   public static void render(DrawContext context, float f, float f2) {
      render(context, l -> NanoVGHelper.INSTANCE.drawTextEx(l, "Southside", f, f2, -12363413, 19.0F, OneConfigFonts.SEMIBOLD));
   }

   public static void render(DrawContext context, PressableWidget widget) {
      render(context, widget, widget.isHovered());
   }

   public static void render(DrawContext context, PressableWidget widget, boolean flag) {
      if (MinecraftClient.getInstance().currentScreen instanceof SouthsideTitleScreen) {
         renderEx(context, widget, flag);
      } else {
         boolean flag2 = widget.active && (flag || widget.isFocused());
         int x = widget.getX();
         int y = widget.getY();
         int width = widget.getWidth();
         int height = widget.getHeight();
         String string = widget.getMessage().getString();
         int n = flag2 ? ColorPalette.INSTANCE3.getColorHovered() : ColorPalette.INSTANCE3.getColorNormal();
         int n2 = widget.active ? OneConfigColors.WHITE_90 : OneConfigColors.WHITE_50;
         render(
            context,
            l -> {
               NanoVGHelper nvg = NanoVGHelper.INSTANCE;
               nvg.drawRoundedRect(l, x, y, width, height, n, 4.0F);
               if (widget.isFocused() && widget.active) {
                  nvg.drawRoundedRect(l, x, y, width, height, OneConfigColors.PRIMARY_400, 4.0F, 1.0F);
               }

               float f = 12.0F;
               OneConfigFont oneConfigFont = getOneConfigFont(string);
               NanoVG.nvgIntersectScissor(l, x + 6, y, width - 12, height);
               String text = formatText(l, string, width - 12, f, oneConfigFont);
               if (widget instanceof CheckboxWidget checkboxWidget) {
                  nvg.drawRoundedRect(
                     l, x + 10, y + height / 2.0F - 6.0F, 12.0F, 12.0F, checkboxWidget.isChecked() ? OneConfigColors.PRIMARY_400 : OneConfigColors.GRAY_850, 3.0F
                  );
                  if (checkboxWidget.isChecked()) {
                     nvg.drawLine(l, x + 13, y + height / 2.0F, x + 16, y + height / 2.0F + 3.0F, 1.5F, OneConfigColors.WHITE);
                     nvg.drawLine(l, x + 16, y + height / 2.0F + 3.0F, x + 20, y + height / 2.0F - 3.0F, 1.5F, OneConfigColors.WHITE);
                  }

                  nvg.drawTextEx(l, formatText(l, string, width - 42, f, oneConfigFont), x + 32, y + height / 2.0F, n2, f, oneConfigFont);
               } else {
                  nvg.drawTextAlt(l, text, x + width / 2.0F, y + height / 2.0F, n2, f, oneConfigFont);
               }
            }
         );
      }
   }

   private static void renderEx(DrawContext context, PressableWidget widget, boolean flag) {
      int x = widget.getX();
      int y = widget.getY();
      int width = widget.getWidth();
      int height = widget.getHeight();
      boolean flag2 = widget.active && widget.isFocused();
      boolean flag3 = widget.active && (flag || flag2);
      int n = flag3 ? -1963525377 : 922155775;
      int n2 = widget.active ? -14271925 : -12363413;
      String string = widget.getMessage().getString();
      render(context, l -> {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.draw(l, x, y, width, height, n, 5.0F);
         if (flag2) {
            nvg.draw(l, x + 5, y + height / 2.0F - 6.0F, 2.0F, 12.0F, -12363413, 1.0F);
         }

         OneConfigFont oneConfigFont = getOneConfigFont(string);
         nvg.drawTextEx(l, formatText(l, string, width - 24, 15.0F, oneConfigFont), x + 12, y + height / 2.0F, n2, 15.0F, oneConfigFont);
      });
   }

   public static void render(DrawContext context, float f, float f2, float f3, float f4, String text, boolean flag, float f5) {
      render(context, l -> {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         if (flag) {
            nvg.draw(l, f, f2, f3, f4, -1963525377, f4 * 0.14F);
         }

         nvg.drawTextAlt(l, text, f + f3 / 2.0F, f2 + f4 / 2.0F, flag ? -14271925 : -12363413, f5, getOneConfigFont(text));
      });
   }

   public static void render(DrawContext context, SliderWidget sliderWidget, double d) {
      int x = sliderWidget.getX();
      int y = sliderWidget.getY();
      int width = sliderWidget.getWidth();
      int height = sliderWidget.getHeight();
      String string = sliderWidget.getMessage().getString();
      render(
         context,
         l -> {
            NanoVGHelper nvg = NanoVGHelper.INSTANCE;
            nvg.drawRoundedRect(l, x, y, width, height, OneConfigColors.GRAY_700, 4.0F);
            float f = width - 8;
            float f2 = (float)(f * d);
            float f3 = y + height - 4;
            nvg.drawRoundedRect(l, x + 4, f3, f, 2.0F, OneConfigColors.GRAY_500, 1.0F);
            if (f2 > 0.0F) {
               nvg.drawRoundedRect(l, x + 4, f3, f2, 2.0F, OneConfigColors.PRIMARY_400, 1.0F);
            }

            nvg.drawCircle(l, x + 4 + f2, f3 + 1.0F, 2.5F, OneConfigColors.WHITE_90);
            NanoVG.nvgIntersectScissor(l, x + 4, y, width - 8, height);
            nvg.drawTextAlt(
               l,
               formatText(l, string, width - 12, 10.0F, getOneConfigFont(string)),
               x + width / 2.0F,
               y + (height - 3) / 2.0F,
               sliderWidget.active ? OneConfigColors.WHITE_90 : OneConfigColors.WHITE_50,
               10.0F,
               getOneConfigFont(string)
            );
         }
      );
   }

   private static OneConfigFont getOneConfigFont(String text) {
      return text.codePoints().anyMatch(n -> n > 255) ? OneConfigFonts.WQY_MICROHEI : OneConfigFonts.REGULAR;
   }

   public static void render(DrawContext context, String text, float f, float f2, float f3, int n) {
      render(context, l -> NanoVGHelper.INSTANCE.drawTextEx(l, text, f, f2, n, f3, getOneConfigFont(text)));
   }

   public static void render(DrawContext context, String text, float f, float f2, float f3) {
      render(context, l -> NanoVGHelper.INSTANCE.drawWrappedText(l, text, f, f2, f3, OneConfigColors.WHITE_80, 13.0F, 1.3F, getOneConfigFont(text)));
   }

   public static void render(DrawContext context, float f, float f2, boolean flag) {
      render(context, l -> {
         float f3 = flag ? 1.0F : -1.0F;
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.drawRoundedRect(l, f - 4.0F, f2 - 5.0F, 20.0F, 18.0F, OneConfigColors.GRAY_600, 4.0F);
         nvg.drawLine(l, f, f2 + f3 * 4.0F, f + 6.0F, f2, 1.5F, OneConfigColors.WHITE_90);
         nvg.drawLine(l, f + 6.0F, f2, f + 12.0F, f2 + f3 * 4.0F, 1.5F, OneConfigColors.WHITE_90);
      });
   }

   public static void render(DrawContext context, String text, String text2, int n, int n2, int n3, int n4, boolean flag, boolean flag2) {
      render(
         context,
         l -> {
            NanoVGHelper nvg = NanoVGHelper.INSTANCE;
            nvg.drawRoundedRect(l, n, n2, n3, n4, flag ? OneConfigColors.GRAY_500 : (flag2 ? OneConfigColors.GRAY_700 : OneConfigColors.GRAY_850_95), 7.0F);
            if (flag) {
               nvg.drawRoundedRect(l, n, n2 + 12, 3.0F, n4 - 24, OneConfigColors.PRIMARY_400, 1.5F);
            }

            NanoVG.nvgIntersectScissor(l, n + 12, n2 + 4, n3 - 24, n4 - 8);
            nvg.drawTextEx(
               l,
               formatText(l, text, n3 - 28, 14.0F, getOneConfigFont(text)),
               n + 14,
               n2 + (text2.isBlank() ? n4 / 2.0F : 18.0F),
               OneConfigColors.WHITE_90,
               14.0F,
               getOneConfigFont(text)
            );
            if (!text2.isBlank()) {
               nvg.drawTextEx(
                  l,
                  formatText(l, text2.replace('\n', ' '), n3 - 28, 10.0F, getOneConfigFont(text2)),
                  n + 14,
                  n2 + n4 - 15,
                  OneConfigColors.WHITE_60,
                  10.0F,
                  getOneConfigFont(text2)
               );
            }
         }
      );
   }

   public static void render(DrawContext context, float f, float f2, float f3, float f4, float f5) {
      render(context, l -> {
         float f6 = Math.max(22.0F, f3 * f3 / f4);
         float f7 = (f3 - f6) * f5 / Math.max(1.0F, f4 - f3);
         NanoVGHelper.INSTANCE.drawRoundedRect(l, f, f2 + f7, 3.0F, f6, OneConfigColors.GRAY_400_80, 1.5F);
      });
   }

   private static String formatText(long vg, String text, float x, float y, OneConfigFont font) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      if (nvg.getTextWidth(vg, text, y, font) <= x) {
         return text;
      }

      int len = text.length();

      while (len > 0) {
         len = text.offsetByCodePoints(len, -1);
         String text2 = text.substring(0, len) + "…";
         if (nvg.getTextWidth(vg, text2, y, font) <= x) {
            return text2;
         }
      }

      return "";
   }

   private record Entry(Matrix4f matrix, ScreenRect clip, LongConsumer painter) {

      public Matrix4f getMatrix() {
         return this.matrix;
      }

      public ScreenRect getClip() {
         return this.clip;
      }

      public LongConsumer getPainter() {
         return this.painter;
      }
   }
}
