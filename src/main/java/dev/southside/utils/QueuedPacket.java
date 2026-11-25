package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.packet.Packet;

public class QueuedPacket {
   @SerializedName("packet")
   public final Packet packet;
   @SerializedName("queuedAt")
   public final long queuedAt;

   public Packet getPacket() {
      return this.packet;
   }

   public long getQueuedAt() {
      return this.queuedAt;
   }

   public QueuedPacket(Packet<?> packet2) {
      this.packet = packet2;
      this.queuedAt = System.currentTimeMillis();
   }
}
