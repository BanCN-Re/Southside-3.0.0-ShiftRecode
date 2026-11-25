package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.Southside;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class ConfigsArgumentType implements ArgumentType<String> {
   private static final ConfigsArgumentType INSTANCE = new ConfigsArgumentType();
   private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(
      obj -> Text.literal("Config with name " + obj + " doesn't exist.")
   );
   private static final Collection<String> EXAMPLES;

   private ConfigsArgumentType() {
   }

   public static ConfigsArgumentType getINSTANCE() {
      return INSTANCE;
   }

   public static String get(CommandContext<?> ctx) {
      return (String)ctx.getArgument("config", String.class);
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      String text = reader.readString();
      if (!Southside.INSTANCE.getConfigManager().matches(text)) {
         throw NO_SUCH_MODULE.create(text);
      } else {
         return text;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(Southside.INSTANCE.getConfigManager().configs.stream().map(gameCharacter -> gameCharacter.name), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static {
      long l = 3L;
      EXAMPLES = Southside.INSTANCE.getConfigManager().configs.stream().limit(l).map(gameCharacter -> gameCharacter.name).collect(Collectors.toList());
   }
}
