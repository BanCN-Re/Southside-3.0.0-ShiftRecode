package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.math.Vec3d;

public final class Rotation {
   @SerializedName("yaw")
   public float yaw;
   @SerializedName("pitch")
   public float pitch;
   @SerializedName("targetPosition")
   public Vec3d targetPosition;
   @SerializedName("sourcePosition")
   public Vec3d sourcePosition;

   public void setProgress(float f) {
      float f2 = f * 0.6F + 0.2F;
      float f3 = f2 * f2 * f2 * 1.2F;
      this.yaw = this.yaw - this.yaw % f3;
      this.pitch = this.pitch - this.pitch % f3;
   }

   public void setYaw(float f) {
      this.yaw = f;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public Rotation(float f, float f2, Vec3d vec, Vec3d vec2) {
      this.yaw = f;
      this.pitch = f2;
      this.targetPosition = vec;
      this.sourcePosition = vec2;
   }

   public Vec3d getSourcePosition() {
      return this.sourcePosition;
   }

   public void setTargetPosition(Vec3d vec) {
      this.targetPosition = vec;
   }

   public Rotation(float x, float y) {
      this.yaw = x;
      this.pitch = y;
   }

   public Vec3d getTargetPosition() {
      return this.targetPosition;
   }

   public void setPitch(float f) {
      this.pitch = f;
   }

   public void setSourcePosition(Vec3d vec) {
      this.sourcePosition = vec;
   }
}
