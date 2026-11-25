package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class IntHolder {
   @SerializedName("intValue")
   public int intValue;

   public IntHolder(int n) {
      this.intValue = n;
   }

   public int getIntValue() {
      return this.intValue;
   }
}
