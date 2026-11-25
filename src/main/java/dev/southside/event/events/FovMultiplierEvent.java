package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;

public class FovMultiplierEvent extends CancellableEvent {
   @SerializedName("fovMultiplier")
   public float fovMultiplier;

   public void setFovMultiplier(float f) {
      this.fovMultiplier = f;
   }

   public void setFovMultiplier2(float f) {
      this.fovMultiplier = f;
   }

   public FovMultiplierEvent(float f) {
      this.fovMultiplier = f;
   }

   public float getFovMultiplier() {
      return this.fovMultiplier;
   }
}
