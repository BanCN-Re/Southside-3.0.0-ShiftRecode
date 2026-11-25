package dev.southside.mixin.misc;

import dev.southside.Southside;
import dev.southside.event.EventBus;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ConnectionEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.BlinkUtils;
import dev.southside.utils.PingSpoofManager;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.PacketSentEvent;
import dev.southside.utils.MovementFreezeManager;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.heypixel.HeypixelViaversionItemTransfer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.data.DataTracker.SerializedEntry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
   @Shadow
   @Nullable
   private volatile PacketListener packetListener;
   @Shadow
   @Nullable
   private volatile PacketListener prePlayStateListener;
   @Shadow
   @Final
   private static Logger LOGGER;
   @Shadow
   private Channel channel;
   @Shadow
   private int packetsReceivedCounter;

   @Shadow
   public void send(Packet<?> packet, @Nullable PacketCallbacks packetCallbacks) {
   }

   @Shadow
   private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener packetListener2) {
   }

   @Shadow
   public void disconnect(Text text) {
   }

   @Inject(
      method = "Lnet/minecraft/network/ClientConnection;channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void beforePacketRead(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
      if (this.shouldCancelConnectionPacket(packet, TransferOrigin.RECEIVE)) {
         ci.cancel();
      }
   }

   @Inject(
      method = "Lnet/minecraft/network/ClientConnection;sendInternal(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void beforePacketSend(Packet<?> packet, PacketCallbacks packetCallbacks, boolean flag, CallbackInfo ci) {
      if (this.shouldCancelConnectionPacket(packet, TransferOrigin.SEND)) {
         ci.cancel();
      }
   }

   @Unique
   private boolean shouldCancelConnectionPacket(Packet<?> packet, TransferOrigin transferOrigin) {
      EventBus eventBus = Southside.INSTANCE.getEventBus();
      PacketListener packetListener2 = this.packetListener == null ? this.prePlayStateListener : this.packetListener;
      ConnectionEvent connectionEvent = new ConnectionEvent((ClientConnection)(Object)this, packetListener2, packet, transferOrigin);
      return eventBus.postCancellable(connectionEvent).isCancelled();
   }

   @Overwrite
   public void send(Packet<?> packet) {
      DirectionEvent directionEvent = new DirectionEvent(TransferOrigin.SEND, packet, (ClientConnection)(Object)this);
      PacketUtils.trackMovementPacket(directionEvent.getPacket());
      if (!Southside.INSTANCE.getEventBus().postCancellable(directionEvent).isCancelled()) {
         if (BlinkUtils.handleOutgoingPacket(directionEvent.getPacket())) {
            this.send(directionEvent.getPacket(), null);
            Southside.INSTANCE.getEventBus().dispatch(new PacketSentEvent(directionEvent.getPacket()));
         }
      }
   }

   @Overwrite
   public void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
      if (this.channel.isOpen()) {
         PacketListener packetListener2 = this.packetListener;
         if (packetListener2 == null) {
            throw new IllegalStateException("Received a packet before the packet listener was initialized");
         }

         if (!(packet instanceof BundleS2CPacket bundleS2CPacket)) {
            Packet packet2 = HeypixelViaversionItemTransfer.transferStack(packet);
            if (this.prepareIncomingPacket(packet2) && packetListener2.accepts(packet2)) {
               try {
                  handlePacket(packet2, packetListener2);
               } catch (OffThreadException e) {
               } catch (RejectedExecutionException e2) {
                  this.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
               } catch (ClassCastException e3) {
                  LOGGER.error("Received {} that couldn't be processed", packet2.getClass(), e3);
                  this.disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
               }

               this.packetsReceivedCounter++;
            }
         } else {
            for (Packet packet3 : bundleS2CPacket.getPackets()) {
               this.handleBundledPacket(packet3, packetListener2);
            }
         }
      }
   }

   @Unique
   private void handleBundledPacket(Packet<?> packet, PacketListener packetListener2) {
      Packet packet2 = HeypixelViaversionItemTransfer.transferStack(packet);
      if (this.prepareIncomingPacket(packet2)) {
         try {
            handlePacket(packet2, packetListener2);
         } catch (OffThreadException e) {
         }
      }
   }

   @Unique
   private boolean prepareIncomingPacket(Packet<?> packet) {
      this.updateSprintState(packet);
      if (PingSpoofManager.shouldHandlePacket(packet)) {
         return false;
      }

      if (Disabler.isValidEx() && Disabler.isValidAlt()) {
         MinecraftClient.getInstance().execute(() -> Disabler.postDelayPackets(packet));
         return false;
      }

      if (packet instanceof GameMessageS2CPacket gameMessageS2CPacket && gameMessageS2CPacket.content().getString().contains("Veta")) {
         Southside.INSTANCE.setInitialized(true);
      }

      DirectionEvent directionEvent = new DirectionEvent(TransferOrigin.RECEIVE, packet, (ClientConnection)(Object)this);
      if (!Southside.INSTANCE.getEventBus().postCancellable(directionEvent).isCancelled() && !ServerPacketManager.shouldHandlePacket(packet)) {
         MovementFreezeManager.handlePacket(packet);
         return true;
      } else {
         return false;
      }
   }

   @Unique
   private void updateSprintState(Packet<?> packet) {
      if (packet instanceof EntityTrackerUpdateS2CPacket entityTrackerUpdateS2CPacket) {
         int n;
         List list;
         try {
            n = entityTrackerUpdateS2CPacket.id();
            list = entityTrackerUpdateS2CPacket.trackedValues();
         } catch (Throwable e) {
            throw new MatchException(e.toString(), e);
         }

         if (IMinecraft.mc.player != null && n == IMinecraft.mc.player.getId()) {
            for (Object obj : list) {
               SerializedEntry serializedEntry = (SerializedEntry)obj;
               if (serializedEntry.id() == 0 && serializedEntry.value() instanceof Byte val) {
                  MovementUtils.active = (val & 8) != 0;
               }
            }
         }
      }
   }
}
