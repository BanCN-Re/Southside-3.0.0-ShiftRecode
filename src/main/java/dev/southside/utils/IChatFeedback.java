package dev.southside.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface IChatFeedback {
   default void applyText(String text, Object... objArray) {
      this.applyText(text, null, objArray);
   }

   default void applyTextEx(String text, Object... objArray) {
      this.applyText(text, Formatting.RED, objArray);
   }

   default void applyTextAlt(String text, Object... objArray) {
      this.applyText(text, Formatting.GRAY, objArray);
   }

   default void applyTextImpl(String text, Object... objArray) {
      this.applyTextEx(text, null, objArray);
   }

   default void applyTextDirect(String text, Object... objArray) {
      this.applyTextEx(text, null, objArray);
   }

   default void applyTextSafe(String text, Object... objArray) {
      this.applyTextEx(text, Formatting.RED, objArray);
   }

   default void applyTextCustom(String text, Object... objArray) {
      this.applyTextEx(text, Formatting.RED, objArray);
   }

   default void applyTextWithOffset(String text, Object... objArray) {
      this.applyTextEx(text, Formatting.GRAY, objArray);
   }

   private void applyText(String text, Formatting formatting, Object[] objArray) {
      if (text != null && MinecraftClient.getInstance().inGameHud != null) {
         ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
         MutableText text2 = Text.empty().append(IText.TEXT);
         MutableText text3 = Text.literal(String.format(text, objArray));
         if (formatting != null) {
            text3.setStyle(Style.EMPTY.withColor(formatting));
         }

         chatHud.addMessage(text2.append(text3));
      }
   }

   private void applyTextEx(String text, Formatting formatting, Object[] objArray) {
      if (text != null && MinecraftClient.getInstance().inGameHud != null) {
         ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
         MutableText text2 = Text.empty().append(IText.TEXT);

         for (Object obj : objArray) {
            text = text.replaceFirst("\\{}", obj != null ? obj.toString() : "null");
         }

         MutableText text3 = Text.literal(text);
         if (formatting != null) {
            text3.setStyle(Style.EMPTY.withColor(formatting));
         }

         chatHud.addMessage(text2.append(text3));
      }
   }
}
