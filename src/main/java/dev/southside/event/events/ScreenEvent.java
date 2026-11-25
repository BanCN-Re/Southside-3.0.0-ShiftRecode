package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;
import net.minecraft.client.gui.screen.Screen;

public class ScreenEvent extends CancellableEvent {
   @SerializedName("screen")
   public Screen screen;

   public ScreenEvent(Screen screen2) {
      this.screen = screen2;
   }
}
