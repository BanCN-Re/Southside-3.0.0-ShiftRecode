package dev.southside.utils;

import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import dev.southside.discordipc.Opcode;
import dev.southside.discordipc.connection.Connection;
import dev.southside.utils.misc.OpcodeRecord;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class UnixIpcConnection extends Connection {
   @SerializedName("selector")
   private final Selector selector = Selector.open();
   @SerializedName("socketChannel")
   private final SocketChannel socketChannel;
   @SerializedName("callback")
   private final Consumer<OpcodeRecord> callback;

   @Override
   protected void performAction(ByteBuffer buffer) {
      try {
         this.socketChannel.write(buffer);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void performActionEx() {
      UnixIpcConnection.Mode Opcode2 = UnixIpcConnection.Mode.Opcode;
      ByteBuffer buffer = ByteBuffer.allocate(4);
      ByteBuffer buffer2 = null;
      Opcode opcode = null;

      try {
         while (true) {
            this.selector.select();
            switch (Opcode2) {
               case Opcode:
                  this.socketChannel.read(buffer);
                  if (!buffer.hasRemaining()) {
                     opcode = Opcode.valueOf(Integer.reverseBytes(buffer.getInt(0)));
                     Opcode2 = UnixIpcConnection.Mode.Length;
                     buffer.rewind();
                  }
                  break;
               case Length:
                  this.socketChannel.read(buffer);
                  if (!buffer.hasRemaining()) {
                     buffer2 = ByteBuffer.allocate(Integer.reverseBytes(buffer.getInt(0)));
                     Opcode2 = UnixIpcConnection.Mode.Data;
                     buffer.rewind();
                  }
                  break;
               case Data:
                  this.socketChannel.read(buffer2);
                  if (!buffer2.hasRemaining()) {
                     String text = Charset.defaultCharset().decode(buffer2.rewind()).toString();
                     this.callback.accept(new OpcodeRecord(opcode, JsonParser.parseString(text).getAsJsonObject()));
                     buffer2 = null;
                     Opcode2 = UnixIpcConnection.Mode.Opcode;
                  }
            }
         }
      } catch (Exception e) {
      }
   }

   public UnixIpcConnection(String text, Consumer<OpcodeRecord> consumer) throws IOException {
      this.socketChannel = SocketChannel.open(UnixDomainSocketAddress.of(text));
      this.callback = consumer;
      this.socketChannel.configureBlocking(false);
      this.socketChannel.register(this.selector, 1);
      Thread thread = new Thread(this::performActionEx, "Discord IPC - Read thread");
      thread.start();
   }

   @Override
   public void performAction() {
      try {
         this.selector.close();
         this.socketChannel.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private enum Mode {
      Opcode,
      Length,
      Data;
   }
}
