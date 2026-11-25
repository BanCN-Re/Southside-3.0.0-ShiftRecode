package dev.southside.utils;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;

public class MissHitResult extends HitResult {
   public MissHitResult(Vec3d vec) {
      super(vec);
   }

   public Type getType() {
      return Type.MISS;
   }
}
