package dev.southside.utils;

import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.utils.Notifications;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CustomEvent;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OneConfigCore {
   public static final OneConfigCore INSTANCE;
   public static final Logger LOGGER;
   public static boolean active;

   public OneConfigCore() {
      OneConfigEventManager.INSTANCE.register(this);
   }

   public void performAction() {
      if (!active) {
         OneConfigConfig.getInstance();
         Preferences.getPreferences();

         for (ModData modData : ConfigLoader.runnableMap.keySet()) {
            ConfigCore.modDatas.add(modData);
         }

         Notifications notifications = Objects.requireNonNull(Notifications.INSTANCE);
         OneConfigEventManager.INSTANCE.register(notifications);
         GuiUtils.getProgress();

         try {
            OneConfigEventManager.INSTANCE.register(IBlurHandler.INSTANCE);
         } catch (Exception e) {
            e.printStackTrace();
         }

         performActionEx();
      }
   }

   @EventTarget
   public void onCustom(CustomEvent customEvent) {
      ConfigCore.performAction();
   }

   private static void performActionEx() {
      OneConfigEventManager.INSTANCE.register(new HudCore());
      OneConfigEventManager.INSTANCE.register(KeyBindHandler.INSTANCE);
      ConfigCore.performActionImpl();
      active = true;
   }

   static {
      INSTANCE = new OneConfigCore();
      LOGGER = LogManager.getLogger("OneConfig");
      active = false;
   }
}
