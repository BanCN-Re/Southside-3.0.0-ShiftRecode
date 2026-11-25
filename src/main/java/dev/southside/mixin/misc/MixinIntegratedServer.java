package dev.southside.mixin.misc;

import dev.southside.utils.IIntegratedServerExt;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer implements IIntegratedServerExt {
   @Unique
   private boolean southside$skipSaveOnShutdown;

   @Inject(method = "saveAll", at = @At("HEAD"), cancellable = true)
   private void skipSaveIfFlagged(boolean flag, boolean flag2, boolean flag3, CallbackInfoReturnable<Boolean> cir) {
      if (this.southside$skipSaveOnShutdown) {
         cir.setReturnValue(true);
      }
   }

   @Override
   public void southside$setSkipSaveOnShutdown(boolean flag) {
      this.southside$skipSaveOnShutdown = flag;
   }
}
