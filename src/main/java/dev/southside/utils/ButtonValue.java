package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class ButtonValue extends Value<Runnable> {
   @SerializedName("text")
   private final String text;
   @SerializedName("description")
   private final String description;
   @SerializedName("size")
   private final int size;

   public String getDescription() {
      return this.description;
   }

   public String getTextEx() {
      return this.text;
   }

   public int getSize() {
      return this.size;
   }

   public ButtonValue(@NotNull String text2, String text3, String text4, int n, @NotNull Runnable action) {
      super(text2, action);
      this.text = text3;
      this.description = text4;
      this.size = n;
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.BUTTON;
   }
}
