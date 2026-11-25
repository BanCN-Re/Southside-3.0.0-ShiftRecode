package dev.southside.command.args;

import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class EnumStringArgumentType implements ArgumentType<String> {
   private static final DynamicCommandExceptionType INVALID_OPTION = new DynamicCommandExceptionType(obj -> Text.literal(obj + " is not a valid option."));
   @SerializedName("allowedValues")
   private final Collection<String> allowedValues;

   public EnumStringArgumentType(Collection<String> collection) {
      this.allowedValues = collection;
   }

   public static String getString(CommandContext<?> ctx, String text) {
      return (String)ctx.getArgument(text, String.class);
   }

   public Collection<String> getExamples() {
      return this.allowedValues;
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      String text = reader.readUnquotedString();
      if (!this.allowedValues.contains(text)) {
         throw INVALID_OPTION.create(text);
      } else {
         return text;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(this.allowedValues, builder);
   }

   @Override
   public String toString() {
      return "string()";
   }
}
