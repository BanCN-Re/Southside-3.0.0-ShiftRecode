package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;

public class MessageEvent extends CancellableEvent {
   @SerializedName("message")
   public String message;

   public MessageEvent(String text) {
      this.message = text;
   }

   public void setMessage(String text) {
      this.message = text;
   }

   public String getMessage() {
      return this.message;
   }
}
