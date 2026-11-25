package dev.southside.utils;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class BasicButton extends BasicElement {
   @SerializedName("text")
   public String text;
   @SerializedName("icon1")
   protected SvgAsset icon1;
   @SerializedName("icon2")
   protected SvgAsset icon2;
   @SerializedName("alignment")
   private final int alignment;
   @SerializedName("fontSize")
   private final float fontSize;
   @SerializedName("cornerRadius")
   private final float cornerRadius;
   @SerializedName("xSpacing")
   private final float xSpacing;
   @SerializedName("xPadding")
   private final float xPadding;
   @SerializedName("iconSize")
   private final int iconSize;
   @SerializedName("x")
   public float x;
   @SerializedName("y")
   public float y;
   public static final int DEFAULT_INT = 0;
   public static final int DEFAULT_INT_VALUE = 2;
   public static final int DEFAULT_INT_DATA = 3;
   public static final int DEFAULT_INT_REF = 32;
   public static final int DEFAULT_INT_PRIMARY = 36;
   public static final int DEFAULT_INT_SECONDARY = 40;
   public static final int DEFAULT_INT_TERTIARY = 48;
   @SerializedName("toggleable")
   private boolean toggleable = false;
   @SerializedName("page")
   private GuiPage page;
   @SerializedName("runnable")
   private Runnable runnable;

   public BasicButton(int n, int n2, int n3, int n4, int n5, String text2, SvgAsset svg, SvgAsset svg2, int n6, @NotNull ColorPalette palette) {
      super(n, 32, palette, true);
      if (text2 != null) {
         this.text = text2;
      }

      if (svg != null) {
         this.icon1 = svg;
      }

      if (svg2 != null) {
         this.icon2 = svg2;
      }

      super.colorPalette = palette;
      this.alignment = n6;
      this.cornerRadius = n2 == 48 ? 14.0F : 10.0F;
      this.xSpacing = n4;
      this.xPadding = n5;
      super.height = n2;
      this.iconSize = n3;
      this.fontSize = n2 == 48 ? 20.0F : n2 / 2 - 4;
   }

   public BasicButton(int n, int n2, String text2, SvgAsset svg, SvgAsset svg2, int n3, @NotNull ColorPalette palette) {
      this(n, n2, n2 / 2, n2 == 48 ? 12 : 8, n2 != 36 && n2 != 40 ? (n2 == 48 ? 20 : 12) : 16, text2, svg, svg2, n3, palette);
   }

   public BasicButton(int n, int n2, SvgAsset svg, int n3, @NotNull ColorPalette palette) {
      this(n, n2, null, svg, null, n3, palette);
   }

   public BasicButton(int n, int n2, String text2, int n3, @NotNull ColorPalette palette) {
      this(n, n2, text2, null, null, n3, palette);
   }

   public BasicButton(int n, int n2, String text2, ColorPalette palette) {
      this(n, n2, text2, 2, palette);
   }

   public BasicButton(int n, int n2, SvgAsset svg, ColorPalette palette) {
      this(n, n2, svg, 2, palette);
   }

   @Override
   public void draw(long l, float f, float f2, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      this.x = f;
      this.y = f2;
      this.performAction(f, f2, input);
      if (super.disabled) {
         nvg.setAlpha(l, 0.5F);
      }

      float f3 = 0.0F;
      int n;
      if (super.colorPalette != ColorPalette.INSTANCE4 && super.colorPalette != ColorPalette.INSTANCE7) {
         nvg.drawRoundedRect(l, f, f2, super.width, super.height, super.currentColor, this.cornerRadius);
         n = ArgbColorUtils.withAlpha(OneConfigColors.WHITE, (int)(super.colorAnimation.getProgress() * 255.0F));
      } else {
         n = super.currentColor;
      }

      float f4 = f + super.width / 2.0F;
      float f5 = f2 + super.height / 2.0F - this.iconSize / 2.0F;
      float f6 = f2 + super.height / 2.0F + this.fontSize / 8.0F;
      if (this.text != null) {
         f3 += nvg.getTextWidth(l, this.text, this.fontSize, OneConfigFonts.ONE_CONFIG_FONT);
      }

      if (this.alignment == 2) {
         if (this.icon1 != null && this.icon2 == null && this.text == null) {
            this.draw(l, this.icon1, f4 - this.iconSize / 2.0F, f5, this.iconSize, this.iconSize, n);
         } else {
            if (this.icon1 != null) {
               f3 += this.iconSize + this.xSpacing;
            }

            if (this.icon2 != null) {
               f3 += this.iconSize + this.xSpacing;
            }

            if (this.text != null) {
               nvg.drawTextEx(
                  l,
                  this.text,
                  f4 - f3 / 2.0F + (this.icon1 == null ? 0.0F : this.iconSize + this.xSpacing),
                  f6,
                  n,
                  this.fontSize,
                  OneConfigFonts.ONE_CONFIG_FONT
               );
            }

            if (this.icon1 != null) {
               this.draw(l, this.icon1, f4 - f3 / 2.0F, f5, this.iconSize, this.iconSize, n);
            }

            if (this.icon2 != null) {
               this.draw(l, this.icon2, f4 + f3 / 2.0F - this.iconSize, f5, this.iconSize, this.iconSize, n);
            }
         }
      } else if (this.alignment == 3) {
         if (this.text != null) {
            nvg.drawTextEx(l, this.text, f4 - f3 / 2.0F, f6, n, this.fontSize, OneConfigFonts.ONE_CONFIG_FONT);
         }

         if (this.icon1 != null) {
            this.draw(l, this.icon1, f + this.xPadding, f5, this.iconSize, this.iconSize, n);
         }

         if (this.icon2 != null) {
            this.draw(l, this.icon2, f + super.width - this.xPadding - this.iconSize, f5, this.iconSize, this.iconSize, n);
         }
      } else if (this.alignment == 0) {
         f3 = this.xPadding;
         if (this.icon1 != null) {
            this.draw(l, this.icon1, f + f3, f5, this.iconSize, this.iconSize, n);
            f3 += this.iconSize + this.xSpacing;
         }

         if (this.text != null) {
            nvg.drawTextEx(l, this.text, f + f3, f6, n, this.fontSize, OneConfigFonts.ONE_CONFIG_FONT);
         }

         if (this.icon2 != null) {
            this.draw(l, this.icon2, f + super.width - this.xPadding - this.iconSize, f5, this.iconSize, this.iconSize, n);
         }
      }

      if (super.disabled) {
         nvg.setAlpha(l, 1.0F);
      }
   }

   public void draw(long l, SvgAsset svg, float f, float f2, float f3, float f4, int n) {
      NanoVGHelper.INSTANCE.draw(l, svg, f, f2, f3, f4, n);
   }

   @Override
   public void performAction() {
      if (!super.disabled) {
         if (this.page != null && OneConfigGui.INSTANCE != null) {
            OneConfigGui.INSTANCE.performAction(this.page);
         } else if (this.runnable != null) {
            this.runnable.run();
         }

         if (this.toggleable && super.toggled) {
            this.performAction(ColorPalette.INSTANCE2);
         } else if (this.toggleable) {
            this.performAction(ColorPalette.INSTANCE3);
         }
      }
   }

   @Override
   public void setToggled(boolean flag) {
      super.toggled = flag;
      if (flag && this.toggleable) {
         this.performAction(ColorPalette.INSTANCE2);
      } else if (this.toggleable) {
         this.performAction(ColorPalette.INSTANCE3);
      }
   }

   public void setToggleable(boolean flag) {
      this.toggleable = flag;
   }

   public void setPage(GuiPage guiPage) {
      this.page = guiPage;
   }

   public void setRunnable(Runnable action) {
      this.runnable = action;
   }

   public GuiPage getPage() {
      return this.page;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text2) {
      this.text = text2;
   }

   public void setIcon1(SvgAsset svg) {
      this.icon1 = svg;
   }

   public void setIcon2(SvgAsset svg) {
      this.icon2 = svg;
   }

   public boolean isValid() {
      return this.page != null || this.runnable != null;
   }
}
