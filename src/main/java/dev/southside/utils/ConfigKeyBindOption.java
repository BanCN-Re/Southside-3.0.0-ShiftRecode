package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;
import org.lwjgl.glfw.GLFW;

public class ConfigKeyBindOption extends BasicOption implements IFocusableOption {
   @SerializedName("button")
   private final BasicButton button;
   @SerializedName("mouseButtons")
   private final boolean[] mouseButtons = new boolean[8];
   @SerializedName("recordedKeys")
   private final OneKeyBind recordedKeys = new OneKeyBind();
   @SerializedName("recordingScreen")
   private OneConfigGui recordingScreen;

   public ConfigKeyBindOption(Field field, Object obj, String text, String text2, String text3, String text4, int n) {
      super(field, obj, text, text2, text3, text4, n);
      this.button = new BasicButton(256, 32, "", OneConfigSvgs.KEYSTROKE, null, 3, ColorPalette.INSTANCE3);
      this.button.setToggleable(true);
      this.button.setRunnable(this::performActionAlt);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      boolean isValidEx = this.isValidEx();
      if (!isValidEx) {
         nvg.setAlpha(vg, 0.5F);
         this.performActionEx();
      } else if (this.isValidImpl()) {
         this.performActionDirect();
         if (this.recordedKeys.getHeight() != 0 && !this.recordedKeys.isValid()) {
            this.performActionImpl();
         }
      }

      nvg.drawTextEx(vg, this.name, x, y + 17, super.nameColor, 14.0F, OneConfigFonts.ONE_CONFIG_FONT);
      String displayText = (this.isValidImpl() ? this.recordedKeys : this.getOneKeyBind()).getDisplayText();
      this.button.setDisabled(!isValidEx);
      this.button.setText(displayText.isEmpty() ? (this.isValidImpl() ? "Recording... (ESC to clear)" : "NONE") : displayText);
      this.button.draw(vg, x + (this.size != 1 ? 736 : 224), y, input);
      nvg.setAlpha(vg, 1.0F);
   }

   private void performActionAlt() {
      OneConfigGui oneConfigGui = OneConfigGui.INSTANCE;
      if (oneConfigGui != null && oneConfigGui.isValid(this)) {
         this.recordingScreen = oneConfigGui;
         this.recordedKeys.clear();
         long windowHandle = MinecraftAccess.getWindowHandle();

         for (int i = 2; i < this.mouseButtons.length; i++) {
            this.mouseButtons[i] = GLFW.glfwGetMouseButton(windowHandle, i) == 1;
         }
      } else {
         this.button.setToggled(this.isValidImpl());
      }
   }

   @Override
   public void onKeyTypedEx(int n) {
      if (this.isValidImpl()) {
         if (n == 256) {
            this.recordedKeys.clear();
            this.performActionImpl();
         } else if (n != -1) {
            this.recordedKeys.onKeyTyped(n);
         }
      }
   }

   private void performActionImpl() {
      OneKeyBind oneKeyBind = this.getOneKeyBind();
      oneKeyBind.clear();
      oneKeyBind.getKeyBinds().addAll(this.recordedKeys.getKeyBinds());
      this.performActionEx();
   }

   @Override
   public void performActionEx() {
      if (this.recordingScreen != null) {
         this.recordingScreen.performAction(this);
         this.recordingScreen = null;
         this.recordedKeys.clear();
         this.button.setToggled(false);
      }
   }

   private OneKeyBind getOneKeyBind() {
      OneKeyBind oneKeyBind = new OneKeyBind();

      try {
         this.field.setAccessible(true);
         oneKeyBind = (OneKeyBind)this.resolveValue();
      } catch (IllegalAccessException e) {
      }

      return oneKeyBind;
   }

   private void performActionDirect() {
      long windowHandle = MinecraftAccess.getWindowHandle();

      for (int i = 2; i < this.mouseButtons.length; i++) {
         boolean flag = GLFW.glfwGetMouseButton(windowHandle, i) == 1;
         if (flag && !this.mouseButtons[i]) {
            this.recordedKeys.performAction(i, true);
         }

         this.mouseButtons[i] = flag;
      }
   }

   @Override
   public int getHeight() {
      return 32;
   }

   @Override
   public boolean isValidImpl() {
      return this.recordingScreen != null;
   }
}
