package dev.southside.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.southside.module.impl.render.BrokenClouds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.CloudRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticlesMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
   @Final
   @Shadow
   private MinecraftClient client;
   @Shadow
   private ClientWorld world;
   @Final
   @Shadow
   private CloudRenderer cloudRenderer;
   @Unique
   private boolean southside$brokenClouds;

   @ModifyExpressionValue(
      method = "render",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getCloudRenderModeValue()Lnet/minecraft/client/option/CloudRenderMode;")
   )
   private CloudRenderMode applyBrokenCloudMode(CloudRenderMode cloudRenderMode) {
      boolean isValidEx = BrokenClouds.isValidEx();
      if (this.southside$brokenClouds != isValidEx) {
         this.southside$brokenClouds = isValidEx;
         this.cloudRenderer.scheduleTerrainUpdate();
      }

      return isValidEx ? CloudRenderMode.FAST : cloudRenderMode;
   }

   @Overwrite
   private ParticlesMode getRandomParticleSpawnChance(boolean flag) {
      if (this.client != null && this.world != null) {
         ParticlesMode particlesMode = (ParticlesMode)(Object)this.client.options.getParticles().getValue();
         if (flag && particlesMode == ParticlesMode.MINIMAL && this.world.random.nextInt(10) == 0) {
            particlesMode = ParticlesMode.DECREASED;
         }

         if (particlesMode == ParticlesMode.DECREASED && this.world.random.nextInt(3) == 0) {
            particlesMode = ParticlesMode.MINIMAL;
         }

         return particlesMode;
      } else {
         return ParticlesMode.MINIMAL;
      }
   }
}
