package dev.southside.utils;

import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import net.minecraft.client.resource.language.I18n;

public class I18nPlatformImpl implements II18nPlatform {
   @Override
   public String translate(String text, Object... objArray) {
      return I18n.translate(text, objArray);
   }

   @Override
   public String getKeyName(int n) {
      if (n <= -96) {
         int n2 = n + 100;

         return switch (n2) {
            case 0 -> "Mouse Left";
            case 1 -> "Mouse Right";
            case 2 -> "Mouse Middle";
            default -> "Mouse " + (n2 + 1);
         };
      } else {
         String keyName = UKeyboard.getKeyName(n, 28);
         return keyName == null ? "Unknown" : keyName;
      }
   }
}
