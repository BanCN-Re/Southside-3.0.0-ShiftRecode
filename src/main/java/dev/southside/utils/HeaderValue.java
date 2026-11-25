package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class HeaderValue extends Value<String> {
   @SerializedName("size")
   private final int size;

   public int getSize() {
      return this.size;
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.HEADER;
   }

   public HeaderValue(@NotNull String text, int n, @NotNull String text2) {
      super(text, text2);
      this.size = n;
   }
}
