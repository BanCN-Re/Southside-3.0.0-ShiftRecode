package dev.southside.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface IText {
   Text TEXT = Text.empty()
      .append(Text.literal("[").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
      .append(Text.literal("S").setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)))
      .append(Text.literal("] ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));

   static void performAction(Text text) {
      performAction(text, Formatting.GRAY);
   }

   static void applyText(String text, Object... objArray) {
      if (text != null && MinecraftClient.getInstance().inGameHud != null) {
         for (Object obj : objArray) {
            text = text.replaceFirst("\\{}", obj != null ? obj.toString() : "null");
         }

         MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.empty().append(TEXT).append(Text.literal(text)));
      }
   }

   static void performActionEx(Text text) {
      performAction(text, Formatting.RED);
   }

   private static void performAction(Text text, Formatting formatting) {
      if (text != null && MinecraftClient.getInstance().inGameHud != null) {
         MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.empty().append(TEXT).append(text).setStyle(Style.EMPTY.withColor(formatting)));
      }
   }

   static void applyText(String text, String text2, String text3, String text4) {
      Formatting formatting = switch (text4) {
         case "Admin" -> Formatting.RED;
         case "Reseller" -> Formatting.DARK_BLUE;
         default -> Formatting.WHITE;
      };
      String text5 = String.valueOf(Formatting.AQUA);
      String text6 = String.valueOf(Formatting.RESET);
      String text7 = String.valueOf(Formatting.GREEN);
      String text8 = String.valueOf(Formatting.RESET);
      String text9 = String.valueOf(Formatting.BOLD);
      String text10 = String.valueOf(Formatting.GOLD);
      String text11 = String.valueOf(Formatting.RESET);
      String text12 = String.valueOf(formatting);
      String text13 = String.valueOf(Formatting.RESET);
      String text14 = String.valueOf(Formatting.RESET);
      String text15 = String.valueOf(Formatting.RESET);
      String text16 = text2;
      String text17 = text15;
      String text18 = text14;
      String text19 = text4;
      String text20 = text13;
      String text21 = text12;
      String text22 = text11;
      String text23 = text;
      String text24 = text10;
      String text25 = text9;
      String text26 = text8;
      String text27 = text3;
      String text28 = text7;
      String text29 = text6;
      String text30 = text5
         + "[IRC] "
         + text29
         + text28
         + "("
         + text27
         + ") "
         + text26
         + text25
         + text24
         + text23
         + " "
         + text22
         + text21
         + text20
         + "["
         + text19
         + text18
         + "] "
         + text17
         + ": "
         + text16;
      if (MinecraftClient.getInstance().inGameHud != null) {
         MinecraftClient.getInstance()
            .inGameHud
            .getChatHud()
            .addMessage(Text.empty().append(TEXT).append(Text.literal(String.format(text30, Formatting.WHITE)).setStyle(Style.EMPTY.withColor(Formatting.RED))));
      }
   }
}
