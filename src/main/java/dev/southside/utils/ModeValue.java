package dev.southside.utils;

import cc.polyfrost.oneconfig.config.data.OptionType;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import java.util.Arrays;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class ModeValue extends Value<Integer> {
   @SerializedName("options")
   public final String[] options;
   @SerializedName("description")
   private final String description;
   @SerializedName("size")
   private final int size;

   public ModeValue(@NotNull String text, String[] textArray, String text2, int n, @NotNull Integer integer) {
      super(text, computeInt(textArray, integer));
      this.options = textArray;
      this.description = text2;
      this.size = n;
   }

   public String getDescription() {
      return this.description;
   }

   public String[] getOptions() {
      return this.options;
   }

   public ModeValue(String text, String text2, String... textArray) {
      this(text, textArray, "", 1, Arrays.asList(textArray).indexOf(text2));
   }

   public String getDisplayTextEx() {
      return this.options[this.getValue()];
   }

   public String getDisplayTextAlt() {
      return this.getDisplayTextEx();
   }

   public boolean matches(String text) {
      return this.getDisplayTextAlt().equalsIgnoreCase(text);
   }

   public Integer getValue() {
      if (this.options != null) {
         super.value = computeInt(this.options, (Integer)super.value);
      }

      return (Integer)super.value;
   }

   public void performAction(Integer integer) {
      if (this.options != null) {
         super.performAction(computeInt(this.options, integer));
      }
   }

   private static int computeInt(String[] textArray, Integer integer) {
      if (textArray != null && textArray.length != 0) {
         return MathHelper.clamp(integer == null ? 0 : integer, 0, textArray.length - 1);
      } else {
         throw new IllegalArgumentException("A mode setting must have at least one option");
      }
   }

   @Override
   public OptionType getOptionType() {
      return OptionType.DROPDOWN;
   }

   public int getSize() {
      return this.size;
   }
}
