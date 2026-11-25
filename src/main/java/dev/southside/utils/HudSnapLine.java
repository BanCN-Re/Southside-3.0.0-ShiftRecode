package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.awt.Color;

public class HudSnapLine {
   private static final int DEFAULT_INT;
   @SerializedName("line")
   private final float line;
   @SerializedName("distance")
   private final float distance;
   @SerializedName("position")
   private final float position;
   private static String label;

   public HudSnapLine(float f, float f2, float f3, boolean flag) {
      this.line = f;
      float f4 = f2 + f3 / 2.0F;
      float f5 = f2 + f3;
      float f6 = Math.abs(f - f2);
      float f7 = Math.abs(f - f4);
      float f8 = Math.abs(f - f5);
      if (flag && (!(f6 <= f7) || !(f6 <= f8))) {
         if (f7 <= f8) {
            this.distance = f7;
            this.position = f - f3 / 2.0F;
         } else {
            this.distance = f8;
            this.position = f - f3;
         }
      } else {
         this.distance = f6;
         this.position = f;
      }
   }

   public void draw(long l, float f, boolean flag) {
      float f2 = this.line * WindowMetrics.getScaleFactor() - f / 2.0F;
      if (flag) {
         NanoVGHelper.INSTANCE.drawLine(l, f2, 0.0F, f2, WindowMetrics.getFramebufferHeight(), f, DEFAULT_INT);
      } else {
         NanoVGHelper.INSTANCE.drawLine(l, 0.0F, f2, WindowMetrics.getFramebufferWidth(), f2, f, DEFAULT_INT);
      }
   }

   public float getPosition() {
      return this.position;
   }

   public float getDistance() {
      return this.distance;
   }

   static {
      setLabel(null);
      DEFAULT_INT = new Color(138, 246, 226).getRGB();
   }

   public static void setLabel(String text) {
      label = text;
   }
}
