package dev.southside.utils.misc;

import dev.southside.utils.Rotation;
import net.minecraft.util.math.Vec3d;

public record PositionRecord(Vec3d position, Rotation rotation) {
   public Vec3d getPosition() {
      return this.position;
   }

   public Rotation getRotation() {
      return this.rotation;
   }
}
