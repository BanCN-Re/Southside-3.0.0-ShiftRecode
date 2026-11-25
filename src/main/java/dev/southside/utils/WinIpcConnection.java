package dev.southside.utils;

import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import dev.southside.discordipc.Opcode;
import dev.southside.discordipc.connection.Connection;
import dev.southside.utils.misc.OpcodeRecord;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class WinIpcConnection extends Connection {
   @SerializedName("pipe")
   private final RandomAccessFile pipe;
   @SerializedName("callback")
   private final Consumer<OpcodeRecord> callback;
   private static final long DEFAULT_LONG = 100L;

   @Override
   public void performAction() {
      try {
         this.pipe.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void performActionEx() {
      ByteBuffer buffer = ByteBuffer.allocate(4);

      try {
         while (true) {
            this.performActionEx(buffer);
            Opcode opcode = Opcode.valueOf(Integer.reverseBytes(buffer.getInt(0)));
            this.performActionEx(buffer);
            int n = Integer.reverseBytes(buffer.getInt(0));
            ByteBuffer buffer2 = ByteBuffer.allocate(n);
            this.performActionEx(buffer2);
            String text = Charset.defaultCharset().decode(buffer2.rewind()).toString();
            this.callback.accept(new OpcodeRecord(opcode, JsonParser.parseString(text).getAsJsonObject()));
         }
      } catch (Exception e) {
      }
   }

   public WinIpcConnection(String text, Consumer<OpcodeRecord> consumer) throws IOException {
      this.pipe = new RandomAccessFile(text, "rw");
      this.callback = consumer;
      Thread thread = new Thread(this::performActionEx, "Discord IPC - Read thread");
      thread.start();
   }

   @Override
   protected void performAction(ByteBuffer buffer) {
      try {
         this.pipe.write(buffer.array());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void performActionEx(ByteBuffer buffer) throws IOException {
      buffer.rewind();

      while (this.pipe.length() < buffer.remaining()) {
         Thread.onSpinWait();

         try {
            Thread.sleep(DEFAULT_LONG);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }

      while (buffer.hasRemaining()) {
         this.pipe.getChannel().read(buffer);
      }
   }
}
