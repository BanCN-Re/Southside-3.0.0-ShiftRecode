package dev.southside.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.Vec3d;

public class ClientPosArgumentType implements ArgumentType<Vec3d> {
   public static final Collection<String> collection;
   public static final MinecraftClient mc;
   private static final long a = 4771457216995655712L;

   public static ClientPosArgumentType getClientPosArgumentType() {
      return new ClientPosArgumentType();
   }

   public static Vec3d computePosition(CommandContext<?> ctx, String text) {
      return (Vec3d)ctx.getArgument(text, Vec3d.class);
   }

   public Vec3d parse(StringReader reader) throws CommandSyntaxException {
      int cursor = reader.getCursor();
      CoordinateArgument coordinateArgument = CoordinateArgument.parse(reader);
      performAction(reader, cursor);
      CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(reader);
      performAction(reader, cursor);
      CoordinateArgument coordinateArgument3 = CoordinateArgument.parse(reader);
      return new Vec3d(coordinateArgument.toAbsoluteCoordinate(mc.player.getX()), coordinateArgument2.toAbsoluteCoordinate(mc.player.getY()), coordinateArgument3.toAbsoluteCoordinate(mc.player.getZ()));
   }

   private static void performAction(StringReader reader, int n) throws CommandSyntaxException {
      if (reader.canRead() && reader.peek() == (int)a) {
         reader.skip();
      } else {
         reader.setCursor(n);
         throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      if (ctx.getSource() instanceof CommandSource commandSource) {
         Collection blockPositionSuggestions = commandSource.getBlockPositionSuggestions();
         return CommandSource.suggestPositions(builder.getRemaining(), blockPositionSuggestions, builder, CommandManager.getCommandValidator(this::parse));
      } else {
         return Suggestions.empty();
      }
   }

   static {
      collection = Arrays.asList("0 0 0", "~ ~ ~", "~0.5 ~1 ~-5");
      mc = MinecraftClient.getInstance();
   }
}
