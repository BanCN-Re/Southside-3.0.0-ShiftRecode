package dev.southside.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonConfigMigrator implements IConfigMigrator {
   @SerializedName("object")
   protected JsonObject object;
   @SerializedName("values")
   protected HashMap<String, Object> values = null;

   private Object resolveValue(JsonPrimitive primitive) {
      if (primitive.isJsonNull()) {
         return null;
      } else if (primitive.isBoolean()) {
         return primitive.getAsBoolean();
      } else if (primitive.isNumber()) {
         Number asNumber = primitive.getAsNumber();
         return asNumber.floatValue() % 1.0F != 0.0F ? asNumber.floatValue() : asNumber.intValue();
      } else {
         return primitive.getAsString();
      }
   }

   @Override
   public Object getValue(Field field, @Nullable String text, @Nullable String text2, String text3) {
      if (this.object == null) {
         return null;
      }

      if (this.values == null) {
         this.performAction();
      }

      String text4;
      if (field.isAnnotationPresent(CfgSection.class)) {
         CfgSection annotation = field.getAnnotation(CfgSection.class);
         text4 = annotation.value();
      } else {
         text4 = field.getName();
      }

      return this.values.get(this.formatText(text4));
   }

   protected void performAction() {
      if (this.object != null) {
         this.values = new HashMap<>();

         for (Entry entry : this.object.entrySet()) {
            this.applyText((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsJsonObject());
         }
      }
   }

   protected String formatText(@NotNull String text) {
      if (text.startsWith("/") || text.startsWith(".")) {
         text = text.substring(1);
      }

      return text.replace('/', '.');
   }

   protected void applyText(String text, JsonElement element) {
      if (element.isJsonNull()) {
         this.values.put(text, null);
      } else if (element.isJsonPrimitive()) {
         this.values.put(text, this.resolveValue(element.getAsJsonPrimitive()));
      } else if (element.isJsonArray()) {
         JsonArray asJsonArray = element.getAsJsonArray();
         Object[] objArray = new Object[asJsonArray.size()];

         for (int i = 0; i < asJsonArray.size(); i++) {
            objArray[i] = this.resolveValue(asJsonArray.get(i).getAsJsonPrimitive());
         }

         this.values.put(text, objArray);
      } else {
         this.values.put(text, element);
      }
   }

   public JsonConfigMigrator(String text) {
      File file = new File(text);

      try (FileReader fileReader = new FileReader(file)) {
         this.object = JsonParser.parseReader(fileReader).getAsJsonObject();
      } catch (Exception e) {
         e.printStackTrace();
         this.object = null;
      }
   }

   protected void applyText(String text, JsonObject json) {
      for (Entry entry : json.entrySet()) {
         String text2 = text + "." + (String)entry.getKey();
         if (((JsonElement)entry.getValue()).isJsonObject()) {
            this.applyText(text2, ((JsonElement)entry.getValue()).getAsJsonObject());
         } else {
            this.applyText(text2, (JsonElement)entry.getValue());
         }
      }
   }
}
