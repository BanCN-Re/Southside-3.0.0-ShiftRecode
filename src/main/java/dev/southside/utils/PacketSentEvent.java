package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.packet.Packet;

public class PacketSentEvent {
   @SerializedName("packet")
   public Packet<?> packet;

   public PacketSentEvent(Packet<?> packet2) {
      this.packet = packet2;
   }

   public Packet<?> getPacket() {
      return this.packet;
   }
}
