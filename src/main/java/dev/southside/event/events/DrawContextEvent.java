package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.gui.DrawContext;

public class DrawContextEvent {
   @SerializedName("drawContext")
   public DrawContext drawContext;
   @SerializedName("tickDelta")
   public float tickDelta;

   public DrawContextEvent(DrawContext context, float f) {
      this.drawContext = context;
      this.tickDelta = f;
   }

   public DrawContext getDrawContext() {
      return this.drawContext;
   }

   public float getTickDelta() {
      return this.tickDelta;
   }
}
