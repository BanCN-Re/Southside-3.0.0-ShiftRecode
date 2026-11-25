package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class SliderWidget extends BasicElement {
   public static final int DEFAULT_INT = 1;
   public static final int DEFAULT_INT_VALUE = 2;
   @SerializedName("min")
   private final float min;
   @SerializedName("max")
   private final float max;
   @SerializedName("value")
   protected float value;
   @SerializedName("currentDragPoint")
   public float currentDragPoint;
   @SerializedName("dragPointerSize")
   public float dragPointerSize = 8.0F;
   @SerializedName("dragging")
   private boolean dragging = false;
   @SerializedName("mode")
   private final int mode;
   @SerializedName("mouseWasDown")
   private boolean mouseWasDown = false;
   @SerializedName("inputHandler")
   private InputHandler inputHandler;

   public SliderWidget(int n, float f, float f2, float f3, int n2) {
      super(n2 == 2 ? n : 8, n2 == 2 ? 8 : n, false);
      this.min = f;
      this.max = f2;
      this.mode = n2;
      this.setProgress(f3);
   }

   @Override
   public void draw(long l, float f, float f2, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      this.inputHandler = input;
      if (this.mode == 2) {
         this.currentDragPoint = f + (super.width - this.dragPointerSize) * this.value;
      } else {
         this.currentDragPoint = f2 + (super.height - this.dragPointerSize) * this.value;
      }

      if (!super.disabled) {
         this.performAction(f, f2, input);
      } else {
         nvg.setAlpha(l, 0.5F);
      }

      if (this.dragging) {
         input.clear();
      }

      if (this.mode == 2) {
         nvg.drawRoundedRect(l, f, f2 + 2.0F, super.width, super.height - 4, OneConfigColors.GRAY_300, 3.0F);
         nvg.drawRoundedRect(l, f, f2 + 2.0F, super.width * this.value, super.height - 4, OneConfigColors.PRIMARY_400, 3.0F);
         nvg.drawRoundedRect(l, this.currentDragPoint - this.dragPointerSize / 2.0F, f2 - 8.0F + 6.0F, 12.0F, 12.0F, OneConfigColors.WHITE, 12.0F);
      } else {
         nvg.drawRoundedRect(l, f + 2.0F, f2, super.width - 4, super.height, OneConfigColors.GRAY_300, 3.0F);
         nvg.drawRoundedRect(
            l, f + 2.0F, f2 + super.height - super.height * this.value, super.width - 4, super.height * this.value, OneConfigColors.PRIMARY_400, 3.0F
         );
         nvg.drawRoundedRect(l, f - 8.0F + 6.0F, this.currentDragPoint - this.dragPointerSize / 2.0F, 12.0F, 12.0F, OneConfigColors.WHITE, 12.0F);
      }

      nvg.setAlpha(l, 1.0F);
      if (this.dragging) {
         input.getScissorRect();
      }
   }

   @Override
   public void performAction(float f, float f2, InputHandler input) {
      super.performAction(f, f2, input);
      boolean isKeyDown = OneConfigPlatform.getMousePlatform().isKeyDown(0);
      boolean flag;
      if (this.mode == 2) {
         flag = input.isHovered(f - 6.0F, f2 - 3.0F, super.width + 12, super.height + 6);
      } else {
         flag = input.isHovered(f - 3.0F, f2 - 6.0F, super.width + 6, super.height + 12);
      }

      if (flag && isKeyDown && !this.mouseWasDown) {
         this.dragging = true;
      }

      this.mouseWasDown = isKeyDown;
      if (this.dragging) {
         if (this.mode == 2) {
            this.value = (input.getProgress() - f) / super.width;
         } else {
            this.value = (input.getProgressEx() - f2) / super.height;
         }
      }

      if (this.dragging && input.isValid(true)) {
         this.dragging = false;
         input.clear();
         if (this.mode == 2) {
            this.value = (input.getProgress() - f) / super.width;
         } else {
            this.value = (input.getProgressEx() - f2) / super.height;
         }
      }

      if (this.value < 0.0F) {
         this.value = 0.0F;
      }

      if (this.value > 1.0F) {
         this.value = 1.0F;
      }

      if (this.mode == 2) {
         this.currentDragPoint = f + (super.width - this.dragPointerSize) * this.value;
      } else {
         this.currentDragPoint = f2 + (super.height - this.dragPointerSize) * this.value;
      }
   }

   public static float computeFloat(float x, float y) {
      return x - y;
   }

   public float getProgress() {
      return computeFloat(1.0F, this.value) * (this.max - this.min) + this.min;
   }

   public float getProgressEx() {
      return this.value * (this.max - this.min) + this.min;
   }

   public void setProgress(float f) {
      this.value = (f - this.min) / (this.max - this.min);
   }

   public void setProgressEx(float f) {
      this.setProgress(computeFloat(this.max, f));
   }

   public boolean isDragging() {
      return this.dragging;
   }

   @Override
   public void setDisabled(boolean flag) {
      if (flag) {
         this.performActionEx();
      }

      super.setDisabled(flag);
   }

   public void performActionEx() {
      if (this.dragging && this.inputHandler != null) {
         this.inputHandler.clear();
      }

      this.dragging = false;
      this.mouseWasDown = false;
   }
}
