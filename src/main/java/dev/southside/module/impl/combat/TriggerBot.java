package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.AttackTracker;
import dev.southside.utils.IMinecraftClientExt;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.math.RandomUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TriggerBot extends Module {
   @SerializedName("minCps")
   public NumberValue minCps = new NumberValue("Min CPS", 8, 0, 20, 1);
   @SerializedName("maxCps")
   public NumberValue maxCps = new NumberValue("Max CPS", 10, 0, 20, 1);
   @SerializedName("useAttackCooldown")
   public final BooleanValue useAttackCooldown = new BooleanValue("Attack 1.9+", false);
   private static final MillisTimer lastTimeUtil = new MillisTimer();
   private static Entity target;
   @SerializedName("attackDelay")
   private long attackDelay;
   private static final long n = 100L;

   public TriggerBot() {
      super(Category.Combat, "TriggerBot");
      this.attackDelay = n;
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (Module.mc.player != null && Module.mc.world != null && Module.mc.interactionManager != null) {
         if (Module.mc.crosshairTarget instanceof EntityHitResult entityHit && entityHit.getType().equals(Type.ENTITY)) {
            target = entityHit.getEntity();
         }

         if (target instanceof LivingEntity living && this.isValidTarget(living) && lastTimeUtil.hasElapsed(this.attackDelay)) {
            if (!(Boolean)this.useAttackCooldown.getValue() || Module.mc.player.getAttackCooldownProgress(0.0F) >= 1.0) {
               ((IMinecraftClientExt)Module.mc).attack();
               AttackTracker.INSTANCE.performAction();
            }

            this.performActionAlt();
         }
      }
   }

   private void performActionAlt() {
      double d = ((Number)this.minCps.getValue()).intValue();
      double d2 = ((Number)this.maxCps.getValue()).intValue();
      double d3 = d != d2 && !(d2 - d <= 0.0) ? d + (d2 - d) * RandomUtils.RAND.nextDouble() : d;
      this.attackDelay = (long)(1000.0 / d3);
   }

   public boolean isValidTarget(LivingEntity living) {
      if (Module.mc.player != null && Module.mc.world != null && living != null && !Module.mc.player.isUsingItem()) {
         return computeFloat(living) > 5.0F ? false : this.computeDistance(living) <= 3.0;
      } else {
         return false;
      }
   }

   public double computeDistance(LivingEntity living) {
      if (Module.mc.player == null) {
         return 0.0;
      }

      Vec3d eyePos = Module.mc.player.getEyePos();
      Box box = living.getBoundingBox().expand(living.getTargetingMargin());
      double clamped = MathHelper.clamp(eyePos.x, box.minX, box.maxX);
      double clamped2 = MathHelper.clamp(eyePos.y, box.minY, box.maxY);
      double clamped3 = MathHelper.clamp(eyePos.z, box.minZ, box.maxZ);
      return eyePos.distanceTo(new Vec3d(clamped, clamped2, clamped3));
   }

   private static float computeFloat(Entity entity) {
      if (RotationManager.mc.player == null) {
         return 0.0F;
      }

      Vec3d cameraPosVec = RotationManager.mc.player.getCameraPosVec(1.0F);
      Box boundingBox = entity.getBoundingBox();
      double d = computeDouble(cameraPosVec.x, boundingBox.minX, boundingBox.maxX);
      double d2 = computeDouble(cameraPosVec.y, boundingBox.minY, boundingBox.maxY);
      double d3 = computeDouble(cameraPosVec.z, boundingBox.minZ, boundingBox.maxZ);
      double d4 = Math.abs(d - cameraPosVec.x);
      double d5 = Math.abs(d2 - cameraPosVec.y);
      double d6 = Math.abs(d3 - cameraPosVec.z);
      return (float)Math.sqrt(Math.pow(d4, 2.0) + Math.pow(d5, 2.0) + Math.pow(d6, 2.0));
   }

   private static double computeDouble(double x, double y, double z) {
      return x > z ? z : (x < y ? y : x);
   }
}
