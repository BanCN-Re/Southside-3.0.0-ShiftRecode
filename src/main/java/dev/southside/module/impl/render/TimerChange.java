package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.WorldTimeController;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class TimerChange extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Pinned", "Pinned", "Fast");
   @SerializedName("timer")
   public final NumberValue timer;
   @SerializedName("speed")
   public final NumberValue speed;
   @SerializedName("timeOverride")
   private final WorldTimeController timeOverride;
   private static final long DEFAULT_LONG_VALUE = 4803855224756240408L;
   private static final long m = 1000L;

   public TimerChange() {
      super(Category.Render, "TimerChange");
      this.timer = new NumberValue("Timer", 1, 0, (int)DEFAULT_LONG_VALUE, 1);
      this.speed = new NumberValue("Speed", 2.0, 0.5, 20.0, 0.5);
      this.timeOverride = new WorldTimeController();
   }

   @Override
   public String getSuffix() {
      return this.mode.getDisplayTextEx();
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.timer.getLabel(), () -> !this.mode.matches("Pinned"));
      this.hideIf(this.speed.getLabel(), () -> !this.mode.matches("Fast"));
   }

   @Override
   public void onEnable() {
      this.timeOverride.performAction(mc2.world);
   }

   @Override
   public void onDisable() {
      this.timeOverride.performActionAlt(mc2.world);
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (!directionEvent.isValid() && directionEvent.getPacket() instanceof WorldTimeUpdateS2CPacket worldTimeUpdateS2CPacket) {
         ClientWorld world = mc2.world;
         directionEvent.setCancelled(true);
         mc2.execute(() -> {
            if (mc2.world == world) {
               this.timeOverride.update(world, worldTimeUpdateS2CPacket);
               if (!this.isEnabled()) {
                  this.timeOverride.performActionAlt(world);
               }
            }
         });
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (this.mode.matches("Pinned")) {
         this.timeOverride.performAction(mc2.world, ((Number)this.timer.getValue()).longValue() * m);
      } else {
         this.timeOverride.performAction(mc2.world, ((Number)this.speed.getValue()).doubleValue());
      }
   }
}
