package dev.southside.utils;

import java.io.InputStream;

public class FontResourceLoader {
   public static InputStream getInputStream(String text) {
      try {
         String text2 = "/assets/southside/fonts/ui/" + text;
         return FontResourceLoader.class.getResourceAsStream(text2);
      } catch (Exception e) {
         return null;
      }
   }
}
