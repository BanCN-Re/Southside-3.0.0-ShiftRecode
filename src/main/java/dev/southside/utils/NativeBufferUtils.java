package dev.southside.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import org.lwjgl.system.MemoryUtil;

public class NativeBufferUtils {
   private static final long DEFAULT_LONG = -1717324956565499904L;

   public static ByteBuffer getByteBuffer(ReadableByteChannel readableByteChannel) throws IOException {
      ByteBuffer buffer = null;

      try {
         if (readableByteChannel instanceof SeekableByteChannel seekableByteChannel) {
            SeekableByteChannel seekableByteChannel2 = seekableByteChannel;
            long l = seekableByteChannel.size() - seekableByteChannel.position() + 1L;
            buffer = MemoryUtil.memAlloc((int)Math.min(l, 2147483647L));

            while (seekableByteChannel2.read(buffer) > 0) {
            }
         } else {
            buffer = MemoryUtil.memAlloc((int)DEFAULT_LONG);

            while (readableByteChannel.read(buffer) != -1) {
               if (!buffer.hasRemaining()) {
                  long l2 = buffer.capacity();
                  if (l2 == 2147483647L) {
                     break;
                  }

                  buffer = MemoryUtil.memRealloc(buffer, (int)Math.min(l2 + (l2 >> 1), 2147483647L));
               }
            }
         }

         return buffer;
      } catch (Throwable e) {
         MemoryUtil.memFree(buffer);
         throw e;
      }
   }

   public static ByteBuffer getByteBuffer(InputStream in) throws IOException {
      return getByteBuffer(Channels.newChannel(in));
   }
}
