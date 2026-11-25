package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class HeadLookController {
   private static final float DEFAULT_FLOAT = 0.65F;
   private static final float DEFAULT_FLOAT_VALUE = 0.35F;
   @SerializedName("inversePose")
   private final Matrix4f inversePose = new Matrix4f();
   @SerializedName("head")
   private final Vector3f head = new Vector3f();
   @SerializedName("direction")
   private final Vector3f direction = new Vector3f();
   @SerializedName("yaw")
   private float yaw;
   @SerializedName("pitch")
   private float pitch;

   public void performAction(Matrix4f matrix, float f, float f2, float f3, float f4) {
      if (!Float.isFinite(this.yaw)) {
         this.yaw = 0.0F;
      }

      if (!Float.isFinite(this.pitch)) {
         this.pitch = 0.0F;
      }

      if (Float.isFinite(f) && Float.isFinite(f2) && Float.isFinite(f3) && !(f3 <= 0.0F) && Float.isFinite(f4) && matrix.isFinite()) {
         float f5 = matrix.determinant();
         if (Float.isFinite(f5) && !(Math.abs(f5) < 1.0E-8F)) {
            matrix.transformPosition(this.head.set(0.0F, -0.3F, 0.0F));
            this.direction.set(f - this.head.x, f2 - this.head.y, f3 * 3.0F);
            this.inversePose.set(matrix).invert().transformDirection(this.direction);
            if (this.head.isFinite() && this.direction.isFinite()) {
               float f6 = this.direction.z < 0.0F ? (float)Math.atan2(-this.direction.x, -this.direction.z) : 0.0F;
               float angle = (float)Math.atan2(this.direction.y, Math.hypot(this.direction.x, this.direction.z));
               float f7 = computeFloat(f4, 0.0F, 1.0F);
               this.yaw = this.yaw + (computeFloat(f6, -0.65F, 0.65F) - this.yaw) * f7;
               this.pitch = this.pitch + (computeFloat(angle, -0.35F, 0.35F) - this.pitch) * f7;
            }
         }
      }
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   private static float computeFloat(float x, float y, float z) {
      return Math.max(y, Math.min(z, x));
   }
}
