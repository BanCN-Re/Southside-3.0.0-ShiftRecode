package dev.southside.mixin.gui;

import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReconfiguringScreen.class)
public class MixinReconfiguringScreen {
   @Shadow
   private ButtonWidget disconnectButton;

   @Inject(method = "Lnet/minecraft/client/gui/screen/ReconfiguringScreen;tick()V", at = @At("TAIL"))
   private void onTickTail(CallbackInfo ci) {
      if (this.disconnectButton != null && !this.disconnectButton.active) {
         this.disconnectButton.active = true;
      }
   }

   @Inject(method = "Lnet/minecraft/client/gui/screen/ReconfiguringScreen;init()V", at = @At("TAIL"))
   private void onInitTail(CallbackInfo ci) {
      if (this.disconnectButton != null) {
         this.disconnectButton.active = true;
      }
   }
}
