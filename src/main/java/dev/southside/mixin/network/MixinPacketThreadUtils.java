package dev.southside.mixin.network;

import dev.southside.utils.network.ProtectionUtils;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.thread.ThreadExecutor;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkThreadUtils.class)
public class MixinPacketThreadUtils {
   @Shadow
   @Final
   private static Logger LOGGER;

   @Inject(
      method = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
      at = @At("HEAD"),
      cancellable = true,
      require = 0
   )
   private static <T extends PacketListener> void onForceMainThreadHead(Packet<T> packet, T val, ThreadExecutor<?> threadExecutor, CallbackInfo ci) {
      ci.cancel();
      ProtectionUtils.onEnsureRunningOnSameThread(LOGGER, packet, val, threadExecutor);
   }
}
