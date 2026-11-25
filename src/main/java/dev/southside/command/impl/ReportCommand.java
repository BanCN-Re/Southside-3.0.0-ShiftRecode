package dev.southside.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.southside.command.Command;
import dev.southside.utils.player.ReportUtils;
import net.minecraft.command.CommandSource;

public class ReportCommand extends Command {
   public ReportCommand() {
      super("report", "Report a player");
   }

   private static int computeInt(CommandContext<CommandSource> ctx) {
      String string = StringArgumentType.getString(ctx, "player_name");
      ReportUtils.applyText(string);
      return 1;
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.then(Command.getRequiredArgumentBuilder("player_name", StringArgumentType.word()).executes(ReportCommand::computeInt));
   }
}
