package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;

public class YawEvent {
   @SerializedName("yaw")
   public float yaw;

   public YawEvent(float f) {
      this.yaw = f;
   }

   public void setYaw(float f) {
      this.yaw = f;
   }

   public float getYaw() {
      return this.yaw;
   }
}
