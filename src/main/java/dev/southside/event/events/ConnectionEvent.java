package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.TransferOrigin;
import dev.southside.utils.CancellableEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;

public class ConnectionEvent extends CancellableEvent {
   @SerializedName("connection")
   public ClientConnection connection;
   @SerializedName("packetListener")
   public PacketListener packetListener;
   @SerializedName("packet")
   public Packet<?> packet;
   @SerializedName("direction")
   public TransferOrigin direction;

   public ConnectionEvent(ClientConnection connection2, PacketListener packetListener2, Packet<?> packet2, TransferOrigin transferOrigin) {
      this.connection = connection2;
      this.packetListener = packetListener2;
      this.packet = packet2;
      this.direction = transferOrigin;
   }
}
