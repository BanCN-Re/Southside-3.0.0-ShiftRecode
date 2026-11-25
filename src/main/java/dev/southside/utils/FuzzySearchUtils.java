package dev.southside.utils;

import cc.polyfrost.oneconfig.internal.config.Preferences;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

public class FuzzySearchUtils {
   public static boolean isValid(String text, String text2) {
      return isValid(text, text2, ((Number)Preferences.INSTANCE.searchDistance.getValue()).intValue());
   }

   public static boolean isValid(String text, String text2, int n) {
      text = text.toLowerCase(Locale.ENGLISH);
      text2 = text2.toLowerCase(Locale.ENGLISH);
      if (text.length() <= n) {
         return text.contains(text2);
      }

      boolean flag = false;

      for (String text3 : StringUtils.split(text)) {
         flag = text3.contains(text2) || StringUtils.getLevenshteinDistance(text3, text2) <= n;
         if (flag) {
            break;
         }
      }

      return flag || text.contains(text2) || StringUtils.getLevenshteinDistance(text, text2) <= n;
   }
}
