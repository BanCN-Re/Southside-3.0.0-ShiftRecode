package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.StateEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;

public class BlinkUtils {
   public static final BlinkUtils INSTANCE = new BlinkUtils();
   public static boolean blinking = false;
   public static final List<Class<?>> exemptPackets = new ArrayList<>();
   public static final Map<Class<?>, Predicate<Packet<?>>> cancelPredicates = new HashMap<>();
   public static final Map<Class<?>, Predicate<Packet<?>>> exemptPredicates = new HashMap<>();
   public static final Map<Class<?>, Predicate<Packet<?>>> releaseCancelPredicates = new HashMap<>();
   public static final Map<Class<?>, Consumer<Packet<?>>> queueCallbacks = new HashMap<>();
   public static final Map<Class<?>, Consumer<Packet<?>>> releaseCallbacks = new HashMap<>();
   public static final List<Class<?>> whitelistPackets = new ArrayList<>();
   public static LinkedBlockingQueue<Packet<?>> queuedPackets = new LinkedBlockingQueue<>();
   public static boolean flushing = false;

   public static boolean startBlink(Class<?>... clazzArray) {
      if (blinking) {
         return false;
      }

      Arrays.asList(clazzArray).forEach(BlinkUtils::addExemptPacket);
      blinking = true;
      return true;
   }

   public static void stopBlink() {
      blinking = false;
      flushing = false;
      releaseAll();
      exemptPackets.clear();
      exemptPredicates.clear();
      queueCallbacks.clear();
      releaseCallbacks.clear();
      releaseCancelPredicates.clear();
      whitelistPackets.clear();
      queuedPackets.clear();
   }

   public static void addWhitelistPackets(Class<?>... clazzArray) {
      whitelistPackets.addAll(Arrays.asList(clazzArray));
   }

   public static void addExemptPacket(Class<?> clazz) {
      exemptPackets.add((Class<?>)clazz);
      exemptPredicates.put((Class<?>)clazz, packet -> true);
   }

   public static void removeExemptPacket(Class<?> clazz) {
      exemptPackets.remove(clazz);
   }

   public static void clearExemptPackets() {
      exemptPackets.clear();
   }

   public static void performAction(Class<?> clazz, Predicate<Packet<?>> predicate) {
      cancelPredicates.put(clazz, predicate);
   }

   public static void performActionEx(Class<?> clazz, Predicate<Packet<?>> predicate) {
      exemptPredicates.put(clazz, predicate);
   }

   public static void performActionAlt(Class<?> clazz, Predicate<Packet<?>> predicate) {
      releaseCancelPredicates.put(clazz, predicate);
   }

   public static void performAction(Class<?> clazz, Consumer<Packet<?>> consumer) {
      queueCallbacks.put(clazz, consumer);
   }

   public static void performActionEx(Class<?> clazz, Consumer<Packet<?>> consumer) {
      releaseCallbacks.put(clazz, consumer);
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (IMinecraft.mc.getNetworkHandler() == null) {
         stopBlink();
      }

      if (blinking) {
         queuedPackets.add(new KeepAliveC2SPacket(MathRandomUtils.secureRandom.nextLong()));
      }
   }

   public static boolean handleOutgoingPacket(Packet<?> packet) {
      if (blinking && !flushing) {
         queueCallbacks.forEach((clazz, consumer) -> handlePacket(packet, (Class<?>)clazz, (Consumer<Packet<?>>)consumer));

         for (Class clazz2 : exemptPackets) {
            if (clazz2.isAssignableFrom(packet.getClass())) {
               return true;
            }
         }

         for (Entry entry : exemptPredicates.entrySet()) {
            if (((Class)entry.getKey()).isAssignableFrom(packet.getClass()) && ((Predicate)entry.getValue()).test(packet)) {
               return true;
            }
         }

         if (!whitelistPackets.isEmpty() && !whitelistPackets.contains(packet.getClass())) {
            return true;
         }

         boolean flag = false;

         for (Entry entry2 : cancelPredicates.entrySet()) {
            if (((Class)entry2.getKey()).isAssignableFrom(packet.getClass()) && ((Predicate)entry2.getValue()).test(packet)) {
               flag = true;
            }
         }

         if (!flag) {
            queuedPackets.add(packet);
         }

         return false;
      } else {
         return true;
      }
   }

   private static void handlePacket(Packet<?> packet, Class<?> clazz, Consumer<Packet<?>> consumer) {
      if (clazz.isAssignableFrom(packet.getClass())) {
         consumer.accept(packet);
      }
   }

   public static void releaseAll() {
      releasePackets(queuedPackets.size(), true);
   }

   public static void releaseUntilKeepAlive(boolean flag) {
      releasePacketsUntilKeepAlive(queuedPackets.size(), true, flag);
   }

   public static void releasePackets(int n, boolean flag) {
      releasePacketsUntilKeepAlive(n, flag, false);
   }

   public static void releasePacketsUntilKeepAlive(int n, boolean flag, boolean flag2) {
      int n2 = 0;

      try {
         while (!queuedPackets.isEmpty()) {
            Packet packet = queuedPackets.take();
            if (packet instanceof KeepAliveC2SPacket) {
               if (flag2) {
                  break;
               }
            } else if (!shouldHandlePacket(packet)) {
               releaseCallbacks.forEach((clazz, consumer) -> handlePacket(packet, (Class<?>)clazz, (Consumer<Packet<?>>)consumer));
               n2++;
               sendPacket(packet, flag);
               if (n2 >= n) {
                  break;
               }
            }
         }
      } catch (Exception e) {
         Southside.LOGGER.error("Unexpected error in BlinkUtils", e);
      }
   }

   private static boolean shouldHandlePacket(Packet<?> packet) {
      for (Entry entry : releaseCancelPredicates.entrySet()) {
         if (((Class)entry.getKey()).isAssignableFrom(packet.getClass()) && ((Predicate)entry.getValue()).test(packet)) {
            return true;
         }
      }

      return false;
   }

   public static void dispatchPacket(Packet<?> packet, boolean flag) {
      if (flag) {
         DirectionEvent directionEvent = new DirectionEvent(TransferOrigin.SEND, packet, IMinecraft.mc.getNetworkHandler().getConnection());
         Southside.INSTANCE.getEventBus().postCancellable(directionEvent);
         if (directionEvent.isCancelled()) {
            return;
         }
      }

      sendPacket(packet, true);
   }

   private static void sendPacket(Packet<?> packet, boolean flag) {
      flushing = true;
      if (flag) {
         if (PacketUtils.mc.getNetworkHandler() != null) {
            PacketUtils.mc.getNetworkHandler().getConnection().send(packet, null);
            Southside.INSTANCE.getEventBus().dispatch(new PacketSentEvent(packet));
         }
      } else {
         IMinecraft.mc.getNetworkHandler().sendPacket(packet);
      }

      flushing = false;
   }
}
