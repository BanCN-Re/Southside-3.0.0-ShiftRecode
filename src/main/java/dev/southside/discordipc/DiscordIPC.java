package dev.southside.discordipc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.southside.discordipc.connection.Connection;
import dev.southside.utils.DiscordUser;
import dev.southside.utils.misc.OpcodeRecord;
import java.lang.management.ManagementFactory;
import java.util.function.BiConsumer;

public class DiscordIPC {
   private static final Gson GSON = new Gson();
   private static BiConsumer<Integer, String> onError = DiscordIPC::println;
   private static Connection connection;
   private static Runnable onReady;
   private static boolean active;
   private static JsonObject queuedActivity;
   private static DiscordUser idUtil;
   private static final long DEFAULT_LONG = -6619877851178991552L;

   public static boolean isValid(long l, Runnable action) {
      connection = Connection.open(DiscordIPC::performAction);
      if (connection == null) {
         return false;
      }

      onReady = action;
      JsonObject json = new JsonObject();
      json.addProperty("v", 1);
      json.addProperty("client_id", Long.toString(l));
      connection.write(Opcode.Handshake, json);
      return true;
   }

   private static void performAction() {
      JsonObject json = new JsonObject();
      json.addProperty("pid", computeInt());
      json.add("activity", queuedActivity);
      JsonObject json2 = new JsonObject();
      json2.addProperty("cmd", "SET_ACTIVITY");
      json2.add("args", json);
      connection.write(Opcode.Frame, json2);
      queuedActivity = null;
   }

   private static void performAction(OpcodeRecord opcodeRecord) {
      JsonObject data = opcodeRecord.getData();
      if (opcodeRecord.getOpcode() == Opcode.Close) {
         if (onError != null) {
            onError.accept(data.get("code").getAsInt(), data.get("message").getAsString());
         }

         performActionEx();
      } else if (opcodeRecord.getOpcode() == Opcode.Frame) {
         if (data.has("evt") && data.get("evt").getAsString().equals("ERROR")) {
            JsonObject asJsonObject = data.getAsJsonObject("data");
            if (onError != null) {
               onError.accept(asJsonObject.get("code").getAsInt(), asJsonObject.get("message").getAsString());
            }
         } else if (data.has("cmd") && data.get("cmd").getAsString().equals("DISPATCH")) {
            active = true;
            idUtil = (DiscordUser)GSON.fromJson(data.getAsJsonObject("data").getAsJsonObject("user"), DiscordUser.class);
            if (onReady != null) {
               onReady.run();
            }

            if (queuedActivity != null) {
               performAction();
            }
         }
      }
   }

   private static void println(int n, String text) {
      System.err.println("Discord IPC error " + n + " with message: " + text);
   }

   public static boolean isValid() {
      return connection != null;
   }

   public static void performActionEx() {
      if (connection != null) {
         connection.performAction();
         connection = null;
         onReady = null;
         active = false;
         queuedActivity = null;
         idUtil = null;
      }
   }

   public static DiscordUser getIdUtil() {
      return idUtil;
   }

   public static void performAction(RichPresence richPresence) {
      if (connection != null) {
         queuedActivity = richPresence.toJson();
         if (active) {
            performAction();
         }
      }
   }

   public static void setOnError(BiConsumer<Integer, String> biConsumer) {
      onError = biConsumer;
   }

   private static int computeInt() {
      String name = ManagementFactory.getRuntimeMXBean().getName();
      return Integer.parseInt(name.substring(0, name.indexOf((int)DEFAULT_LONG)));
   }
}
