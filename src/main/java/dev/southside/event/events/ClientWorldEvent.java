package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.world.ClientWorld;

public class ClientWorldEvent {
   @SerializedName("clientWorld")
   public ClientWorld clientWorld;

   public ClientWorld getClientWorld() {
      return this.clientWorld;
   }

   public ClientWorldEvent(ClientWorld world) {
      this.clientWorld = world;
   }
}
