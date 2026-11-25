package dev.southside.utils;

import java.util.ArrayList;
import java.util.List;

public class OneConfigFonts {
   public static final List<OneConfigFont> oneConfigFonts;
   public static final OneConfigFont BOLD;
   public static final OneConfigFont SEMIBOLD;
   public static final OneConfigFont REGULAR;
   public static final OneConfigFont ONE_CONFIG_FONT;
   public static final OneConfigFont MINECRAFT_REGULAR;
   public static final OneConfigFont MINECRAFT_BOLD;
   public static final OneConfigFont WQY_MICROHEI;
   public static final OneConfigFont JELLOLIGHT;
   public static final OneConfigFont JELLOMEDIUM;
   public static final OneConfigFont JELLOREGULAR;
   private static String label;

   public static OneConfigFont getOneConfigFont(String text, String text2) {
      OneConfigFont font = new OneConfigFont(text, text2);
      oneConfigFonts.add(font);
      return font;
   }

   // $VF: Irreducible bytecode was duplicated to produce valid code
   static {
      oneConfigFonts = new ArrayList<>();
      BOLD = getOneConfigFont("inter-bold", "/assets/oneconfig/font/bold.otf");
      SEMIBOLD = getOneConfigFont("inter-semibold", "/assets/oneconfig/font/semibold.ttf");
      REGULAR = getOneConfigFont("inter-regular", "/assets/oneconfig/font/regular.ttf");
      ONE_CONFIG_FONT = REGULAR;
      MINECRAFT_REGULAR = getOneConfigFont("mc-regular", "/assets/oneconfig/font/minecraft-regular.otf");
      MINECRAFT_BOLD = getOneConfigFont("mc-bold", "/assets/oneconfig/font/minecraft-bold.otf");
      WQY_MICROHEI = getOneConfigFont("wqy", "/assets/southside/fonts/ui/wqy_microhei.ttf");
      JELLOLIGHT = getOneConfigFont("jello-light", "/assets/southside/fonts/ui/jellolight.ttf");
      JELLOMEDIUM = getOneConfigFont("jello-medium", "/assets/southside/fonts/ui/jellomedium.ttf");
      JELLOREGULAR = getOneConfigFont("jello-regular", "/assets/southside/fonts/ui/jelloregular.ttf");
   }

   public static void setLabel(String text) {
      label = text;
   }

}
