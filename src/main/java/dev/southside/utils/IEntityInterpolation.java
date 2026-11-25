package dev.southside.utils;

import net.minecraft.util.math.Vec3d;

public interface IEntityInterpolation {
   Vec3d getInterpolatedPosition();

   void setInterpolatedPosition(Vec3d vec);

   Vec3d getTargetPosition();

   void setTargetPosition(Vec3d vec);

   int getInterpolationTicks();

   void setInterpolationTicks(int n);
}
