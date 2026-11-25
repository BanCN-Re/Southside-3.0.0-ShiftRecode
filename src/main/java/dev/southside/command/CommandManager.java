package dev.southside.command;

import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Lifecycle;
import dev.southside.Southside;
import dev.southside.command.impl.BindCommand;
import dev.southside.command.impl.ConfigsCommand;
import dev.southside.command.impl.FakePlayerCommand;
import dev.southside.command.impl.FriendCommand;
import dev.southside.command.impl.GotoCommand;
import dev.southside.command.impl.IRCCommand;
import dev.southside.command.impl.StickersCommand;
import dev.southside.command.impl.TestCommand;
import dev.southside.command.impl.ToggleCommand;
import dev.southside.utils.IMinecraft;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class CommandManager {
   public static CommandManager INSTANCE;
   public static final CommandManager.Inner INNER = new CommandManager.Inner();
   @SerializedName("commandDispatcher")
   public final CommandDispatcher<CommandSource> commandDispatcher = new CommandDispatcher();
   @SerializedName("commandSource")
   public final CommandSource commandSource = new ClientCommandSource(null, IMinecraft.mc);
   @SerializedName("commands")
   public final List<Command> commands = new ArrayList<>();

   public void performAction(Command command) {
      this.commands.removeIf(command2 -> command2.name.equals(command.name));
      command.performAction(this.commandDispatcher);
      this.commands.add(command);
   }

   public static CommandManager getINSTANCE() {
      return INSTANCE;
   }

   public void update() {
      Southside.INSTANCE.getEventBus().subscribeTarget(this);
      this.performAction(new BindCommand());
      this.performAction(new ToggleCommand());
      this.performAction(new ConfigsCommand());
      this.performAction(new GotoCommand());
      this.performAction(new FakePlayerCommand());
      this.performAction(new TestCommand());
      this.performAction(new IRCCommand());
      this.performAction(new StickersCommand());
      this.performAction(new FriendCommand());
      this.commands.sort(Command::compareTo);
   }

   public void applyText(String text) throws CommandSyntaxException {
      this.commandDispatcher.execute(text, this.commandSource);
   }

   public CommandManager() {
      INSTANCE = this;
   }

   public Command getCommand(String text) {
      for (Command command : this.commands) {
         if (command.name.equals(text)) {
            return command;
         }
      }

      return null;
   }

   public static class Inner extends SimpleRegistry<Command> {
      public int size() {
         return CommandManager.getINSTANCE().commands.size();
      }

      @NotNull
      public Iterator<Command> iterator() {
         return new CommandManager.IteratorImpl();
      }

      public Inner() {
         super(RegistryKey.ofRegistry(Identifier.of("southside", "commands")), Lifecycle.stable());
      }
   }

   public static class IteratorImpl implements Iterator<Command> {
      @SerializedName("iterator")
      private final Iterator<Command> iterator = Southside.INSTANCE.getCommandManager().commands.iterator();

      public Command next() {
         return this.iterator.next();
      }

      @Override
      public boolean hasNext() {
         return this.iterator.hasNext();
      }
   }
}
