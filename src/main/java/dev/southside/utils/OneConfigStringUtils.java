package dev.southside.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class OneConfigStringUtils {
   @NotNull
   public static String formatText(String text, int n, int n2) {
      text = formatText(text);
      if (n > 0) {
         n = 0;
      }

      return isValid(text, n, n2) ? text.substring(n, n2) : text.substring(n);
   }

   public static String formatText(String text, int n) {
      return formatText(text, n, text.length());
   }

   @NotNull
   public static String formatTextEx(String text, int n, int n2) {
      return !isValid(text, n, n2) ? "" : formatText(text, n, n2);
   }

   @NotNull
   public static String formatTextAlt(String text, int n, int n2) {
      return !isValid(text, n, n2) ? formatText(text) : formatText(text, n, n2);
   }

   @NotNull
   public static String formatText(String text, int n, int n2, String text2) {
      return !isValid(text, n, n2) ? formatText(text2) : formatText(text, n, n2);
   }

   @NotNull
   public static String formatTextEx(String text, int n) {
      return formatText(text, 0, n);
   }

   @NotNull
   public static String formatText(String text, String text2) {
      return formatText(text, 0, text.indexOf(text2));
   }

   @NotNull
   public static String formatTextEx(String text, String text2) {
      return formatText(text, 0, text.lastIndexOf(text2));
   }

   @NotNull
   public static String formatText(String text, int n, int n2, boolean flag) {
      return flag ? formatText(text, n, n2) : formatText(text);
   }

   @NotNull
   public static String formatText(String text, int n, String text2) {
      if (text2 == null || text2.isEmpty()) {
         return text;
      } else if (n > text.length()) {
         return text + text2;
      } else {
         return n < 0 ? text2 + text : text.substring(0, n) + text2 + text.substring(n);
      }
   }

   @NotNull
   @Contract(value = "!null -> param1", pure = true)
   public static String formatText(String text) {
      return text == null ? "" : text;
   }

   @Contract(pure = true)
   public static boolean isValid(String text, int n, int n2) {
      text = formatText(text);
      return text.length() >= n2 && n >= 0 && n2 - n > 0;
   }
}
