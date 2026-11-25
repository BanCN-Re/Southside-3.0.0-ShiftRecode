package dev.southside.utils;

import net.minecraft.client.gui.screen.Screen;

public class IASCompat {
   public static Screen getScreen(Screen screen) {
      try {
         Class clazz = Class.forName("ru.vidtu.ias.screen.AccountScreen");
         return (Screen)clazz.getConstructor(Screen.class).newInstance(screen);
      } catch (Exception e) {
         return null;
      }
   }
}
