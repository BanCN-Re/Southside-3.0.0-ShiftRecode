package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class KeyBindValue extends Value<OneKeyBind> {
   @SerializedName("description")
   private final String description;
   @SerializedName("size")
   private final int size;

   @Override
   public OptionType getOptionType() {
      return OptionType.KEYBIND;
   }

   public String getDescription() {
      return this.description;
   }

   public KeyBindValue(@NotNull String text, String text2, int n, @NotNull OneKeyBind oneKeyBind) {
      super(text, oneKeyBind);
      this.description = text2;
      this.size = n;
   }

   public int getSize() {
      return this.size;
   }
}
