package dev.southside.module.impl.movement;

import dev.southside.event.EventTarget;
import dev.southside.event.events.JumpYawEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.KeyBindHandler;
import dev.southside.utils.MovementUtils;

public class Speed extends Module {
   public Speed() {
      super(Category.Movement, "Speed", "fake.", false);
   }

   @Override
   public int getHeightEx() {
      return 0;
   }

   @Override
   public int getHeightAlt() {
      return this.getHeightEx();
   }

   @EventTarget
   public void onYawEvent2(JumpYawEvent jumpYawEvent) {
      if (mc2.player != null && mc2.world != null) {
         if (mc2.player.isOnGround()) {
            mc2.player.jump();
         }

         mc2.options.sneakKey.setPressed(MovementUtils.countValue >= 2 && MovementUtils.countValue % 2 == 0 && !mc2.player.isOnGround());
      }
   }

   @Override
   public void onDisable() {
      mc2.options.sneakKey.setPressed(KeyBindHandler.isValid(mc2.options.sneakKey));
   }
}
