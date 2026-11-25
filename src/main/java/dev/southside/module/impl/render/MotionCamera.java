package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

public final class MotionCamera extends Module {
   private static final double DEFAULT_DOUBLE = 0.05;
   private static final double DEFAULT_DOUBLE_VALUE = 4.0E-6;
   private static final double DEFAULT_DOUBLE_DATA = 64.0;
   private static final double DEFAULT_DOUBLE_REF = 5.0;
   private static final float DEFAULT_FLOAT = 0.001F;
   @SerializedName("followSpeed")
   public final NumberValue followSpeed = new NumberValue("Follow Speed", 0.12F, 0.0F, 1.0F, 0.01F);
   @SerializedName("distance")
   public final NumberValue distance = new NumberValue("Camera Distance", 4.0F, 1.0F, 8.0F, 0.1F);
   @SerializedName("world")
   private BlockView world;
   @SerializedName("focus")
   private Entity focus;
   @SerializedName("lastEyePosition")
   private Vec3d lastEyePosition;
   @SerializedName("anchor")
   private Vec3d anchor;
   @SerializedName("lastFrame")
   private long lastFrame;
   @SerializedName("currentDistance")
   private float currentDistance;

   public MotionCamera() {
      super(Category.Render, "MotionCamera", "运动相机：第三人称镜头带惯性跟随，切换视角时平滑拉远或收回。");
   }

   public Vec3d computePosition(BlockView blockView, Entity entity, Vec3d vec, boolean flag) {
      if (this.world != blockView || this.focus != entity) {
         this.resetToDefaults();
         this.world = blockView;
         this.focus = entity;
      }

      long now = System.nanoTime();
      double d = this.lastEyePosition == null ? 0.016666666666666666 : MathHelper.clamp((now - this.lastFrame) * 1.0E-9, 0.0, 0.05);
      this.lastFrame = now;
      if (flag && this.lastEyePosition != null && !(this.lastEyePosition.squaredDistanceTo(vec) > 64.0)) {
         double dist = this.anchor.squaredDistanceTo(vec);
         if (dist > 4.0E-6) {
            double clamped = MathHelper.clamp(((Number)this.followSpeed.getValue()).doubleValue(), 0.0, 1.0);
            double d2 = clamped * clamped * clamped;
            double d3 = 0.18 + d2 * 14.0 + Math.min(Math.sqrt(dist), 6.0) * (0.35 + d2 * 4.5);
            this.anchor = this.anchor.lerp(vec, -Math.expm1(-d3 * d));
         }
      } else {
         this.anchor = vec;
      }

      this.lastEyePosition = vec;
      float f = flag ? MathHelper.clamp(((Number)this.distance.getValue()).floatValue(), 1.0F, 8.0F) : 0.0F;
      this.currentDistance = MathHelper.lerp((float)(-Math.expm1(-5.0 * d)), this.currentDistance, f);
      if (Math.abs(this.currentDistance - f) < 0.001F) {
         this.currentDistance = f;
      }

      return this.anchor;
   }

   public float getCurrentDistance() {
      return this.currentDistance;
   }

   @Override
   public void resetToDefaults() {
      this.world = null;
      this.focus = null;
      this.lastEyePosition = null;
      this.anchor = null;
      this.lastFrame = 0L;
      this.currentDistance = 0.0F;
   }

   @Override
   public void onDisable() {
      this.resetToDefaults();
   }
}
