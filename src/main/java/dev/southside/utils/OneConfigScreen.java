package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.gui.CustomScreen;
import dev.southside.render.UIMatrixStack;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

public abstract class OneConfigScreen extends CustomScreen implements IPauseControlScreen, IBlurScreen {
   @SerializedName("useMinecraftScale")
   private final boolean useMinecraftScale;
   @SerializedName("inputHandler")
   private final InputHandler inputHandler = new InputHandler();

   public OneConfigScreen(boolean flag, boolean flag2) {
      super(flag2);
      this.useMinecraftScale = flag;
   }

   public OneConfigScreen(boolean flag) {
      this(flag, false);
   }

   public OneConfigScreen() {
      this(false, false);
   }

   @Override
   public final void renderAll(@NotNull UIMatrixStack matrices, DrawContext context, int n, int n2, float f) {
      super.renderAll(matrices, context, n, n2, f);
      if (this.useMinecraftScale) {
         this.inputHandler.performAction(WindowMetrics.getScaleFactor(), WindowMetrics.getScaleFactor());
      }

      NanoVGHelper.INSTANCE.performAction(this.useMinecraftScale, l -> this.performAction(l, this.inputHandler));
   }

   public abstract void performAction(long l, InputHandler input);

   public final InputHandler getInputHandler() {
      return this.inputHandler;
   }

   @Override
   public void performAction(double d) {
      this.inputHandler.performAction(d * 40.0);
   }

   @Override
   public boolean isValid() {
      return false;
   }

   @Override
   public boolean shouldPause() {
      return false;
   }
}
