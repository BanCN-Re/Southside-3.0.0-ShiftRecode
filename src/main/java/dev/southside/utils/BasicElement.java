package dev.southside.utils;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class BasicElement {
   @SerializedName("width")
   public int width;
   @SerializedName("height")
   public int height;
   @SerializedName("colorPalette")
   public ColorPalette colorPalette;
   @SerializedName("hitBoxX")
   protected int hitBoxX;
   @SerializedName("hitBoxY")
   protected int hitBoxY;
   @SerializedName("hoverFx")
   protected boolean hoverFx;
   @SerializedName("hovered")
   public boolean hovered = false;
   @SerializedName("pressed")
   protected boolean pressed = false;
   @SerializedName("clicked")
   public boolean clicked = false;
   @SerializedName("toggled")
   public boolean toggled = false;
   @SerializedName("disabled")
   public boolean disabled = false;
   @SerializedName("currentColor")
   public int currentColor;
   @SerializedName("radius")
   protected final float radius;
   @SerializedName("block")
   private boolean block = false;
   @SerializedName("colorAnimation")
   public ColorAnimation colorAnimation;

   public BasicElement(int n, int n2, @NotNull ColorPalette palette, boolean flag) {
      this(n, n2, palette, flag, 12.0F);
   }

   public BasicElement(int n, int n2, @NotNull ColorPalette palette, boolean flag, float f) {
      this.height = n2;
      this.width = n;
      this.colorPalette = palette;
      this.hoverFx = flag;
      this.radius = f;
      this.colorAnimation = new ColorAnimation(palette);
   }

   public BasicElement(int n, int n2, boolean flag) {
      this(n, n2, ColorPalette.INSTANCE, flag, 12.0F);
   }

   public void draw(long l, float f, float f2, InputHandler input) {
      this.performAction(f, f2, input);
      NanoVGHelper.INSTANCE.drawRoundedRect(l, f, f2, this.width, this.height, this.currentColor, this.radius);
   }

   public void performAction(float f, float f2, InputHandler input) {
      if (this.disabled) {
         this.hovered = false;
         this.pressed = false;
         this.clicked = false;
      } else {
         this.hovered = input.isHovered(f - this.hitBoxX, f2 - this.hitBoxY, this.width + this.hitBoxX, this.height + this.hitBoxY);
         this.pressed = this.hovered && OneConfigPlatform.getMousePlatform().isKeyDown(0);
         this.clicked = input.isValid(this.block) && this.hovered;
         if (this.clicked) {
            this.toggled = !this.toggled;
            this.performAction();
         }
      }

      if (this.hoverFx) {
         this.currentColor = this.colorAnimation.computeInt(this.hovered, this.pressed);
      } else {
         this.currentColor = this.colorAnimation.computeInt(false, false);
      }
   }

   public void setBlock(boolean flag) {
      this.block = flag;
   }

   public void performAction() {
   }

   public void performActionEx(int n, int n2) {
      this.hitBoxX = n;
      this.hitBoxY = n2;
   }

   public void setWidth(int n) {
      this.width = n;
   }

   public void setHeight(int n) {
      this.height = n;
   }

   public void performAction(ColorPalette palette) {
      if (this.colorPalette.equals(ColorPalette.INSTANCE4) || this.colorPalette.equals(ColorPalette.INSTANCE7)) {
         this.colorAnimation.performAction(palette.getColorNormalf());
      }

      this.colorPalette = palette;
      this.colorAnimation.performAction(palette);
   }

   public ColorPalette getColorPalette() {
      return this.colorPalette;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean isHovered() {
      return this.hovered;
   }

   public boolean isPressed() {
      return this.pressed;
   }

   public boolean isClicked() {
      return this.clicked;
   }

   public boolean isToggled() {
      return this.toggled;
   }

   public void setToggled(boolean flag) {
      this.toggled = flag;
   }

   public boolean isDisabled() {
      return this.disabled;
   }

   public void setDisabled(boolean flag) {
      this.disabled = flag;
   }
}
