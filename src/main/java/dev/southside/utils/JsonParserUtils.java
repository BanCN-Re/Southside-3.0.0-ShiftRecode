package dev.southside.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.function.Consumer;

public final class JsonParserUtils {
   public static final JsonParser jsonParser = new JsonParser();

   public static boolean isValid(String text, Consumer<JsonElement> consumer) {
      try {
         consumer.accept(jsonParser.parse(text));
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   public static boolean matches(String text) {
      try {
         jsonParser.parse(text);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   public static JsonElement toJson(String text, boolean flag) {
      try {
         return jsonParser.parse(text);
      } catch (Exception e) {
         if (flag) {
            return null;
         } else {
            throw e;
         }
      }
   }

   public static JsonElement toJson(String text) {
      return toJson(text, true);
   }
}
