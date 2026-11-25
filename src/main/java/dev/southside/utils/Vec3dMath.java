package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Vec3dMath {
   @SerializedName("x")
   public double x;
   @SerializedName("y")
   public double y;
   @SerializedName("z")
   public double z;

   public Vec3dMath getVec3dMath(Vec3dMath other) {
      return new Vec3dMath(other.x - this.x, other.y - this.y, other.z - this.z);
   }

   public double computeDouble(Vec3dMath other) {
      double dx = other.x - this.x;
      double dy = other.y - this.y;
      double dz = other.z - this.z;
      return dx * dx + dy * dy + dz * dz;
   }

   public Vec3dMath(double x2, double y2, double z2) {
      if (x2 == -0.0) {
         x2 = 0.0;
      }

      if (y2 == -0.0) {
         y2 = 0.0;
      }

      if (z2 == -0.0) {
         z2 = 0.0;
      }

      this.x = x2;
      this.y = y2;
      this.z = z2;
   }

   public Vec3dMath getVec3dMath(double x2, double y2, double z2) {
      return new Vec3dMath(this.x + x2, this.y + y2, this.z + z2);
   }

   public double computeDoubleEx(Vec3dMath other) {
      return this.x * other.x + this.y * other.y + this.z * other.z;
   }

   public Vec3dMath getVec3dMath(Vec3dMath vec3dMath, double d) {
      double dx = vec3dMath.x - this.x;
      double dy = vec3dMath.y - this.y;
      double dz = vec3dMath.z - this.z;
      if (dy * dy < 1.0E-7F) {
         return null;
      }

      double d2 = (d - this.y) / dy;
      return d2 >= 0.0 && d2 <= 1.0 ? new Vec3dMath(this.x + dx * d2, this.y + dy * d2, this.z + dz * d2) : null;
   }

   public double computeDistance() {
      return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public Vec3dMath getVec3dMathEx(Vec3dMath vec3dMath, double d) {
      double dx = vec3dMath.x - this.x;
      double dy = vec3dMath.y - this.y;
      double dz = vec3dMath.z - this.z;
      if (dz * dz < 1.0E-7F) {
         return null;
      }

      double d2 = (d - this.z) / dz;
      return d2 >= 0.0 && d2 <= 1.0 ? new Vec3dMath(this.x + dx * d2, this.y + dy * d2, this.z + dz * d2) : null;
   }

   public void performAction(Vec3d vec) {
      this.x = vec.getX();
      this.y = vec.getY();
      this.z = vec.getZ();
   }

   public Vec3dMath getVec3dMathEx(Vec3dMath other) {
      return this.getVec3dMath(other.x, other.y, other.z);
   }

   public Vec3dMath getVec3dMathEx(double x2, double y2, double z2) {
      return new Vec3dMath(this.x * x2, this.y * y2, this.z * z2);
   }

   @Override
   public String toString() {
      double z2 = this.z;
      double y2 = this.y;
      double x2 = this.x;
      double d = z2;
      double d2 = y2;
      double d3 = x2;
      return "(" + d3 + ", " + d2 + ", " + d + ")";
   }

   public Vec3dMath getVec3dMathAlt(double x2, double y2, double z2) {
      return this.getVec3dMath(-x2, -y2, -z2);
   }

   public Vec3dMath getVec3dMathAlt(Vec3dMath vec3dMath, double d) {
      double dx = vec3dMath.x - this.x;
      double dy = vec3dMath.y - this.y;
      double dz = vec3dMath.z - this.z;
      if (dx * dx < 1.0E-7F) {
         return null;
      }

      double d2 = (d - this.x) / dx;
      return d2 >= 0.0 && d2 <= 1.0 ? new Vec3dMath(this.x + dx * d2, this.y + dy * d2, this.z + dz * d2) : null;
   }

   public Vec3dMath getVec3dMathAlt(Vec3dMath other) {
      return new Vec3dMath(this.y * other.z - this.z * other.y, this.z * other.x - this.x * other.z, this.x * other.y - this.y * other.x);
   }

   public static Vec3dMath getVec3dMath(BlockPos pos) {
      return new Vec3dMath(pos.getX(), pos.getY(), pos.getZ());
   }

   public Vec3dMath getVec3dMath() {
      return new Vec3dMath(this.x, 0.0, this.z);
   }

   public Vec3dMath getVec3dMathImpl(Vec3dMath other) {
      return this.getVec3dMathAlt(other.x, other.y, other.z);
   }

   public Vec3dMath getVec3dMath(double d) {
      return this.getVec3dMathEx(d, d, d);
   }
}
