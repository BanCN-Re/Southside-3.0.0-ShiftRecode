package dev.southside.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.southside.command.Command;
import dev.southside.command.args.ModuleArgumentType;
import dev.southside.module.Module;
import net.minecraft.command.CommandSource;

public class ToggleCommand extends Command {
   public ToggleCommand() {
      super("toggle", "Toggle a module.", "t");
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.then(Command.getRequiredArgumentBuilder("module", ModuleArgumentType.getINSTANCE()).executes(this::build));
   }

   private int build(CommandContext<CommandSource> ctx) {
      Module argument = (Module)ctx.getArgument("module", Module.class);
      if (argument == null) {
         this.applyTextImpl("Module not found.");
         return 1;
      } else {
         argument.setEnabled(!argument.enabled.getValue());
         this.applyTextImpl(argument.getName() + " is " + (argument.isEnabled() ? "enabled" : "disabled"));
         return 1;
      }
   }
}
