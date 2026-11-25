package dev.southside.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ChatHelper {
   public static void addMessage(String text) {
      MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(text));
   }

   public static void sendChatMessage(String text) {
      MinecraftAccess.getMc().player.networkHandler.sendChatMessage(text);
   }
}
