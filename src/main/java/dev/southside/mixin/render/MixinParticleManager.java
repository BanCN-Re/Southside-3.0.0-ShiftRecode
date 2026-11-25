package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.FireworksSparkParticle.FireworkParticle;
import net.minecraft.client.particle.FireworksSparkParticle.Flash;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
   @Inject(
      at = @At("HEAD"),
      method = "Lnet/minecraft/client/particle/ParticleManager;addParticle(Lnet/minecraft/client/particle/Particle;)V",
      cancellable = true
   )
   public void onAddParticleHead(Particle particle, CallbackInfo ci) {
      NoRender noRender = Southside.INSTANCE.getModuleManager().getModule(NoRender.class);
      if (noRender.isEnabled()) {
         if (noRender.explosions.getValue() && particle instanceof ExplosionLargeParticle) {
            ci.cancel();
         }

         if (noRender.breakParticles.getValue() && particle instanceof BlockDustParticle) {
            ci.cancel();
         }

         if (noRender.flowerRocket.getValue() && (particle instanceof FireworkParticle || particle instanceof Flash)) {
            ci.cancel();
         }
      }
   }
}
