package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;

public class ButtonEvent {
   @SerializedName("button")
   public int button;
   @SerializedName("action")
   public int action;
   @SerializedName("modifiers")
   public int modifiers;

   public ButtonEvent(int n, int n2, int n3) {
      this.button = n;
      this.action = n2;
      this.modifiers = n3;
   }

   public int getButton() {
      return this.button;
   }

   public int getAction() {
      return this.action;
   }

   public int getModifiers() {
      return this.modifiers;
   }
}
