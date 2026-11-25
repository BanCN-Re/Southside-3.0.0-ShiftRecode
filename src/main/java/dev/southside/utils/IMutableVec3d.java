package dev.southside.utils;

import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public interface IMutableVec3d {
   void set(double x, double y, double z);

   default void performAction(Vector3d vec) {
      this.set(vec.x, vec.y, vec.z);
   }

   default void performAction(Vec3i vec3i) {
      this.set(vec3i.getX(), vec3i.getY(), vec3i.getZ());
   }
}
