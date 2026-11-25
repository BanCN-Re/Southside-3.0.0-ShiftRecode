package dev.southside.command.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.manager.FriendManager;
import dev.southside.utils.IText;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;

public class FriendCommand extends Command {
   public FriendCommand() {
      super("friend", "管理好友列表", "f");
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.executes(ctx -> this.listFriends());
      literalArgumentBuilder.then(Command.literal("list").executes(ctx2 -> this.listFriends()));
      literalArgumentBuilder.then(
         Command.literal("add")
            .then(
               Command.getRequiredArgumentBuilder("name", StringArgumentType.word())
                  .suggests(this::getCompletableFuture)
                  .executes(ctx3 -> this.getStringWidth(StringArgumentType.getString(ctx3, "name")))
            )
      );
      literalArgumentBuilder.then(
         Command.literal("del")
            .then(
               Command.getRequiredArgumentBuilder("name", StringArgumentType.word())
                  .suggests(this::getCompletableFuture2)
                  .executes(ctx4 -> this.getStringWidthEx(StringArgumentType.getString(ctx4, "name")))
            )
      );
   }

   private int listFriends() {
      FriendManager friendManager = Southside.INSTANCE.getFriendManager();
      if (friendManager.friends.isEmpty()) {
         this.applyText("好友列表为空。", (Formatting)null);
      } else {
         this.applyText("好友列表 (" + friendManager.friends.size() + "):", (Formatting)null);
         friendManager.friends.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(text -> this.performAction(this.getMutableText(text)));
      }

      if (mc.getNetworkHandler() != null) {
         List<String> list = this.getStream(friendManager).toList();
         if (!list.isEmpty()) {
            this.applyText("在线玩家，可点击添加:", (Formatting)null);
            list.forEach(text2 -> this.performAction(this.getMutableText2(text2)));
         }
      }

      return 1;
   }

   private int getStringWidth(String text) {
      FriendManager friendManager = Southside.INSTANCE.getFriendManager();
      if (friendManager.friends.contains(text)) {
         this.applyText(text + " 已经在好友列表中。", Formatting.RED);
      } else {
         friendManager.applyTextEx(text);
         this.applyText("已添加好友: " + text, Formatting.GRAY);
      }

      return 1;
   }

   private int getStringWidthEx(String text) {
      FriendManager friendManager = Southside.INSTANCE.getFriendManager();
      if (!friendManager.friends.contains(text)) {
         this.applyText("好友不存在: " + text, Formatting.RED);
      } else {
         friendManager.applyText(text);
         this.applyText("已删除好友: " + text, Formatting.GRAY);
      }

      return 1;
   }

   private CompletableFuture<Suggestions> getCompletableFuture(CommandContext<CommandSource> ctx, SuggestionsBuilder builder) {
      if (mc.getNetworkHandler() == null) {
         return Suggestions.empty();
      }

      FriendManager friendManager = Southside.INSTANCE.getFriendManager();
      return CommandSource.suggestMatching(this.getStream(friendManager), builder);
   }

   private CompletableFuture<Suggestions> getCompletableFuture2(CommandContext<CommandSource> ctx, SuggestionsBuilder builder) {
      FriendManager friendManager = Southside.INSTANCE.getFriendManager();
      return CommandSource.suggestMatching(friendManager.friends.stream().sorted(String.CASE_INSENSITIVE_ORDER), builder);
   }

   private Stream<String> getStream(FriendManager friendManager) {
      return mc.getNetworkHandler()
         .getPlayerList()
         .stream()
         .map(PlayerListEntry::getProfile)
         .<String>map(GameProfile::getName)
         .filter(text -> text != null && !text.isBlank())
         .filter(text2 -> mc.player == null || !text2.equalsIgnoreCase(mc.player.getName().getString()))
         .filter(text3 -> !friendManager.friends.contains(text3))
         .sorted(String.CASE_INSENSITIVE_ORDER);
   }

   private MutableText getMutableText(String text) {
      return Text.empty()
         .append(Text.literal("- ").formatted(Formatting.GRAY))
         .append(Text.literal(text).formatted(Formatting.GREEN))
         .append(Text.literal(" "))
         .append(this.actionButton("删除", Formatting.RED, ".friend del " + text, "点击删除好友 " + text));
   }

   private MutableText getMutableText2(String text) {
      return Text.empty()
         .append(Text.literal("- ").formatted(Formatting.GRAY))
         .append(Text.literal(text).formatted(Formatting.WHITE))
         .append(Text.literal(" "))
         .append(this.actionButton("添加", Formatting.GREEN, ".friend add " + text, "点击添加好友 " + text));
   }

   private MutableText actionButton(String text, Formatting formatting, String text2, String text3) {
      return Text.literal("[" + text + "]")
         .setStyle(
            Style.EMPTY
               .withColor(formatting)
               .withClickEvent(new ClickEvent(Action.RUN_COMMAND, text2))
               .withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.literal(text3)))
         );
   }

   private void applyText(String text, Formatting formatting) {
      if (MinecraftClient.getInstance().inGameHud != null) {
         ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
         MutableText text2 = Text.empty().append(IText.TEXT);
         MutableText text3 = Text.literal(text);
         if (formatting != null) {
            text3.setStyle(Style.EMPTY.withColor(formatting));
         }

         chatHud.addMessage(text2.append(text3));
      }
   }

   private void performAction(Text text) {
      if (text != null && MinecraftClient.getInstance().inGameHud != null) {
         MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.empty().append(IText.TEXT).append(text));
      }
   }
}
