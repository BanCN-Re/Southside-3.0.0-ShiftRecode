package cc.polyfrost.oneconfig.utils;

import com.google.gson.annotations.SerializedName;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.security.MessageDigest;
import org.jetbrains.annotations.NotNull;

public final class IOUtils {
   @Deprecated
   public static ByteBuffer getByteBuffer(String text) throws IOException {
      return getByteBuffer(text, IOUtils.class);
   }

   public static void performAction(Image image) {
      IOUtils.TransferableImpl transferableImpl = new IOUtils.TransferableImpl(image);
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferableImpl, null);
   }

   @Deprecated
   public static ByteBuffer getByteBuffer2(String text) {
      try {
         return getByteBuffer(text);
      } catch (Exception e) {
         return null;
      }
   }

   public static String formatText(File file) {
      try (FileInputStream fileInputStream = new FileInputStream(file)) {
         MessageDigest instance = MessageDigest.getInstance("SHA-256");
         byte[] bytes = new byte[1024];

         int n;
         while ((n = fileInputStream.read(bytes)) != -1) {
            instance.update(bytes, 0, n);
         }

         return formatText(instance.digest());
      } catch (Exception e) {
         e.printStackTrace();
         return "";
      }
   }

   public static Image getImage() {
      try {
         return (Image)Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.imageFlavor);
      } catch (Exception e) {
         return null;
      }
   }

   public static void applyText(String text) {
      StringSelection stringSelection = new StringSelection(text);
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
   }

   private static String formatText(byte[] bytes) {
      StringBuilder sb = new StringBuilder();

      for (byte b : bytes) {
         sb.append(Integer.toString((b & 255) + 256, 16).substring(1));
      }

      return sb.toString();
   }

   public static ByteBuffer getByteBuffer(String text, Class<?> clazz) throws IOException {
      text = text.trim();
      byte[] bytes;
      if (text.startsWith("http")) {
         try (InputStream inputStream = NetworkUtils.getInputStream(text, "OneConfig", 5000, true)) {
            bytes = org.apache.commons.io.IOUtils.toByteArray(inputStream);
         }
      } else {
         File file = new File(text);

         try (InputStream in = file.exists() && file.isFile() ? Files.newInputStream(file.toPath()) : clazz.getResourceAsStream(text)) {
            if (in == null) {
               throw new FileNotFoundException(text);
            }

            bytes = org.apache.commons.io.IOUtils.toByteArray(in);
         }
      }

      return ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder()).put(bytes).flip();
   }

   public static String getDisplayText() {
      try {
         return Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
      } catch (Exception e) {
         return null;
      }
   }

   public static ByteBuffer getByteBuffer2(String text, Class<?> clazz) {
      try {
         return getByteBuffer(text, clazz);
      } catch (Exception e) {
         return null;
      }
   }

   public static class TransferableImpl implements Transferable {
      @SerializedName("image")
      public final Image image;

      @NotNull
      @Override
      public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException {
         if (!DataFlavor.imageFlavor.equals(dataFlavor)) {
            throw new UnsupportedFlavorException(dataFlavor);
         } else {
            return this.image;
         }
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
         return DataFlavor.imageFlavor.equals(dataFlavor);
      }

      public TransferableImpl(Image image2) {
         this.image = image2;
      }

      @Override
      public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[]{DataFlavor.imageFlavor};
      }
   }
}
