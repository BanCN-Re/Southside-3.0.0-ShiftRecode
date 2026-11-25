package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.StateEvent;

public class TickDelayTask {
   @SerializedName("runnable")
   private final Runnable runnable;
   @SerializedName("ticksRemaining")
   private int ticksRemaining;

   public TickDelayTask(Runnable action, int n) {
      if (n < 1) {
         action.run();
      } else {
         OneConfigEventManager.INSTANCE.register(this);
         this.ticksRemaining = n;
      }

      this.runnable = action;
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE) {
         if (this.ticksRemaining < 1) {
            this.runnable.run();
            OneConfigEventManager.INSTANCE.unregister(this);
         }

         this.ticksRemaining--;
      }
   }
}
