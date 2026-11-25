package dev.southside.utils;

import cc.polyfrost.oneconfig.hud.Hud;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class HudPosition {
   @SerializedName("anchor")
   public HudPosition.Mode anchor;
   @SerializedName("x")
   public float x;
   @SerializedName("y")
   public float y;
   @SerializedName("width")
   private float width;
   @SerializedName("height")
   private float height;
   @SerializedName("hud")
   @Nullable
   private Hud hud;

   public HudPosition(Hud hud2, float f, float f2, float f3, float f4) {
      this(hud2, f, f2, f3, f4, 1920.0F, 1080.0F);
   }

   public HudPosition(Hud hud2, float f, float f2, float f3, float f4, float f5, float f6) {
      this.hud = hud2;
      this.performActionEx(f3, f4);
      this.performAction(f, f2, f5, f6);
   }

   public void performAction(float x2, float y2, float width2, float height2) {
      float f = x2 + this.width;
      float f2 = y2 + this.height;
      if (x2 <= width2 / 3.0F && y2 <= height2 / 3.0F) {
         this.anchor = HudPosition.Mode.TOP_LEFT;
      } else if (f >= width2 / 3.0F * 2.0F && y2 <= height2 / 3.0F) {
         this.anchor = HudPosition.Mode.TOP_RIGHT;
      } else if (x2 <= width2 / 3.0F && f2 >= height2 / 3.0F * 2.0F) {
         this.anchor = HudPosition.Mode.BOTTOM_LEFT;
      } else if (f >= width2 / 3.0F * 2.0F && f2 >= height2 / 3.0F * 2.0F) {
         this.anchor = HudPosition.Mode.BOTTOM_RIGHT;
      } else if (y2 <= height2 / 3.0F) {
         this.anchor = HudPosition.Mode.TOP_CENTER;
      } else if (x2 <= width2 / 3.0F) {
         this.anchor = HudPosition.Mode.MIDDLE_LEFT;
      } else if (f >= width2 / 3.0F * 2.0F) {
         this.anchor = HudPosition.Mode.MIDDLE_RIGHT;
      } else if (f2 >= height2 / 3.0F * 2.0F) {
         this.anchor = HudPosition.Mode.BOTTOM_CENTER;
      } else {
         this.anchor = HudPosition.Mode.MIDDLE_CENTER;
      }

      this.x = x2 - this.evaluateCustom(width2) + this.evaluateCustom(this.width);
      this.y = y2 - this.evaluateWithOffset(height2) + this.evaluateWithOffset(this.height);
   }

   public void performAction(float x2, float y2) {
      this.performAction(x2, y2, WindowMetrics.getScaledWidth(), WindowMetrics.getScaledHeight());
   }

   public void performActionEx(float x2, float y2) {
      this.width = x2;
      this.height = y2;
   }

   public void performActionAlt(float x2, float y2) {
      float progress = this.getProgress();
      float progressEx = this.getProgressEx();
      this.performActionEx(x2, y2);
      this.performAction(progress, progressEx);
   }

   public float evaluate(float f) {
      return this.x + this.evaluateCustom(f) - this.evaluateCustom(this.width);
   }

   public float getProgress() {
      return this.evaluate(WindowMetrics.getScaledWidth());
   }

   public void setProgress(float f) {
      this.x = f - this.evaluateCustom(WindowMetrics.getScaledWidth()) + this.evaluateCustom(this.width);
   }

   public float evaluateEx(float f) {
      return this.y + this.evaluateWithOffset(f) - this.evaluateWithOffset(this.height);
   }

   public float getProgressEx() {
      return this.evaluateEx(WindowMetrics.getScaledHeight());
   }

   public void setProgressEx(float f) {
      this.y = f - this.evaluateWithOffset(WindowMetrics.getScaledHeight()) + this.evaluateWithOffset(this.height);
   }

   public float evaluateAlt(float f) {
      return this.evaluate(f) + this.width;
   }

   public float getProgressAlt() {
      return this.evaluateAlt(WindowMetrics.getScaledWidth());
   }

   public float evaluateImpl(float f) {
      return this.evaluateEx(f) + this.height;
   }

   public float getProgressImpl() {
      return this.evaluateImpl(WindowMetrics.getScaledHeight());
   }

   public float evaluateDirect(float f) {
      return this.evaluate(f) + this.width / 2.0F;
   }

   public float getProgressDirect() {
      return this.evaluateDirect(WindowMetrics.getScaledWidth());
   }

   public float evaluateSafe(float f) {
      return this.evaluateEx(f) + this.height / 2.0F;
   }

   public float getProgressSafe() {
      return this.evaluateSafe(WindowMetrics.getScaledHeight());
   }

   public float getWidth() {
      return this.width;
   }

   public float getHeight() {
      return this.height;
   }

   private float evaluateCustom(float f) {
      return f * this.anchor.x;
   }

   private float evaluateWithOffset(float f) {
      return f * this.anchor.y;
   }

   public static HudPosition.Mode getMode(int n) {
      return switch (n) {
         case 1 -> HudPosition.Mode.TOP_CENTER;
         case 2 -> HudPosition.Mode.TOP_RIGHT;
         case 3 -> HudPosition.Mode.MIDDLE_LEFT;
         case 4 -> HudPosition.Mode.MIDDLE_CENTER;
         case 5 -> HudPosition.Mode.MIDDLE_RIGHT;
         case 6 -> HudPosition.Mode.BOTTOM_LEFT;
         case 7 -> HudPosition.Mode.BOTTOM_CENTER;
         case 8 -> HudPosition.Mode.BOTTOM_RIGHT;
         default -> HudPosition.Mode.TOP_LEFT;
      };
   }

   public HudPosition.Mode getAnchor() {
      return this.anchor;
   }

   public Hud getHud() {
      return this.hud;
   }

   public void setHud(Hud hud2) {
      this.hud = hud2;
   }

   public enum Mode {
      @SerializedName("0")
      TOP_LEFT(0.0F, 0.0F),
      @SerializedName("1")
      TOP_CENTER(0.5F, 0.0F),
      @SerializedName("2")
      TOP_RIGHT(1.0F, 0.0F),
      @SerializedName("3")
      MIDDLE_LEFT(0.0F, 0.5F),
      @SerializedName("4")
      MIDDLE_CENTER(0.5F, 0.5F),
      @SerializedName("5")
      MIDDLE_RIGHT(1.0F, 0.5F),
      @SerializedName("6")
      BOTTOM_LEFT(0.0F, 1.0F),
      @SerializedName("7")
      BOTTOM_CENTER(0.5F, 1.0F),
      @SerializedName("8")
      BOTTOM_RIGHT(1.0F, 1.0F);

      @SerializedName("x")
      public final float x;
      @SerializedName("y")
      public final float y;

      Mode(float x2, float y2) {
         this.x = x2;
         this.y = y2;
      }

      // $VF: synthetic method
      private static HudPosition.Mode[] createValues() {
         return new HudPosition.Mode[]{TOP_LEFT, TOP_CENTER, TOP_RIGHT, MIDDLE_LEFT, MIDDLE_CENTER, MIDDLE_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT};
      }


   }
}
