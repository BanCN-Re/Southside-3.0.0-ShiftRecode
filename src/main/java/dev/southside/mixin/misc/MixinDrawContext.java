package dev.southside.mixin.misc;

import dev.southside.render.MenuOverlayRenderer;
import dev.southside.utils.CustomMenuController;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class MixinDrawContext {
   @Inject(method = "setScissor", at = @At("HEAD"))
   private void menuClip(ScreenRect screenRect, CallbackInfo ci) {
      if (CustomMenuController.isActive()) {
         MenuOverlayRenderer.setScreenRect(screenRect);
      }
   }
}
