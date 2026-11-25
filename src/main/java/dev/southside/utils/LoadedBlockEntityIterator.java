package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.mixin.accessors.ChunkAccessor;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.Chunk;

public class LoadedBlockEntityIterator implements Iterator<BlockEntity> {
   @SerializedName("chunkIterator")
   public final Iterator<Chunk> chunkIterator = new LoadedChunkIterator(false);
   @SerializedName("blockEntityIterator")
   public Iterator<BlockEntity> blockEntityIterator;

   public LoadedBlockEntityIterator() {
      this.performAction();
   }

   public void performAction() {
      while (this.chunkIterator.hasNext()) {
         Map blockEntities = ((ChunkAccessor)this.chunkIterator.next()).getBlockEntities();
         if (!blockEntities.isEmpty()) {
            this.blockEntityIterator = blockEntities.values().iterator();
            return;
         }
      }
   }

   @Override
   public boolean hasNext() {
      if (this.blockEntityIterator == null) {
         return false;
      }

      if (this.blockEntityIterator.hasNext()) {
         return true;
      }

      this.performAction();
      return this.blockEntityIterator.hasNext();
   }

   public BlockEntity next() {
      return this.blockEntityIterator.next();
   }
}
