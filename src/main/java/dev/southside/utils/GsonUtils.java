package dev.southside.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public final class GsonUtils {
   public static final Gson GSON = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy[]{new GsonUtils.ExclusionStrategyImpl()}).create();

   private GsonUtils() {
   }

   public static GsonBuilder getGsonBuilder() {
      return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy[]{new GsonUtils.ExclusionStrategyImpl()}).setPrettyPrinting();
   }

   public static <T> T getT(String text, Class<T> clazz) {
      return (T)GSON.fromJson(text, clazz);
   }

   public static String formatText(String text) {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      return gson.toJson(JsonParser.parseString(text));
   }

   public static String toJson(Object obj) {
      return GSON.toJson(obj);
   }

   public static class ExclusionStrategyImpl implements ExclusionStrategy {
      public boolean shouldSkipField(FieldAttributes fieldAttributes) {
         return fieldAttributes.getAnnotation(GsonExclude.class) != null;
      }

      public boolean shouldSkipClass(Class<?> clazz) {
         return clazz.getAnnotation(GsonExclude.class) != null;
      }
   }
}
