package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;

public class JumpYawEvent {
   @SerializedName("yaw")
   public float yaw;

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float f) {
      this.yaw = f;
   }

   public JumpYawEvent(float f) {
      this.yaw = f;
   }
}
