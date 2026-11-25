package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class StringValue extends Value<String> {
   @SerializedName("placeholder")
   private final String placeholder;
   @SerializedName("secure")
   private final boolean secure;
   @SerializedName("multiline")
   private final boolean multiline;
   @SerializedName("description")
   private final String description;
   @SerializedName("size")
   private final int size;

   public int getSize() {
      return this.size;
   }

   public boolean isSecure() {
      return this.secure;
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.TEXT;
   }

   public StringValue(@NotNull String text, String text2, boolean flag, boolean flag2, String text3, int n, @NotNull String text4) {
      super(text, text4);
      this.placeholder = text2;
      this.secure = flag;
      this.multiline = flag2;
      this.description = text3;
      this.size = n;
   }

   public String getDescription() {
      return this.description;
   }

   public String getPlaceholder() {
      return this.placeholder;
   }

   public boolean isMultiline() {
      return this.multiline;
   }
}
