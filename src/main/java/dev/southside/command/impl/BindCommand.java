package dev.southside.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.command.args.KeybindingArgumentType;
import dev.southside.command.args.ModuleArgumentType;
import dev.southside.module.Module;
import dev.southside.utils.OneKeyBind;
import java.util.ArrayList;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;

public class BindCommand extends Command {
   private static int computeInt(CommandContext<CommandSource> ctx) {
      Module argument = (Module)ctx.getArgument("module", Module.class);
      KeybindingArgumentType.Entry argument2 = (KeybindingArgumentType.Entry)ctx.getArgument("bind", KeybindingArgumentType.Entry.class);
      ((OneKeyBind)argument.bind.getValue()).clear();
      ((OneKeyBind)argument.bind.getValue()).onKeyTyped(argument2.getKeyCode());
      argument.applyTextImpl("Bound module '" + argument.getNameKey() + "' to " + argument2.getKeyName());
      return 1;
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.then(Command.literal("list").executes(this::build));
      literalArgumentBuilder.then(
         Command.getRequiredArgumentBuilder("module", ModuleArgumentType.getINSTANCE())
            .then(Command.getRequiredArgumentBuilder("bind", KeybindingArgumentType.keybinding()).executes(BindCommand::computeInt))
      );
   }

   private int build(CommandContext<CommandSource> ctx) {
      boolean flag = false;

      for (Module module : Southside.INSTANCE.getModuleManager().getModules()) {
         ArrayList keyBinds = ((OneKeyBind)module.getBind().getValue()).getKeyBinds();
         if (!keyBinds.isEmpty()) {
            int first = (Integer)keyBinds.getFirst();
            if (first != 0 && first != -1) {
               this.applyTextImpl("- {}: {}", module.getNameKey(), InputUtil.fromKeyCode(first, 0).getLocalizedText().getString());
               flag = true;
            }
         }
      }

      if (!flag) {
         this.applyTextImpl("No modules are currently bound to any keys.");
      }

      return 1;
   }

   public BindCommand() {
      super("bind", "Bound a module to a key");
   }
}
