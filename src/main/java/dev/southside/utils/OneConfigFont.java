package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class OneConfigFont {
   @SerializedName("fileName")
   private final String fileName;
   @SerializedName("name")
   private final String name;
   @SerializedName("loaded")
   private boolean loaded = false;
   @SerializedName("buffer")
   private ByteBuffer buffer = null;
   @SerializedName("handle")
   public int handle = -1;

   public OneConfigFont(String text, String text2) {
      this.name = text;
      this.fileName = text2;

      try (InputStream resourceAsStream = OneConfigFont.class.getResourceAsStream(text2)) {
         this.buffer = NativeBufferUtils.getByteBuffer(resourceAsStream).flip();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public String getName() {
      return this.name;
   }

   public String getFileName() {
      return this.fileName;
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public void setLoaded(boolean flag) {
      this.loaded = flag;
   }

   public ByteBuffer getBuffer() {
      return this.buffer;
   }

   public void setBuffer(ByteBuffer buffer2) {
      this.buffer = buffer2;
   }

   public int getHandle() {
      return this.handle;
   }

   public void setHandle(int n) {
      this.handle = n;
   }
}
