package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class DualBooleanValue extends Value<Boolean> {
   @SerializedName("left")
   private final String left;
   @SerializedName("right")
   private final String right;
   @SerializedName("description")
   private final String description;
   @SerializedName("size")
   private final int size;

   public String getRight() {
      return this.right;
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.DUAL_OPTION;
   }

   public String getDescription() {
      return this.description;
   }

   public String getLeft() {
      return this.left;
   }

   public int getSize() {
      return this.size;
   }

   public DualBooleanValue(@NotNull String text, String text2, String text3, String text4, int n, @NotNull Boolean val) {
      super(text, val);
      this.left = text2;
      this.right = text3;
      this.description = text4;
      this.size = n;
   }
}
