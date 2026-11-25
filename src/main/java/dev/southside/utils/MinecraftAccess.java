package dev.southside.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;

public class MinecraftAccess {
   public static MinecraftClient getMc() {
      return MinecraftClient.getInstance();
   }

   public static ClientWorld getClientWorld() {
      return getMc().world;
   }

   public static GameOptions getGameOptions() {
      return getMc().options;
   }

   public static long getWindowHandle() {
      return getMc().getWindow().getHandle();
   }

   public static int getGuiScale() {
      return getHeight();
   }

   private static int getHeight() {
      return (Integer)getGameOptions().getGuiScale().getValue();
   }

   public static void setGuiScale(int n) {
      setValue(n);
   }

   public static void setValue(int n) {
      getGameOptions().getGuiScale().setValue(n);
   }
}
