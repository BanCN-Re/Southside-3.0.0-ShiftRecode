package dev.southside.utils;

import dev.southside.Southside;
import io.netty.channel.ChannelFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public final class PacketDispatchHook {
   private static volatile long timestamp;
   private static volatile boolean active;
   private static ClientConnection clientConnection;
   private static Vec3d vector;
   private static Rotation rotation;

   private PacketDispatchHook() {
   }

   public static void setActive(boolean flag) {
      active = false;
      performAction();
      active = flag;
   }

   public static boolean isActive() {
      return active;
   }

   public static void performAction(ClientConnection connection, Packet<?> packet, ChannelFuture channelFuture) {
      long l = timestamp;
      if (active) {
         PlayerMoveC2SPacket playerMoveC2SPacket = packet instanceof PlayerMoveC2SPacket playerMoveC2SPacket2 ? playerMoveC2SPacket2 : null;
         Vec3d vec = playerMoveC2SPacket != null && playerMoveC2SPacket.changesPosition() ? new Vec3d(playerMoveC2SPacket.getX(0.0), playerMoveC2SPacket.getY(0.0), playerMoveC2SPacket.getZ(0.0)) : null;
         Rotation rotation2 = playerMoveC2SPacket != null && playerMoveC2SPacket.changesLook() ? new Rotation(playerMoveC2SPacket.getYaw(0.0F), playerMoveC2SPacket.getPitch(0.0F)) : null;
         channelFuture.addListener(future -> MinecraftClient.getInstance().execute(() -> {
            MinecraftClient instance = MinecraftClient.getInstance();
            if (active && l == timestamp && instance.getNetworkHandler() != null && instance.getNetworkHandler().getConnection() == connection) {
               if (clientConnection != connection) {
                  clientConnection = connection;
                  vector = null;
                  rotation = null;
               }

               if (!future.isSuccess()) {
                  performAction();
               } else {
                  if (vec != null) {
                     vector = vec;
                  }

                  if (rotation2 != null) {
                     rotation = rotation2;
                  }
               }

               Southside.INSTANCE.getEventBus().dispatch(new PacketDispatchHook.Entry2(packet, future.isSuccess()));
            }
         }));
      }
   }

   public static void performAction() {
      timestamp++;
      clientConnection = null;
      vector = null;
      rotation = null;
   }

   public static Rotation getRotation() {
      return rotation;
   }

   public static Vec3d getVector() {
      return vector;
   }

   public record Entry(Packet<?> packet) {
      public Packet<?> getPacket() {
         return this.packet;
      }
   }

   public record Entry2(Packet<?> packet, boolean success) {
      public Packet<?> getPacket() {
         return this.packet;
      }

      public boolean isSuccess() {
         return this.success;
      }
   }
}
