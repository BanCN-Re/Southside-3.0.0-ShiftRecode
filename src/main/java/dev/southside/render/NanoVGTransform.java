package dev.southside.render;

import java.awt.Color;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

public class NanoVGTransform {
   public static void translate(float x, float y) {
      NanoVG.nvgSave(NanoVGContext.timestamp);
      NanoVG.nvgTranslate(NanoVGContext.timestamp, x, y);
   }

   public static void scissor(float x, float y, float width, float height) {
      NanoVG.nvgSave(NanoVGContext.timestamp);
      NanoVG.nvgScissor(NanoVGContext.timestamp, x, y, width, height);
   }

   public static void rotate(float x, float y, float z) {
      NanoVG.nvgSave(NanoVGContext.timestamp);
      NanoVG.nvgTranslate(NanoVGContext.timestamp, x, y);
      NanoVG.nvgRotate(NanoVGContext.timestamp, NanoVG.nvgDegToRad(z));
      NanoVG.nvgTranslate(NanoVGContext.timestamp, -x, -y);
   }

   public static void scaleX(float x, float y, float z) {
      NanoVG.nvgSave(NanoVGContext.timestamp);
      NanoVG.nvgTranslate(NanoVGContext.timestamp, x, y);
      NanoVG.nvgScale(NanoVGContext.timestamp, z, 1.0F);
      NanoVG.nvgTranslate(NanoVGContext.timestamp, -x, -y);
   }

   public static void scale(float x, float y, float z) {
      NanoVG.nvgSave(NanoVGContext.timestamp);
      NanoVG.nvgTranslate(NanoVGContext.timestamp, x, y);
      NanoVG.nvgScale(NanoVGContext.timestamp, z, z);
      NanoVG.nvgTranslate(NanoVGContext.timestamp, -x, -y);
   }

   public static void restore() {
      NanoVG.nvgRestore(NanoVGContext.timestamp);
   }

   public static void closePath() {
      NanoVG.nvgClosePath(NanoVGContext.timestamp);
   }

   public static void drawLine(float[] floats, int n, float f) {
      if (floats.length < 4) {
         throw new IllegalArgumentException("At least two points are required to render a line.");
      }

      NanoVG.nvgBeginPath(NanoVGContext.timestamp);
      applyColorEx(new Color(n));
      NanoVG.nvgStrokeWidth(NanoVGContext.timestamp, f);
      NanoVG.nvgMoveTo(NanoVGContext.timestamp, floats[0], floats[1]);

      for (byte i = 2; i < floats.length; i += 2) {
         NanoVG.nvgLineTo(NanoVGContext.timestamp, floats[i], floats[i + 1]);
      }

      NanoVG.nvgStroke(NanoVGContext.timestamp);
   }

   public static void drawRect(float f, float f2, float f3, float f4, Color color) {
      NanoVG.nvgBeginPath(NanoVGContext.timestamp);
      NanoVG.nvgRect(NanoVGContext.timestamp, f, f2, f3, f4);
      applyColor(color);
      NanoVG.nvgFill(NanoVGContext.timestamp);
   }

   public static void performAction(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, Color color) {
      NanoVG.nvgBeginPath(NanoVGContext.timestamp);
      SquirclePath.draw(NanoVGContext.timestamp, f, f2, f3, f4, f5, f6, f7, f8);
      applyColor(color);
      NanoVG.nvgFill(NanoVGContext.timestamp);
   }

   public static void drawCircle(float f, float f2, float f3, Color color) {
      NanoVG.nvgBeginPath(NanoVGContext.timestamp);
      NanoVG.nvgCircle(NanoVGContext.timestamp, f, f2, f3);
      applyColor(color);
      NanoVG.nvgFill(NanoVGContext.timestamp);
   }

   public static void performAction(float f, float f2, float f3, float f4, Color color) {
      if (!(f4 <= 0.0F) && !(f3 <= f4)) {
         NanoVG.nvgBeginPath(NanoVGContext.timestamp);
         NanoVG.nvgArc(NanoVGContext.timestamp, f, f2, f3, 0.0F, 6.2831855F, 2);
         NanoVG.nvgArc(NanoVGContext.timestamp, f, f2, f4, 0.0F, 6.2831855F, 1);
         applyColor(color);
         NanoVG.nvgFill(NanoVGContext.timestamp);
         closePath();
      }
   }

   public static void performAction(float f, float f2, float f3, float f4, float f5, float f6, Color color, Color color2) {
      NanoVG.nvgBeginPath(NanoVGContext.timestamp);
      SquirclePath.draw(NanoVGContext.timestamp, f, f2, f3, f4, f5);
      applyColor(color);
      NanoVG.nvgFill(NanoVGContext.timestamp);
      closePath();
      applyColorEx(color2);
      NanoVG.nvgStrokeWidth(NanoVGContext.timestamp, f6);
      NanoVG.nvgStroke(NanoVGContext.timestamp);
   }

   public static void drawGradientRect(float f, float f2, float f3, float f4, Color color, Color color2) {
      float f5 = f + f3;
      float f6 = f2 + f4;
      NVGPaint paint = NVGPaint.calloc();
      long timestamp = NanoVGContext.timestamp;
      NVGPaint paint2 = paint;

      try {
         NVGColor nvgcolor = getNVGColor(color2);

         try {
            NVGColor nvgcolor2 = getNVGColor(color);

            try {
               NanoVG.nvgLinearGradient(timestamp, f, f2, f5, f6, nvgcolor, nvgcolor2, paint);
               NanoVG.nvgBeginPath(NanoVGContext.timestamp);
               NanoVG.nvgRect(NanoVGContext.timestamp, f, f2, f3, f4);
               NanoVG.nvgFillPaint(NanoVGContext.timestamp, paint);
               NanoVG.nvgFill(NanoVGContext.timestamp);
            } catch (Throwable e) {
               if (nvgcolor2 != null) {
                  try {
                     nvgcolor2.close();
                  } catch (Throwable e2) {
                     e.addSuppressed(e2);
                  }
               }

               throw e;
            }

            if (nvgcolor2 != null) {
               nvgcolor2.close();
            }
         } catch (Throwable e3) {
            if (nvgcolor != null) {
               try {
                  nvgcolor.close();
               } catch (Throwable e4) {
                  e3.addSuppressed(e4);
               }
            }

            throw e3;
         }

         if (nvgcolor != null) {
            nvgcolor.close();
         }
      } catch (Throwable e5) {
         if (paint2 != null) {
            try {
               paint2.close();
            } catch (Throwable e6) {
               e5.addSuppressed(e6);
            }
         }

         throw e5;
      }

      if (paint2 != null) {
         paint2.close();
      }
   }

   public static void drawGradientRect(float f, float f2, float f3, float f4, float f5, Color color, Color color2) {
      float f6 = f + f3;
      float f7 = f2 + f4;
      NVGPaint paint = NVGPaint.calloc();
      long timestamp = NanoVGContext.timestamp;
      NVGPaint paint2 = paint;

      try {
         NVGColor nvgcolor = getNVGColor(color2);

         try {
            NVGColor nvgcolor2 = getNVGColor(color);

            try {
               NanoVG.nvgLinearGradient(timestamp, f, f2, f6, f7, nvgcolor, nvgcolor2, paint);
               NanoVG.nvgBeginPath(NanoVGContext.timestamp);
               SquirclePath.draw(NanoVGContext.timestamp, f, f2, f3, f4, f5);
               NanoVG.nvgFillPaint(NanoVGContext.timestamp, paint);
               NanoVG.nvgFill(NanoVGContext.timestamp);
            } catch (Throwable e) {
               if (nvgcolor2 != null) {
                  try {
                     nvgcolor2.close();
                  } catch (Throwable e2) {
                     e.addSuppressed(e2);
                  }
               }

               throw e;
            }

            if (nvgcolor2 != null) {
               nvgcolor2.close();
            }
         } catch (Throwable e3) {
            if (nvgcolor != null) {
               try {
                  nvgcolor.close();
               } catch (Throwable e4) {
                  e3.addSuppressed(e4);
               }
            }

            throw e3;
         }

         if (nvgcolor != null) {
            nvgcolor.close();
         }
      } catch (Throwable e5) {
         if (paint2 != null) {
            try {
               paint2.close();
            } catch (Throwable e6) {
               e5.addSuppressed(e6);
            }
         }

         throw e5;
      }

      if (paint2 != null) {
         paint2.close();
      }
   }

   private static NVGColor getNVGColor(Color color) {
      NVGColor nvgColor = NVGColor.calloc();
      return nvgColor.r(color.getRed() / 255.0F).g(color.getGreen() / 255.0F).b(color.getBlue() / 255.0F).a(color.getAlpha() / 255.0F);
   }

   private static void applyColor(Color color) {
      NVGColor nvgcolor = getNVGColor(color);

      try {
         NanoVG.nvgFillColor(NanoVGContext.timestamp, nvgcolor);
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

   public static void applyColorEx(Color color) {
      NVGColor nvgcolor = getNVGColor(color);

      try {
         NanoVG.nvgStrokeColor(NanoVGContext.timestamp, nvgcolor);
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
}
