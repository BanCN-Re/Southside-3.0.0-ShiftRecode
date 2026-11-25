package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class PageValue extends Value<Object> {
   @SerializedName("location")
   private final PageLocation location;
   @SerializedName("description")
   private final String description;

   public String getDescription() {
      return this.description;
   }

   public PageLocation getLocation() {
      return this.location;
   }

   @Override
   public OptionType getOptionType() {
      return null;
   }

   public PageValue(@NotNull String text, PageLocation pageLocation, String text2, @NotNull Object obj) {
      super(text, obj);
      this.location = pageLocation;
      this.description = text2;
   }
}
