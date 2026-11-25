package dev.southside.utils.misc;

import dev.southside.module.impl.world.sca.Line;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record PointRecord(Vec3d point, Vec3d normal) {
   public static PointRecord getPointRecord2(Vec3d vec, Vec3d vec2, Vec3d vec3) {
      Vec3d dir = vec2.crossProduct(vec3).normalize();
      if (MathHelper.approximatelyEquals(dir.lengthSquared(), 0.0)) {
         throw new IllegalArgumentException("Points must not be on the same line");
      } else {
         return new PointRecord(vec, dir);
      }
   }

   public Line getLine(PointRecord other) {
      double otherNormalX = other.normal.x;
      double otherNormalY = other.normal.y;
      double otherNormalZ = other.normal.z;
      double d = other.normal.dotProduct(other.point);
      double normalX = this.normal.x;
      double normalY = this.normal.y;
      double normalZ = this.normal.z;
      double d2 = this.normal.dotProduct(this.point);
      double d3 = normalX * otherNormalZ - otherNormalX * normalZ;
      double d4 = normalX * otherNormalY - otherNormalX * normalY;
      if (!MathHelper.approximatelyEquals(d3, 0.0)) {
         return new Line(
            new Vec3d((-d * normalZ + d2 * otherNormalZ) / d3, 0.0, (d * normalX - d2 * otherNormalX) / d3),
            new Vec3d((-otherNormalZ * normalY + normalZ * otherNormalY) / d3, 1.0, (otherNormalX * normalY - normalX * otherNormalY) / d3)
         );
      } else {
         return !MathHelper.approximatelyEquals(d4, 0.0)
            ? new Line(
               new Vec3d((-d * normalY + d2 * otherNormalY) / d4, (d * normalX - d2 * otherNormalX) / d4, 0.0),
               new Vec3d((-otherNormalY * normalZ + normalY * otherNormalZ) / d4, (otherNormalX * normalZ - normalX * otherNormalZ) / d4, 1.0)
            )
            : null;
      }
   }

   public static PointRecord getPointRecord(Vec3d vec, Vec3d vec2, Vec3d vec3) {
      Vec3d delta = vec2.subtract(vec);
      Vec3d delta2 = vec3.subtract(vec);
      return getPointRecord2(vec, delta, delta2);
   }

   public Double getDouble(Line line) {
      double d = this.point.dotProduct(this.normal);
      double d2 = line.direction.dotProduct(this.normal);
      return MathHelper.approximatelyEquals(d2, 0.0) ? null : (d - line.origin.dotProduct(this.normal)) / d2;
   }

   public Vec3d computePosition(Line line) {
      Double val = this.getDouble(line);
      return val != null ? line.getPointAt(val) : null;
   }

   public Vec3d getPoint() {
      return this.point;
   }

   public Vec3d getNormal() {
      return this.normal;
   }
}
