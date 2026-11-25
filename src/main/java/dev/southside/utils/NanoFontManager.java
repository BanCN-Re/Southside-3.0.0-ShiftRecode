package dev.southside.utils;

import dev.southside.nanovg.NanoFontRenderer;
import java.util.ArrayList;
import java.util.List;

public class NanoFontManager {
   public static List<NanoFontRenderer> nanoFontRenderers = new ArrayList<>();
   public static NanoFontRenderer nanoFontRenderer;

   public static String[] getStringList() {
      String[] textArray = new String[nanoFontRenderers.size()];

      for (int i = 0; i < nanoFontRenderers.size(); i++) {
         NanoFontRenderer nanoFontRenderer2 = nanoFontRenderers.get(i);
         textArray[i] = nanoFontRenderer2.getFontKey();
      }

      return textArray;
   }
}
