package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.module.impl.render.Glow;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {
   @Inject(method = "Lnet/minecraft/entity/Entity;isGlowing()Z", at = @At("HEAD"), cancellable = true)
   private void applyGlow(CallbackInfoReturnable<Boolean> cir) {
      Entity entity = (Entity)(Object)this;
      Glow glowModule = this.getGlowModule();
      if (glowModule != null && glowModule.isValidTarget(entity)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "Lnet/minecraft/entity/Entity;getTeamColorValue()I", at = @At("HEAD"), cancellable = true)
   private void applyGlowColor(CallbackInfoReturnable<Integer> cir) {
      Entity entity = (Entity)(Object)this;
      Glow glowModule = this.getGlowModule();
      if (glowModule != null && glowModule.isValidTarget(entity)) {
         cir.setReturnValue(glowModule.computeInt(entity));
      }
   }

   @Unique
   private Glow getGlowModule() {
      try {
         return Southside.INSTANCE
            .getModuleManager()
            .getModules()
            .stream()
            .filter(module -> module.getClass() == Glow.class)
            .map(Glow.class::cast)
            .findFirst()
            .orElse(null);
      } catch (Exception e) {
         return null;
      }
   }
}
