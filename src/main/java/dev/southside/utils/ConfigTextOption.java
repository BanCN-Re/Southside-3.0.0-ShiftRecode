package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.freecookie.FreeCookieAPI;
import java.lang.reflect.Field;

public class ConfigTextOption extends BasicOption implements IFocusableOption {
   @SerializedName("secure")
   private final boolean secure;
   @SerializedName("multiLine")
   private final boolean multiLine;
   @SerializedName("textField")
   public final FreeCookieAPI textField;

   public ConfigTextOption(Field field, Object obj, String text, String text2, String text3, String text4, int n, String text5, boolean flag, boolean flag2) {
      super(field, obj, text, text2, text3, text4, n);
      this.secure = flag;
      this.multiLine = flag2;
      this.textField = new FreeCookieAPI(n != 1 ? 640 : 256, flag2 ? 64 : 32, text5, flag2, flag, OneConfigSvgs.TEXT_INPUT, 12.0F);
   }

   @Override
   public void onMouseClick(double d, double d2, int n) {
      if (this.isValidEx()) {
         this.textField.onKeyTypedAlt(n);
         this.performActionAlt();
      }
   }

   @Override
   public void onMouseClickEx(double d, double d2, int n) {
      if (this.isValidEx()) {
         this.textField.onKeyTypedImpl(n);
         this.performActionAlt();
      }
   }

   @Override
   public void onKeyTyped(int n) {
      if (this.isValidEx()) {
         this.textField.onKeyTyped(n);
         this.performActionAlt();
      }
   }

   @Override
   public void onKeyTypedEx(int n) {
      if (this.isValidEx()) {
         this.textField.onKeyTypedEx(n);
         this.performActionAlt();
      }
   }

   private void performActionAlt() {
      try {
         this.performAction(this.textField.getInput());
      } catch (IllegalAccessException e) {
      }
   }

   @Override
   public boolean isValidImpl() {
      return this.textField.isToggled();
   }

   @Override
   public int getHeight() {
      return this.multiLine ? this.textField.getHeight() : 32;
   }

   @Override
   public boolean isValid() {
      return super.isValid() && !this.textField.isToggled();
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      if (!this.isValidEx()) {
         nvg.setAlpha(vg, 0.5F);
      }

      this.textField.setDisabled(!this.isValidEx());
      nvg.drawTextEx(vg, this.name, x, y + 16, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);

      try {
         String text = (String)this.resolveValue();
         this.textField.setInput(text != null ? text : "");
      } catch (IllegalAccessException e) {
      }

      if (this.multiLine && this.textField.getLines() > 2) {
         this.textField.setHeight(64 + 24 * (this.textField.getLines() - 2));
      } else if (this.multiLine) {
         this.textField.setHeight(64);
      }

      this.textField.draw(vg, x + (this.size != 1 ? 352 : 224), y, input);
      if (this.secure) {
         SvgAsset svg = this.textField.isPassword() ? OneConfigSvgs.EYE_OFF : OneConfigSvgs.EYE;
         boolean flag = input.isHovered(x + 967, y + 7, 18.0F, 18.0F) && this.isValidEx();
         int n = flag ? OneConfigColors.WHITE : OneConfigColors.WHITE_80;
         if (flag && input.isValid()) {
            this.textField.setPassword(!this.textField.isPassword());
         }

         if (flag && OneConfigPlatform.getMousePlatform().isKeyDown(0)) {
            nvg.setAlpha(vg, 0.5F);
         }

         nvg.draw(vg, svg, x + 967, y + 7, 18.0F, 18.0F, n);
      }

      nvg.setAlpha(vg, 1.0F);
   }
}
