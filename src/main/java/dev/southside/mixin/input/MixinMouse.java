package dev.southside.mixin.input;

import dev.southside.Southside;
import dev.southside.event.events.ButtonEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {
   @Shadow
   @Final
   private MinecraftClient client;

   @Inject(method = "Lnet/minecraft/client/Mouse;onMouseButton(JIII)V", at = @At("TAIL"))
   private void onMouseButtonTail(long l, int n, int n2, int n3, CallbackInfo ci) {
      if (l == this.client.getWindow().getHandle()) {
         Southside.INSTANCE.getEventBus().postEvent(new ButtonEvent(n, n2, n3));
      }
   }
}
