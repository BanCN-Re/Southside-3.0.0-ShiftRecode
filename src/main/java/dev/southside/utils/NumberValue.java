package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import org.jetbrains.annotations.NotNull;

public class NumberValue extends Value<Number> {
   @SerializedName("min")
   private final float min;
   @SerializedName("max")
   private final float max;
   @SerializedName("step")
   private final int step;
   @SerializedName("description")
   private final String description;
   @SerializedName("instant")
   private final boolean instant;
   @SerializedName("increment")
   private final float increment;

   public NumberValue(String text, Number number, Number number2, Number number3, Number number4) {
      this(text, number2.floatValue(), number3.floatValue(), "", false, number4.floatValue(), number);
   }

   public String getDescription() {
      return this.description;
   }

   public double getCurrentValue() {
      return ((Number)this.getValue()).doubleValue();
   }

   public double getCurrentValueEx() {
      return ((Number)this.getValue()).doubleValue();
   }

   public boolean isInstant() {
      return this.instant;
   }

   public float getIncrement() {
      return this.increment;
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.SLIDER;
   }

   public float getMax() {
      return this.max;
   }

   public float getMin() {
      return this.min;
   }

   public int getStep() {
      return this.step;
   }

   public NumberValue(String text, float f, float f2, String text2, boolean flag, Number number) {
      this(text, f, f2, text2, flag, 0.1F, number);
   }

   public NumberValue(@NotNull String text, float f, float f2, String text2, boolean flag, float f3, Number number) {
      super(text, number);
      this.min = f;
      this.max = f2;
      this.step = 0;
      this.description = text2;
      this.instant = flag;
      this.increment = f3;
   }

   public NumberValue(String text, int n, int n2, int n3, int n4) {
      this(text, n, n2, n3, (double)n4);
   }

   public NumberValue(String text, Number number, Number number2, Number number3) {
      this(text, number2.floatValue(), number3.floatValue(), "", false, 0.1F, number);
   }
}
