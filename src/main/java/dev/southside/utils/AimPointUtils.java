package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class AimPointUtils {
   private AimPointUtils() {
   }

   public static float[] getFloatList(Vec3d vec, Vec3d vec2) {
      double dx = vec2.x - vec.x;
      double dy = vec2.y - vec.y;
      double dz = vec2.z - vec.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float f = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float f2 = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return new float[]{MathHelper.wrapDegrees(f), f2};
   }

   public static Vec3d computePosition(Entity entity) {
      Box boundingBox = entity.getBoundingBox();
      return new Vec3d((boundingBox.minX + boundingBox.maxX) * 0.5, MathHelper.lerp(0.8, boundingBox.minY, boundingBox.maxY), (boundingBox.minZ + boundingBox.maxZ) * 0.5);
   }

   public static Vec3d computePositionEx(Entity entity) {
      Box boundingBox = entity.getBoundingBox();
      return new Vec3d((boundingBox.minX + boundingBox.maxX) * 0.5, MathHelper.lerp(0.15, boundingBox.minY, boundingBox.maxY), (boundingBox.minZ + boundingBox.maxZ) * 0.5);
   }

   public static Vec3d computePositionAlt(Entity entity) {
      Box boundingBox = entity.getBoundingBox();
      return new Vec3d((boundingBox.minX + boundingBox.maxX) * 0.5, (boundingBox.minY + boundingBox.maxY) * 0.5, (boundingBox.minZ + boundingBox.maxZ) * 0.5);
   }

   public static Vec3d computePosition(Vec3d vec, Box box) {
      return new Vec3d(
         MathHelper.clamp(vec.x, box.minX, box.maxX), MathHelper.clamp(vec.y, box.minY, box.maxY), MathHelper.clamp(vec.z, box.minZ, box.maxZ)
      );
   }

   public static float computeFloat(float x, float y) {
      return Math.abs(MathHelper.wrapDegrees(x - y));
   }

   public static Rotation computeRotation(AimPointUtils.Inner inner) {
      return new Rotation(inner.targetYaw, inner.pitch);
   }

   public static AimPointUtils.Inner getInner(ClientPlayerEntity player, Entity entity, float f) {
      Vec3d vector = BlockLookUtils.vector;
      Vec3d vec = computePosition(vector, entity.getBoundingBox());
      List list = List.of(vec, computePosition(entity), computePositionAlt(entity), computePositionEx(entity));
      Vec3d vec2 = vec;
      float max_value = Float.MAX_VALUE;
      boolean flag = false;
      float yaw = player.getYaw(f);
      float pitch = player.getPitch(f);

      for (Object obj : list) {
         Vec3d vec3 = (Vec3d)obj;
         float[] floatList = getFloatList(vector, vec3);
         boolean isHovered = BlockLookUtils.isHovered(floatList[0], floatList[1], 3.0, entity.getBoundingBox());
         float f2 = computeFloat(yaw, floatList[0]) + Math.abs(pitch - floatList[1]);
         if (isHovered) {
            if (!flag || f2 < max_value) {
               flag = true;
               max_value = f2;
               vec2 = vec3;
            }
         } else if (!flag && f2 < max_value) {
            max_value = f2;
            vec2 = vec3;
         }
      }

      float[] floatList2 = getFloatList(vector, vec2);
      return new AimPointUtils.Inner(floatList2[0], floatList2[1], vec2, flag, computeFloat(floatList2[0], yaw), Math.abs(floatList2[1] - pitch));
   }

   public static final class Inner {
      @SerializedName("targetYaw")
      public final float targetYaw;
      @SerializedName("pitch")
      public final float pitch;
      @SerializedName("point")
      public final Vec3d point;
      @SerializedName("visible")
      public final boolean visible;
      @SerializedName("yawDiff")
      public final float yawDiff;
      @SerializedName("pitchDiff")
      public final float pitchDiff;

      public Inner(float f, float f2, Vec3d vec, boolean flag, float f3, float f4) {
         this.targetYaw = f;
         this.pitch = f2;
         this.point = vec;
         this.visible = flag;
         this.yawDiff = f3;
         this.pitchDiff = f4;
      }
   }
}
