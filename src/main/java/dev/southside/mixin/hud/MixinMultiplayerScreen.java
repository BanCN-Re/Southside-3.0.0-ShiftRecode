package dev.southside.mixin.hud;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MixinMultiplayerScreen {
   @Inject(
      method = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;init()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V")
   )
   public void onInitInvoke(CallbackInfo ci) {
   }
}
