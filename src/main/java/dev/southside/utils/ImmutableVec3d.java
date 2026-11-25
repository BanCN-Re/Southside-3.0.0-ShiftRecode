package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class ImmutableVec3d {
   @SerializedName("x")
   public final double x;
   @SerializedName("y")
   public final double y;
   @SerializedName("z")
   public final double z;

   public ImmutableVec3d getImmutableVec3d(double d) {
      return new ImmutableVec3d(this.x * d, this.y * d, this.z * d);
   }

   public double getX() {
      return this.x;
   }

   public ImmutableVec3d(double x2, double y2, double z2) {
      this.x = x2;
      this.y = y2;
      this.z = z2;
   }

   public double getZ() {
      return this.z;
   }

   public double getY() {
      return this.y;
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof ImmutableVec3d immutableVec3d)
         ? false
         : Math.floor(this.x) == Math.floor(immutableVec3d.x) && Math.floor(this.y) == Math.floor(immutableVec3d.y) && Math.floor(this.z) == Math.floor(immutableVec3d.z);
   }

   public double computeDistance() {
      return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public ImmutableVec3d getImmutableVec3d(double x2, double y2, double z2) {
      return new ImmutableVec3d(this.x + x2, this.y + y2, this.z + z2);
   }

   public ImmutableVec3d getImmutableVec3d(ImmutableVec3d other) {
      return this.getImmutableVec3d(other.x, other.y, other.z);
   }

   public ImmutableVec3d getImmutableVec3dEx(ImmutableVec3d other) {
      return this.getImmutableVec3d(-other.x, -other.y, -other.z);
   }

   public ImmutableVec3d getImmutableVec3dEx(double x2, double y2, double z2) {
      return this.getImmutableVec3d(-x2, -y2, -z2);
   }
}
