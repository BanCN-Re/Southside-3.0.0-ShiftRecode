package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.Stage;

public class RenderStageEvent {
   @SerializedName("phase")
   public Stage phase;
   @SerializedName("tickDelta")
   public float tickDelta;

   public RenderStageEvent(Stage stage, float f) {
      this.phase = stage;
      this.tickDelta = f;
   }
}
