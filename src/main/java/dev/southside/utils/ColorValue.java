package dev.southside.utils;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class ColorValue extends Value<OneColor> {
   @SerializedName("description")
   private final String description;
   @SerializedName("allowAlpha")
   private final boolean allowAlpha;
   @SerializedName("size")
   private final int size;

   public boolean isAllowAlpha() {
      return this.allowAlpha;
   }

   public ColorValue(@NotNull String text, String text2, boolean flag, OneColor color) {
      super(text, color);
      this.description = text2;
      this.allowAlpha = flag;
      this.size = 1;
   }

   public ColorValue(@NotNull String text, String text2, @NotNull OneColor color) {
      this(text, text2, true, color);
   }

   public String getDescription() {
      return this.description;
   }

   public ColorValue(@NotNull String text, @NotNull OneColor color) {
      this(text, "", true, color);
   }

   public ColorValue(@NotNull String text, boolean flag, @NotNull OneColor color) {
      this(text, "", flag, color);
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.COLOR;
   }

   public int getSize() {
      return this.size;
   }
}
