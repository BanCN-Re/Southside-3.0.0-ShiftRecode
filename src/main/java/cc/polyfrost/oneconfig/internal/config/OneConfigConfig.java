package cc.polyfrost.oneconfig.internal.config;

import cc.polyfrost.oneconfig.config.core.OneColor;
import dev.southside.utils.InternalConfig;
import dev.southside.utils.ConfigField;
import java.util.ArrayList;
import java.util.List;

public class OneConfigConfig extends InternalConfig {
   public static ConfigField<String> currentProfile;
   public static boolean activeValue;
   public static int count;
   public static List<String> favoriteMods;
   public static List<OneColor> favoriteColors;
   public static List<OneColor> recentColors;
   public static boolean activeData;
   private static OneConfigConfig INSTANCE;

   public OneConfigConfig() {
      super("", "Southside_OneConfig.json");
      this.initialize();
      INSTANCE = this;
   }

   public static OneConfigConfig getInstance() {
      return INSTANCE == null ? (INSTANCE = new OneConfigConfig()) : INSTANCE;
   }

   // $VF: Irreducible bytecode was duplicated to produce valid code
   static {
      currentProfile = new ConfigField<>("Current Profile", "default");
      activeValue = false;
      count = 0;
      favoriteMods = new ArrayList<>();
      favoriteColors = new ArrayList<>(6);
      recentColors = new ArrayList<>(6);
      activeData = false;
   }

}
