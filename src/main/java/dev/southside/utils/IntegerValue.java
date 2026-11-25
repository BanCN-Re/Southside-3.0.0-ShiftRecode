package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class IntegerValue extends Value<Number> {
   @SerializedName("min")
   private final float min;
   @SerializedName("max")
   private final float max;
   @SerializedName("size")
   private final int size;
   @SerializedName("step")
   private final int step;
   @SerializedName("description")
   private final String description;
   private static final long d;

   public float getMax() {
      return this.max;
   }

   public int getSize() {
      return this.size;
   }

   public float getMin() {
      return this.min;
   }

   public int getStep() {
      return this.step;
   }

   public String getDescription() {
      return this.description;
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.NUMBER;
   }

   public IntegerValue(@NotNull String text, float f, float f2, String text2, Number number) {
      super(text, number);
      this.min = f;
      this.max = f2;
      this.step = 0;
      this.size = (int)d;
      this.description = text2;
   }

   static {
      long l = 1112618995526835493L;
      long l2 = 6630115265948065063L ^ l;
      byte b = -1;
      d = l2;
   }
}
