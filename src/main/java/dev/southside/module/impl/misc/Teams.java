package dev.southside.module.impl.misc;

import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ModeValue;
import java.util.Objects;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

public class Teams extends Module {
   public static ModeValue mode = new ModeValue("Mode", "Color", "Color", "Scoreboard");

   public Teams() {
      super(Category.Misc, "Teams", "队伍");
   }

   @Nullable
   public static String formatText(Entity entity) {
      if (entity instanceof PlayerEntity && Module.mc.getNetworkHandler() != null) {
         PlayerListEntry playerListEntry = Module.mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
         if (playerListEntry == null) {
            return null;
         }

         Team scoreboardTeam = playerListEntry.getScoreboardTeam();
         return scoreboardTeam != null ? scoreboardTeam.getName() : null;
      } else {
         return null;
      }
   }

   public static boolean isValidTarget(Entity entity) {
      if (entity instanceof PlayerEntity && Module.mc.player != null) {
         if (!Southside.INSTANCE.getModuleManager().getModule(Teams.class).isEnabled()) {
            return false;
         } else {
            return getMode() == Teams.Mode.COLOR
               ? entity.getTeamColorValue() == Module.mc.player.getTeamColorValue()
               : Objects.equals(formatText(entity), formatText(Module.mc.player));
         }
      } else {
         return false;
      }
   }

   public static Teams.Mode getMode() {
      return mode.getDisplayTextEx().equals("Color") ? Teams.Mode.COLOR : Teams.Mode.SCOREBOARD;
   }

   public enum Mode {
      SCOREBOARD,
      COLOR;


      // $VF: synthetic method
      private static Teams.Mode[] createValues() {
         return new Teams.Mode[]{SCOREBOARD, COLOR};
      }


   }
}
