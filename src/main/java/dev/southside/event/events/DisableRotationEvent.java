package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;

public class DisableRotationEvent {
   @SerializedName("disableRotation")
   private boolean disableRotation;

   public void setDisableRotation(boolean flag) {
      this.disableRotation = flag;
   }

   public DisableRotationEvent(boolean flag) {
      this.disableRotation = flag;
   }

   public boolean isDisableRotation() {
      return this.disableRotation;
   }
}
