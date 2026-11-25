package dev.southside.mixin.input;

import dev.southside.Southside;
import dev.southside.event.events.KeyCodeEvent;
import dev.southside.event.events.KeyPressEvent;
import dev.southside.render.ImeOverlayManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
   @Shadow
   @Final
   private MinecraftClient client;

   @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
   private void onUnicodeInput(long vg, int x, int y, CallbackInfo ci) {
      if (vg == this.client.getWindow().getHandle() && ImeOverlayManager.INSTANCE.isKeyDown(x)) {
         ci.cancel();
      }
   }

   @Inject(method = "Lnet/minecraft/client/Keyboard;onKey(JIIII)V", at = @At("HEAD"))
   public void onKeyInput(long l, int n, int n2, int n3, int n4, CallbackInfo ci) {
      if (l == this.client.getWindow().getHandle() && MinecraftClient.getInstance().currentScreen == null) {
         Southside.INSTANCE.getEventBus().dispatch(new KeyCodeEvent(n, n2, n3, n4));
         if (n3 == 1 && n != -1) {
            Southside.INSTANCE.getEventBus().dispatch(new KeyPressEvent(n));
         }
      }
   }
}
