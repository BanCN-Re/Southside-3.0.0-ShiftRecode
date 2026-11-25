package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.misc.NotesRecord;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public final class NbsSongParser {
   public static final long DEFAULT_LONG = 16777216L;
   public static final int DEFAULT_INT = 250000;
   private static final int DEFAULT_INT_VALUE = 500000;
   private static final int DEFAULT_INT_DATA = 1048576;
   private static final int DEFAULT_INT_REF = 10000000;
   private static final float DEFAULT_FLOAT = 100.0F;

   private NbsSongParser() {
   }

   public static NotesRecord getNotesRecord(Path path) throws IOException {
      long size = Files.size(path);
      if (size <= 0L) {
         throw new IOException("NBS file is empty");
      }

      if (size > 16777216L) {
         throw new IOException("NBS file exceeds 16777216 bytes");
      }

      try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {
         NbsSongParser.Inner inner = new NbsSongParser.Inner(bufferedInputStream, 16777216L);
         int n = inner.computeInt();
         int n2 = 0;
         if (n == 0) {
            n2 = inner.getByte() & 255;
            if (n2 > 5) {
               throw new IOException("Unsupported NBS version: " + n2);
            }

            inner.getByte();
            if (n2 >= 3) {
               inner.computeInt();
            }
         }

         inner.computeInt();
         inner.performAction();
         inner.performAction();
         inner.performAction();
         inner.performAction();
         float f = inner.computeInt() / 100.0F;
         if (f <= 0.0F || f > 100.0F) {
            throw new IOException("Unsupported NBS tempo: " + f);
         }

         inner.getByte();
         inner.getByte();
         inner.getByte();
         inner.computeIntEx();
         inner.computeIntEx();
         inner.computeIntEx();
         inner.computeIntEx();
         inner.computeIntEx();
         inner.performAction();
         if (n2 >= 4) {
            inner.getByte();
            inner.getByte();
            inner.computeInt();
         }

         ArrayList list = new ArrayList();
         int n3 = -1;
         int n4 = 0;

         while (true) {
            int n5 = inner.computeInt();
            if (n5 == 0) {
               if (list.isEmpty()) {
                  throw new IOException("NBS file contains no supported notes");
               } else {
                  return new NotesRecord(list, f);
               }
            }

            n3 += n5;
            if (n3 > 10000000) {
               throw new IOException("NBS song exceeds tick limit 10000000");
            }

            while (true) {
               int n6 = inner.computeInt();
               if (n6 == 0) {
                  break;
               }

               if (++n4 > 500000) {
                  throw new IOException("NBS file contains too many note events");
               }

               int n7 = inner.getByte() & 255;
               int n8 = inner.getByte() & 255;
               if (n2 >= 4) {
                  inner.getByte();
                  inner.getByte();
                  inner.computeInt();
               }

               int n9 = n8 - 33;
               if (n9 >= 0 && n9 <= 24 && n7 <= 15) {
                  if (list.size() >= 250000) {
                     throw new IOException("NBS file contains more than 250000 playable notes");
                  }

                  list.add(new NotesRecord.Entry(n3, n7, n9));
               }
            }
         }
      }
   }

   private static final class Inner {
      @SerializedName("input")
      private final InputStream input;
      @SerializedName("byteLimit")
      private final long byteLimit;
      @SerializedName("bytesRead")
      private long bytesRead;

      public Inner(InputStream in, long l) {
         this.input = in;
         this.byteLimit = l;
      }

      public byte getByte() throws IOException {
         this.performActionEx();
         if (this.bytesRead >= this.byteLimit) {
            throw new IOException("NBS read limit exceeded");
         }

         int n = this.input.read();
         if (n < 0) {
            throw new IOException("Unexpected EOF");
         }

         this.bytesRead++;
         return (byte)n;
      }

      public int computeInt() throws IOException {
         int n = this.getByte() & 255;
         int n2 = this.getByte() & 255;
         return n | n2 << 8;
      }

      public int computeIntEx() throws IOException {
         int n = this.getByte() & 255;
         int n2 = this.getByte() & 255;
         int n3 = this.getByte() & 255;
         int n4 = this.getByte() & 255;
         return n | n2 << 8 | n3 << 16 | n4 << 24;
      }

      public void performAction() throws IOException {
         int n = this.computeIntEx();
         if (n >= 0 && n <= 1048576) {
            for (int i = 0; i < n; i++) {
               this.getByte();
            }
         } else {
            throw new IOException("Invalid NBS string length: " + n);
         }
      }

      private void performActionEx() throws InterruptedIOException {
         if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("NBS loading cancelled");
         }
      }
   }
}
