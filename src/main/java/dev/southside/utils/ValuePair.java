package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class ValuePair<K, V> {
   @SerializedName("first")
   public K first;
   @SerializedName("second")
   public V second;

   public K getFirst() {
      return this.first;
   }

   public void setFirst(K val) {
      this.first = (K)val;
   }

   public void setSecond(V val) {
      this.second = (V)val;
   }

   public ValuePair(K val, V val2) {
      this.first = (K)val;
      this.second = (V)val2;
   }

   public V getSecond() {
      return this.second;
   }
}
