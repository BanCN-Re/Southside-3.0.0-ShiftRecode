package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.Stage;

public class PhaseEvent {
   @SerializedName("phase")
   public Stage phase;

   public PhaseEvent(Stage stage) {
      this.phase = stage;
   }
}
