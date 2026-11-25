package dev.southside.module.impl.world.sca;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.misc.PointRecord;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;

public class Line {
   @SerializedName("origin")
   public final Vec3d origin;
   @SerializedName("direction")
   public final Vec3d direction;

   public Vec3d intersectBoxFace(Box box, Direction direction2) {
      double axisComponent = getAxisComponent(this.origin, direction2.getAxis());
      double axisComponent2 = getAxisComponent(this.direction, direction2.getAxis());
      Double val = this.computeIntersection(getBoxFaceCoordinate(box, direction2), axisComponent, axisComponent2);
      if (val == null) {
         return null;
      }

      Vec3d vec = Vec3d.of(direction2.getVector()).multiply(val);
      Vec3d vec2 = this.origin.add(this.direction.normalize().multiply(val));
      return combineNonZeroComponents(vec, vec2);
   }

   public static boolean isNonNull(Vec3d vec) {
      return vec != null;
   }

   public Vec3d pointAt(double d) {
      return this.origin.add(this.direction.multiply(d));
   }

   public Line.Inner closestPointsTo(Line other) {
      double[] coords = this.closestParametersTo(other);
      return coords == null ? null : new Line.Inner(this.pointAt(coords[0]), other.pointAt(coords[1]));
   }

   public static double deviationFromMean(double d, double[] coords) {
      return Math.abs(coords[1] - d);
   }

   public Vec3d getPointAt(double d) {
      return this.pointAt(d);
   }

   public static double getBoxFaceCoordinate(Box box, Direction direction2) {
      return switch (direction2) {
         case DOWN -> box.minY;
         case UP -> box.maxY;
         case NORTH -> box.minZ;
         case SOUTH -> box.maxZ;
         case WEST -> box.minX;
         case EAST -> box.maxX;
         default -> throw new MatchException(null, null);
      };
   }

   @Override
   public String toString() {
      return "Line(position=" + this.origin + ", direction=" + this.direction + ")";
   }

   public static double getDirectionComponent(double[] coords) {
      return coords[1];
   }

   public Line(Vec3d vec, Vec3d vec2) {
      this.origin = vec;
      this.direction = vec2;
   }

   public static boolean hasNonZeroDirection(double[] coords) {
      return !MathHelper.approximatelyEquals(coords[1], 0.0);
   }

   public double parameterForPoint(Vec3d vec) {
      Vec3d delta = vec.subtract(this.origin);
      double[][] val = new double[][]{{delta.x, this.direction.x}, {delta.y, this.direction.y}, {delta.z, this.direction.z}};
      List<double[]> list = Arrays.stream(val).filter(Line::hasNonZeroDirection).collect(Collectors.toList());
      double d = list.stream().mapToDouble(Line::getDirectionComponent).average().orElse(0.0);
      double[] coords = list.stream().min(Comparator.comparingDouble(coords2 -> deviationFromMean(d, coords2))).orElse(list.get(0));
      return coords[0] / coords[1];
   }

   public Vec3d intersectBox(Box box) {
      return Arrays.stream(Direction.values())
         .map(direction2 -> this.intersectBoxFace(box, direction2))
         .filter(Line::isNonNull)
         .min(Comparator.comparingDouble(this::squaredDistanceFromOrigin))
         .orElse(null);
   }

   public static Line fromPoints(Vec3d vec, Vec3d vec2, boolean flag) {
      Vec3d delta = vec2.subtract(vec);
      Vec3d vec3 = flag ? delta.normalize() : delta;
      return new Line(vec, vec3);
   }

   public double[] closestParametersTo(Line other) {
      Double val = this.closestParameterTo(other);
      Double val2 = other.closestParameterTo(this);
      return val != null && val2 != null ? new double[]{val, val2} : null;
   }

   public static double getAxisComponent(Vec3d vec, Axis axis) {
      return axis.choose(vec.x, vec.y, vec.z);
   }

   public static Vec3d combineNonZeroComponents(Vec3d vec, Vec3d vec2) {
      double d = vec.x == 0.0 ? vec2.x : vec.x;
      double d2 = vec.y == 0.0 ? vec2.y : vec.y;
      double d3 = vec.z == 0.0 ? vec2.z : vec.z;
      return new Vec3d(d, d2, d3);
   }

   public Double closestParameterTo(Line other) {
      double otherOriginX = other.origin.x;
      double otherOriginY = other.origin.y;
      double otherOriginZ = other.origin.z;
      double otherDirX = other.direction.x;
      double otherDirY = other.direction.y;
      double otherDirZ = other.direction.z;
      double originX = this.origin.x;
      double originY = this.origin.y;
      double originZ = this.origin.z;
      double dirX = this.direction.x;
      double dirY = this.direction.y;
      double dirZ = this.direction.z;
      double denom = (otherDirY * otherDirY + otherDirX * otherDirX) * dirZ * dirZ
         + (-2.0 * otherDirY * otherDirZ * dirY - 2.0 * otherDirX * otherDirZ * dirX) * dirZ
         + (otherDirZ * otherDirZ + otherDirX * otherDirX) * dirY * dirY
         - 2.0 * otherDirX * otherDirY * dirX * dirY
         + (otherDirZ * otherDirZ + otherDirY * otherDirY) * dirX * dirX;
      if (MathHelper.approximatelyEquals(denom, 0.0)) {
         return null;
      }

      double t = -(
            ((otherDirY * otherDirY + otherDirX * otherDirX) * dirZ - otherDirY * otherDirZ * dirY - otherDirX * otherDirZ * dirX) * originZ
               + (-otherDirY * otherDirZ * dirZ + (otherDirZ * otherDirZ + otherDirX * otherDirX) * dirY - otherDirX * otherDirY * dirX) * originY
               + (-otherDirX * otherDirZ * dirZ - otherDirX * otherDirY * dirY + (otherDirZ * otherDirZ + otherDirY * otherDirY) * dirX) * originX
               + ((-otherDirY * otherDirY - otherDirX * otherDirX) * dirZ + otherDirY * otherDirZ * dirY + otherDirX * otherDirZ * dirX) * otherOriginZ
               + (otherDirY * otherDirZ * dirZ + (-otherDirZ * otherDirZ - otherDirX * otherDirX) * dirY + otherDirX * otherDirY * dirX) * otherOriginY
               + (otherDirX * otherDirZ * dirZ + otherDirX * otherDirY * dirY + (-otherDirZ * otherDirZ - otherDirY * otherDirY) * dirX) * otherOriginX
         )
         / denom;
      return t;
   }

   public Vec3d projectPoint(Vec3d vec) {
      PointRecord plane = new PointRecord(vec, this.direction);
      Vec3d vec2 = plane.computePosition(this);
      return vec2 != null ? vec2 : vec;
   }

   public Double computeIntersection(double x, double y, double z) {
      if (MathHelper.approximatelyEquals(z, 0.0)) {
         return null;
      }

      double d = (x - y) / z;
      return d > 0.0 ? d : null;
   }

   public double squaredDistanceFromOrigin(Vec3d vec) {
      return this.origin.squaredDistanceTo(vec);
   }

   public double squaredDistanceToPoint(Vec3d vec) {
      return this.projectPoint(vec).squaredDistanceTo(vec);
   }

   public static class Inner {
      @SerializedName("firstPoint")
      public final Vec3d firstPoint;
      @SerializedName("secondPoint")
      public final Vec3d secondPoint;

      public Inner(Vec3d vec, Vec3d vec2) {
         this.firstPoint = vec;
         this.secondPoint = vec2;
      }
   }
}
