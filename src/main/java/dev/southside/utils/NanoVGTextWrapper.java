package dev.southside.utils;

import java.util.ArrayList;

@Deprecated
public final class NanoVGTextWrapper {
   @Deprecated
   public static ArrayList<String> collectItems(long vg, String text, float x, float y, OneConfigFont font) {
      ArrayList list = new ArrayList();
      text = text + " ";
      int n = 0;

      for (int i = text.indexOf(" "); i >= 0; i = text.indexOf(" ", i + 1)) {
         String text2 = text.substring(0, i);
         float textWidth = NanoVGHelper.INSTANCE.getTextWidth(vg, text2, y, font);
         if (!(textWidth < x)) {
            list.add(text.substring(0, n) + " ");
            list.addAll(collectItems(vg, text.substring(n + 1), x, y, font));
            break;
         }

         n = i;
      }

      if (list.size() == 0) {
         list.add(text);
      }

      String text3 = (String)list.get(list.size() - 1);
      if (text3.length() != 0) {
         list.remove(list.size() - 1);
         list.add(text3.substring(0, text3.length() - 1));
      }

      return list;
   }
}
