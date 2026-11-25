package dev.southside.utils;

import dev.southside.gui.CustomScreen;

public class BlurConditionHelper {
   public static boolean isValid() {
      return NanoVGHelper.INSTANCE.isNvgFrameActive()
         || CustomScreen.getScreen() instanceof IBlurScreen && ((IBlurScreen)CustomScreen.getScreen()).isValid();
   }
}
