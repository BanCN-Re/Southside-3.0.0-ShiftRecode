package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import org.lwjgl.opengl.GL45C;

public class UniformBufferObject implements AutoCloseable {
   @SerializedName("bufferId")
   public int bufferId;

   public final int getOrCreateId() {
      if (this.bufferId == 0) {
         this.bufferId = GL45C.glGenBuffers();
      }

      return this.bufferId;
   }

   public void allocate(long l) {
      GL45C.glBindBuffer(35345, this.getOrCreateId());
      GL45C.nglBufferData(35345, l, 0L, 35048);
   }

   public void upload(long l, long l2, long l3) {
      GL45C.glBindBuffer(35345, this.getOrCreateId());
      GL45C.nglBufferSubData(35345, l, l2, l3);
   }

   public void bindBufferBase() {
      GL45C.glBindBufferBase(35345, 0, this.getOrCreateId());
   }

   public void bindBufferRange(int n, long l, long l2) {
      GL45C.glBindBufferRange(35345, n, this.getOrCreateId(), l, l2);
   }

   @Override
   public void close() {
      if (this.bufferId != 0) {
         GL45C.glDeleteBuffers(this.bufferId);
      }

      this.bufferId = 0;
   }
}
