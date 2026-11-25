package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.render.OldHitting;

public final class HeldItemAnimationGuard {
   private HeldItemAnimationGuard() {
   }

   public static boolean isValid() {
      ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
      return !moduleManager.getModule(OldHitting.class).isEnabled() ? false : moduleManager.getModule(KillAura.class).isEnabled() && KillAura.isBlocking();
   }
}
