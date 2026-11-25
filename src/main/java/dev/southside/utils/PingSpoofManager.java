package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.module.impl.player.Stealer;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;

public final class PingSpoofManager implements IMinecraft {
   public static final LinkedBlockingQueue<QueuedPacket> linkedBlockingQueue = new LinkedBlockingQueue<>();
   public static boolean active;
   private static final long DEFAULT_LONG = 1000L;

   private PingSpoofManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static void performAction() {
      Stealer stealer = Southside.INSTANCE.getModuleManager().getModule(Stealer.class);
      if ((Boolean)stealer.instant.getValue()) {
         active = true;
      } else if (active) {
         active = false;
         synchronized (linkedBlockingQueue) {
            while (!linkedBlockingQueue.isEmpty()) {
               QueuedPacket queuedPacket = linkedBlockingQueue.poll();
               if (queuedPacket != null) {
                  performAction(queuedPacket);
               }
            }
         }
      }
   }

   public static void performActionEx() {
      performAction();
      if (active) {
         synchronized (linkedBlockingQueue) {
            while (!linkedBlockingQueue.isEmpty()) {
               QueuedPacket queuedPacket = linkedBlockingQueue.peek();
               if (queuedPacket == null || System.currentTimeMillis() - queuedPacket.getQueuedAt() < DEFAULT_LONG) {
                  break;
               }

               linkedBlockingQueue.poll();
               performAction(queuedPacket);
            }
         }
      }
   }

   private static void performAction(QueuedPacket queuedPacket) {
      try {
         queuedPacket.getPacket().apply(mc.getNetworkHandler());
      } catch (Exception e) {
         Southside.LOGGER.error("Unexpected error in PingSpoofManager", e);
      }
   }

   public static boolean shouldHandlePacket(Packet<?> packet) {
      if (active && packet instanceof KeepAliveS2CPacket) {
         linkedBlockingQueue.add(new QueuedPacket(packet));
         return true;
      } else {
         return false;
      }
   }
}
