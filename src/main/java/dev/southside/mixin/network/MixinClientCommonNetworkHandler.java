package dev.southside.mixin.network;

import dev.southside.Southside;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class MixinClientCommonNetworkHandler {
   @Inject(
      method = "Lnet/minecraft/client/network/ClientCommonNetworkHandler;onPacketException(Lnet/minecraft/network/packet/Packet;Ljava/lang/Exception;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   public void onOnPacketExceptionHead(Packet packet, Exception e, CallbackInfo ci) {
      Southside.LOGGER.error("Unexpected error in ClientCommonNetworkHandlerMixin", e);
      ci.cancel();
   }
}
