package dev.southside.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.southside.command.Command;
import dev.southside.utils.misc.AIWalkController;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;

public class GotoCommand extends Command {
   public GotoCommand() {
      super("goto", "走路到指定坐标");
   }

   private static int computeInt(CommandContext<CommandSource> ctx) {
      AIWalkController.performActionSafe();
      return 1;
   }

   private static int computeIntEx(CommandContext<CommandSource> ctx) {
      int integer = IntegerArgumentType.getInteger(ctx, "x");
      int integer2 = IntegerArgumentType.getInteger(ctx, "y");
      int integer3 = IntegerArgumentType.getInteger(ctx, "z");
      mc.execute(() -> AIWalkController.walkTo(new BlockPos(integer, integer2, integer3)));
      return 1;
   }

   private int build(CommandContext<CommandSource> ctx) {
      this.applyTextImpl("Usage: .goto <x> <y> <z>");
      return 1;
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.executes(this::build);
      literalArgumentBuilder.then(
         Command.getRequiredArgumentBuilder("x", IntegerArgumentType.integer())
            .then(
               Command.getRequiredArgumentBuilder("y", IntegerArgumentType.integer())
                  .then(Command.getRequiredArgumentBuilder("z", IntegerArgumentType.integer()).executes(GotoCommand::computeIntEx))
            )
      );
      literalArgumentBuilder.then(Command.literal("stop").executes(GotoCommand::computeInt));
   }
}
