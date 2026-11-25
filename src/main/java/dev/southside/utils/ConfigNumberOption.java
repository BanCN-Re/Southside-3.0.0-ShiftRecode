package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.elements.text.NumberInputField;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LazilyParsedNumber;
import java.lang.reflect.Field;

public class ConfigNumberOption extends BasicOption implements IFocusableOption {
   @SerializedName("inputField")
   private final NumberInputField inputField;
   @SerializedName("size")
   private final int size;

   public ConfigNumberOption(Field field, Object obj, String text, String text2, String text3, String text4, float f, float f2, int n, int n2) {
      super(field, obj, text, text2, text3, text4, n2);
      this.inputField = new NumberInputField(84, 32, 0.0F, f, f2, n);
      this.size = n2;
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

   @Override
   public boolean isValid() {
      return super.isValid() && !this.inputField.isToggled();
   }

   @Override
   public int getHeight() {
      return 32;
   }

   private void setProgress(float f) {
      try {
         super.performAction((Object)f);
      } catch (IllegalAccessException e) {
      }
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      float f = 0.0F;
      this.inputField.setDisabled(!this.isValidEx());
      if (!this.isValidEx()) {
         nvg.setAlpha(vg, 0.5F);
      }

      if (this.inputField.isToggled() || this.inputField.isValid()) {
         f = this.inputField.getCurrent();
      }

      if (this.inputField.isToggled() || this.inputField.isValid()) {
         this.setProgress(f);
      }

      if (!this.inputField.isToggled()) {
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
         } catch (IllegalAccessException e2) {
         }
      }

      if (!this.inputField.isToggled()) {
         this.inputField.setProgress(f);
      }

      nvg.drawTextEx(vg, this.name, x, y + 17, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      this.inputField.draw(vg, x + (this.size != 1 ? 892 : 396), y, input);
      nvg.setAlpha(vg, 1.0F);
   }
}
