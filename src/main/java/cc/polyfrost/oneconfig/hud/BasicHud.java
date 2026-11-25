package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.UIMatrixStack;
import dev.southside.utils.ColorValue;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.NumberValue;
import net.minecraft.client.gui.DrawContext;

public abstract class BasicHud extends Hud {
   @SerializedName("background")
   public BooleanValue background = new BooleanValue("Background", "If the background of the HUD is enabled.", false);
   @SerializedName("rounded")
   public BooleanValue rounded = new BooleanValue("Rounded corners", "If the background has rounded corners.", false);
   @SerializedName("border")
   public BooleanValue border = new BooleanValue("Outline/border", "If the hud has an outline.", false);
   @SerializedName("bgColor")
   public ColorValue bgColor = new ColorValue("Background color:", "The color of the background.", new OneColor("#00000000"));
   @SerializedName("borderColor")
   public ColorValue borderColor = new ColorValue("Border color:", "The color of the border.", new OneColor("#00000000"));
   @SerializedName("cornerRadius")
   public NumberValue cornerRadius = new NumberValue("Corner radius:", 0.0F, 10.0F, "The corner radius of the background.", false, 0);
   @SerializedName("borderSize")
   public NumberValue borderSize = new NumberValue("Border thickness:", 0.0F, 10.0F, "The thickness of the outline.", false, 0);
   @SerializedName("paddingX")
   public NumberValue paddingX = new NumberValue("X-Padding", 0.0F, 10.0F, "The horizontal padding of the HUD.", false, 0);
   @SerializedName("paddingY")
   public NumberValue paddingY = new NumberValue("Y-Padding", 0.0F, 10.0F, "The vertical padding of the HUD.", false, 0);
   @SerializedName("defaultPaddingX")
   protected float defaultPaddingX;
   @SerializedName("defaultPaddingY")
   protected float defaultPaddingY;
   private static final long DEFAULT_LONG = -8814239224673861512L;

   public BasicHud(
      float f,
      float f2,
      float f3,
      boolean flag,
      boolean flag2,
      float f4,
      float f5,
      float f6,
      OneColor color,
      boolean flag3,
      float f7,
      OneColor color2
   ) {
      super(f, f2, f3);
      this.background.performAction(flag);
      this.rounded.performAction(flag2);
      this.cornerRadius.performAction(f4);
      this.paddingX.performAction(f5);
      this.paddingY.performAction(f6);
      this.bgColor.performAction(color);
      this.border.performAction(flag3);
      this.borderSize.performAction(f7);
      this.borderColor.performAction(color2);
      super.position.getValue().performActionEx(this.computeFloat(f3, true) + f5 * f3 * 2.0F, this.setScale(f3, true) + f6 * f3 * 2.0F);
      this.defaultPaddingX = f5;
      this.defaultPaddingY = f6;
   }

   public BasicHud(float x, float y) {
      this(x, y, 1.0F, true, false, 2.0F, 5.0F, 5.0F, new OneColor(0, 0, 0, (int)DEFAULT_LONG), false, 2.0F, new OneColor(0, 0, 0));
   }

   @Override
   public void drawAll(DrawContext context, UIMatrixStack matrices, boolean flag) {
      if (flag || this.isValid()) {
         this.setActive(flag);
         super.position
            .getValue()
            .performActionEx(
               this.computeFloat(((Number)super.scale.getValue()).floatValue(), flag)
                  + ((Number)this.paddingX.getValue()).floatValue() * ((Number)super.scale.getValue()).floatValue() * 2.0F,
               this.setScale(((Number)super.scale.getValue()).floatValue(), flag)
                  + ((Number)this.paddingY.getValue()).floatValue() * ((Number)super.scale.getValue()).floatValue() * 2.0F
            );
         if (this.shouldDrawBackground() && (Boolean)this.background.getValue()) {
            this.drawBackground(
               super.position.getValue().getProgress(),
               super.position.getValue().getProgressEx(),
               super.position.getValue().getWidth(),
               super.position.getValue().getHeight(),
               ((Number)super.scale.getValue()).floatValue()
            );
         }

         this.draw(
            context,
            matrices,
            super.position.getValue().getProgress() + ((Number)this.paddingX.getValue()).floatValue() * ((Number)super.scale.getValue()).floatValue(),
            super.position.getValue().getProgressEx() + ((Number)this.paddingY.getValue()).floatValue() * ((Number)super.scale.getValue()).floatValue(),
            ((Number)super.scale.getValue()).floatValue(),
            flag
         );
      }
   }

   @Override
   public void resetPosition() {
      this.paddingX.performAction(this.defaultPaddingX);
      this.paddingY.performAction(this.defaultPaddingY);
      super.resetPosition();
   }

   @Override
   public void performAction(float f, boolean flag) {
      super.scale.performAction(f);
      super.position
         .getValue()
         .performActionAlt(
            this.computeFloat(f, flag) + ((Number)this.paddingX.getValue()).floatValue() * f * 2.0F,
            this.setScale(f, flag) + ((Number)this.paddingY.getValue()).floatValue() * f * 2.0F
         );
   }

   public boolean shouldDrawBackground() {
      return true;
   }

   public void drawBackground(float f, float f2, float f3, float f4, float f5) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      nvg.performAction(
         true,
         l -> {
            if ((Boolean)this.rounded.getValue()) {
               nvg.drawRoundedRect(
                  l, f, f2, f3, f4, ((OneColor)this.bgColor.getValue()).fromHsb(), ((Number)this.cornerRadius.getValue()).floatValue() * f5
               );
               if ((Boolean)this.border.getValue()) {
                  nvg.drawRoundedRect(
                     l,
                     f - ((Number)this.borderSize.getValue()).floatValue() * f5,
                     f2 - ((Number)this.borderSize.getValue()).floatValue() * f5,
                     f3 + ((Number)this.borderSize.getValue()).floatValue() * f5,
                     f4 + ((Number)this.borderSize.getValue()).floatValue() * f5,
                     ((OneColor)this.borderColor.getValue()).fromHsb(),
                     ((Number)this.cornerRadius.getValue()).floatValue() * f5,
                     ((Number)this.borderSize.getValue()).floatValue() * f5
                  );
               }
            } else {
               nvg.drawRect(l, f, f2, f3, f4, ((OneColor)this.bgColor.getValue()).fromHsb());
               if ((Boolean)this.border.getValue()) {
                  float f6 = f - ((Number)this.borderSize.getValue()).floatValue() * f5;
                  float f7 = f2 - ((Number)this.borderSize.getValue()).floatValue() * f5;
                  float f8 = f3 + ((Number)this.borderSize.getValue()).floatValue() * f5;
                  float f9 = f4 + ((Number)this.borderSize.getValue()).floatValue() * f5;
                  int n = ((OneColor)this.borderColor.getValue()).fromHsb();
                  float f10 = ((Number)this.borderSize.getValue()).floatValue() * f5;
                  nvg.drawRoundedRect(l, f6, f7, f8, f9, n, 0.0F, f10);
               }
            }
         }
      );
   }
}
