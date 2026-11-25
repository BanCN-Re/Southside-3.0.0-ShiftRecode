package dev.southside.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.southside.auth.IrcClient;
import dev.southside.auth.IrcState;
import dev.southside.auth.IrcUser;
import dev.southside.command.Command;
import dev.southside.manager.IRCManager;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.utils.IText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.util.Formatting;

public class IRCCommand extends Command {
   public IRCCommand() {
      super("irc", "Southside irc (TM)");
   }

   private int computeInt(CommandContext<CommandSource> ctx) {
      String string = StringArgumentType.getString(ctx, "message");
      if ((Boolean)ClientSettings.enableIrcChat.getValue()) {
         return IRCManager.INSTANCE.matches(string) ? 1 : 0;
      }

      this.applyTextSafe("未开启IRC聊天");
      return 0;
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.then(Command.literal("users").executes(IRCCommand::computeIntEx));
      literalArgumentBuilder.then(Command.literal("chat").then(Command.getRequiredArgumentBuilder("message", StringArgumentType.greedyString()).executes(this::computeInt)));
   }

   private static int computeIntEx(CommandContext<CommandSource> ctx) {
      if (!IrcClient.getINSTANCE().isValid()) {
         IText.performActionEx(Text.literal("IRC 尚未连接，无法获取在线用户。"));
         return 0;
      }

      MutableText text = Text.literal(" ").formatted(Formatting.GRAY).append("[").append(Text.literal("IRC").formatted(Formatting.GOLD)).append("] ");
      text.append(Text.literal("online(" + IrcState.ircUsersSet.size() + "): ").formatted(Formatting.GRAY));
      IrcState.ircUsersSet.forEach(ircUser -> performAction(text, ircUser));
      if (MinecraftClient.getInstance().inGameHud != null) {
         MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.empty().append(IText.TEXT).append(text));
      }

      return 1;
   }

   private static void performAction(MutableText text, IrcUser ircUser) {
      Style style = Style.EMPTY.withColor(ircUser.getUuids().isEmpty() ? Formatting.RED : Formatting.LIGHT_PURPLE);
      String text2 = ircUser.getUuids().isEmpty() ? "Not in game" : "Hidden";
      if (ircUser.getUuids().contains(mc.player.getUuid().toString())) {
         text2 = ircUser.getUuids().toString();
      }

      text.append(
            Text.literal(ircUser.getUsername())
               .setStyle(
                  Style.EMPTY
                     .withColor(Formatting.GREEN)
                     .withHoverEvent(
                        new HoverEvent(
                           Action.SHOW_TEXT,
                           Text.literal("")
                              .formatted(Formatting.GRAY)
                              .append("name: ")
                              .append(Text.literal(ircUser.getUsername()).formatted(Formatting.AQUA))
                              .append("\n")
                              .append("pvp: ")
                              .append(Text.literal(String.valueOf(ircUser.isPvp())).formatted(Formatting.DARK_PURPLE))
                              .append("\n")
                              .append("uuid: ")
                              .append(Text.literal(text2).setStyle(style))
                        )
                     )
               )
         )
         .append(", ");
   }
}
