package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.EntityEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ValuePair;
import dev.southside.utils.EnchantmentUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class KBController extends Module {
   @SerializedName("horizontalAngle")
   public final NumberValue horizontalAngle = new NumberValue("Horizontal Angle", 0.0, -180.0, 180.0, 1.0);
   @SerializedName("onlyKnockbackEnchantment")
   public final BooleanValue onlyKnockbackEnchantment = new BooleanValue("Only KB Enchant", false);
   @SerializedName("target")
   private Entity target;
   @SerializedName("targetRotation")
   private Rotation targetRotation;
   @SerializedName("rotationReached")
   private boolean rotationReached;
   @SerializedName("replayingAttack")
   private boolean replayingAttack;

   public KBController() {
      super(Category.Combat, "KB Controller", "Control attack knockback direction");
   }

   @Override
   public int getHeightAlt() {
      return 2;
   }

   public boolean isValidEx() {
      return this.isEnabled() && this.target != null;
   }

   @Override
   public void onEnable() {
      this.performActionAlt();
   }

   @Override
   public void onDisable() {
      this.performActionAlt();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.performActionAlt();
   }

   private void performActionAlt() {
      this.target = null;
      this.targetRotation = null;
      this.rotationReached = false;
      this.replayingAttack = false;
   }

   @EventTarget(priority = 200)
   public void onEntity(EntityEvent entityEvent) {
      if (entityEvent.phase == Stage.PRE && Module.mc.player != null && Module.mc.world != null) {
         if (this.replayingAttack) {
            this.replayingAttack = false;
         } else if (entityEvent.entity != null && entityEvent.entity.isAlive()) {
            if (!(Boolean)this.onlyKnockbackEnchantment.getValue() || EnchantmentUtils.computeInt(Enchantments.KNOCKBACK, Module.mc.player.getMainHandStack()) > 0) {
               entityEvent.setCancelled(true);
               this.target = entityEvent.entity;
               Rotation currentRotation = RotationManager.getCurrentRotation();
               if (currentRotation == null) {
                  currentRotation = RotationManager.mc.player == null
                     ? new Rotation(0.0F, 0.0F)
                     : new Rotation(RotationManager.mc.player.getYaw(), RotationManager.mc.player.getPitch());
               }

               float wrapped = MathHelper.wrapDegrees(currentRotation.yaw + ((Number)this.horizontalAngle.getValue()).floatValue());
               this.targetRotation = new Rotation(wrapped, currentRotation.pitch);
               this.rotationReached = false;
            }
         }
      }
   }

   @EventTarget(priority = 100)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (Module.mc.player != null && Module.mc.world != null && this.target != null && this.targetRotation != null) {
         if (!this.target.isAlive()) {
            this.performActionAlt();
         } else if (!this.rotationReached) {
            RotationManager.rotationRequests.add(new ValuePair<>(this.targetRotation, this.getHeightAlt()));
         }
      }
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (xevent.getPhase() == Stage.POST && Module.mc.player != null && Module.mc.world != null && this.target != null && this.targetRotation != null) {
         if (!this.target.isAlive()) {
            this.performActionAlt();
         } else if (!this.rotationReached) {
            float f = Math.abs(MathHelper.wrapDegrees(xevent.getYaw() - this.targetRotation.yaw));
            float f2 = Math.abs(xevent.getPitch() - this.targetRotation.pitch);
            if (f < 1.0F && f2 < 1.0F) {
               this.rotationReached = true;
            }
         }
      }
   }

   @EventTarget(priority = -200)
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (this.rotationReached && Module.mc.player != null && Module.mc.world != null && this.target != null && Module.mc.interactionManager != null) {
         if (!this.target.isAlive()) {
            this.performActionAlt();
         } else {
            this.replayingAttack = true;
            Module.mc.interactionManager.attackEntity(Module.mc.player, this.target);
            Module.mc.player.swingHand(Hand.MAIN_HAND);
            this.performActionAlt();
         }
      }
   }
}
