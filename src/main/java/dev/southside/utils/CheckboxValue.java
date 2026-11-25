package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class CheckboxValue extends Value<Boolean> {
   @SerializedName("description")
   private final String description;
   @SerializedName("size")
   private final int size;

   public CheckboxValue(@NotNull String text, @NotNull Boolean val, String text2, int n) {
      super(text, val);
      this.description = text2;
      this.size = n;
   }

   public int getSize() {
      return this.size;
   }

   public String getDescription() {
      return this.description;
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.CHECKBOX;
   }
}
