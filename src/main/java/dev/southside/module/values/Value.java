package dev.southside.module.values;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.IOption;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class Value<V> implements IOption {
   @SerializedName("label")
   private final String label;
   @SerializedName("defaultValue")
   private final V defaultValue;
   @SerializedName("value")
   public V value;
   public transient BasicOption basicOption = null;

   public Value(@NotNull String text, @NotNull V val) {
      this.label = Objects.requireNonNull(text);
      this.defaultValue = (V)val;
      this.value = (V)val;
      this.performAction();
   }

   public String getLabel() {
      return this.label;
   }

   public V getDefaultValue() {
      return this.defaultValue;
   }

   public void performAction(V val) {
      this.value = (V)val;
      if (this.basicOption != null) {
         this.basicOption.performAction();
      }
   }

   public V getValue() {
      return this.value;
   }

   public void performAction() {
      try {
         this.value = this.defaultValue;
         Config.performAction(Config.toJson(new JsonObject(), this), this);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
