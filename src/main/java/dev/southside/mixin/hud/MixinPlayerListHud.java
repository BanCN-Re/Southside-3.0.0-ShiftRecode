package dev.southside.mixin.hud;

import dev.southside.Southside;
import dev.southside.auth.IrcUser;
import dev.southside.manager.FriendManager;
import dev.southside.manager.IRCManager;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.Teams;
import dev.southside.module.impl.render.NameProtect;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerListHud.class)
public abstract class MixinPlayerListHud {
   @Redirect(
      method = "Lnet/minecraft/client/gui/hud/PlayerListHud;render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/hud/PlayerListHud;getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;"
      )
   )
   private Text renderPlayerNameWithTags(PlayerListHud playerListHud, PlayerListEntry playerListEntry) {
      Text playerName = playerListHud.getPlayerName(playerListEntry);
      Southside southside = Southside.INSTANCE;
      MinecraftClient instance = MinecraftClient.getInstance();
      if (southside != null && instance != null) {
         FriendManager friendManager = southside.getFriendManager();
         UUID uuid = playerListEntry.getProfile() != null ? playerListEntry.getProfile().getId() : null;
         String text = playerListEntry.getProfile() != null ? playerListEntry.getProfile().getName() : null;
         PlayerEntity player = instance.world != null && uuid != null ? instance.world.getPlayerByUuid(uuid) : null;
         IrcUser ircUser = IRCManager.getIrcUser(playerListEntry.getProfile().getId().toString());
         boolean flag = player != null && AntiBot.isBot(player);
         boolean flag2 = friendManager != null && text != null && friendManager.friends.contains(text);
         boolean flag3 = player != null && Teams.isValidTarget(player);
         if (ircUser == null && !flag && !flag2 && !flag3) {
            return playerName;
         }

         MutableText text2 = Text.empty();
         if (ircUser != null) {
            text2.append(Text.literal("[").formatted(Formatting.GRAY))
               .append(Text.literal(ircUser.getUsername()).formatted(Formatting.AQUA))
               .append(Text.literal("] ").formatted(Formatting.GRAY));
            if (NameProtect.isValidEx()) {
               IRCManager.stringMap.put(ircUser, playerListEntry.getProfile().getName());
            }
         }

         if (flag) {
            text2.append(Text.literal("[Bot] ").formatted(Formatting.RED));
         }

         if (flag2) {
            text2.append(Text.literal("[Friend] ").formatted(Formatting.GREEN));
         }

         if (flag3) {
            text2.append(Text.literal("[Team] ").formatted(Formatting.GREEN));
         }

         return text2.append(playerName.copy());
      } else {
         return playerName;
      }
   }
}
