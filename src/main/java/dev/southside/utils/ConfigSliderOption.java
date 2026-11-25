package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.elements.text.NumberInputField;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LazilyParsedNumber;
import java.lang.reflect.Field;

public class ConfigSliderOption extends BasicOption implements IFocusableOption {
   private static final int DEFAULT_INT = 400;
   private static final int DEFAULT_INT_VALUE = 200;
   private static final float DEFAULT_FLOAT = 16.0F;
   private static final float DEFAULT_FLOAT_VALUE = 10.0F;
   private static final float DEFAULT_FLOAT_DATA = 16.0F;
   private static final float DEFAULT_FLOAT_REF = 16.0F;
   private static final float DEFAULT_FLOAT_PRIMARY = 10.0F;
   private static final int DEFAULT_INT_DATA = 352;
   private static final int DEFAULT_INT_REF = 512;
   @SerializedName("inputField")
   private final NumberInputField inputField;
   @SerializedName("min")
   private final float min;
   @SerializedName("max")
   private final float max;
   @SerializedName("step")
   private final int step;
   @SerializedName("instant")
   private final boolean instant;
   @SerializedName("increment")
   private final float increment;
   @SerializedName("dragging")
   private boolean dragging;
   @SerializedName("mouseWasDown")
   private boolean mouseWasDown;
   @SerializedName("stepsAnimation")
   private UiAnimation stepsAnimation;
   @SerializedName("targetAnimation")
   private UiAnimation targetAnimation;
   @SerializedName("stepSlideAnimation")
   private UiAnimation stepSlideAnimation;
   @SerializedName("animationStart")
   private float animationStart;
   @SerializedName("lastSliderTarget")
   private float lastSliderTarget = 1.0F;
   @SerializedName("animReset")
   private boolean animReset;
   @SerializedName("lastX")
   private float lastX = -1.0F;

   public ConfigSliderOption(
      Field field, Object obj, String text, String text2, String text3, String text4, float f, float f2, int n, boolean flag, float f3
   ) {
      super(field, obj, text, text2, text3, text4, 2);
      this.min = f;
      this.max = f2;
      this.step = n;
      this.instant = flag;
      this.increment = f3;
      this.inputField = new NumberInputField(84, 32, 0.0F, f, f2, n != 0 ? n : 1.0F);
      this.stepsAnimation = new DummyAnimation(0.0F);
      this.targetAnimation = new DummyAnimation(0.0F);
      this.stepSlideAnimation = new DummyAnimation(1.0F);
   }

   public ConfigSliderOption(Field field, Object obj, String text, String text2, String text3, String text4, float f, float f2, int n) {
      this(field, obj, text, text2, text3, text4, f, f2, n, false, 0.1F);
   }

   @Override
   public int getHeight() {
      return 32;
   }

   @Override
   public void onMouseClick(double d, double d2, int n) {
      if (this.isValidEx()) {
         this.inputField.onKeyTypedAlt(n);
      }
   }

   @Override
   public void onMouseClickEx(double d, double d2, int n) {
      if (this.isValidEx()) {
         this.inputField.onKeyTypedImpl(n);
      }
   }

   @Override
   public void onKeyTyped(int n) {
      this.inputField.onKeyTyped(n);
   }

   @Override
   public void onKeyTypedEx(int n) {
      this.inputField.onKeyTypedEx(n);
   }

   @Override
   public boolean isValidImpl() {
      return this.inputField.isToggled();
   }

   private void setProgress(float f) {
      try {
         super.performAction((Object)f);
      } catch (IllegalAccessException e) {
      }
   }

   private int computeInt(int n, int n2) {
      Integer integer = null;
      float f = n2 + 352;

      while (f <= n2 + 352 + 512) {
         if (integer == null || Math.abs(n - f) < Math.abs(n - integer)) {
            integer = (int)f;
         }

         f += 512.0F / ((this.max - this.min) / this.step);
      }

      return integer == null ? 0 : integer;
   }

   private int computeInt(float f, int n, int n2) {
      float f2 = MathClampUtils.mapRange(f, this.min, this.max, n, n2);
      return (int)MathClampUtils.clamp(f2, n, n2);
   }

   private float computeFloat(int n, int n2, int n3) {
      return (float)MathClampUtils.mapRangeStepped(n, n2, n3, this.min, this.max, this.increment);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      int n = x + 352;
      int n2 = n + 512;
      int n3 = 0;
      float f = 0.0F;
      boolean flag = input.isHovered(n, y, 512.0F, 32.0F) && this.isValidEx();
      this.inputField.setDisabled(!this.isValidEx());
      if (!this.isValidEx()) {
         nvg.setAlpha(vg, 0.5F);
      }

      boolean isKeyDown = OneConfigPlatform.getMousePlatform().isKeyDown(0);
      if (flag && isKeyDown && !this.mouseWasDown) {
         this.dragging = true;
      }

      boolean flag2 = !this.mouseWasDown && isKeyDown;
      this.mouseWasDown = isKeyDown;
      if (this.dragging) {
         n3 = (int)MathClampUtils.clamp(input.getProgress(), n, n2);
         if (this.step > 0) {
            n3 = this.computeInt(n3, x);
         }

         f = this.computeFloat(n3, n, n2);
      } else if (this.inputField.isToggled() || this.inputField.isValid()) {
         f = this.inputField.getCurrent();
         n3 = this.computeInt(f, n, n2);
      }

      if ((!this.dragging || !input.isValid()) && !this.inputField.isToggled() && !this.inputField.isValid()) {
         if (this.dragging && this.instant) {
            this.setProgress(f);
         }
      } else {
         this.dragging = false;
         if (this.step > 0) {
            n3 = this.computeInt(n3, x);
            f = this.computeFloat(n3, n, n2);
         }

         this.setProgress(f);
      }

      float progress = this.stepsAnimation.getProgress();
      float progress2 = this.targetAnimation.getProgress();
      if (this.isValidEx()) {
         if (this.dragging && flag2) {
            this.stepsAnimation = new LinearAnimation(400, progress, 1.0F, false);
            this.targetAnimation = new LinearAnimation(200, progress2, 0.625F, false);
            this.animReset = true;
         } else if (!this.dragging && flag) {
            if (this.targetAnimation.getProgressEx() != 1.0F) {
               this.stepsAnimation = new LinearAnimation(400, progress, 0.625F, false);
               this.targetAnimation = new LinearOffsetAnimation(200, progress2, 1.0F, false);
               this.animReset = true;
            }
         } else if (!this.dragging && this.animReset) {
            this.stepsAnimation = new LinearAnimation(400, progress, 0.0F, false);
            this.targetAnimation = new LinearAnimation(200, progress2, 0.0F, false);
            this.animReset = false;
         }
      }

      if (!this.dragging && !this.inputField.isToggled()) {
         try {
            Object obj = this.resolveValue();

            try {
               if (obj instanceof LazilyParsedNumber lazilyParsedNumber) {
                  f = lazilyParsedNumber.floatValue();
               } else if (obj instanceof Double val) {
                  f = val.floatValue();
               } else if (obj instanceof Integer integer) {
                  f = integer.floatValue();
               } else {
                  f = (Float)obj;
               }
            } catch (ClassCastException e) {
               System.err.println("?");
            }

            n3 = this.computeInt(f, n, n2);
         } catch (IllegalAccessException e2) {
         }
      }

      if (!this.inputField.isToggled()) {
         this.inputField.setProgress(f);
      }

      if (this.stepSlideAnimation.isValid()
         && this.lastSliderTarget != -1.0F
         && this.lastSliderTarget != n3
         && (!this.dragging || flag2 || this.step > 0)
         && this.lastX == x) {
         this.animationStart = this.lastSliderTarget;
         this.stepSlideAnimation = new EaseLinearCentered(((Number)Preferences.INSTANCE.trackerResponseDuration.getValue()).intValue(), 0.0F, 1.0F, false);
      }

      float progress3 = this.stepSlideAnimation.getProgress();
      this.lastSliderTarget = n3;
      this.lastX = x;
      n3 = (int)(n3 * progress3 + this.animationStart * (1.0F - progress3));
      float f2 = 4.0F;
      if (this.step > 0) {
         f2 *= 1.0F - Math.min(progress, 0.625F) * 16.0F / 10.0F;
      }

      nvg.drawTextEx(vg, this.name, x, y + 17, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      nvg.drawRoundedRect(vg, n, y + 13, 512.0F, 4.0F, OneConfigColors.GRAY_300, f2);
      nvg.drawRoundedRect(vg, n, y + 13 - 1, n3 - x - 352, 6.0F, OneConfigColors.PRIMARY_400, 4.0F);
      if (this.step > 0 && progress > 0.05F) {
         float f3 = progress * 16.0F;

         for (float i = x + 352 + 2; i <= n2; i += 512.0F / ((this.max - this.min) / this.step)) {
            int n4 = n3 > i - 2.0F ? OneConfigColors.PRIMARY_400 : OneConfigColors.GRAY_300;
            nvg.drawRoundedRect(vg, i - 2.0F, y + 16 - 1 - f3 / 2.0F, 4.0F, f3, n4, 2.0F);
         }
      }

      nvg.drawRoundedRect(vg, n3 - 12, y + 4, 24.0F, 24.0F, OneConfigColors.WHITE, 12.0F);
      if (progress2 > 0.02F) {
         nvg.drawRoundedRect(vg, n3 - 8.0F * progress2, y + 16 - 8.0F * progress2, 16.0F * progress2, 16.0F * progress2, OneConfigColors.PRIMARY_400, 12.0F);
      }

      this.inputField.draw(vg, x + 892, y, input);
      nvg.setAlpha(vg, 1.0F);
   }
}
