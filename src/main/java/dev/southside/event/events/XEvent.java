package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.Stage;
import dev.southside.utils.CancellableEvent;

public class XEvent extends CancellableEvent {
   @SerializedName("x")
   public double x;
   @SerializedName("y")
   public double y;
   @SerializedName("z")
   public double z;
   @SerializedName("yaw")
   public float yaw;
   @SerializedName("pitch")
   public float pitch;
   @SerializedName("onGround")
   public boolean onGround;
   @SerializedName("horizontalCollision")
   public boolean horizontalCollision;
   @SerializedName("rotationModified")
   public boolean rotationModified = false;
   @SerializedName("phase")
   public Stage phase;
   @SerializedName("forceGroundPacket")
   public boolean forceGroundPacket;
   @SerializedName("forceFullPacket")
   private boolean forceFullPacket;

   public boolean isForceFullPacket() {
      return this.forceFullPacket;
   }

   public void setForceFullPacket(boolean flag) {
      this.forceFullPacket = flag;
   }

   public boolean isForceGroundPacket() {
      return this.forceGroundPacket;
   }

   public void setY(double d) {
      this.y = d;
   }

   public void setForceGroundPacket(boolean flag) {
      this.forceGroundPacket = flag;
   }

   public void setPhase(Stage stage) {
      this.phase = stage;
   }

   public Stage getPhase() {
      return this.phase;
   }

   public void setProgress(float f) {
      this.yaw = f;
      this.rotationModified = true;
   }

   public float getYaw() {
      return this.yaw;
   }

   public XEvent(double d, double d2, double d3, float f, float f2, boolean flag, boolean flag2, Stage stage) {
      this.x = d;
      this.y = d2;
      this.z = d3;
      this.yaw = f;
      this.pitch = f2;
      this.onGround = flag;
      this.horizontalCollision = flag2;
      this.phase = stage;
      this.forceGroundPacket = false;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setZ(double d) {
      this.z = d;
   }

   public double getZ() {
      return this.z;
   }

   public void setOnGround(boolean flag) {
      this.onGround = flag;
   }

   public boolean isRotationModified() {
      return this.rotationModified;
   }

   public void setPitch(float f) {
      this.pitch = f;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public void setHorizontalCollision(boolean flag) {
      this.horizontalCollision = flag;
   }

   public void setRotationModified(boolean flag) {
      this.rotationModified = flag;
   }

   public double getX() {
      return this.x;
   }

   public boolean isHorizontalCollision() {
      return this.horizontalCollision;
   }

   public void setX(double d) {
      this.x = d;
   }

   public double getY() {
      return this.y;
   }
}
