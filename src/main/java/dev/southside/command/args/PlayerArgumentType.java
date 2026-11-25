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
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class PlayerArgumentType implements ArgumentType<PlayerEntity> {
   private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(
      obj -> Text.literal("Player with name " + obj + " doesn't exist.")
   );
   private static final Collection<String> EXAMPLES = List.of("cao_jia_hao");

   public static PlayerArgumentType create() {
      return new PlayerArgumentType();
   }

   public static PlayerEntity get(CommandContext<?> ctx) {
      return (PlayerEntity)ctx.getArgument("player", PlayerEntity.class);
   }

   public PlayerEntity parse(StringReader reader) throws CommandSyntaxException {
      String text = reader.readString();

      for (PlayerEntity player : IMinecraft.mc.world.getPlayers()) {
         if (player.getName().getString().equalsIgnoreCase(text)) {
            return player;
         }
      }

      throw NO_SUCH_PLAYER.create(text);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(IMinecraft.mc.world.getPlayers().stream().map(player -> player.getName().getString()), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static {
      short s = -16701;
      short s2 = -2095;
   }
}
