package dev.southside.mixin.render;

import dev.southside.module.impl.render.WorldManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Precipitation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class MixinBiome {
   @Inject(
      method = "Lnet/minecraft/world/biome/Biome;getPrecipitation(Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/world/biome/Biome$Precipitation;",
      at = @At("RETURN"),
      cancellable = true
   )
   private void onGetPrecipitationReturn(BlockPos pos, int n, CallbackInfoReturnable<Precipitation> cir) {
      if (WorldManager.activeValue) {
         cir.setReturnValue(Precipitation.SNOW);
      } else if (WorldManager.activeData) {
         cir.setReturnValue(Precipitation.RAIN);
      }
   }

   @Inject(method = "Lnet/minecraft/world/biome/Biome;hasPrecipitation()Z", at = @At("RETURN"), cancellable = true)
   private void onHasPrecipitationReturn(CallbackInfoReturnable<Boolean> cir) {
      if (WorldManager.activeValue || WorldManager.activeData) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "Lnet/minecraft/world/biome/Biome;doesNotSnow(Lnet/minecraft/util/math/BlockPos;I)Z", at = @At("RETURN"), cancellable = true)
   private void onDoesNotSnowReturn(BlockPos pos, int n, CallbackInfoReturnable<Boolean> cir) {
      if (WorldManager.activeValue) {
         cir.setReturnValue(false);
      }
   }
}
