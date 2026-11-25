package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ButtonValue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class PlayerAppearance extends Module {
   private static final String DEFAULT_TEXT = Southside.label.toLowerCase(Locale.ROOT);
   private static final Identifier TEXTURE_ID = Identifier.of(DEFAULT_TEXT, "textures/cape/peiban.png");
   private static final Identifier TEXTURE_ID_VALUE = Identifier.of(DEFAULT_TEXT, "textures/skin/skin.png");
   private static final Identifier TEXTURE_ID_DATA = getId("cape");
   private static final Identifier TEXTURE_ID_REF = getId("skin");
   @SerializedName("clientCape")
   public final BooleanValue clientCape = new BooleanValue("Client Cape", true);
   @SerializedName("playerSkin")
   public final BooleanValue playerSkin = new BooleanValue("Player Skin", true);
   @SerializedName("dragonWings")
   public final BooleanValue dragonWings = new BooleanValue("Dragon Wings", true);
   @SerializedName("setCape")
   public final ButtonValue setCape = new ButtonValue("Set Cape", "File Selector", "cnm", 1, this::loadTextureFromDisk);
   @SerializedName("setSkin")
   public final ButtonValue setSkin = new ButtonValue("Set Skin", "File Selector", "cnm", 1, this::loadTextureFromDiskEx);
   @SerializedName("capeTexture")
   private Identifier capeTexture;
   @SerializedName("skinTexture")
   private Identifier skinTexture;
   @SerializedName("textureGeneration")
   private int textureGeneration;

   public PlayerAppearance() {
      super(Category.Render, "Player Appearance", "Allows you to change your player's appearance.");
   }

   private void loadTextureFromDisk() {
      this.loadTextureFromDisk("Select Cape", "cape", id -> this.capeTexture = id);
   }

   private void loadTextureFromDiskEx() {
      this.loadTextureFromDisk("Select Skin", "skin", id -> this.skinTexture = id);
   }

   private static String formatText(String text) {
      MemoryStack stack = MemoryStack.stackPush();

      String text2;
      try {
         PointerBuffer pointerBuffer = stack.mallocPointer(1);
         pointerBuffer.put(stack.UTF8("*.png")).flip();
         text2 = TinyFileDialogs.tinyfd_openFileDialog(text, null, pointerBuffer, "PNG (*.png)", false);
      } catch (Throwable e) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable e2) {
               e.addSuppressed(e2);
            }
         }

         throw e;
      }

      if (stack != null) {
         stack.close();
      }

      return text2;
   }

   private void loadTextureFromDisk(String text, String text2, Consumer<Identifier> consumer) {
      String text3 = formatText(text);
      if (text3 != null) {
         NativeImage nativeImage = null;

         try {
            Path path = Path.of(text3);
            nativeImage = getNativeImage(path);
            Path path2 = Southside.FOLDER.toPath().resolve(text2 + ".png");
            Files.createDirectories(path2.getParent());
            if (!Files.exists(path2) || !Files.isSameFile(path, path2)) {
               Files.copy(path, path2, StandardCopyOption.REPLACE_EXISTING);
            }

            if (this.isEnabled() || "skin".equals(text2) && this.skinTexture != null) {
               NativeImage nativeImage2 = nativeImage;
               nativeImage = null;
               consumer.accept(getId(text2, nativeImage2));
            }
         } catch (Exception e) {
            Southside.LOGGER.warn("Failed to select custom {} texture: {}", text2, text3, e);
         } finally {
            if (nativeImage != null) {
               nativeImage.close();
            }
         }
      }
   }

   private static NativeImage getNativeImage(Path path) throws IOException {
      try (InputStream in = Files.newInputStream(path)) {
         return NativeImage.read(in);
      }
   }

   private static Identifier getId(String text) {
      return Identifier.of(DEFAULT_TEXT, "textures/" + text + "/custom_" + text);
   }

   private static Identifier getId(String text, NativeImage nativeImage) {
      NativeImageBackedTexture nativeImageBackedTexture = null;
      boolean flag = false;

      try {
         nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
         Identifier id = getId(text);
         mc2.getTextureManager().registerTexture(id, nativeImageBackedTexture);
         flag = true;
         return id;
      } finally {
         if (!flag) {
            if (nativeImageBackedTexture != null) {
               nativeImageBackedTexture.close();
            } else {
               nativeImage.close();
            }
         }
      }
   }

   private static Identifier getId(String text, Identifier id) {
      Path path = Southside.FOLDER.toPath().resolve(text + ".png");
      if (!Files.isRegularFile(path)) {
         return id;
      }

      try {
         return getId(text, getNativeImage(path));
      } catch (Exception e) {
         Southside.LOGGER.warn("Failed to load custom {} texture: {}", text, path, e);
         return id;
      }
   }

   @Override
   public void onEnable() {
      int n = ++this.textureGeneration;
      mc2.execute(() -> {
         if (n == this.textureGeneration) {
            this.performActionAlt();
            this.capeTexture = getId("cape", TEXTURE_ID);
            this.skinTexture = getId("skin", TEXTURE_ID_VALUE);
         }
      });
      super.onEnable();
   }

   @Override
   public void onDisable() {
      int n = ++this.textureGeneration;
      mc2.execute(() -> {
         if (n == this.textureGeneration) {
            this.performActionAlt();
         }
      });
      super.onDisable();
   }

   private void performActionAlt() {
      if (TEXTURE_ID_DATA.equals(this.capeTexture)) {
         mc2.getTextureManager().destroyTexture(TEXTURE_ID_DATA);
      }

      if (TEXTURE_ID_REF.equals(this.skinTexture)) {
         mc2.getTextureManager().destroyTexture(TEXTURE_ID_REF);
      }

      this.capeTexture = null;
      this.skinTexture = null;
   }

   public Identifier getCapeTexture() {
      return this.capeTexture;
   }

   public Identifier getSkinTexture() {
      return this.skinTexture;
   }

   public Identifier getId() {
      if (this.skinTexture == null) {
         this.skinTexture = getId("skin", TEXTURE_ID_VALUE);
      }

      return this.skinTexture;
   }
}
