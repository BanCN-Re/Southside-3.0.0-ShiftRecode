package dev.southside.discordipc.connection;

import com.google.gson.JsonObject;
import dev.southside.discordipc.Opcode;
import dev.southside.utils.UnixIpcConnection;
import dev.southside.utils.WinIpcConnection;
import dev.southside.utils.misc.OpcodeRecord;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class Connection {
   private static final String[] DEFAULT_NAMES = new String[]{"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"};

   public void write(Opcode opcode, JsonObject json) {
      json.addProperty("nonce", UUID.randomUUID().toString());
      byte[] bytes = json.toString().getBytes();
      ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 8);
      buffer.putInt(Integer.reverseBytes(opcode.ordinal()));
      buffer.putInt(Integer.reverseBytes(bytes.length));
      buffer.put(bytes);
      buffer.rewind();
      this.performAction(buffer);
   }

   public abstract void performAction();

   public static Connection open(Consumer<OpcodeRecord> consumer) {
      String text = System.getProperty("os.name").toLowerCase();
      if (text.contains("win")) {
         for (int i = 0; i < 10; i++) {
            try {
               return new WinIpcConnection("\\\\?\\pipe\\discord-ipc-" + i, consumer);
            } catch (IOException e) {
            }
         }
      } else {
         String text2 = null;

         for (String text3 : DEFAULT_NAMES) {
            text2 = System.getenv(text3);
            if (text2 != null) {
               break;
            }
         }

         if (text2 == null) {
            text2 = "/tmp";
         }

         String text4 = text2 + "/discord-ipc-";

         for (int j = 0; j < 10; j++) {
            try {
               return new UnixIpcConnection(text4 + j, consumer);
            } catch (IOException e2) {
            }
         }
      }

      return null;
   }

   protected abstract void performAction(ByteBuffer buffer);
}
