package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class EnumArgumentType<T extends Enum<T>> implements ArgumentType<T> {
   private static final DynamicCommandExceptionType dynamicCommandExceptionType = new DynamicCommandExceptionType(
      obj -> Text.literal(obj + " is not a valid argument.")
   );
   @SerializedName("enumConstants")
   public T[] enumConstants;

   public EnumArgumentType(T val) {
      this.enumConstants = (T[])val.getDeclaringClass().getEnumConstants();
   }

   public static <T extends Enum<T>> EnumArgumentType<T> getEnumArgumentType(T val) {
      return new EnumArgumentType<>((T)val);
   }

   public static <T extends Enum<T>> T getT(CommandContext<?> ctx, String text, T val) {
      return (T)ctx.getArgument(text, val.getDeclaringClass());
   }

   public T parse(StringReader reader) throws CommandSyntaxException {
      String text = reader.readString();

      for (Enum val : this.enumConstants) {
         if (val.toString().equals(text)) {
            return (T)val;
         }
      }

      throw dynamicCommandExceptionType.create(text);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(Arrays.<Enum>stream(this.enumConstants).map(Enum::toString), builder);
   }
}
