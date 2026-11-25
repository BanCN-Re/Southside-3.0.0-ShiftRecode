package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;

public class PlayerMoveEvent extends CancellableEvent {
   @SerializedName("x")
   public double x;
   @SerializedName("y")
   public double y;
   @SerializedName("z")
   public double z;

   public double getZ() {
      return this.z;
   }

   public PlayerMoveEvent(double x2, double y2, double z2) {
      this.x = x2;
      this.y = y2;
      this.z = z2;
   }

   public double getX() {
      return this.x;
   }

   public void setY(double d) {
      this.y = d;
   }

   public void setX(double d) {
      this.x = d;
   }

   public double getY() {
      return this.y;
   }

   public void setZ(double d) {
      this.z = d;
   }
}
