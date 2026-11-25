package dev.southside.module.impl.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.WorldJoinEvent;
import dev.southside.event.events.CustomPayloadEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.CustomPayloadHelper;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AntiNick extends Module {
   public static final Identifier S2CEVENT_ID = Identifier.of("heypixel", "s2cevent");
   private static final Map<String, String> stringMap = new ConcurrentHashMap<>();
   @SerializedName("decoders")
   private final List<Function<byte[], String>> decoders = List.of(
      AntiNick::formatText, AntiNick::formatTextEx, AntiNick::formatTextAlt, AntiNick::formatTextImpl, AntiNick::formatTextDirect
   );

   public AntiNick() {
      super(Category.Misc, "AntiNick", "显示花雨庭 Nick 玩家的真实名字");
   }

   public static void register() {
      PayloadTypeRegistry.playS2C().register(new Id(S2CEVENT_ID), CustomPayloadHelper.Class8710Impl.getPacketCodec(S2CEVENT_ID));
   }

   public static boolean isValidTarget(PlayerEntity player) {
      return player != null && matches(player.getGameProfile().getName());
   }

   public static boolean matches(String text) {
      return text != null && isValidEx() && stringMap.containsKey(text);
   }

   public static String formatText(String text) {
      return text == null ? null : stringMap.get(text);
   }

   public static String formatText(PlayerEntity player) {
      return player == null ? null : stringMap.get(player.getGameProfile().getName());
   }

   private static boolean isValidEx() {
      AntiNick antiNick = Southside.INSTANCE.getModuleManager().getModule(AntiNick.class);
      return antiNick.enabled.getValue();
   }

   @Override
   public void onEnable() {
      stringMap.clear();
   }

   @EventTarget
   public void onCustomEvent4(WorldJoinEvent worldJoinEvent) {
      stringMap.clear();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      ClientPlayNetworkHandler networkHandler = mc2.getNetworkHandler();
      if (stateEvent.state == Stage.PRE && networkHandler != null && !stringMap.isEmpty()) {
         for (PlayerListEntry playerListEntry : networkHandler.getPlayerList()) {
            String name = playerListEntry.getProfile().getName();
            String text = stringMap.get(name);
            if (text != null) {
               playerListEntry.setDisplayName(Text.literal("§d[Nick] §f| " + name + " §b(" + text + ")§r"));
            }
         }
      }
   }

   @EventTarget
   public void onCustomPayload(CustomPayloadEvent customPayloadEvent) {
      if (customPayloadEvent.customPayload instanceof CustomPayloadHelper.Class8710Impl class8710Impl && S2CEVENT_ID.equals(class8710Impl.channel)) {
         PacketByteBuf data = class8710Impl.data;
         byte[] bytes = new byte[data.readableBytes()];
         data.getBytes(data.readerIndex(), bytes);
         this.performAction(bytes);
      }
   }

   private void performAction(byte[] bytes) {
      if (bytes.length != 0) {
         String text = null;

         for (Function fn : this.decoders) {
            text = (String)fn.apply(bytes);
            if (matchesEx(text)) {
               break;
            }
         }

         if (matchesEx(text)) {
            try {
               JsonObject asJsonObject = JsonParser.parseString(text).getAsJsonObject();
               this.performAction(asJsonObject, "players", false);
               this.performAction(asJsonObject, "data", true);
            } catch (RuntimeException e) {
            }
         }
      }
   }

   private void performAction(JsonObject json, String text, boolean flag) {
      if (json.has(text) && json.get(text).isJsonObject()) {
         for (Entry entry : json.getAsJsonObject(text).entrySet()) {
            if (((JsonElement)entry.getValue()).isJsonObject()) {
               JsonObject asJsonObject = ((JsonElement)entry.getValue()).getAsJsonObject();
               if (!flag || asJsonObject.has("name") || asJsonObject.has("uuid")) {
                  String text2 = asJsonObject.has("name") ? asJsonObject.get("name").getAsString() : "Unknown";
                  String text3 = asJsonObject.has("uuid") ? asJsonObject.get("uuid").getAsString() : (String)entry.getKey();
                  this.applyTextAlt(text2, text3);
               }
            }
         }
      }
   }

   private void applyTextAlt(String text, String text2) {
      ClientPlayNetworkHandler networkHandler = mc2.getNetworkHandler();
      if (networkHandler != null) {
         String text3 = text2.replace("-", "");

         for (PlayerListEntry playerListEntry : networkHandler.getPlayerList()) {
            UUID id = playerListEntry.getProfile().getId();
            if (id != null && id.toString().replace("-", "").equalsIgnoreCase(text3)) {
               String name = playerListEntry.getProfile().getName();
               if (name != null && !name.equals(text)) {
                  stringMap.putIfAbsent(name, text);
               }

               return;
            }
         }
      }
   }

   private static boolean matchesEx(String text) {
      if (text == null) {
         return false;
      }

      String text2 = text.trim();
      return text2.startsWith("{") || text2.startsWith("[");
   }

   private static String formatText(byte[] bytes) {
      return bytes.length >= 6 && (bytes[0] & 255) == 250 && isValid(bytes, 5) ? formatText(bytes, 5) : null;
   }

   private static String formatTextEx(byte[] bytes) {
      return bytes.length >= 2 && (bytes[0] & 255) == 233 ? formatTextEx(bytes, 1) : null;
   }

   private static String formatTextAlt(byte[] bytes) {
      for (int i = 0; i < bytes.length - 1; i++) {
         if (isValid(bytes, i)) {
            return formatText(bytes, i);
         }
      }

      return null;
   }

   private static String formatTextImpl(byte[] bytes) {
      for (int i = 0; i < bytes.length; i++) {
         if (bytes[i] == 123 || bytes[i] == 91) {
            return formatTextEx(bytes, i);
         }
      }

      return null;
   }

   private static String formatTextDirect(byte[] bytes) {
      return formatTextEx(bytes, 0);
   }

   private static boolean isValid(byte[] bytes, int n) {
      if (n >= bytes.length - 1) {
         return false;
      }

      int n2 = bytes[n] & 255;
      int n3 = bytes[n + 1] & 255;
      return n2 == 120 && (n3 == 1 || n3 == 156 || n3 == 218);
   }

   private static String formatText(byte[] bytes, int n) {
      short s = 15714;
      Inflater inflater = new Inflater();
      long l = 6530424956937962310L;
      inflater.setInput(bytes, n, bytes.length - n);
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] bytes2 = new byte[8192];

      try {
         int n2;
         while (!inflater.finished() && (n2 = inflater.inflate(bytes2)) != 0) {
            byteArrayOutputStream.write(bytes2, 0, n2);
         }

         return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
      } catch (DataFormatException e) {
         return null;
      } finally {
         inflater.end();
      }
   }

   private static String formatTextEx(byte[] bytes, int n) {
      return n >= bytes.length ? null : new String(bytes, n, bytes.length - n, StandardCharsets.UTF_8);
   }
}
