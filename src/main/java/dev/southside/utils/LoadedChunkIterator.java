package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.mixin.accessors.ClientChunkManagerAccessor;
import dev.southside.mixin.misc.MixinClientChunkManager_ClientChunkMap;
import java.util.Iterator;
import net.minecraft.client.world.ClientChunkManager.ClientChunkMap;
import net.minecraft.world.chunk.Chunk;

public class LoadedChunkIterator implements Iterator<Chunk> {
   @SerializedName("chunkMap")
   public final MixinClientChunkManager_ClientChunkMap chunkMap = getMixinClientChunkManager_ClientChunkMap(
      ((ClientChunkManagerAccessor)IMinecraft.mc.world.getChunkManager()).getChunks()
   );
   @SerializedName("requireLoadedNeighbors")
   public final boolean requireLoadedNeighbors;
   @SerializedName("index")
   public int index = 0;
   @SerializedName("currentChunk")
   public Chunk currentChunk;

   public static MixinClientChunkManager_ClientChunkMap getMixinClientChunkManager_ClientChunkMap(ClientChunkMap clientChunkMap) {
      return (MixinClientChunkManager_ClientChunkMap)(Object)clientChunkMap;
   }

   public Chunk next() {
      return this.getChunk();
   }

   public boolean isValid(Chunk chunk) {
      int x = chunk.getPos().x;
      int z = chunk.getPos().z;
      return IMinecraft.mc.world.getChunkManager().isChunkLoaded(x + 1, z)
         && IMinecraft.mc.world.getChunkManager().isChunkLoaded(x - 1, z)
         && IMinecraft.mc.world.getChunkManager().isChunkLoaded(x, z + 1)
         && IMinecraft.mc.world.getChunkManager().isChunkLoaded(x, z - 1);
   }

   public Chunk getChunk() {
      Chunk currentChunk2 = this.currentChunk;
      this.currentChunk = null;

      while (this.index < this.chunkMap.getChunks().length()) {
         this.currentChunk = (Chunk)this.chunkMap.getChunks().get(this.index++);
         if (this.currentChunk != null && (!this.requireLoadedNeighbors || this.isValid(this.currentChunk))) {
            break;
         }
      }

      return currentChunk2;
   }

   public LoadedChunkIterator(boolean flag) {
      this.requireLoadedNeighbors = flag;
      this.getChunk();
   }

   @Override
   public boolean hasNext() {
      return this.currentChunk != null;
   }
}
