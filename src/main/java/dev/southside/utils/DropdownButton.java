package dev.southside.utils;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class DropdownButton extends BasicButton {
   @SerializedName("opts")
   private final LinkedHashMap<String, ColorAnimation> opts;
   @SerializedName("selected")
   private int selected;
   @SerializedName("optsWidth")
   private float optsWidth = -1.0F;
   @SerializedName("rotation")
   private float rotation;
   @SerializedName("xPadding")
   private final float xPadding;
   @SerializedName("fontSize")
   private final float fontSize;
   @SerializedName("anim")
   private EaseOutQuadAnimation anim = null;
   @SerializedName("inputHandler")
   private InputHandler inputHandler;

   public DropdownButton(int n, int n2, int n3, String[] textArray, int n4, ColorPalette palette) {
      super(n, n2, (n2 - 8) / 2, 8, n3, textArray[n4], null, OneConfigSvgs.CHEVRON_DOWN, 0, palette);
      this.xPadding = n3;
      this.selected = n4;
      this.opts = new LinkedHashMap<>(textArray.length);

      for (String text : textArray) {
         this.opts.put(text, new ColorAnimation(palette == ColorPalette.INSTANCE4 ? ColorPalette.INSTANCE3 : palette));
      }

      this.fontSize = n2 == 48 ? 20.0F : n2 / 2 - 4;
   }

   @Override
   public void draw(long l, float f, float f2, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      this.inputHandler = input;
      if (this.optsWidth == -1.0F) {
         for (String text : this.opts.keySet()) {
            this.optsWidth = Math.max(this.optsWidth, nvg.getTextWidth(l, text, this.fontSize, OneConfigFonts.REGULAR));
         }

         this.optsWidth = Math.max(this.optsWidth, super.width);
         super.width = (int)this.optsWidth;
      }

      if (super.toggled) {
         input.clear();
      }

      if (this.opts.size() != 1 && this.anim != null) {
         float progress = this.anim.getProgress();
         this.rotation = progress / ((this.opts.size() - 1) * this.fontSize * 2.0F) * 180.0F;
         nvg.drawRoundedRect(
            l,
            f,
            f2 + super.height,
            this.optsWidth,
            progress,
            super.colorPalette == ColorPalette.INSTANCE4 ? ColorPalette.INSTANCE3.getColorNormal() : super.currentColor,
            8.0F
         );
         if (this.anim.isValid() && !this.anim.isReverse() && super.toggled) {
            float f3 = f2 + super.height;
            int n = 0;

            for (Entry entry : this.opts.entrySet()) {
               String key = (String)entry.getKey();
               ColorAnimation colorAnimation = (ColorAnimation)entry.getValue();
               if (n == this.selected) {
                  n++;
               } else {
                  boolean isHovered = input.isHovered(f, f3, this.optsWidth, 24.0F);
                  boolean flag = isHovered && input.isValid(true);
                  nvg.drawRoundedRect(l, f, f3, this.optsWidth, this.fontSize * 2.0F, colorAnimation.computeInt(isHovered, flag), 8.0F);
                  nvg.drawTextEx(l, key, f + this.xPadding, f3 + this.fontSize, OneConfigColors.WHITE_90, this.fontSize, OneConfigFonts.REGULAR);
                  if (flag) {
                     this.selected = n;
                     this.onKeyTyped(this.selected);
                     super.text = key;
                     super.toggled = false;
                  }

                  n++;
                  f3 += this.fontSize * 2.0F;
               }
            }
         }
      }

      super.draw(l, f, f2, input);
      if (super.toggled) {
         input.getScissorRect();
      }
   }

   @Override
   public void setToggled(boolean flag) {
      super.setToggled(flag);
      if (!flag) {
         this.inputHandler.getScissorRect();
      }
   }

   @Override
   public void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4, int n) {
      if (this.rotation == 0.0F) {
         super.draw(l, svg, f, f2, f3, f4, n);
      } else {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         float f5 = f + f3 / 2.0F;
         float f6 = f2 + f4 / 2.0F;
         nvg.translate(l, f5, f6);
         nvg.rotate(l, (double)this.rotation);
         nvg.draw(l, svg, -(f3 / 2.0F), -(f4 / 2.0F), f3, f4, n);
         nvg.rotate(l, (double)(-this.rotation));
         nvg.translate(l, -f5, -f6);
      }
   }

   @Override
   public void performAction(float f, float f2, InputHandler input) {
      super.performAction(f, f2, input);
      if (input.isValid() && !input.isHovered(f, f2, this.optsWidth, super.height) && super.toggled) {
         super.toggled = false;
         input.clear();
      }

      if (super.toggled && this.anim == null) {
         this.anim = new EaseOutQuadAnimation(200, 0.0F, (this.opts.size() - 1) * this.fontSize * 2.0F, false);
      }

      if (this.anim != null && this.anim.isValid() && this.anim.isReverse()) {
         this.anim = null;
      }

      if (!super.toggled && this.anim != null && !this.anim.isReverse()) {
         this.anim = new EaseOutQuadAnimation(200, 0.0F, (this.opts.size() - 1) * this.fontSize * 2.0F, true);
      }
   }

   public int getSelected() {
      return this.selected;
   }

   public void onKeyTypedEx(int n) {
      this.selected = n;
      super.text = this.opts.keySet().toArray(String[]::new)[n];
      this.onKeyTyped(n);
   }

   public void onKeyTyped(int n) {
   }
}
