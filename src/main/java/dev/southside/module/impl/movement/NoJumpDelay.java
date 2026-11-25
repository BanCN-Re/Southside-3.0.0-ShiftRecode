package dev.southside.module.impl.movement;

import dev.southside.event.EventTarget;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class NoJumpDelay extends Module {
   public NoJumpDelay() {
      super(Category.Movement, "NoJumpDelay", "Removes the delay between jumping and falling");
   }

   @EventTarget(priority = 8964)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (mc2.player != null && mc2.world != null) {
         mc2.player.jumpingCooldown = 0;
      }
   }
}
