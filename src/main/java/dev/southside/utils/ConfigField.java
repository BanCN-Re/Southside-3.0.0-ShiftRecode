package dev.southside.utils;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class ConfigField<V> {
   @SerializedName("label")
   private final String label;
   @SerializedName("clz")
   private final Class<?> clz;
   @SerializedName("value")
   private V value;
   @SerializedName("defaultValue")
   private V defaultValue;

   public ConfigField(String text, V val) {
      this.clz = val.getClass();
      this.label = text;
      this.defaultValue = (V)val;
      this.setValue((V)val);
      this.performAction();
   }

   public void setDefaultValue(V val) {
      this.defaultValue = (V)val;
   }

   public Class<?> getClz() {
      return this.clz;
   }

   public V getValue() {
      return this.value;
   }

   public void setValue(V val) {
      this.value = (V)val;
   }

   public String getLabel() {
      return this.label;
   }

   public V getDefaultValue() {
      return this.defaultValue;
   }

   public void performAction() {
      try {
         Config.performAction(Config.toJson(new JsonObject(), this), this);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
