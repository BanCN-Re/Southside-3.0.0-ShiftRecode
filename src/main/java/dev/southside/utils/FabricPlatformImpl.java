package dev.southside.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public class FabricPlatformImpl implements OneConfigPlatform {
   private static long timestamp;
   @Override
   public OneConfigPlatform.Mode getMode() {
      return OneConfigPlatform.Mode.FABRIC;
   }

   @Override
   public boolean isOnThread() {
      return MinecraftClient.getInstance().isOnThread();
   }

   @Override
   public int getHeight() {
      return (int)timestamp;
   }

   @Override
   public boolean isDevelopmentEnvironment() {
      return FabricLoader.getInstance().isDevelopmentEnvironment();
   }

   static {
      long l = 2556407591626354440L;
      timestamp = l;
   }
}
