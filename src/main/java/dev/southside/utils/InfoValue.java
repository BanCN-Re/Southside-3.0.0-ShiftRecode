package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class InfoValue extends Value<String> {
   @SerializedName("type")
   private final InfoType type;
   @SerializedName("size")
   private final int size;

   public InfoValue(@NotNull String text, InfoType infoType, int n, @NotNull String text2) {
      super(text, text2);
      this.type = infoType;
      this.size = n;
   }

   public int getSize() {
      return this.size;
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.INFO;
   }

   public InfoType getType() {
      return this.type;
   }
}
