package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.DirectionEvent;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;

public final class ServerPacketManager {
   public static MinecraftClient mc = MinecraftClient.getInstance();
   public static boolean active = false;
   public static LinkedBlockingQueue<Object> linkedBlockingQueue = new LinkedBlockingQueue<>();
   public static int count;
   public static int countValue = 0;
   private static final long DEFAULT_LONG = -1337L;

   public static void performAction() {
      if (!active) {
         active = true;
      }
   }

   public static boolean shouldHandlePacket(Packet<?> packet) {
      if (mc.currentScreen instanceof ConnectScreen) {
         clear();
         return false;
      }

      synchronized (linkedBlockingQueue) {
         if (active) {
            Southside.INSTANCE.getEventBus().postCancellable(new ServerPacketEvent(packet));
            if (PacketUtils.isImportantPacket(packet)) {
               linkedBlockingQueue.add(packet);
               return true;
            }
         }

         return false;
      }
   }

   public static void performActionEx() {
      if (active) {
         linkedBlockingQueue.add(new KeepAliveC2SPacket(DEFAULT_LONG));
      }
   }

   private ServerPacketManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static void clear() {
      active = false;
      count = 0;
      linkedBlockingQueue.clear();
   }

   public static void performActionAlt() {
      if (active) {
         count++;
      }
   }

   public static void performActionImpl() {
      setActive(true);
   }

   public static void setActive(boolean flag) {
      try {
         if (mc.getNetworkHandler() == null) {
            clear();
            return;
         }

         while (true) {
            Packet packet;
            synchronized (linkedBlockingQueue) {
               Object obj = linkedBlockingQueue.poll();
               if (!(obj instanceof Packet packet2)) {
                  if (!(obj instanceof Runnable action)) {
                     active = false;
                     count = 0;
                     break;
                  }

                  action.run();
                  continue;
               }

               packet = packet2;
            }

            if (!(packet instanceof KeepAliveC2SPacket)) {
               handlePacket(packet, flag);
            }
         }
      } catch (Exception e) {
      }
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   public static void handlePacket(Packet<?> packet) {
      ((Packet)packet).apply(mc.getNetworkHandler());
   }

   public static void setActiveEx(boolean flag) {
      if (mc.getNetworkHandler() == null) {
         clear();
      } else {
         while (true) {
            Packet packet;
            synchronized (linkedBlockingQueue) {
               Object obj = linkedBlockingQueue.poll();
               if (obj instanceof Runnable action) {
                  action.run();
                  continue;
               }

               if (!(obj instanceof Packet packet2)) {
                  count = 0;
                  return;
               }

               packet = packet2;
            }

            if (packet instanceof KeepAliveC2SPacket) {
               count = Math.max(0, count - 1);
               return;
            }

            handlePacket(packet, flag);
         }
      }
   }

   private static void handlePacket(Packet<?> packet, boolean flag) {
      if (flag) {
         DirectionEvent directionEvent = new DirectionEvent(TransferOrigin.RECEIVE, packet, mc.getNetworkHandler().getConnection());
         Southside.INSTANCE.getEventBus().postCancellable(directionEvent);
         if (directionEvent.isCancelled()) {
            return;
         }
      }

      MovementFreezeManager.handlePacket(packet);

      try {
         handlePacket(packet);
      } catch (Exception e) {
         Southside.LOGGER.error("Unexpected error in ServerPacketManager", e);
      }
   }

   public static void setCountValue(int n) {
      countValue = n;
   }
}
