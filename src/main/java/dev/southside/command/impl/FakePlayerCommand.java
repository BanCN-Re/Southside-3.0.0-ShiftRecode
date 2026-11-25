package dev.southside.command.impl;

import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.southside.command.Command;
import dev.southside.utils.FakePlayerEntity;
import net.minecraft.command.CommandSource;

public class FakePlayerCommand extends Command {
   @SerializedName("fakePlayer")
   private FakePlayerEntity fakePlayer;

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.executes(this::build);
   }

   public FakePlayerCommand() {
      super("fakeplayer", "Spawn or remove a stationary fake player.");
   }

   private int build(CommandContext<CommandSource> ctx) {
      if (this.fakePlayer != null) {
         if (mc.world != null) {
            this.fakePlayer.performAction();
         }

         this.fakePlayer = null;
         this.applyTextImpl("Fake player removed.");
      } else if (mc.player != null && mc.world != null) {
         this.fakePlayer = new FakePlayerEntity(mc.player, "fakeplayer", 20.0F, true);
         this.fakePlayer.spawned = true;
         this.fakePlayer.targeted = true;
         this.fakePlayer.addEntity();
         this.fakePlayer.setVelocity(0.0, 0.0, 0.0);
         this.applyTextImpl("Spawned fake player 'fakeplayer'.");
      } else {
         this.applyTextSafe("You must be in a world to use this command.");
      }

      return 1;
   }
}
