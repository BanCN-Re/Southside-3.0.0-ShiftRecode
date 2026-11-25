package dev.southside.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

public class IsoDateDeserializer implements JsonDeserializer<Date> {
   public Date deserialize(JsonElement element, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
      return this.getDate(element);
   }

   public Date getDate(JsonElement element) throws JsonParseException {
      try {
         return Date.from(Instant.parse(element.getAsString()));
      } catch (Exception e) {
         return null;
      }
   }
}
