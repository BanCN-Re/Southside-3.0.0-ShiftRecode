package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.TimerSpeedManager;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.NumberValue;
import net.minecraft.entity.player.PlayerEntity;

public class TimerRange extends Module {
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 3.0, 3.0, 6.0, 0.02);
   @SerializedName("boost")
   public final NumberValue boost = new NumberValue("Boost", 4.0, 1.0, 9.0, 0.1);
   @SerializedName("slowSpeed")
   public final NumberValue slowSpeed = new NumberValue("SlowSpeed", 0.2, 0.1, 0.9, 0.02);
   @SerializedName("onlyGround")
   public final BooleanValue onlyGround = new BooleanValue("OnlyGround", true);
   @SerializedName("notOnEat")
   public final BooleanValue notOnEat = new BooleanValue("NotOnEat", true);
   @SerializedName("onlyKillAura")
   public final BooleanValue onlyKillAura = new BooleanValue("OnlyKillaura", true);
   @SerializedName("phase")
   private TimerRange.Mode phase = TimerRange.Mode.OUTSIDE_RANGE;
   @SerializedName("target")
   private PlayerEntity target;

   public TimerRange() {
      super(Category.Combat, "TimerRange", "Boost timer when enemies enter range");
   }

   public PlayerEntity searchTargets() {
      if (KillAura.target != null
         && Module.mc.player.distanceTo(KillAura.target) < ((Number)this.range.getValue()).floatValue()
         && KillAura.target instanceof PlayerEntity) {
         return (PlayerEntity)KillAura.target;
      }

      double max_value = Double.MAX_VALUE;
      PlayerEntity player = null;

      for (PlayerEntity player2 : Module.mc.world.getPlayers()) {
         if (player2 != Module.mc.player && EntityTargetUtils.isLivingEntity(player2)) {
            double dist = Module.mc.player.distanceTo(player2);
            if (dist <= ((Number)this.range.getValue()).floatValue() && dist < max_value) {
               max_value = dist;
               player = player2;
            }
         }
      }

      return player;
   }

   @Override
   public void onDisable() {
      if (Module.mc.player != null) {
         TimerSpeedManager.setTimerSpeed(1.0F);
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() != Stage.POST && Module.mc.world != null && Module.mc.player != null) {
         if ((Boolean)this.onlyGround.getValue() && !Module.mc.player.isOnGround()) {
            TimerSpeedManager.setTimerSpeed(1.0F);
         } else if ((Boolean)this.notOnEat.getValue() && Module.mc.player.isUsingItem()) {
            TimerSpeedManager.setTimerSpeed(1.0F);
         } else if ((Boolean)this.onlyKillAura.getValue() && !Southside.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled()) {
            TimerSpeedManager.setTimerSpeed(1.0F);
         } else {
            this.target = this.searchTargets();
            if (this.target == null) {
               this.phase = this.phase == TimerRange.Mode.IN_RANGE ? TimerRange.Mode.LEAVING_RANGE : TimerRange.Mode.OUTSIDE_RANGE;
            } else {
               this.phase = this.phase == TimerRange.Mode.OUTSIDE_RANGE ? TimerRange.Mode.ENTERING_RANGE : TimerRange.Mode.IN_RANGE;
            }
            float f = switch (this.phase) {
               case ENTERING_RANGE -> ((Number)this.boost.getValue()).floatValue();
               case LEAVING_RANGE -> ((Number)this.slowSpeed.getValue()).floatValue();
               default -> 1.0F;
            };
            TimerSpeedManager.setTimerSpeed(f);
         }
      }
   }

   private enum Mode {
      OUTSIDE_RANGE,
      ENTERING_RANGE,
      IN_RANGE,
      LEAVING_RANGE;


      // $VF: synthetic method
      private static TimerRange.Mode[] createValues() {
         return new TimerRange.Mode[]{OUTSIDE_RANGE, ENTERING_RANGE, IN_RANGE, LEAVING_RANGE};
      }


   }
}
