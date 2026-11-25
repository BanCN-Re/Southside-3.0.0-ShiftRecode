package dev.southside.utils;

import com.google.common.reflect.TypeToken;
import dev.southside.Southside;
import java.util.HashMap;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreResetS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.util.Hand;

public class PacketUtils implements IMinecraft {
   public static int noPositionMoveTicks = 0;
   public static final HashMap<Class<?>, NetworkSide> packetSideCache = new HashMap<>();

   public static void interactItemWithRotation(Rotation rotation) {
      if (mc.interactionManager != null) {
         Rotation currentRotation = RotationManager.currentRotation;
         Rotation rotation2 = new Rotation(currentRotation.yaw, currentRotation.pitch, currentRotation.targetPosition, currentRotation.sourcePosition);
         RotationManager.currentRotation = rotation;
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         RotationManager.currentRotation = rotation2;
      }
   }

   public static void sendOrApplyDirect(Packet packet) {
      try {
         if (mc.getNetworkHandler() == null) {
            return;
         }

         NetworkSide networkSide = getNetworkSide(packet.getClass());
         switch (networkSide) {
            case CLIENTBOUND:
               packet.apply(mc.getNetworkHandler());
               break;
            case SERVERBOUND:
               if (mc.getNetworkHandler() != null) {
                  mc.getNetworkHandler().getConnection().send(packet, null);
                  Object obj = Southside.INSTANCE.getEventBus().dispatch(new PacketSentEvent(packet));
               }
         }
      } catch (OffThreadException e) {
      } catch (Throwable e2) {
         Southside.LOGGER.error("Unexpected error in PacketUtils", e2);
      }
   }

   public static void sendSequencedPacket(SequencedPacketCreator sequencedPacketCreator) {
      mc.interactionManager.sendSequencedPacket(mc.world, sequencedPacketCreator);
   }

   public static void sendOrApplyPacket(Packet packet) {
      try {
         if (mc.getNetworkHandler() == null) {
            return;
         }

         NetworkSide networkSide = getNetworkSide(packet.getClass());
         switch (networkSide) {
            case CLIENTBOUND:
               packet.apply(mc.getNetworkHandler());
               break;
            case SERVERBOUND:
               sendPacket(packet);
         }
      } catch (OffThreadException e) {
      } catch (Throwable e2) {
         Southside.LOGGER.error("Unexpected error in PacketUtils", e2);
      }
   }

   public static void sendPacketDirect(Packet<?> packet) {
      if (mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().getConnection().send(packet, null);
         Southside.INSTANCE.getEventBus().dispatch(new PacketSentEvent(packet));
      }
   }

   public static NetworkSide getNetworkSide(Class<?> clazz) {
      if (packetSideCache.containsKey(clazz)) {
         return packetSideCache.get(clazz);
      }

      NetworkSide clientbound = NetworkSide.CLIENTBOUND;

      try {
         TypeToken typeToken = TypeToken.of(clazz).resolveType(Packet.class.getTypeParameters()[0]);
         if (typeToken.isSubtypeOf(ServerPacketListener.class)) {
            clientbound = NetworkSide.SERVERBOUND;
         }
      } catch (Throwable e) {
      }

      packetSideCache.put(clazz, clientbound);
      return clientbound;
   }

   public static boolean isImportantPacket(Packet packet) {
      return !(packet instanceof ChatMessageS2CPacket)
         && !(packet instanceof ScoreboardDisplayS2CPacket)
         && !(packet instanceof EntityEquipmentUpdateS2CPacket)
         && !(packet instanceof ClearTitleS2CPacket)
         && !(packet instanceof TitleS2CPacket)
         && !(packet instanceof BossBarS2CPacket)
         && !(packet instanceof EntitiesDestroyS2CPacket)
         && !(packet instanceof HealthUpdateS2CPacket)
         && !(packet instanceof ScoreboardObjectiveUpdateS2CPacket)
         && !(packet instanceof ScoreboardScoreResetS2CPacket)
         && !(packet instanceof ScoreboardScoreUpdateS2CPacket)
         && !(packet instanceof SubtitleS2CPacket)
         && !(packet instanceof TitleFadeS2CPacket)
         && !(packet instanceof PlaySoundS2CPacket)
         && !(packet instanceof PlaySoundFromEntityS2CPacket)
         && !(packet instanceof EntitySetHeadYawS2CPacket)
         && !(packet instanceof EntityS2CPacket)
         && !(packet instanceof EntityPositionS2CPacket)
         && !(packet instanceof EntityPositionSyncS2CPacket)
         && !(packet instanceof KeepAliveS2CPacket)
         && !(packet instanceof EntityDamageS2CPacket);
   }

   public static void trackMovementPacket(Packet<?> packet) {
      if (packet instanceof PlayerMoveC2SPacket playerMoveC2SPacket) {
         if (playerMoveC2SPacket.changesPosition()) {
            noPositionMoveTicks = 0;
         } else {
            noPositionMoveTicks++;
         }
      }
   }

   public static void sendPacket(Packet packet) {
      if (mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendPacket(packet);
      }
   }
}
