package dev.southside.command.args;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.Southside;
import dev.southside.module.Module;
import dev.southside.module.ModuleManager;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class ModuleArgumentType implements ArgumentType<Module> {
   public static final ModuleArgumentType INSTANCE = new ModuleArgumentType();
   private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(ModuleArgumentType::get);
   private static final Collection<String> EXAMPLES;

   private ModuleArgumentType() {
   }

   public static ModuleArgumentType getINSTANCE() {
      return INSTANCE;
   }

   public static Module get(CommandContext<?> ctx) {
      return (Module)ctx.getArgument("module", Module.class);
   }

   public Module parse(StringReader reader) throws CommandSyntaxException {
      String text = reader.readString();
      String text2 = text.replaceAll("-", " ");
      return Southside.INSTANCE
         .getModuleManager()
         .modules
         .stream()
         .filter(module -> ModuleManager.isValid(text2, module))
         .findFirst()
         .orElseThrow(() -> NO_SUCH_MODULE.create(text));
   }

   private static Message get(Object obj) {
      return Text.literal("Module with name " + obj + " doesn't exist.");
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(Southside.INSTANCE.getModuleManager().getModules().stream().map(Module::getConfigId), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static {
      long l = 3L;
      EXAMPLES = Southside.INSTANCE.getModuleManager().getModules().stream().limit(l).map(Module::getConfigId).collect(Collectors.toList());
   }
}
