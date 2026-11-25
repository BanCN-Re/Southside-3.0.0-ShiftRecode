package dev.southside.mixin.network;

import dev.southside.Southside;
import dev.southside.utils.PacketDispatchHook;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
   @Inject(method = "onBlockUpdate", at = @At("TAIL"))
   private void appliedBlock(BlockUpdateS2CPacket blockUpdateS2CPacket, CallbackInfo ci) {
      if (PacketDispatchHook.isActive()) {
         Southside.INSTANCE.getEventBus().dispatch(new PacketDispatchHook.Entry(blockUpdateS2CPacket));
      }
   }

   @Inject(method = "onChunkDeltaUpdate", at = @At("TAIL"))
   private void appliedBlocks(ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket, CallbackInfo ci) {
      if (PacketDispatchHook.isActive()) {
         Southside.INSTANCE.getEventBus().dispatch(new PacketDispatchHook.Entry(chunkDeltaUpdateS2CPacket));
      }
   }

   @Inject(method = "onPlayerActionResponse", at = @At("TAIL"))
   private void appliedSequence(PlayerActionResponseS2CPacket playerActionResponseS2CPacket, CallbackInfo ci) {
      if (PacketDispatchHook.isActive()) {
         Southside.INSTANCE.getEventBus().dispatch(new PacketDispatchHook.Entry(playerActionResponseS2CPacket));
      }
   }

   @Inject(method = "onPlayerPositionLook", at = @At("TAIL"))
   private void appliedCorrection(PlayerPositionLookS2CPacket playerPositionLookS2CPacket, CallbackInfo ci) {
      if (PacketDispatchHook.isActive()) {
         PacketDispatchHook.performAction();
         Southside.INSTANCE.getEventBus().dispatch(new PacketDispatchHook.Entry(playerPositionLookS2CPacket));
      }
   }
}
