package dev.southside.utils;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.JsonObject;
import dev.southside.module.Category;
import java.util.HashMap;
import org.jetbrains.annotations.Nullable;

public class ConfigLoader {
   public static final HashMap<ModData, Runnable> runnableMap = new HashMap<>();
   private static boolean active;

   static {
      setActive(true);
   }

   public static void setActive(boolean flag) {
      active = flag;
   }

   public static class Inner extends ModData {
      public Inner(String text, Category category, @Nullable String text2) {
         super(text, category, text2);
         super.config = new ConfigLoader.Inner2(this);
      }

      public Inner(String text, Category category) {
         this(text, category, null);
      }
   }

   public static class Inner2 extends Config {
      @Override
      public boolean isValid() {
         return false;
      }

      @Override
      public void openGui() {
         ConfigLoader.runnableMap.get(this.modData).run();
      }

      @Override
      public void initialize() {
      }

      @Override
      public JsonObject saveToJson() {
         return new JsonObject();
      }

      public Inner2(ModData modData) {
         super(modData, true, false);
      }

      @Override
      public void loadFromJson(JsonObject json) {
      }
   }
}
