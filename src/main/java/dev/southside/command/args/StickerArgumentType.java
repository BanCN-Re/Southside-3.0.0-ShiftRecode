package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.Southside;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;

public class StickerArgumentType implements ArgumentType<String> {
   private static final StickerArgumentType INSTANCE = new StickerArgumentType();

   public static StickerArgumentType getINSTANCE() {
      return INSTANCE;
   }

   public Collection<String> getExamples() {
      return List.of("sh1t.gif", "example.png");
   }

   public String parse(StringReader reader) {
      return reader.readUnquotedString();
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(Southside.INSTANCE.getStickerManager().collectItems(), builder);
   }

   private StickerArgumentType() {
   }
}
