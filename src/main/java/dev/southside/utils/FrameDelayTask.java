package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.RenderStageEvent;

public class FrameDelayTask {
   @SerializedName("runnable")
   private final Runnable runnable;
   @SerializedName("framesRemaining")
   private int framesRemaining;

   @EventTarget
   public void onPhaseEvent2(RenderStageEvent renderStageEvent) {
      if (renderStageEvent.phase == Stage.PRE) {
         if (this.framesRemaining < 1) {
            this.runnable.run();
            OneConfigEventManager.INSTANCE.unregister(this);
         }

         this.framesRemaining--;
      }
   }

   public FrameDelayTask(Runnable action) {
      OneConfigEventManager.INSTANCE.register(this);
      this.framesRemaining = 1;
      this.runnable = action;
   }
}
