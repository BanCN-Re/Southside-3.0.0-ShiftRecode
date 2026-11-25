package cc.polyfrost.oneconfig.gui.elements;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.ArgbColorUtils;
import dev.southside.utils.BasicButton;
import dev.southside.utils.BasicElement;
import dev.southside.utils.DropdownButton;
import dev.southside.utils.InputHandler;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.OneConfigColors;
import dev.southside.utils.OneConfigGui;
import dev.southside.utils.OneConfigImages;
import dev.southside.utils.OneConfigPlatform;
import dev.southside.utils.OneConfigSvgs;
import dev.southside.utils.ScissorHelper;
import dev.southside.utils.ScissorRect;
import dev.southside.utils.SliderWidget;
import dev.southside.utils.WindowMetrics;
import dev.southside.utils.MousePositionHelper.Inner;
import dev.southside.utils.NanoVGHelper.Mode;
import dev.southside.utils.freecookie.FreeCookieAPI;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class ColorSelector {
   @SerializedName("color")
   public OneColor color;
   private static final BasicButton textUtil2 = new BasicButton(32, 32, OneConfigSvgs.X_CLOSE, 2, ColorPalette.INSTANCE7);
   @SerializedName("faveBtn")
   private final BasicButton faveBtn = new BasicButton(32, 32, OneConfigSvgs.HEART_OUTLINE, 2, ColorPalette.INSTANCE3);
   @SerializedName("recentBtn")
   private final BasicButton recentBtn = new BasicButton(32, 32, OneConfigSvgs.HISTORY, 2, ColorPalette.INSTANCE3) {
      public void execute2() {
         super.performAction();
         if (!ColorSelector.this.recentColors.get(0).getColor().equals(ColorSelector.this.color)) {
            ColorSelector.assignRecentColor(ColorSelector.this.color.clone());
            ColorSelector.this.recentColors.remove(5);
            ColorSelector.this.recentColors.add(0, new ColorSelector.Inner10(ColorSelector.this.color.clone()));
         }
      }
   };
   private static final BasicButton textUtil22 = new BasicButton(32, 32, OneConfigSvgs.PICKER, 2, ColorPalette.INSTANCE4);
   @SerializedName("modeDropdown")
   public final DropdownButton modeDropdown = new DropdownButton(128, 36, 18, new String[]{"Solid Color", "Chroma"}, 0, ColorPalette.INSTANCE4) {
      public void execute(int n5) {
         switch (n5) {
            case 0:
            default:
               ColorSelector.this.picker = ColorSelector.this.new Inner2();
               break;
            case 1:
               ColorSelector.this.picker = ColorSelector.this.new Inner4();
         }
      }
   };
   @SerializedName("inputTypeDropdown")
   public final DropdownButton inputTypeDropdown = new DropdownButton(90, 36, 20, new String[]{"HEX", "HSBA"}, 0, ColorPalette.INSTANCE4) {
      public void execute(int n5) {
         switch (n5) {
            case 0:
            default:
               ColorSelector.this.colorInput = ColorSelector.this.new Inner3();
               break;
            case 1:
               ColorSelector.this.colorInput = ColorSelector.this.new Inner5Impl();
         }
      }
   };
   @SerializedName("favoriteColors")
   private final ArrayList<ColorSelector.Inner10> favoriteColors = new ArrayList<>(6);
   @SerializedName("recentColors")
   public final ArrayList<ColorSelector.Inner10> recentColors = new ArrayList<>(6);
   @SerializedName("x")
   private float x;
   @SerializedName("y")
   private float y;
   @SerializedName("dragX")
   private float dragX;
   @SerializedName("dragY")
   private float dragY;
   @SerializedName("colorInput")
   @NotNull
   public ColorSelector.Inner5 colorInput;
   @SerializedName("picker")
   @NotNull
   public ColorSelector.Inner6 picker;
   public static float factor = 296.0F;
   public static float factorValue = 398.0F;
   @SerializedName("pickerIsActive")
   public boolean pickerIsActive;
   @SerializedName("mouseWasDown")
   public boolean mouseWasDown;
   @SerializedName("dragging")
   private boolean dragging;
   @SerializedName("hasAlpha")
   public final boolean hasAlpha;
   @SerializedName("inputScissor")
   private ScissorRect inputScissor;
   @SerializedName("inputHandler")
   private final InputHandler inputHandler;

   public ColorSelector(OneColor color2, float f, float f2, boolean flag, InputHandler input) {
      this.inputHandler = input;

      for (OneColor color3 : OneConfigConfig.recentColors) {
         this.recentColors.add(new ColorSelector.Inner10(color3));
      }

      for (OneColor color4 : OneConfigConfig.favoriteColors) {
         this.favoriteColors.add(new ColorSelector.Inner10(color4));
      }

      while (this.favoriteColors.size() < 6) {
         this.favoriteColors.add(new ColorSelector.Inner10(new OneColor(0, 0, 0, 0)));
      }

      while (this.recentColors.size() < 6) {
         this.recentColors.add(new ColorSelector.Inner10(new OneColor(0, 0, 0, 0)));
      }

      this.color = color2;
      this.hasAlpha = flag;
      if (color2.getHeightWithOffset() != -1) {
         this.modeDropdown.onKeyTypedEx(1);
      } else {
         this.picker = new ColorSelector.Inner2();
      }

      this.x = f - factor / 2.0F;
      this.y = Math.max(0.0F, f2 - factorValue);
      this.colorInput = new ColorSelector.Inner3();
   }

   public void render(long l) {
      if (this.inputScissor != null) {
         this.inputHandler.remove(this.inputScissor);
      }

      if (textUtil22.toggled) {
         this.inputHandler.getScissorRect();
         int n = NanoVGHelper.INSTANCE.getIntList((int)Inner.getX(), WindowMetrics.getWindowHeight() - (int)Inner.getY(), 1, 1)[0];
         NanoVGHelper.INSTANCE
            .drawRoundedRect(l, this.inputHandler.getProgress() - 16.0F, this.inputHandler.getProgressEx() - 33.0F, 32.0F, 32.0F, -1, 16.0F);
         NanoVGHelper.INSTANCE
            .drawRoundedRect(l, this.inputHandler.getProgress() - 15.0F, this.inputHandler.getProgressEx() - 32.0F, 30.0F, 30.0F, n, 15.0F);
         if (this.inputHandler.isValid(true)) {
            this.performAction(new OneColor(n));
            textUtil22.toggled = false;
            this.inputHandler.clear();
         }
      } else {
         ScissorRect clip = ScissorHelper.INSTANCE.scissor(l, this.x - 3.0F, this.y - 3.0F, factor + 6.0F, factorValue + 6.0F);
         NanoVGHelper.INSTANCE.drawRoundedRect(l, this.x - 3.0F, this.y - 3.0F, factor + 3.0F, factorValue + 3.0F, OneConfigColors.GRAY_700, 20.0F, 3.0F);
         NanoVGHelper.INSTANCE.drawRoundedRect(l, this.x, this.y, factor, factorValue, OneConfigColors.GRAY_850, 20.0F);
         textUtil2.draw(l, this.x + 248.0F + 8.0F, this.y + 12.0F, this.inputHandler);
         this.picker.draw(l, this.x + 16.0F, this.y + 48.0F, this.inputHandler);
         if (this.modeDropdown.getSelected() == 1) {
            this.y += 20.0F;
            this.inputHandler.clear();
         }

         this.colorInput.draw(l, this.x + 100.0F, this.y + 260.0F, this.inputHandler);
         textUtil22.draw(l, this.x + 252.0F, this.y + 260.0F, this.inputHandler);
         this.faveBtn.draw(l, this.x + 12.0F, this.y + 304.0F, this.inputHandler);
         if (this.faveBtn.isClicked()) {
            this.favoriteColors.remove(5);
            this.favoriteColors.add(0, new ColorSelector.Inner10(this.color.clone()));
         }

         this.recentBtn.draw(l, this.x + 12.0F, this.y + 348.0F, this.inputHandler);

         for (int i = 0; i < 6; i++) {
            ColorSelector.Inner10 inner10 = this.favoriteColors.get(i);
            inner10.draw(l, this.x + 52.0F + i * 40, this.y + 304.0F, this.inputHandler);
            if (inner10.isClicked()) {
               this.performAction(inner10.getColor().clone());
            }

            ColorSelector.Inner10 inner102 = this.recentColors.get(i);
            inner102.draw(l, this.x + 52.0F + i * 40, this.y + 348.0F, this.inputHandler);
            if (inner102.isClicked()) {
               this.performAction(inner102.getColor().clone());
            }
         }

         this.inputTypeDropdown.draw(l, this.x - 2.0F, this.y + 260.0F, this.inputHandler);
         if (this.modeDropdown.getSelected() == 1) {
            this.y -= 20.0F;
         }

         this.modeDropdown.draw(l, this.x - 2.0F, this.y + 8.0F, this.inputHandler);
         boolean flag = OneConfigPlatform.getMousePlatform().isKeyDown(0) && this.inputHandler.isHovered(this.x, this.y, factor, 48.0F);
         if (flag && OneConfigPlatform.getMousePlatform().isKeyDown(0) && !this.mouseWasDown) {
            this.dragging = true;
            this.dragX = this.inputHandler.getProgress() - this.x;
            this.dragY = this.inputHandler.getProgressEx() - this.y;
         }

         if (this.dragging) {
            this.x = this.inputHandler.getProgress() - this.dragX;
            this.y = this.inputHandler.getProgressEx() - this.dragY;
         }

         this.inputScissor = this.inputHandler.getScissorRect(this.x, this.y, factor, factorValue);
         if (textUtil2.isClicked() && !this.pickerIsActive) {
            OneConfigGui.INSTANCE.getOneColor2();
         }

         ScissorHelper.INSTANCE.resetScissor(l, clip);
         if (this.dragging && this.inputHandler.isValid(true)) {
            this.dragging = false;
         }

         if (this.pickerIsActive && this.inputHandler.isValid(true)) {
            this.pickerIsActive = false;
         }

         this.mouseWasDown = OneConfigPlatform.getMousePlatform().isKeyDown(0);
      }
   }

   public OneColor getColor() {
      return this.color;
   }

   public void onKeyTyped(int n) {
      this.colorInput.onKeyTyped(n);
   }

   public void onKeyTypedEx(int n) {
      this.colorInput.onKeyTypedEx(n);
   }

   public void onKeyTypedAlt(int n) {
      this.colorInput.onKeyTypedAlt(n);
   }

   public void onKeyTypedImpl(int n) {
      this.colorInput.onKeyTypedImpl(n);
   }

   public void performAction() {
      this.inputHandler.clear();
      int n = 0;

      for (ColorSelector.Inner10 inner10 : this.favoriteColors) {
         if (!inner10.isValid()) {
            assignFavoriteColor(inner10.getColor().clone(), n);
            n++;
         }
      }

      assignRecentColor(this.color.clone());
      OneConfigConfig.getInstance().saveToJson();
   }

   public static void assignRecentColor(OneColor color2) {
      if (OneConfigConfig.recentColors.size() == 6) {
         OneConfigConfig.recentColors.remove(5);
         OneConfigConfig.recentColors.add(0, color2);
      } else {
         OneConfigConfig.recentColors.add(color2);
      }
   }

   public static void assignFavoriteColor(OneColor color2, int n) {
      if (n < 0) {
         n = 0;
      }

      if (n > 5) {
         n = 5;
      }

      if (n >= OneConfigConfig.favoriteColors.size()) {
         OneConfigConfig.favoriteColors.add(color2);
      } else {
         OneConfigConfig.favoriteColors.set(n, color2);
      }
   }

   private void performAction(OneColor color2) {
      if (color2.fromHsb() != 0) {
         this.color = color2;
         this.picker.performAction();
         this.colorInput.performAction();
      }
   }

   public boolean isHasAlpha() {
      return this.hasAlpha;
   }

   public static final class Inner10 extends BasicElement {
      @SerializedName("color")
      private OneColor color;

      public Inner10(OneColor color2) {
         super(
            32,
            32,
            new ColorPalette(
               color2.fromHsb(), color2.blendColor().brighter().getRGB(), ArgbColorUtils.withAlpha(color2.fromHsb(), (int)(color2.getHeightCustom() * 0.8F))
            ),
            true
         );
         this.color = color2;
      }

      @Override
      public void draw(long l, float f, float f2, InputHandler input) {
         NanoVGHelper.INSTANCE.drawRoundedRect(l, f, f2, 32.0F, 32.0F, OneConfigColors.GRAY_900, 8.0F);
         NanoVGHelper.INSTANCE.drawRoundedRect(l, f, f2, 32.0F, 32.0F, this.color.fromHsb(), 8.0F);
         super.performAction(f, f2, input);
      }

      public OneColor getColor() {
         return this.color;
      }

      public void setColor(OneColor color2) {
         this.color = color2;
      }

      public boolean isValid() {
         return this.color.fromHsb() == 0;
      }
   }

   private final class Inner2 implements ColorSelector.Inner6 {
      @SerializedName("cursorX")
      private float cursorX;
      @SerializedName("cursorY")
      private float cursorY;
      @SerializedName("alphaSlider")
      private final ColorSelector.Inner9 alphaSlider;
      @SerializedName("hueSlider")
      private final ColorSelector.Inner9 hueSlider;

      public Inner2() {
         ColorSelector.factor = 296.0F;
         ColorSelector.factorValue = 398.0F;
         this.cursorX = ColorSelector.this.color.getHeightDirect() / 100.0F * 200.0F;
         this.cursorY = (1.0F - ColorSelector.this.color.getHeightSafe() / 100.0F) * 200.0F;
         this.alphaSlider = new ColorSelector.Inner9(
            200, 0.0F, 255.0F, 255 - ColorSelector.this.color.getHeightCustom(), OneConfigColors.TRANSPARENT, ColorSelector.this.color.fromHsb(true)
         );
         this.hueSlider = new ColorSelector.Inner9(200, 0.0F, 360.0F, 360 - ColorSelector.this.color.getHeightImpl());
         ColorSelector.this.color.onKeyTypedEx(-1);
         if (!ColorSelector.this.hasAlpha) {
            this.alphaSlider.setDisabled(true);
         }
      }

      @Override
      public void draw(long l, float f, float f2, InputHandler input) {
         NanoVGHelper.INSTANCE.drawRoundedRect(l, f, f2, 200.0F, 200.0F, ColorSelector.this.color.fromHsb(true));
         this.draw(l, f + this.cursorX, f2 + this.cursorY, ColorSelector.this.color.fromHsb());
         this.hueSlider.draw(l, f + 216.0F, f2, input);
         this.alphaSlider.draw(l, f + 248.0F, f2, input);
         this.alphaSlider.performAction(ColorSelector.this.color.getHeightByIndex(), OneConfigColors.TRANSPARENT);
         if (this.hueSlider.isDragging() || this.alphaSlider.isDragging()) {
            ColorSelector.this.color
               .performAction(
                  (int)this.hueSlider.getProgress(),
                  ColorSelector.this.color.getHeightDirect(),
                  ColorSelector.this.color.getHeightSafe(),
                  (int)this.alphaSlider.getProgress()
               );
            ColorSelector.this.colorInput.performAction();
         }

         boolean flag = OneConfigPlatform.getMousePlatform().isKeyDown(0) && input.isHovered(f, f2, 200.0F, 200.0F);
         if (flag && OneConfigPlatform.getMousePlatform().isKeyDown(0) && !ColorSelector.this.mouseWasDown) {
            ColorSelector.this.pickerIsActive = true;
         }

         if (ColorSelector.this.pickerIsActive) {
            this.cursorX = input.getProgress() - f;
            this.cursorY = input.getProgressEx() - f2;
            if (this.cursorX < 0.0F) {
               this.cursorX = 0.0F;
            }

            if (this.cursorY < 0.0F) {
               this.cursorY = 0.0F;
            }

            if (this.cursorX > 200.0F) {
               this.cursorX = 200.0F;
            }

            if (this.cursorY > 200.0F) {
               this.cursorY = 200.0F;
            }

            float f3 = this.cursorX / 200.0F;
            float f4 = Math.abs(this.cursorY / 200.0F - 1.0F);
            ColorSelector.this.color
               .performAction((int)this.hueSlider.getProgress(), Math.round(f3 * 100.0F), Math.round(f4 * 100.0F), (int)this.alphaSlider.getProgress());
         }
      }

      @Override
      public void performAction() {
         this.cursorX = ColorSelector.this.color.getHeightDirect() / 100.0F * 200.0F;
         this.cursorY = (1.0F - ColorSelector.this.color.getHeightSafe() / 100.0F) * 200.0F;
         this.alphaSlider.setProgressEx(ColorSelector.this.color.getHeightCustom());
         this.hueSlider.setProgressEx(ColorSelector.this.color.getHeightImpl());
         this.alphaSlider.performAction(ColorSelector.this.color.getHeightByIndex(), OneConfigColors.TRANSPARENT);
      }
   }

   public final class Inner3 implements ColorSelector.Inner5 {
      @SerializedName("alphaInput")
      public final FreeCookieAPI alphaInput = new FreeCookieAPI(56, 32, true, 4.0F) {
         public void execute() {
            Inner3.this.alphaInput.setInput(Math.round(ColorSelector.this.color.getHeightCustom() / 2.55F) + "%");
         }
      };
      @SerializedName("hexInput")
      public final FreeCookieAPI hexInput = new FreeCookieAPI(80, 32, true, 4.0F) {
         public void execute() {
            Inner3.this.hexInput.setInput("#" + ColorSelector.this.color.getDisplayText());
         }
      };
      @SerializedName("editing")
      private boolean editing = false;

      public Inner3() {
         this.hexInput.setBoarderThickness(1.0F);
         this.alphaInput.setBoarderThickness(1.0F);
         this.hexInput.setInput("#" + ColorSelector.this.color.getDisplayText());
         this.alphaInput.setInput(Math.round(ColorSelector.this.color.getHeightCustom() / 2.55F) + "%");
         if (!ColorSelector.this.hasAlpha) {
            this.alphaInput.setDisabled(true);
         }
      }

      @Override
      public void draw(long l, float f, float f2, InputHandler input) {
         this.hexInput.draw(l, f, f2, input);
         this.alphaInput.draw(l, f + 88.0F, f2, input);
         this.hexInput.setErrored(false);
         this.alphaInput.setErrored(false);
         this.editing = false;
         if (this.hexInput.isToggled()) {
            this.editing = true;

            try {
               ColorSelector.this.color.toHsb(this.hexInput.getInput());
               ColorSelector.this.picker.performAction();
            } catch (Exception e) {
               this.hexInput.setErrored(true);
            }
         }

         if (this.alphaInput.isToggled()) {
            this.editing = true;
            String text = this.alphaInput.getInput().endsWith("%")
               ? this.alphaInput.getInput().substring(0, this.alphaInput.getInput().length() - 1)
               : this.alphaInput.getInput();

            try {
               ColorSelector.this.color.onKeyTyped(Math.round(Math.max(0.0F, Math.min(Float.parseFloat(text), 100.0F)) * 2.55F));
               ColorSelector.this.picker.performAction();
            } catch (Exception e2) {
               this.alphaInput.setErrored(true);
            }
         }
      }

      @Override
      public void performAction() {
         this.hexInput.setInput("#" + ColorSelector.this.color.getDisplayText());
         this.alphaInput.setInput(Math.round(ColorSelector.this.color.getHeightCustom() / 2.55F) + "%");
      }

      @Override
      public void onKeyTyped(int n) {
         this.hexInput.onKeyTyped(n);
         this.alphaInput.onKeyTyped(n);
      }

      @Override
      public void onKeyTypedEx(int n) {
         this.hexInput.onKeyTypedEx(n);
         this.alphaInput.onKeyTypedEx(n);
      }

      @Override
      public void onKeyTypedAlt(int n) {
         this.hexInput.onKeyTypedAlt(n);
         this.alphaInput.onKeyTypedAlt(n);
      }

      @Override
      public void onKeyTypedImpl(int n) {
         this.hexInput.onKeyTypedImpl(n);
         this.alphaInput.onKeyTypedImpl(n);
      }

      @Override
      public boolean isEditing() {
         return this.editing;
      }
   }

   private final class Inner4 implements ColorSelector.Inner6 {
      @SerializedName("cursorX")
      private float cursorX;
      @SerializedName("cursorY")
      private float cursorY;
      @SerializedName("alphaSlider")
      private final ColorSelector.Inner9 alphaSlider;
      @SerializedName("speedSlider")
      public final SliderWidget speedSlider = new SliderWidget(200, 1.0F, 30.0F, 1.0F, 2);

      public Inner4() {
         ColorSelector.factor = 296.0F;
         ColorSelector.factorValue = 408.0F;
         this.cursorX = ColorSelector.this.color.getHeightDirect() / 100.0F * 200.0F;
         this.cursorY = (1.0F - ColorSelector.this.color.getHeightSafe() / 100.0F) * 200.0F;
         this.alphaSlider = new ColorSelector.Inner9(
            200, 0.0F, 255.0F, 255 - ColorSelector.this.color.getHeightCustom(), OneConfigColors.TRANSPARENT, ColorSelector.this.color.fromHsb(true)
         );
         if (ColorSelector.this.color.getHeightWithOffset() == -1) {
            ColorSelector.this.color.onKeyTypedEx(30);
         }

         this.speedSlider.setProgressEx(ColorSelector.this.color.getHeightWithOffset());
         if (!ColorSelector.this.hasAlpha) {
            this.alphaSlider.setDisabled(true);
         }
      }

      @Override
      public void draw(long l, float f, float f2, InputHandler input) {
         this.speedSlider.draw(l, f, f2, input);
         f2 += 20.0F;
         NanoVGHelper.INSTANCE.drawRoundedRect(l, f, f2, 200.0F, 200.0F, ColorSelector.this.color.fromHsb(true));
         this.draw(l, f + this.cursorX, f2 + this.cursorY, ColorSelector.this.color.fromHsb());
         this.alphaSlider.draw(l, f + 216.0F, f2, input);
         this.alphaSlider.performAction(ColorSelector.this.color.getHeightByIndex(), OneConfigColors.TRANSPARENT);
         if (this.alphaSlider.isDragging() || this.speedSlider.isDragging()) {
            ColorSelector.this.color
               .performAction(
                  ColorSelector.this.color.getHeightImpl(),
                  ColorSelector.this.color.getHeightDirect(),
                  ColorSelector.this.color.getHeightSafe(),
                  (int)this.alphaSlider.getProgress()
               );
            ColorSelector.this.color.onKeyTypedEx(Math.round(this.speedSlider.getProgress()));
         }

         if (!ColorSelector.this.colorInput.isEditing()) {
            ColorSelector.this.colorInput.performAction();
         }

         boolean flag = OneConfigPlatform.getMousePlatform().isKeyDown(0) && input.isHovered(f, f2, 200.0F, 200.0F);
         if (flag && OneConfigPlatform.getMousePlatform().isKeyDown(0) && !ColorSelector.this.mouseWasDown) {
            ColorSelector.this.pickerIsActive = true;
         }

         if (ColorSelector.this.pickerIsActive && !ColorSelector.this.colorInput.isEditing()) {
            this.cursorX = input.getProgress() - f;
            this.cursorY = input.getProgressEx() - f2;
            if (this.cursorX < 0.0F) {
               this.cursorX = 0.0F;
            }

            if (this.cursorY < 0.0F) {
               this.cursorY = 0.0F;
            }

            if (this.cursorX > 200.0F) {
               this.cursorX = 200.0F;
            }

            if (this.cursorY > 200.0F) {
               this.cursorY = 200.0F;
            }

            float f3 = this.cursorX / 200.0F;
            float f4 = Math.abs(this.cursorY / 200.0F - 1.0F);
            ColorSelector.this.color
               .performAction(
                  ColorSelector.this.color.getHeightImpl(), Math.round(f3 * 100.0F), Math.round(f4 * 100.0F), (int)this.alphaSlider.getProgress()
               );
         }
      }

      @Override
      public void performAction() {
         this.cursorX = ColorSelector.this.color.getHeightDirect() / 100.0F * 200.0F;
         this.cursorY = (1.0F - ColorSelector.this.color.getHeightSafe() / 100.0F) * 200.0F;
         this.alphaSlider.setProgressEx(ColorSelector.this.color.getHeightCustom());
         this.alphaSlider.performAction(ColorSelector.this.color.getHeightByIndex(), OneConfigColors.TRANSPARENT);
         this.speedSlider.setProgressEx(ColorSelector.this.color.getHeightWithOffset());
      }
   }

   public interface Inner5 {
      void draw(long l, float f, float f2, InputHandler input);

      void performAction();

      void onKeyTyped(int n);

      void onKeyTypedEx(int n);

      void onKeyTypedAlt(int n);

      void onKeyTypedImpl(int n);

      boolean isEditing();
   }

   public final class Inner5Impl implements ColorSelector.Inner5 {
      @SerializedName("inputs")
      public final FreeCookieAPI[] inputs = new FreeCookieAPI[4];
      @SerializedName("editing")
      private boolean editing = false;

      public Inner5Impl() {
         for (int i = 0; i < this.inputs.length; i++) {
            final int n3 = i;
            this.inputs[i] = new FreeCookieAPI(40, 32, true, 4.0F) {
               public void execute() {
                  Inner5Impl.this.inputs[n3].setInput(ColorSelector.this.color.getHsba()[n3] + "");
               }
            };
            this.inputs[i].setBoarderThickness(1.0F);
            this.inputs[i].setInput(ColorSelector.this.color.getHsba()[i] + "");
         }

         if (!ColorSelector.this.hasAlpha) {
            this.inputs[3].setDisabled(true);
         }
      }

      @Override
      public void draw(long l, float f, float f2, InputHandler input) {
         this.editing = false;

         for (int i = 0; i < 4; i++) {
            FreeCookieAPI freeCookieAPI = this.inputs[i];
            freeCookieAPI.setErrored(false);
            freeCookieAPI.draw(l, f - 12.0F + 42 * i, f2, input);
            if (freeCookieAPI.isToggled()) {
               this.editing = true;
               ColorSelector.this.color.setHSBA(i, this.computeInt(freeCookieAPI.getInput(), ColorSelector.this.color.getHsba()[i]));
               ColorSelector.this.picker.performAction();
            }
         }
      }

      @Override
      public void performAction() {
         this.inputs[0].setInput(ColorSelector.this.color.getHeightImpl() + "");
         this.inputs[1].setInput(ColorSelector.this.color.getHeightDirect() + "");
         this.inputs[2].setInput(ColorSelector.this.color.getHeightSafe() + "");
         this.inputs[3].setInput(ColorSelector.this.color.getHeightCustom() + "");
      }

      @Override
      public void onKeyTyped(int n) {
         for (FreeCookieAPI freeCookieAPI : this.inputs) {
            freeCookieAPI.onKeyTyped(n);
         }
      }

      @Override
      public void onKeyTypedEx(int n) {
         for (FreeCookieAPI freeCookieAPI : this.inputs) {
            freeCookieAPI.onKeyTypedEx(n);
         }
      }

      @Override
      public void onKeyTypedAlt(int n) {
         for (FreeCookieAPI freeCookieAPI : this.inputs) {
            freeCookieAPI.onKeyTypedAlt(n);
         }
      }

      @Override
      public void onKeyTypedImpl(int n) {
         for (FreeCookieAPI freeCookieAPI : this.inputs) {
            freeCookieAPI.onKeyTypedImpl(n);
         }
      }

      @Override
      public boolean isEditing() {
         return this.editing;
      }

      public int computeInt(String text, int n) {
         try {
            return Integer.parseInt(text);
         } catch (Exception e) {
            return n;
         }
      }
   }

   public interface Inner6 {
      void draw(long l, float f, float f2, InputHandler input);

      void performAction();

      default void draw(long l, float f, float f2, int n) {
         NanoVGHelper.INSTANCE.drawRoundedRect(l, f - 7.0F, f2 - 7.0F, 14.0F, 14.0F, OneConfigColors.WHITE, 14.0F);
         NanoVGHelper.INSTANCE.drawRoundedRect(l, f - 5.0F, f2 - 5.0F, 10.0F, 10.0F, n, 10.0F);
      }
   }

   public static final class Inner9 extends SliderWidget {
      @SerializedName("gradColorStart")
      private int gradColorStart;
      @SerializedName("gradColorEnd")
      private int gradColorEnd;
      @SerializedName("img")
      private final boolean img;
      private static final long c = 1433467398286999568L;

      public Inner9(int n, float f, float f2, float f3, int n2, int n3) {
         super(n, f, f2, f3, 1);
         super.width = (int)c;
         super.dragPointerSize = super.width;
         if (n3 == 0 && n2 == 0) {
            this.gradColorStart = 0;
            this.gradColorEnd = 0;
            this.img = true;
         } else {
            this.gradColorStart = n2;
            this.gradColorEnd = n3;
            this.img = false;
         }
      }

      public Inner9(int n, float f, float f2, float f3) {
         this(n, f, f2, f3, 0, 0);
      }

      @Override
      public void draw(long l, float f, float f2, InputHandler input) {
         if (!super.disabled) {
            super.performAction(f, f2, input);
         } else {
            NanoVGHelper.INSTANCE.setAlpha(l, 0.5F);
         }

         if (this.img) {
            NanoVGHelper.INSTANCE.draw(l, OneConfigImages.HUE_GRADIENT, f, f2, super.width, super.height, 8.0F);
         } else {
            NanoVGHelper.INSTANCE.draw(l, OneConfigImages.VERTICAL_ALPHA_GRID, f + 1.0F, f2 + 1.0F, super.width - 2, super.height - 2, 8.0F);
            NanoVGHelper.INSTANCE.drawRoundedRect(l, f, f2, super.width, super.height, this.gradColorStart, this.gradColorEnd, 8.0F, Mode.DOWN);
         }

         NanoVGHelper.INSTANCE.drawRoundedRect(l, f, super.currentDragPoint, super.width - 1, super.width - 1, OneConfigColors.WHITE, 8.0F, 1.0F);
         NanoVGHelper.INSTANCE.setAlpha(l, 1.0F);
      }

      public void performAction(int n, int n2) {
         if (!this.img) {
            this.gradColorEnd = n2;
            this.gradColorStart = n;
         }
      }
   }
}
