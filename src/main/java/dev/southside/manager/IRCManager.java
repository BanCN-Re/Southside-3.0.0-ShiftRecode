package dev.southside.manager;

import dev.southside.auth.IrcClient;
import dev.southside.auth.IrcMessage;
import dev.southside.auth.IrcState;
import dev.southside.auth.IrcUser;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.ConnectionEvent;
import dev.southside.event.events.WorldJoinEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.EntityAddEvent;
import dev.southside.event.events.MessageEvent;
import dev.southside.event.events.DisconnectEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.module.impl.render.NameProtect;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.IText;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class IRCManager implements IMinecraft {
   public static final IRCManager INSTANCE = new IRCManager();
   public static final Map<IrcUser, String> stringMap = new ConcurrentHashMap<>();

   private IRCManager() {
   }

   public static boolean contains(String text, IrcUser ircUser) {
      return ircUser.getUuids().contains(text);
   }

   public static IrcUser getIrcUser(String text) {
      return IrcState.ircUsersSet.stream().filter(ircUser -> contains(text, ircUser)).findFirst().orElse(null);
   }

   public static IrcUser getIrcUser() {
      return mc.player == null ? null : getIrcUser(mc.player.getUuid().toString());
   }

   @EventTarget
   public void onConnection(ConnectionEvent connectionEvent) {
      if (connectionEvent.packet instanceof PlayerListS2CPacket playerListS2CPacket) {
         for (Entry entry : playerListS2CPacket.getEntries()) {
            if (entry != null && entry.profile() != null && entry.profile().getId() != null) {
               IrcUser ircUser = getIrcUser(entry.profile().getId().toString());
               if (ircUser != null && NameProtect.isValidEx()) {
                  stringMap.put(ircUser, entry.profile().getName());
               }
            }
         }
      }
   }

   @EventTarget
   public void onEntityEvent3(EntityAddEvent entityAddEvent) {
      if (NameProtect.isValidEx() && entityAddEvent.entity instanceof PlayerEntity player) {
         IrcUser ircUser = getIrcUser(player.getUuid().toString());
         if (ircUser != null) {
            stringMap.put(ircUser, player.getGameProfile().getName());
         }
      }
   }

   public boolean matches(String text) {
      if (text != null && !text.isBlank()) {
         if (text.codePointCount(0, text.length()) > 255) {
            text = text.substring(0, text.offsetByCodePoints(0, 255));
         }

         IrcClient instance = IrcClient.getINSTANCE();
         if (instance.isValid() && instance.matches(text)) {
            return true;
         }

         IText.performActionEx(Text.literal("IRC 尚未连接，消息未发送。"));
         return false;
      } else {
         return false;
      }
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      stringMap.clear();
   }

   @EventTarget
   public void onCustomEvent4(WorldJoinEvent worldJoinEvent) {
      IrcState.performAction();
   }

   @EventTarget
   public void onMinecraftClientEvent3(DisconnectEvent disconnectEvent) {
      IrcClient instance = IrcClient.getINSTANCE();
      if (instance.isValid()) {
         instance.isValidEx();
      }

      stringMap.clear();
   }

   @EventTarget
   public void onMessage(MessageEvent messageEvent) {
      if ((Boolean)ClientSettings.enableIrcChat.getValue() && (Boolean)ClientSettings.ircFastChat.getValue()) {
         String message = messageEvent.message;
         String text = (String)ClientSettings.ircChatPrefix.getValue();
         if (!text.isEmpty() && message.startsWith(text)) {
            this.matches(message.substring(text.length()));
            messageEvent.cancel();
         }
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState().equals(Stage.PRE)) {
         IrcMessage ircMessage;
         while ((ircMessage = IrcState.queue.poll()) != null) {
            if ((Boolean)ClientSettings.enableIrcChat.getValue()) {
               MutableText text = Text.empty()
                  .formatted(Formatting.GRAY)
                  .append("[")
                  .append(Text.literal("IRC").formatted(Formatting.GOLD))
                  .append("] ")
                  .append(ircMessage.getTextEx());
               if (MinecraftClient.getInstance().inGameHud != null) {
                  MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.empty().append(IText.TEXT).append(text));
               }
            }
         }
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.isValid()
         && directionEvent.getPacket() instanceof PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket
         && playerInteractEntityC2SPacket.type.getType().equals(InteractType.ATTACK)
         && mc.world != null
         && mc.world.getEntityById(playerInteractEntityC2SPacket.entityId) instanceof PlayerEntity player) {
         IrcUser ircUser = getIrcUser();
         IrcUser ircUser2 = getIrcUser(player.getUuid().toString());
         if (ircUser != null && ircUser2 != null && (!(Boolean)ClientSettings.ircAllowPvp.getValue() || !ircUser2.isPvp())) {
            directionEvent.setCancelled(true);
         }
      }
   }
}
