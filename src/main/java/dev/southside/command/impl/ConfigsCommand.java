package dev.southside.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.command.args.ConfigsArgumentType;
import dev.southside.nel.open_nel.GameCharacter;
import dev.southside.utils.ConfigCore;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;

public class ConfigsCommand extends Command {
   public ConfigsCommand() {
      super("config", "Config command", "cfg");
   }

   private int computeInt(CommandContext<CommandSource> ctx) {
      Southside.INSTANCE.getConfigManager().performAction();
      String argument = (String)ctx.getArgument("name", String.class);

      try {
         Southside.INSTANCE.getConfigManager().applyTextEx(argument);
         this.applyTextWithOffset("成功删除参数 " + argument + ".");
      } catch (Exception e) {
         this.applyTextSafe("Error: " + e);
      }

      return 1;
   }

   private int computeIntEx(CommandContext<CommandSource> ctx) {
      String argument = (String)ctx.getArgument("name", String.class);

      try {
         Southside.INSTANCE.getConfigManager().applyText(argument);
         this.applyTextWithOffset("成功保存参数 " + argument + ".");
      } catch (Exception e) {
         this.applyTextSafe("Error: " + e);
      }

      return 1;
   }

   private int build(CommandContext<CommandSource> ctx) {
      Southside.INSTANCE.getConfigManager().performAction();
      String argument = (String)ctx.getArgument("name", String.class);

      try {
         ConfigCore.performAction();
         Southside.INSTANCE.getConfigManager().applyTextAlt(argument);
         this.applyTextWithOffset("成功加载参数 " + argument + ".");
      } catch (Exception e) {
         this.applyTextSafe("Error: " + e);
      }

      return 1;
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.then(Command.literal("list").executes(this::computeIntAlt));
      literalArgumentBuilder.then(Command.literal("load").then(Command.getRequiredArgumentBuilder("name", ConfigsArgumentType.getINSTANCE()).executes(this::build)));
      literalArgumentBuilder.then(Command.literal("remove").then(Command.getRequiredArgumentBuilder("name", ConfigsArgumentType.getINSTANCE()).executes(this::computeInt)));
      literalArgumentBuilder.then(Command.literal("save").then(Command.getRequiredArgumentBuilder("name", StringArgumentType.string()).executes(this::computeIntEx)));
   }

   private int computeIntAlt(CommandContext<CommandSource> ctx) {
      Southside.INSTANCE.getConfigManager().performAction();
      this.applyTextImpl("------Configs------");

      for (GameCharacter gameCharacter : Southside.INSTANCE.getConfigManager().configs) {
         this.applyTextDirect(Formatting.RED + gameCharacter.name);
      }

      return 1;
   }
}
