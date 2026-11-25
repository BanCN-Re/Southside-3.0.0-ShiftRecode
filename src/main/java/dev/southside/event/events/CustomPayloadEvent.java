package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.packet.CustomPayload;

public class CustomPayloadEvent {
   @SerializedName("customPayload")
   public CustomPayload customPayload;

   public CustomPayloadEvent(CustomPayload customPayload2) {
      this.customPayload = customPayload2;
   }

   public CustomPayload getCustomPayload() {
      return this.customPayload;
   }
}
