package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.InfoType;
import java.awt.Color;
import java.util.function.LongConsumer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.nanovg.NVGColor;

public interface NanoVGHelper {
   NanoVGHelper INSTANCE = LwjglManager.INSTANCE.getNanoVGHelper();

   void performAction(LongConsumer callback);

   void performAction(boolean flag, LongConsumer callback);

   void performAction(float f, LongConsumer callback);

   void drawRect(long vg, float x, float y, float width, float height, int color);

   void draw(long vg, double x, double y, double width, double height, int color);

   void drawRoundedRect(long vg, float x, float y, float width, float height, int color, float radius);

   void draw(long vg, float x, float y, float width, float height, int color, float radius);

   void drawRoundedRectVaried(long vg, float x, float y, float width, float height, int color, float radius, float f, float f2, float f3);

   void draw(long vg, float x, float y, float width, float height, float f, float radius, float f2, float f3, int n);

   void drawRoundedRect(long vg, float x, float y, float width, float height, int color, float radius, float f);

   void draw(long vg, double x, double y, double width, double height, double d, int n);

   void draw(long vg, float x, float y, float width, float height, float f, int n);

   void drawGradientRect(long vg, float x, float y, float width, float height, int color, int n, NanoVGHelper.Mode mode);

   void drawRoundedRect(long vg, float x, float y, float width, float height, int color, int n, float f, NanoVGHelper.Mode mode);

   void drawRoundedRect(long vg, float x, float y, float width, float height, int color);

   void drawCircle(long l, float f, float f2, float f3, int n);

   void draw(long vg, float x, float y, float width, float height, int color);

   void drawEx(long vg, float x, float y, float width, float height, int color, float radius);

   void draw(long vg, float x, float y, float width, float height, float f, float radius);

   void draw(long vg, String text, float x, float y, int n, float f, OneConfigFont font);

   void drawText(long vg, String text, float x, float y, int n, float f, OneConfigFont font);

   void drawEx(long vg, String text, float x, float y, int n, float f, OneConfigFont font);

   void drawTextEx(long vg, String text, float x, float y, int n, float f, OneConfigFont font);

   void draw(long vg, String text, float x, float y, float f, int n, float f2, OneConfigFont font);

   void draw(long vg, float x, float y, float width, float height);

   void drawTextAlt(long vg, String text, float x, float y, int n, float f, OneConfigFont font);

   void drawWrappedText(long vg, String text, float x, float y, float f, int n, float f2, OneConfigFont font);

   void drawWrappedText(long vg, String text, float x, float y, float f, int n, float f2, float f3, OneConfigFont font);

   float[] getWrappedTextBounds(long vg, String text, float x, float y, OneConfigFont font);

   float[] getWrappedTextBounds(long vg, String text, float x, float y, float f, OneConfigFont font);

   float getWrappedTextBoundsEx(long vg, String text, float x, float y, float f, OneConfigFont font);

   float getWrappedTextBoundsEx(long vg, String text, float x, float y, OneConfigFont font);

   float getWrappedTextBoundsAlt(long vg, String text, float x, float y, float f, OneConfigFont font);

   void draw(long l, float f, float f2, float f3, OneConfigFont font, InputHandler input);

   @Deprecated
   void draw(long vg, String text, float x, float y, float f, float f2);

   void drawImage(long vg, String text, float x, float y, float f, float f2, Class<?> clazz);

   void drawImage(long vg, String text, float x, float y, float f, float f2, int n, Class<?> clazz);

   void draw(long l, ImageAsset image, float f, float f2, float f3, float f4);

   void draw(long l, ImageAsset image, float f, float f2, float f3, float f4, int n);

   @Deprecated
   void draw(long vg, String text, float x, float y, float f, float f2, float f3);

   void drawRoundedRect(long vg, String text, float x, float y, float f, float f2, float f3, Class<?> clazz);

   void draw(long l, ImageAsset image, float f, float f2, float f3, float f4, float f5);

   float getTextWidth(long l, String text, float f, OneConfigFont font);

   float getTextHeight(long l, float f, OneConfigFont font);

   void drawLine(long vg, float x, float y, float width, float height, float f, int n);

   void drawEx(long vg, double x, double y, double width, double height, int color);

   void draw(long vg, float x, float y, float width, float height, float f, float radius, float f2);

   void drawRoundedRect(long vg, float x, float y, float width, float height, float f, float radius, float f2, Color color);

   void performAction(float f, float f2, float f3, float f4, NVGColor nvgColor);

   NVGColor getNVGColor(long l, int n);

   void scale(long l, float f, float f2);

   void resetTransform(long l);

   void save(long l);

   void restore(long l);

   void setAlpha(long l, float f);

   float getGlobalAlpha();

   @Deprecated
   void drawEx(long vg, String text, float x, float y, float f, float f2, float f3);

   void drawImage(long vg, String text, float x, float y, float f, float f2, float f3, Class<?> clazz);

   @Deprecated
   void drawEx(long vg, String text, float x, float y, float f, float f2);

   void draw(long vg, String text, float x, float y, float f, float f2, Class<?> clazz);

   @Deprecated
   void draw(long vg, String text, float x, float y, float f, float f2, int n, float f3);

   void drawImage(long vg, String text, float x, float y, float f, float f2, int n, float f3, Class<?> clazz);

   @Deprecated
   void draw(long vg, String text, float x, float y, float f, float f2, int n);

   void draw(long vg, String text, float x, float y, float f, float f2, int n, Class<?> clazz);

   void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4, float f5);

   void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4);

   void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4, int n, float f5);

   void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4, int n);

   void draw(long l, InfoType infoType, float f, float f2, float f3);

   int[] getIntList(int n, int n2, int n3, int n4);

   boolean isNvgFrameActive();

   @Deprecated
   int computeInt(String text, float f, float f2, int n, int n2);

   @Deprecated
   void applyText(String text, float f, float f2, int n, NanoVGHelper.Mode2 mode2, float f3);

   void translate(long l, float f, float f2);

   void rotate(long l, double d);

   enum Mode {
      DOWN,
      UP,
      LEFT,
      RIGHT,
      DIAGONAL_DOWN,
      DIAGONAL_UP;

      private static final long DEFAULT_LONG = -8788952664848203770L;

      public static float[] getFloatList(float f, float f2, float f3, float f4, @NotNull NanoVGHelper.Mode mode) {
         switch (mode) {
            case DOWN:
            default:
               return new float[]{f, f2, f, f2 + f4};
            case UP:
               return new float[]{f, f2 + f4, f, f2};
            case LEFT:
               return new float[]{f + f3, f2, f, f2};
            case RIGHT:
               return new float[]{f, f2, f + f3, f2};
            case DIAGONAL_DOWN:
               return new float[]{f, f2, f + f3, f2 + f4};
            case DIAGONAL_UP:
               return new float[]{f, f2 + f4, f + f3, f2};
         }
      }

      // $VF: synthetic method
      private static NanoVGHelper.Mode[] getModeList() {
         NanoVGHelper.Mode[] modeArray = new NanoVGHelper.Mode[(int)DEFAULT_LONG];
         modeArray[0] = DOWN;
         modeArray[1] = UP;
         modeArray[2] = LEFT;
         modeArray[3] = RIGHT;
         modeArray[4] = DIAGONAL_DOWN;
         modeArray[5] = DIAGONAL_UP;
         return modeArray;
      }


   }

   enum Mode2 {
      NONE,
      SHADOW,
      FULL;


      public static NanoVGHelper.Mode2 fromOrdinal(int n) {
         return values()[n];
      }

      // $VF: synthetic method
      private static NanoVGHelper.Mode2[] getMode2Array() {
         return new NanoVGHelper.Mode2[]{NONE, SHADOW, FULL};
      }


   }
}
