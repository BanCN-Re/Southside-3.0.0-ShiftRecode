package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;

public class SprintingEvent extends CancellableEvent {
   @SerializedName("sprinting")
   public boolean sprinting;

   public void setSprinting(boolean flag) {
      this.sprinting = flag;
   }

   public boolean isSprinting() {
      return this.sprinting;
   }

   public SprintingEvent(boolean flag) {
      this.sprinting = flag;
   }
}
