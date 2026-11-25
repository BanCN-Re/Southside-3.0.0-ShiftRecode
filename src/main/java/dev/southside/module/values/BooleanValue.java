package dev.southside.module.values;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;

public class BooleanValue extends Value<Boolean> {
   @SerializedName("description")
   private final String description;
   @SerializedName("size")
   private final int size;

   public BooleanValue(String text, boolean flag) {
      this(text, "", 1, flag);
   }

   public BooleanValue(String text, String text2, boolean flag) {
      this(text, text2, 1, flag);
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.SWITCH;
   }

   public BooleanValue(String text, String text2, int n, boolean flag) {
      super(text, flag);
      this.description = text2 != null ? text2 : "";
      this.size = n;
   }

   public String getDescription() {
      return this.description;
   }

   public int getSize() {
      return this.size;
   }
}
