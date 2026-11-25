package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class MutableVec3d {
   @SerializedName("x")
   public double x;
   @SerializedName("y")
   public double y;
   @SerializedName("z")
   public double z;

   public double getX() {
      return this.x;
   }

   public MutableVec3d getMutableVec3d(MutableVec3d other) {
      return this.getMutableVec3d(-other.x, -other.y, -other.z);
   }

   public double getZ() {
      return this.z;
   }

   public MutableVec3d getMutableVec3d(double x2, double y2, double z2) {
      return new MutableVec3d(this.x + x2, this.y + y2, this.z + z2);
   }

   public double computeDistance() {
      return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public MutableVec3d getMutableVec3d(double d) {
      return new MutableVec3d(this.x * d, this.y * d, this.z * d);
   }

   public MutableVec3d getMutableVec3dEx(double x2, double y2, double z2) {
      return this.getMutableVec3d(-x2, -y2, -z2);
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof MutableVec3d mutableVec3d)
         ? false
         : Math.floor(this.x) == Math.floor(mutableVec3d.x) && Math.floor(this.y) == Math.floor(mutableVec3d.y) && Math.floor(this.z) == Math.floor(mutableVec3d.z);
   }

   public MutableVec3d getMutableVec3dEx(MutableVec3d other) {
      return this.getMutableVec3d(other.x, other.y, other.z);
   }

   public void setX(double d) {
      this.x = d;
   }

   public double computeDistance(MutableVec3d other) {
      return Math.sqrt(Math.pow(other.x - this.x, 2.0) + Math.pow(other.y - this.y, 2.0) + Math.pow(other.z - this.z, 2.0));
   }

   public MutableVec3d(double x2, double y2, double z2) {
      this.x = x2;
      this.y = y2;
      this.z = z2;
   }

   public double getY() {
      return this.y;
   }

   public void setY(double d) {
      this.y = d;
   }

   public void setZ(double d) {
      this.z = d;
   }
}
