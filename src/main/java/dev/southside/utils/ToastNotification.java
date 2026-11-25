package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.TooltipRenderer.Mode;

public class ToastNotification {
   @SerializedName("message")
   private final String message;
   @SerializedName("icon")
   private final SvgAsset icon;
   @SerializedName("progress")
   private final UiAnimation progress;
   @SerializedName("colorAnimation")
   private UiAnimation colorAnimation = new DummyAnimation(0.0F);

   public ToastNotification(String text, float f, SvgAsset svg) {
      this.message = text;
      this.icon = svg;
      this.progress = new DummyAnimation(150.0F, f);
   }

   public int drawAndGetHeight(long vg, int x, int y, InputHandler input) {
      if (this.colorAnimation.isValid() && !this.progress.isValid()) {
         this.progress.getProgress();
      }

      return TooltipRenderer.drawAndGetHeight(
         vg,
         x,
         y,
         this.message,
         null,
         () -> this.colorAnimation,
         uiAnimation -> this.colorAnimation = uiAnimation,
         this.icon,
         !this.progress.isValid(),
         Mode.MIDDLE,
         input
      );
   }
}
