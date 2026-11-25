package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.packet.Packet;

public class ServerPacketEvent extends CancellableEvent {
   @SerializedName("packet")
   public Packet packet;

   public ServerPacketEvent(Packet packet2) {
      this.packet = packet2;
   }

   public Packet getPacket() {
      return this.packet;
   }
}
