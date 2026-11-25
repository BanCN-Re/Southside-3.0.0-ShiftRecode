package dev.southside.mixin.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientLoginNetworkHandler.class)
public class MixinClientLoginNetworkHandler {
   @Shadow
   @Final
   private ClientConnection connection;
   @Shadow
   @Final
   private MinecraftClient client;
}
