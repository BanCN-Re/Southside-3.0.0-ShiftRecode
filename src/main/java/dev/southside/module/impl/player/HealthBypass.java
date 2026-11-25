package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;

public class HealthBypass extends Module {
   @SerializedName("smooth")
   public final BooleanValue smooth = new BooleanValue("Smooth", false);
   private static final Map<String, Integer> HEALTHS = new HashMap<>();
   private static final Map<String, Float> SMOOTH_HEALTHS = new HashMap<>();

   public HealthBypass() {
      super(Category.Player, "HealthBypass", "视奸正确的血量");
   }

   @Override
   public void onDisable() {
      SMOOTH_HEALTHS.clear();
      super.onDisable();
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getPacket() instanceof ScoreboardScoreUpdateS2CPacket scoreboardScoreUpdateS2CPacket && mc2.world != null && scoreboardScoreUpdateS2CPacket.objectiveName() != null) {
         ScoreboardObjective nullableObjective = mc2.world.getScoreboard().getNullableObjective(scoreboardScoreUpdateS2CPacket.objectiveName());
         if (mc2.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) == nullableObjective) {
            mc2.execute(() -> {
               if (mc2.player != null && !scoreboardScoreUpdateS2CPacket.scoreHolderName().equals(mc2.player.getGameProfile().getName())) {
                  HEALTHS.put(scoreboardScoreUpdateS2CPacket.scoreHolderName(), scoreboardScoreUpdateS2CPacket.score());
               }
            });
         }
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (mc2.world != null) {
         for (PlayerEntity player : mc2.world.getPlayers()) {
            if (player != mc2.player) {
               String string = player.getName().getString();
               Integer integer = HEALTHS.get(string);
               if (integer != null) {
                  float f = Math.max(1, integer);
                  if ((Boolean)this.smooth.getValue()) {
                     float orDefault = SMOOTH_HEALTHS.getOrDefault(string, f);
                     float f2 = orDefault + (f - orDefault) * 0.3F;
                     if (Math.abs(f - f2) < 0.1F) {
                        f2 = f;
                     }

                     SMOOTH_HEALTHS.put(string, f2);
                     f = f2;
                  }

                  player.setHealth(f);
               }
            }
         }
      }
   }
}
