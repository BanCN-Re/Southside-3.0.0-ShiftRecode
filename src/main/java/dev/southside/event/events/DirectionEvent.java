package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.TransferOrigin;
import dev.southside.utils.CancellableEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;

public class DirectionEvent extends CancellableEvent {
   @SerializedName("direction")
   public TransferOrigin direction;
   @SerializedName("packet")
   public Packet packet;
   @SerializedName("clientConnection")
   public ClientConnection clientConnection;

   public DirectionEvent(TransferOrigin transferOrigin, Packet<?> packet2, ClientConnection connection) {
      this.direction = transferOrigin;
      this.packet = packet2;
      this.clientConnection = connection;
   }

   public void setPacket(Packet<?> packet2) {
      this.packet = packet2;
   }

   public TransferOrigin getDirection() {
      return this.direction;
   }

   public boolean isValid() {
      return this.direction == TransferOrigin.SEND;
   }

   public Packet<?> getPacket() {
      return this.packet;
   }

   public ClientConnection getClientConnection() {
      return this.clientConnection;
   }
}
