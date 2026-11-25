package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;

public class KeyCodeEvent {
   @SerializedName("keyCode")
   public int keyCode;
   @SerializedName("scancode")
   public int scancode;
   @SerializedName("action")
   public int action;
   @SerializedName("modifiers")
   public int modifiers;

   public int getKeyCode() {
      return this.keyCode;
   }

   public KeyCodeEvent(int n, int n2, int n3, int n4) {
      this.keyCode = n;
      this.scancode = n2;
      this.action = n3;
      this.modifiers = n4;
   }

   public int getAction() {
      return this.action;
   }

   public int getModifiers() {
      return this.modifiers;
   }

   public int getScancode() {
      return this.scancode;
   }
}
