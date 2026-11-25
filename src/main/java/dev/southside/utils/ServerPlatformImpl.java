package dev.southside.utils;

import net.minecraft.client.network.ClientPlayerEntity;

public class ServerPlatformImpl implements IServerPlatform {
   @Override
   public boolean isValid() {
      return MinecraftAccess.getClientWorld() != null && !MinecraftAccess.getMc().isInSingleplayer();
   }

   @Override
   public boolean isValidEx() {
      return MinecraftAccess.getMc().player != null;
   }

   @Override
   public String getDisplayText() {
      ClientPlayerEntity player = MinecraftAccess.getMc().player;
      return player == null ? null : player.networkHandler.getBrand();
   }
}
