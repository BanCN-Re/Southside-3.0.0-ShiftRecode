package dev.southside.mixin.misc;

import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.client.world.ClientChunkManager.ClientChunkMap;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkMap.class)
public interface MixinClientChunkManager_ClientChunkMap {
   @Accessor("chunks")
   AtomicReferenceArray<WorldChunk> getChunks();
}
