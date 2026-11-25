package dev.southside.module.impl.movement;

import dev.southside.event.EventTarget;
import dev.southside.event.events.BlockStateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.MovementUtils;

public class FastWeb extends Module {
   public FastWeb() {
      super(Category.Movement, "FastWeb", "Fast Web");
   }

   @EventTarget
   public void onBlockState(BlockStateEvent blockStateEvent) {
      if (mc2.player != null && mc2.world != null && blockStateEvent.getEntity().getId() == mc2.player.getId()) {
         if (MovementUtils.isValid()) {
            double d = MovementUtils.countData >= 2 ? 0.5 : 0.35F;
            double d2 = Math.toRadians(MovementUtils.getProgress());
            double d3 = -Math.sin(d2);
            double d4 = Math.cos(d2);
            double dist = Math.sqrt(d3 * d3 + d4 * d4);
            if (dist > 0.0) {
               d3 /= dist;
               d4 /= dist;
            }

            mc2.player.setVelocity(d3 * d, mc2.player.getVelocity().y, d4 * d);
         } else {
            MovementUtils.performAction();
         }

         mc2.player.setVelocity(mc2.player.getVelocity().x, mc2.options.jumpKey.isPressed() ? 1.4 : -3.6, mc2.player.getVelocity().z);
      }
   }
}
