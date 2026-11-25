package dev.southside.mixin.misc;

import dev.southside.utils.SouthsideSounds;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registries.class)
public class MixinRegistries {
   @Inject(method = "Lnet/minecraft/registry/Registries;bootstrap()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registries;init()V"))
   private static void onBootstrapInvoke(CallbackInfo ci) {
      Registry.register(Registries.SOUND_EVENT, SouthsideSounds.SIGMA_ACTIVATE_ID, SouthsideSounds.SIGMA_ACTIVATE);
      Registry.register(Registries.SOUND_EVENT, SouthsideSounds.SIGMA_DEACTIVATE_ID, SouthsideSounds.SIGMA_DEACTIVATE);
      Registry.register(Registries.SOUND_EVENT, SouthsideSounds.TOGGLE_ON_ID, SouthsideSounds.TOGGLE_ON);
      Registry.register(Registries.SOUND_EVENT, SouthsideSounds.TOGGLE_OFF_ID, SouthsideSounds.TOGGLE_OFF);
      Registry.register(Registries.SOUND_EVENT, SouthsideSounds.PIANO_ON_ID, SouthsideSounds.PIANO_ON);
      Registry.register(Registries.SOUND_EVENT, SouthsideSounds.PIANO_OFF_ID, SouthsideSounds.PIANO_OFF);
   }
}
