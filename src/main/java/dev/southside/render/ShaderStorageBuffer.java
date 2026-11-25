package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import org.lwjgl.opengl.GL45C;

public class ShaderStorageBuffer implements AutoCloseable {
   @SerializedName("bufferId")
   public int bufferId;

   public final int getOrCreateId() {
      if (this.bufferId == 0) {
         this.bufferId = GL45C.glGenBuffers();
      }

      return this.bufferId;
   }

   public void allocate(long l) {
      GL45C.glBindBuffer(37074, this.getOrCreateId());
      GL45C.nglBufferData(37074, l, 0L, 35048);
   }

   public void upload(long l, long l2, long l3) {
      GL45C.glBindBuffer(37074, this.getOrCreateId());
      GL45C.nglBufferSubData(37074, l, l2, l3);
   }

   public void bindBufferBase(int n) {
      GL45C.glBindBufferBase(37074, n, this.getOrCreateId());
   }

   public void bindBufferRange(int n, long l, long l2) {
      GL45C.glBindBufferRange(37074, n, this.getOrCreateId(), l, l2);
   }

   @Override
   public void close() {
      if (this.bufferId != 0) {
         GL45C.glDeleteBuffers(this.bufferId);
      }

      this.bufferId = 0;
   }
}
