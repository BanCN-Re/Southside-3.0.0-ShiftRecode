package dev.southside.utils.network;

import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.thread.ThreadExecutor;
import org.slf4j.Logger;

public class ProtectionUtils {
   public static void handleTeamPacketSafely(Logger logger, TeamS2CPacket teamS2CPacket, ClientPlayPacketListener clientPlayPacketListener) {
      try {
         teamS2CPacket.apply(clientPlayPacketListener);
      } catch (IllegalStateException e) {
         logger.warn("Failed to handle team packet: {}", e.getMessage());
      } catch (NullPointerException e2) {
         logger.warn("Safely suppressed NullPointerException while handling team packet: {}", e2.getMessage());
      } catch (Exception e3) {
         logger.error("Error handling team packet", e3);
      }
   }

   public static <T extends PacketListener> void onEnsureRunningOnSameThread(Logger logger, Packet<T> packet, T val, ThreadExecutor<?> threadExecutor) throws OffThreadException {
      if (!threadExecutor.isOnThread()) {
         threadExecutor.executeSync(() -> handleTeamPacketSafely(val, packet, logger));
         throw OffThreadException.INSTANCE;
      }
   }

   public static <T extends PacketListener> void handleTeamPacketSafely(T val, Packet<T> packet, Logger logger) {
      if (!val.accepts(packet)) {
         logger.debug("Ignoring packet due to disconnection: {}", packet);
      } else {
         try {
            if (packet instanceof TeamS2CPacket teamS2CPacket && val instanceof ClientPlayPacketListener clientPlayPacketListener) {
               handleTeamPacketSafely(logger, teamS2CPacket, clientPlayPacketListener);
               return;
            }

            packet.apply(val);
         } catch (Exception e) {
            if (e instanceof NullPointerException) {
               logger.warn("Safely suppressed NullPointerException while handling packet {}: {}", packet, e.getMessage());
               return;
            }

            if (e instanceof CrashException crashException && crashException.getCause() instanceof OutOfMemoryError) {
               return;
            }

            val.onPacketException(packet, e);
         }
      }
   }
}
