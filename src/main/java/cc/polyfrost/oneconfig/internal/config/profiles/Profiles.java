package cc.polyfrost.oneconfig.internal.config.profiles;

import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import dev.southside.utils.ConfigCore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Profiles {
   private static final Logger LOGGER = LogManager.getLogger("OneConfig Profiles");
   public static final File profileDir = new File("Southside/configs");
   private static ArrayList<String> strings;

   public static String getDisplayText() {
      if (OneConfigConfig.getInstance() == null) {
         OneConfigConfig.getInstance();
      }

      if (!profileDir.exists() && !profileDir.mkdir()) {
         LOGGER.fatal("Could not create profiles folder");
         return null;
      }

      if (strings == null) {
         String[] textArray = profileDir.list((file, text) -> file.isDirectory());
         if (textArray != null) {
            strings = new ArrayList<>(Arrays.asList(textArray));
         }
      }

      if (!getFile(OneConfigConfig.currentProfile.getValue()).exists()) {
         applyText(OneConfigConfig.currentProfile.getValue());
      }

      return OneConfigConfig.currentProfile.getValue();
   }

   public static void applyText(String text) {
      File file = new File(profileDir, text);
      if (!file.exists() && !file.mkdir()) {
         LOGGER.fatal("Could not create profile folder");
      } else {
         strings.add(text);
      }
   }

   public static File getFile() {
      return getFile(getDisplayText());
   }

   public static File getFile(String text) {
      return new File(profileDir, text);
   }

   public static File getFile2(String text) {
      return new File(getFile(), text);
   }

   public static List<String> collectItems() {
      return new ArrayList<>(strings);
   }

   public static boolean contains(String text) {
      return strings.contains(text);
   }

   public static void applyTextEx(String text) {
      ConfigCore.performAction();
      OneConfigConfig.currentProfile.setValue(text);
      OneConfigConfig.getInstance().saveToJson();
      ConfigCore.performActionEx();
   }

   public static void applyText(String text, String text2) {
      try {
         File file = new File(profileDir, text2);
         FileUtils.moveDirectory(getFile(text), file);
         if (OneConfigConfig.currentProfile.getValue().equals(text)) {
            OneConfigConfig.currentProfile.setValue(text2);
         }

         strings.remove(text);
         strings.add(text2);
      } catch (IOException e) {
         LOGGER.error("Failed to rename profile");
      }
   }

   public static void applyTextAlt(String text) {
      if (text.equals(getDisplayText())) {
         if (strings.size() == 1) {
            LOGGER.error("Cannot delete only profile!");
            return;
         }

         applyTextEx(strings.stream().filter(text2 -> !text2.equals(text)).findFirst().get());
      }

      try {
         FileUtils.deleteDirectory(getFile(text));
         strings.remove(text);
      } catch (IOException e) {
         LOGGER.error("Failed to delete profile");
      }
   }
}
