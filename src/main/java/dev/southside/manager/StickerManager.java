package dev.southside.manager;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StickerManager {
   public static final MinecraftClient mc = MinecraftClient.getInstance();
   public static final int DEFAULT_INT = 64;
   public static final String[] DEFAULT_NAMES = new String[]{"sh1t.gif", "button.gif", "hot.gif", "pig.gif", "sweat.gif"};
   @SerializedName("stickersDirectory")
   public final File stickersDirectory;
   @SerializedName("stickers")
   public final Map<String, StickerManager.Inner> stickers = new LinkedHashMap<>();
   @SerializedName("selectedSticker")
   public String selectedSticker = null;

   public void update() {
      for (String text : DEFAULT_NAMES) {
         File file = new File(this.stickersDirectory, text);
         if (!file.exists()) {
            try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/assets/southside/stickers/" + text)) {
               if (resourceAsStream != null) {
                  Files.copy(resourceAsStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
               }
            } catch (IOException e) {
               Southside.LOGGER.warn("Failed to extract default sticker: " + text, e);
            }
         }
      }
   }

   public BufferedImage getBufferedImage(BufferedImage bufferedImage) {
      BufferedImage bufferedImage2 = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), 2);
      Graphics2D graphics2D = bufferedImage2.createGraphics();
      graphics2D.drawImage(bufferedImage, 0, 0, null);
      graphics2D.dispose();
      return bufferedImage2;
   }

   public void updateEx() {
      for (StickerManager.Inner inner : this.stickers.values()) {
         this.update(inner);
      }

      this.stickers.clear();
      this.selectedSticker = null;
      File[] fileArray = this.stickersDirectory.listFiles(StickerManager::isValid);
      if (fileArray != null) {
         for (File file : fileArray) {
            try {
               String name = file.getName();
               String text = name.toLowerCase();
               if (text.endsWith(".gif")) {
                  this.applyText(name, file);
               } else {
                  this.applyTextEx(name, file);
               }
            } catch (Exception e) {
               Southside.LOGGER.warn("Failed to load sticker: " + file.getName(), e);
            }
         }
      }
   }

   public StickerManager.Entry2 getEntry2() {
      if (this.selectedSticker == null) {
         return null;
      }

      StickerManager.Inner inner = this.stickers.get(this.selectedSticker);
      if (inner != null && inner.frames.length != 0) {
         Identifier id;
         if (inner.animated && inner.frames.length > 1) {
            long l = System.currentTimeMillis() % inner.totalDurationMs;
            long l2 = 0L;
            int n = 0;

            for (int i = 0; i < inner.frames.length; i++) {
               l2 += inner.frames[i].delayMs;
               if (l < l2) {
                  n = i;
                  break;
               }
            }

            id = inner.frames[n].identifier;
         } else {
            id = inner.frames[0].identifier;
         }

         int n2;
         int n3;
         if (inner.width >= inner.height) {
            n2 = 64;
            n3 = (int)((float)inner.height / inner.width * 64.0F);
         } else {
            n3 = 64;
            n2 = (int)((float)inner.width / inner.height * 64.0F);
         }

         return new StickerManager.Entry2(id, n2, n3);
      } else {
         return null;
      }
   }

   public void applyText(String text) {
      if (this.stickers.containsKey(text)) {
         this.selectedSticker = text;
      }
   }

   public static void destroyTexture(StickerManager.Entry entry) {
      mc.getTextureManager().destroyTexture(entry.identifier);
   }

   public void applyText(String text, File file) throws IOException {
      ImageReader imageReader = ImageIO.getImageReadersByFormatName("gif").next();

      try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
         imageReader.setInput(imageInputStream);
         int numImages = imageReader.getNumImages(true);
         if (numImages == 0) {
            return;
         }

         ArrayList list = new ArrayList();
         int n = 0;
         int n2 = 0;
         long l = 0L;
         BufferedImage bufferedImage = null;
         BufferedImage bufferedImage2 = null;

         for (int i = 0; i < numImages; i++) {
            BufferedImage bufferedImage3 = imageReader.read(i);
            IIOMetadata imageMetadata = imageReader.getImageMetadata(i);
            int n3 = 0;
            int n4 = 0;

            try {
               IIOMetadataNode asTree = (IIOMetadataNode)imageMetadata.getAsTree("javax_imageio_gif_image_1.0");
               NodeList elementsByTagName = asTree.getElementsByTagName("ImageDescriptor");
               if (elementsByTagName.getLength() > 0) {
                  Node node = elementsByTagName.item(0);
                  n3 = Integer.parseInt(node.getAttributes().getNamedItem("imageLeftPosition").getNodeValue());
                  n4 = Integer.parseInt(node.getAttributes().getNamedItem("imageTopPosition").getNodeValue());
               }
            } catch (Exception e) {
            }

            if (bufferedImage == null) {
               n = Math.max(bufferedImage3.getWidth() + n3, bufferedImage3.getWidth());
               n2 = Math.max(bufferedImage3.getHeight() + n4, bufferedImage3.getHeight());

               try {
                  IIOMetadataNode asTree2 = (IIOMetadataNode)imageReader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0");
                  NodeList elementsByTagName2 = asTree2.getElementsByTagName("LogicalScreenDescriptor");
                  if (elementsByTagName2.getLength() > 0) {
                     Node node2 = elementsByTagName2.item(0);
                     n = Integer.parseInt(node2.getAttributes().getNamedItem("logicalScreenWidth").getNodeValue());
                     n2 = Integer.parseInt(node2.getAttributes().getNamedItem("logicalScreenHeight").getNodeValue());
                  }
               } catch (Exception e2) {
               }

               bufferedImage = new BufferedImage(n, n2, 2);
            }

            int n5 = this.computeInt(imageMetadata);
            if (n5 == 3) {
               bufferedImage2 = this.getBufferedImage(bufferedImage);
            }

            Graphics2D graphics2D = bufferedImage.createGraphics();
            graphics2D.drawImage(bufferedImage3, n3, n4, null);
            graphics2D.dispose();
            int n6 = this.computeIntEx(imageMetadata);
            if (n6 <= 0) {
               n6 = 100;
            }

            Identifier id = Identifier.of("southside", "sticker/" + this.formatText(text) + "_f" + i);
            NativeImage nativeImage = this.getNativeImage(bufferedImage);
            NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
            mc.execute(() -> registerTexture(id, nativeImageBackedTexture));
            list.add(new StickerManager.Entry(id, n6));
            l += n6;
            if (n5 == 2) {
               Graphics2D graphics2D2 = bufferedImage.createGraphics();
               graphics2D2.setComposite(AlphaComposite.Clear);
               graphics2D2.fillRect(n3, n4, bufferedImage3.getWidth(), bufferedImage3.getHeight());
               graphics2D2.setComposite(AlphaComposite.SrcOver);
               graphics2D2.dispose();
            } else {
               byte b = 3;
               if (n5 == b && bufferedImage2 != null) {
                  bufferedImage = bufferedImage2;
               }
            }
         }

         StickerManager.Inner inner = new StickerManager.Inner();
         inner.frames = (StickerManager.Entry[])list.toArray(new StickerManager.Entry[0]);
         inner.totalDurationMs = l;
         inner.animated = true;
         inner.width = n;
         inner.height = n2;
         this.stickers.put(text, inner);
      } finally {
         imageReader.dispose();
      }
   }

   public void update(StickerManager.Inner inner) {
      for (StickerManager.Entry entry : inner.frames) {
         mc.execute(() -> destroyTexture(entry));
      }
   }

   public String formatText(String text) {
      return text.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
   }

   public String getSelectedSticker() {
      return this.selectedSticker;
   }

   public static boolean isValid(File file, String text) {
      String text2 = text.toLowerCase();
      return text2.endsWith(".png") || text2.endsWith(".gif");
   }

   public static void registerTexture(Identifier id, NativeImageBackedTexture nativeImageBackedTexture) {
      mc.getTextureManager().registerTexture(id, nativeImageBackedTexture);
   }

   public int computeInt(IIOMetadata iiometadata) {
      try {
         Node asTree = iiometadata.getAsTree("javax_imageio_gif_image_1.0");
         NodeList childNodes = asTree.getChildNodes();

         for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if ("GraphicControlExtension".equals(node.getNodeName())) {
               String nodeValue = node.getAttributes().getNamedItem("disposalMethod").getNodeValue();

               return switch (nodeValue) {
                  case "restoreToBackgroundColor" -> 2;
                  case "restoreToPrevious" -> 3;
                  default -> 0;
               };
            }
         }
      } catch (Exception e) {
      }

      return 0;
   }

   public NativeImage getNativeImage(BufferedImage bufferedImage) {
      int width = bufferedImage.getWidth();
      int height = bufferedImage.getHeight();
      NativeImage nativeImage = new NativeImage(width, height, false);

      for (int i = 0; i < height; i++) {
         for (int j = 0; j < width; j++) {
            int rgb = bufferedImage.getRGB(j, i);
            nativeImage.setColorArgb(j, i, rgb);
         }
      }

      return nativeImage;
   }

   public StickerManager() {
      this.stickersDirectory = new File(Southside.FOLDER, "stickers");
      if (!this.stickersDirectory.exists()) {
         this.stickersDirectory.mkdirs();
      }

      this.update();
      this.updateEx();
   }

   public int computeIntEx(IIOMetadata iiometadata) {
      try {
         Node asTree = iiometadata.getAsTree("javax_imageio_gif_image_1.0");
         NodeList childNodes = asTree.getChildNodes();

         for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if ("GraphicControlExtension".equals(node.getNodeName())) {
               String nodeValue = node.getAttributes().getNamedItem("delayTime").getNodeValue();
               return Integer.parseInt(nodeValue) * 10;
            }
         }
      } catch (Exception e) {
      }

      return 100;
   }

   public void applyTextEx(String text, File file) throws IOException {
      Identifier id = Identifier.of("southside", "sticker/" + this.formatText(text));

      try (FileInputStream fileInputStream = new FileInputStream(file)) {
         NativeImage nativeImage = NativeImage.read(fileInputStream);
         NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
         mc.execute(() -> registerTextureEx(id, nativeImageBackedTexture));
         StickerManager.Inner inner = new StickerManager.Inner();
         inner.frames = new StickerManager.Entry[]{new StickerManager.Entry(id, 0)};
         inner.totalDurationMs = 0L;
         inner.animated = false;
         inner.width = nativeImage.getWidth();
         inner.height = nativeImage.getHeight();
         this.stickers.put(text, inner);
      }
   }

   public static void registerTextureEx(Identifier id, NativeImageBackedTexture nativeImageBackedTexture) {
      mc.getTextureManager().registerTexture(id, nativeImageBackedTexture);
   }

   public List<String> collectItems() {
      return new ArrayList<>(this.stickers.keySet());
   }

   public void updateAlt() {
      this.selectedSticker = null;
   }

   public record Entry(Identifier identifier, int delayMs) {

      public Identifier getIdentifier() {
         return this.identifier;
      }

      public int getDelayMs() {
         return this.delayMs;
      }
   }

   public record Entry2(Identifier identifier, int width, int height) {
      public Identifier getIdentifier() {
         return this.identifier;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }
   }

   public static class Inner {
      @SerializedName("frames")
      public StickerManager.Entry[] frames;
      @SerializedName("totalDurationMs")
      public long totalDurationMs;
      @SerializedName("animated")
      public boolean animated;
      @SerializedName("width")
      public int width;
      @SerializedName("height")
      public int height;
   }
}
