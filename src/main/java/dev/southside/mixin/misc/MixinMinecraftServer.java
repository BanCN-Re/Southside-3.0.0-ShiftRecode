package dev.southside.mixin.misc;

import dev.southside.utils.SkyblockPreviewWorld;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
   @Inject(method = "prepareStartRegion", at = @At("HEAD"), cancellable = true)
   private void skipPrepareStartRegionForIsland(CallbackInfo ci) {
      if (SkyblockPreviewWorld.isValid()) {
         ci.cancel();
      }
   }
}
