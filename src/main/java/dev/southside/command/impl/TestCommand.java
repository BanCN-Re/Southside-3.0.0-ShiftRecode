package dev.southside.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.render.ProgressBarOverlay;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;

public class TestCommand extends Command {
   private static final long DEFAULT_LONG = 5242796879497920532L;
   private static final long DEFAULT_LONG_VALUE = 200L;

   private static void performAction(int n, ProgressBarOverlay.Inner inner) {
      for (int i = 1; i <= n; i++) {
         if (!inner.isValid()) {
            return;
         }

         inner.onKeyTyped(i);

         try {
            Thread.sleep(DEFAULT_LONG_VALUE);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
         }
      }

      inner.performActionEx();
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.then(
         ((LiteralArgumentBuilder)Command.literal("progressbar").executes(this::build))
            .then(
               ((RequiredArgumentBuilder)Command.getRequiredArgumentBuilder("steps", IntegerArgumentType.integer(1)).executes(this::computeInt))
                  .then(Command.getRequiredArgumentBuilder("title", StringArgumentType.greedyString()).executes(this::computeIntEx))
            )
      );
   }

   public TestCommand() {
      super("test", "Collection of small test helpers.");
   }

   private int build(CommandContext<CommandSource> ctx) {
      return this.startDemo((int)DEFAULT_LONG, "Demo Task");
   }

   private int startDemo(int n, @Nullable String text) {
      ProgressBarOverlay progressBarOverlay = Southside.INSTANCE.getProgressBarOverlay();
      if (progressBarOverlay == null) {
         this.applyTextSafe("Progress bar overlay is not ready yet.");
         return 1;
      } else {
         ProgressBarOverlay.Inner inner = progressBarOverlay.getInner(n, text);
         this.applyTextImpl("Displaying demo progress bar (" + n + " steps).");
         CompletableFuture.runAsync(() -> performAction(n, inner));
         return 1;
      }
   }

   private int computeInt(CommandContext<CommandSource> ctx) {
      return this.startDemo(IntegerArgumentType.getInteger(ctx, "steps"), null);
   }

   private int computeIntEx(CommandContext<CommandSource> ctx) {
      return this.startDemo(IntegerArgumentType.getInteger(ctx, "steps"), StringArgumentType.getString(ctx, "title"));
   }
}
