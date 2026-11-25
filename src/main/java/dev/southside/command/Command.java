package dev.southside.command;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.southside.utils.IChatFeedback;
import dev.southside.utils.IMinecraft;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.BuiltinRegistries;
import org.jetbrains.annotations.NotNull;

public abstract class Command implements IChatFeedback, Comparable<Command>, IMinecraft {
   public static final CommandRegistryAccess REGISTRY_ACCESS = net.minecraft.server.command.CommandManager.createRegistryAccess(
      BuiltinRegistries.createWrapperLookup()
   );
   public static final int DEFAULT_INT = 1;
   @SerializedName("name")
   public final String name;
   @SerializedName("description")
   public final String description;
   @SerializedName("aliases")
   public final List<String> aliases = new ArrayList<>();
   private static Config[] configCache;

   public static LiteralArgumentBuilder<CommandSource> literal(String text) {
      return LiteralArgumentBuilder.literal(text);
   }

   public String getDescription() {
      return this.description;
   }

   public final void performAction(CommandDispatcher<CommandSource> commandDispatcher) {
      this.register(commandDispatcher, this.name);

      for (String text : this.aliases) {
         this.register(commandDispatcher, text);
      }
   }

   public int compareTo(@NotNull Command other) {
      return this.name.compareTo(other.name);
   }

   public static <T> RequiredArgumentBuilder<CommandSource, T> getRequiredArgumentBuilder(String text, ArgumentType<T> argumentType) {
      return RequiredArgumentBuilder.argument(text, argumentType);
   }

   public Command(String text, String text2, String... textArray) {
      this.name = text;
      this.description = text2;
      Collections.addAll(this.aliases, textArray);
   }

   public List<String> getAliases() {
      return this.aliases;
   }

   public void register(CommandDispatcher<CommandSource> commandDispatcher, String text) {
      LiteralArgumentBuilder literalArgumentBuilder = LiteralArgumentBuilder.literal(text);
      this.build(literalArgumentBuilder);
      commandDispatcher.register(literalArgumentBuilder);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         Command command = (Command)obj;
         return Objects.equals(this.name, command.name);
      } else {
         return false;
      }
   }

   public String getName() {
      return this.name;
   }

   public abstract void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder);

   static {
      Config[] configArray = new Config[1];
      literal(configArray);
   }

   public static void literal(Config[] configArray) {
      configCache = configArray;
   }
}
