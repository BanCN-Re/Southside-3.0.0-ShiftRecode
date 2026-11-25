package dev.southside.utils.misc;

import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public record StartRecord(Vec3d start, Vec3d end) {
   public static StartRecord getStartRecord(Matrix4f matrix, double d, double d2) {
      if (Float.isFinite((float)d) && Float.isFinite((float)d2) && matrix.isFinite()) {
         Vector3f vec = matrix.transformPosition(new Vector3f((float)d, (float)d2, 4096.0F));
         Vector3f vec2 = matrix.transformPosition(new Vector3f((float)d, (float)d2, -4096.0F));
         return vec.isFinite() && vec2.isFinite() && !vec.equals(vec2)
            ? new StartRecord(new Vec3d(vec.x, vec.y, vec.z), new Vec3d(vec2.x, vec2.y, vec2.z))
            : null;
      } else {
         return null;
      }
   }

   public double computeDouble(Vec3d vec) {
      Vec3d delta = this.end.subtract(this.start);
      return vec.subtract(this.start).dotProduct(delta) / delta.lengthSquared();
   }

   public Vec3d getStart() {
      return this.start;
   }

   public Vec3d getEnd() {
      return this.end;
   }
}
