package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;

public class KeyPressEvent {
   @SerializedName("keyCode")
   public int keyCode;

   public KeyPressEvent(int n) {
      this.keyCode = n;
   }

   public int getKeyCode() {
      return this.keyCode;
   }
}
