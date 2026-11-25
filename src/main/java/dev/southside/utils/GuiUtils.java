package dev.southside.utils;

import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.RenderStageEvent;
import dev.southside.gui.CustomScreen;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;

public final class GuiUtils {
   private static long timestamp = -1L;
   private static long timestampValue = 17L;
   private static boolean active;
   private static final Deque<Optional<Screen>> queue = new ConcurrentLinkedDeque<>();

   @Deprecated
   public static void performAction(Object obj) {
      openScreen((Screen)obj);
   }

   public static void openScreen(Screen screen) {
      openScreen(screen, screen instanceof OneConfigGui ? 2 : 1);
   }

   public static void openScreen(Screen screen, int n) {
      Optional optional = Optional.ofNullable(screen);
      queue.add(optional);
      new TickDelayTask(() -> {
         CustomScreen.openScreen((Screen)optional.orElse(null));
         queue.remove(optional);
      }, n);
   }

   public static Deque<Optional<Screen>> getQueue() {
      return queue;
   }

   public static void performAction() {
      CustomScreen.openScreen(null);
   }

   public static float getProgress() {
      return (float)timestampValue;
   }

   public static boolean isActive() {
      return active;
   }

   @EventTarget
   private void onPhaseEvent2(RenderStageEvent renderStageEvent) {
      if (renderStageEvent.phase == Stage.PRE) {
         if (timestamp == -1L) {
            timestamp = Util.getMeasuringTimeMs();
         } else {
            long now = Util.getMeasuringTimeMs();
            timestampValue = now - timestamp;
            timestamp = now;
         }
      } else if (renderStageEvent.phase == Stage.POST) {
         active = OneConfigPlatform.getMousePlatform().isKeyDown(0);
      }
   }

   static {
      OneConfigEventManager.INSTANCE.register(new GuiUtils());
   }
}
