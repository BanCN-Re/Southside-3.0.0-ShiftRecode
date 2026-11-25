package dev.southside.module.impl.movement;

import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class Sprint extends Module {
   public Sprint() {
      super(Category.Movement, "Sprint", "自动疾跑", true);
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (!stateEvent.getState().equals(Stage.POST)) {
         Module.mc.options.sprintKey.setPressed(true);
         Module.mc.options.sprintToggled.setValue(false);
      }
   }
}
