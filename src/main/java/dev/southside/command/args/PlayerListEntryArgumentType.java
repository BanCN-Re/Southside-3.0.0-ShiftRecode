package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.utils.IMinecraft;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class PlayerListEntryArgumentType implements ArgumentType<PlayerListEntry> {
   private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(
      obj -> Text.literal("Player list entry with name " + obj + " doesn't exist.")
   );
   private static final Collection<String> EXAMPLES = List.of("cao_jia_hao");

   public static PlayerListEntryArgumentType create() {
      return new PlayerListEntryArgumentType();
   }

   public static PlayerListEntry get(CommandContext<?> ctx) {
      return (PlayerListEntry)ctx.getArgument("player", PlayerListEntry.class);
   }

   public PlayerListEntry parse(StringReader reader) throws CommandSyntaxException {
      String text = reader.readString();

      for (PlayerListEntry playerListEntry : IMinecraft.mc.getNetworkHandler().getPlayerList()) {
         if (playerListEntry.getProfile().getName().equalsIgnoreCase(text)) {
            return playerListEntry;
         }
      }

      throw NO_SUCH_PLAYER.create(text);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(IMinecraft.mc.getNetworkHandler().getPlayerList().stream().map(playerListEntry -> playerListEntry.getProfile().getName()), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static {
      short s = -8310;
      short s2 = -9116;
   }
}
