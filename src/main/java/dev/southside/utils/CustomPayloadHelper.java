package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.StateEvent;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

public class CustomPayloadHelper implements IMinecraft {
   public static final List<Runnable> runnables = new ArrayList<>();
   public static final List<Identifier> identifiers = new ArrayList<>();
   private static final long DEFAULT_LONG = -6741983449280579276L;

   public static void performAction(Identifier id) {
      if (!identifiers.contains(id)) {
         identifiers.add(id);
      }
   }

   public static void sendPacket(Identifier id, PacketByteBuf buf) {
      if (mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new CustomPayloadHelper.Class8710Impl(id, buf)));
      }
   }

   public static void writeBytes(PacketByteBuf buf, PacketByteBuf buf2) {
      buf.writeBytes(buf2.copy());
   }

   public static void performAction(PacketByteBuf buf) {
      int n = buf.readableBytes();
      if (n < 0 || n > (int)DEFAULT_LONG) {
         throw new IllegalArgumentException("Payload may not be larger than 1145140 bytes");
      }
   }

   public static void runTask(Runnable action) {
      runnables.add(action);
   }

   @EventTarget(priority = 11451)
   public static void onState(StateEvent stateEvent) {
      if (stateEvent.state == Stage.PRE && isValid() && mc.getNetworkHandler() != null) {
         for (Runnable action : runnables) {
            action.run();
         }

         runnables.clear();
      }
   }

   public static PacketByteBuf getPacketByteBuf(PacketByteBuf buf) {
      performAction(buf);
      PacketByteBuf buf2 = new PacketByteBuf(Unpooled.buffer());
      buf2.writeBytes(buf.copy());
      buf.skipBytes(buf.readableBytes());
      return buf2;
   }

   public static boolean isValid() {
      return mc != null && mc.player != null && mc.world != null;
   }

   public static class Class8710Impl implements CustomPayload {
      @SerializedName("channel")
      public Identifier channel;
      @SerializedName("data")
      public PacketByteBuf data;

      public static CustomPayloadHelper.Class8710Impl getClass8710Impl2(Identifier id, PacketByteBuf buf) {
         return new CustomPayloadHelper.Class8710Impl(id, CustomPayloadHelper.getPacketByteBuf(buf));
      }

      public Id<? extends CustomPayload> getId() {
         return new Id(this.channel);
      }

      public static PacketCodec<PacketByteBuf, CustomPayloadHelper.Class8710Impl> getPacketCodec(Identifier id) {
         return CustomPayload.codecOf(CustomPayloadHelper.Class8710Impl::performAction, buf -> getClass8710Impl2(id, buf));
      }

      public Class8710Impl(Identifier id, PacketByteBuf buf) {
         this.channel = id;
         this.data = buf;
      }

      public void performAction(PacketByteBuf buf) {
         CustomPayloadHelper.writeBytes(buf, this.data);
      }
   }
}
