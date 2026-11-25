package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ModeValue;
import net.minecraft.util.math.MathHelper;

public class Spider extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Test", "Test");
   @SerializedName("wallClimbing")
   private boolean wallClimbing;

   public Spider() {
      super(Category.Movement, "Spider", "Allows you to walk on walls");
   }

   @EventTarget
   public void onX(XEvent xevent) {
      this.setInfoText(this.mode.getDisplayTextEx());
      if (mc2.player != null && mc2.world != null && this.mode.matches("Test")) {
         if (mc2.player.horizontalCollision) {
            mc2.player.setSprinting(false);
            if (!this.wallClimbing && mc2.player.isOnGround()) {
               this.wallClimbing = true;
               this.onMotion(xevent);
            }

            if (mc2.player.getVelocity().y <= 0.0 && this.wallClimbing) {
               xevent.setOnGround(true);
               mc2.player.jump();
            }
         } else if (mc2.player.isOnGround()) {
            this.wallClimbing = false;
         }
      }
   }

   private void onMotion(XEvent xevent) {
      float forwardSpeed = mc2.player.forwardSpeed;
      float f = mc2.player.getYaw() + (forwardSpeed < 0.0F ? 180.0F : 0.0F);
      float f2 = forwardSpeed < 0.0F ? -0.5F : (forwardSpeed > 0.0F ? 0.5F : 1.0F);
      if (mc2.player.sidewaysSpeed > 0.0F) {
         f -= 70.0F * f2;
      } else if (mc2.player.sidewaysSpeed < 0.0F) {
         f += 70.0F * f2;
      }

      float f3 = (float)Math.toRadians(f);
      xevent.setX(xevent.getX() - -MathHelper.sin(f3) * 1.0E-4F);
      xevent.setZ(xevent.getZ() - MathHelper.cos(f3) * 1.0E-4F);
   }
}
