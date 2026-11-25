package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.Stage;

public class StateEvent {
   @SerializedName("state")
   public Stage state;

   public void setState(Stage stage) {
      this.state = stage;
   }

   public StateEvent(Stage stage) {
      this.state = stage;
   }

   public Stage getState() {
      return this.state;
   }
}
