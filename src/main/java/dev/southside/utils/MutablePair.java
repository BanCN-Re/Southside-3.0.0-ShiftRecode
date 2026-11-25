package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class MutablePair<A, B> {
   @SerializedName("first")
   public A first;
   @SerializedName("second")
   public B second;

   public MutablePair(A val, B val2) {
      this.first = (A)val;
      this.second = (B)val2;
   }

   public void setFirst(A val) {
      this.first = (A)val;
   }

   public void setSecond(B val) {
      this.second = (B)val;
   }

   public A getFirst() {
      return this.first;
   }

   public B getSecond() {
      return this.second;
   }
}
