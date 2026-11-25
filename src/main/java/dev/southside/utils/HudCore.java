package dev.southside.utils;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.hud.Hud;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.render.UIMatrixStack;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class HudCore {
   public static final ConcurrentHashMap<HudValue, Hud> hudMap = new ConcurrentHashMap<>();
   public static final ArrayList<BasicOption> basicOptions = new ArrayList<>();
   public static boolean active = false;
   private static String[] names;

   public static void performAction() {
      for (HudValue hudValue : hudMap.keySet()) {
         if (hudValue != null && hudValue.getValue() != null) {
            Hud hud = hudMap.get(hudValue);
            Hud hud2 = (Hud)hudValue.getValue();
            hud2.setConfig(hud.getConfig());

            for (BasicOption option : basicOptions) {
               if (option.getParent() != null && option.getParent().equals(hud)) {
                  option.setParent(hud2);
               }
            }

            hudMap.put(hudValue, hud2);
         }
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      UIMatrixStack matrices = new UIMatrixStack();
      if (!active) {
         for (Hud hud : hudMap.values()) {
            if (hud.isValidEx()) {
               hud.deltaTicks = drawContextEvent.getTickDelta();
               hud.drawAll(drawContextEvent.getDrawContext(), matrices, false);
            }
         }
      }
   }

   static {
      String[] textArray = new String[4];
      setNames(textArray);
   }

   public static void setNames(String[] textArray) {
      names = textArray;
   }
}
