package cc.polyfrost.oneconfig.gui.elements.text;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.InputHandler;
import dev.southside.utils.OneConfigSvgs;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.ColorAnimation;
import dev.southside.utils.BasicElement;
import dev.southside.utils.freecookie.FreeCookieAPI;

public class NumberInputField extends FreeCookieAPI {
   @SerializedName("upArrow")
   private final BasicElement upArrow = new BasicElement(12, 14, false);
   @SerializedName("downArrow")
   private final BasicElement downArrow = new BasicElement(12, 14, false);
   @SerializedName("colorTop")
   private final ColorAnimation colorTop = new ColorAnimation(ColorPalette.INSTANCE3);
   @SerializedName("colorBottom")
   private final ColorAnimation colorBottom = new ColorAnimation(ColorPalette.INSTANCE3);
   @SerializedName("min")
   private float min;
   @SerializedName("max")
   private float max;
   @SerializedName("step")
   private float step;
   @SerializedName("current")
   private float current;

   public NumberInputField(int n, int n2, float f, float f2, float f3, float f4) {
      super(n - 16, n2, true, "");
      super.onlyNums = true;
      this.min = f2;
      this.max = f3;
      this.step = f4;
      this.setProgress(f);
   }

   @Override
   public void draw(long l, float f, float f2, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      super.errored = false;
      if (super.disabled) {
         nvg.setAlpha(l, 0.5F);
      }

      nvg.drawRoundedRect(l, f + super.width + 4.0F, f2, 12.0F, 28.0F, -13552840, 6.0F);
      this.upArrow.setDisabled(super.disabled);
      this.downArrow.setDisabled(super.disabled);
      this.upArrow.performAction(f + super.width + 4.0F, f2, input);
      this.downArrow.performAction(f + super.width + 4.0F, f2 + 14.0F, input);

      try {
         this.current = Float.parseFloat(super.input);
      } catch (NumberFormatException e) {
         super.errored = true;
      }

      if (!(this.current < this.min) && !(this.current > this.max)) {
         this.upArrow.setDisabled(false);
         this.downArrow.setDisabled(false);
      } else {
         super.errored = true;
      }

      if (this.upArrow.isClicked()) {
         this.current = this.current + this.step;
         if (this.current > this.max) {
            this.current = this.max;
         }

         this.setProgress(this.current);
      }

      if (this.downArrow.isClicked()) {
         this.current = this.current - this.step;
         if (this.current < this.min) {
            this.current = this.min;
         }

         this.setProgress(this.current);
      }

      if (this.current >= this.max && !super.disabled) {
         nvg.setAlpha(l, 0.3F);
         this.upArrow.setDisabled(true);
      }

      nvg.drawRoundedRectVaried(
         l,
         f + super.width + 4.0F,
         f2,
         12.0F,
         14.0F,
         this.colorTop.computeInt(this.upArrow.isHovered(), this.upArrow.isPressed()),
         6.0F,
         6.0F,
         0.0F,
         0.0F
      );
      nvg.draw(l, OneConfigSvgs.CHEVRON_UP, f + super.width + 5.0F, f2 + 2.0F, 10.0F, 10.0F);
      if (this.current >= this.max && !super.disabled) {
         nvg.setAlpha(l, 1.0F);
      }

      if (this.current <= this.min && !super.disabled) {
         nvg.setAlpha(l, 0.3F);
         this.downArrow.setDisabled(true);
      }

      nvg.drawRoundedRectVaried(
         l,
         f + super.width + 4.0F,
         f2 + 14.0F,
         12.0F,
         14.0F,
         this.colorBottom.computeInt(this.downArrow.isHovered(), this.downArrow.isPressed()),
         0.0F,
         0.0F,
         6.0F,
         6.0F
      );
      nvg.draw(l, OneConfigSvgs.CHEVRON_DOWN, f + super.width + 5.0F, f2 + 15.0F, 10.0F, 10.0F);
      if (!super.disabled) {
         nvg.setAlpha(l, 1.0F);
      }

      try {
         super.draw(l, f, f2 - 2.0F, input);
      } catch (Exception e2) {
         this.setProgress(this.current);
         super.caretPos = 0;
         super.prevCaret = 0;
      }

      if (super.disabled) {
         nvg.setAlpha(l, 1.0F);
      }
   }

   public float getCurrent() {
      return this.current;
   }

   public void setProgress(float f) {
      if (f == (int)f) {
         super.input = String.format("%d", (int)f);
      } else if (f * 10.0F == (int)(f * 10.0F)) {
         super.input = String.format("%.1f", f);
      } else {
         super.input = String.format("%.2f", f);
      }
   }

   @Override
   public void setInput() {
      try {
         if (this.current < this.min) {
            this.current = this.min;
         }

         float f = this.current - this.max;
         if (f > 0.0F || Float.isNaN(f)) {
            this.current = this.max;
         }

         this.setProgress(this.current);
      } catch (Exception e) {
      }
   }

   public void setMin(float f) {
      this.min = f;
   }

   public void setMax(float f) {
      this.max = f;
   }

   public void setStep(float f) {
      this.step = f;
   }

   public boolean isValid() {
      return this.upArrow.isClicked() || this.downArrow.isClicked();
   }
}
