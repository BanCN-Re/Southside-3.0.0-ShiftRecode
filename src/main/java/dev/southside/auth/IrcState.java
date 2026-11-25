package dev.southside.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.utils.IMinecraft;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import net.minecraft.util.Formatting;

public class IrcState implements IMinecraft {
   public static volatile Set<IrcUser> ircUsersSet = Set.of();
   public static final Queue<IrcMessage> queue;

   public static void applyText(String text) {
      try {
         JsonArray asJsonArray = JsonParser.parseString(text).getAsJsonArray();
         LinkedHashSet linkedHashSet = new LinkedHashSet();

         for (JsonElement element : asJsonArray) {
            JsonObject asJsonObject = element.getAsJsonObject();
            String asString = asJsonObject.get("name").getAsString();
            boolean asBoolean = asJsonObject.get("pvp").getAsBoolean();
            JsonArray asJsonArray2 = asJsonObject.get("uuids").getAsJsonArray();
            IrcUser ircUser = new IrcUser(asString, asBoolean, new ArrayList<>());

            for (JsonElement element2 : asJsonArray2) {
               ircUser.getUuids().add(element2.getAsString());
            }

            linkedHashSet.add(ircUser);
         }

         ircUsersSet = Set.copyOf(linkedHashSet);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void applyTextEx(String text) {
      try {
         queue.offer(IrcMessage.getIrcMessage(JsonParser.parseString(text).getAsJsonObject()));
      } catch (Throwable e) {
         e.printStackTrace();
      }
   }

   public static void offer(String text) {
      queue.offer(IrcMessage.getIrcMessage(text).getIrcMessage(new Formatting[]{Formatting.RED}));
   }

   public static void clear() {
      ircUsersSet = Set.of();
      queue.clear();
   }

   public static void performAction() {
      IrcClient instance = IrcClient.getINSTANCE();
      if (instance.isValid() && mc.player != null && mc.world != null) {
         instance.isValid(mc.player.getUuid().toString(), (Boolean)ClientSettings.ircAllowPvp.getValue());
      }
   }

   static {
      long l = 962634184056534011L;
      long l2 = -4672048752912600325L ^ l;
      byte b = -1;
      long l3 = l2;
      queue = new ArrayBlockingQueue<>((int)l3);
   }
}
