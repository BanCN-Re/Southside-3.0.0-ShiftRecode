package dev.southside.mixin.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.southside.utils.PacketDispatchHook;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {
   @WrapOperation(
      method = "sendInternal",
      at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;writeAndFlush(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;", remap = false)
   )
   private ChannelFuture observeFlushedWrite(Channel channel, Object obj, Operation<ChannelFuture> operation) {
      ChannelFuture channelFuture = (ChannelFuture)operation.call(new Object[]{channel, obj});
      PacketDispatchHook.performAction((ClientConnection)(Object)this, (Packet<?>)obj, channelFuture);
      return channelFuture;
   }

   @WrapOperation(
      method = "sendInternal",
      at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;write(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;", remap = false)
   )
   private ChannelFuture observeWrite(Channel channel, Object obj, Operation<ChannelFuture> operation) {
      ChannelFuture channelFuture = (ChannelFuture)operation.call(new Object[]{channel, obj});
      PacketDispatchHook.performAction((ClientConnection)(Object)this, (Packet<?>)obj, channelFuture);
      return channelFuture;
   }
}
