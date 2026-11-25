package dev.southside.mixin.misc;

import dev.southside.utils.IMutableVec3d;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3d.class)
public abstract class MixinVec3d implements IMutableVec3d {
   @Shadow
   @Final
   @Mutable
   public double x;
   @Shadow
   @Final
   @Mutable
   public double y;
   @Shadow
   @Final
   @Mutable
   public double z;

   @Override
   public void set(double x2, double y2, double z2) {
      this.x = x2;
      this.y = y2;
      this.z = z2;
   }
}
