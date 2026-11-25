package dev.southside.mixin.misc;

import dev.southside.utils.TimerSpeedManager;
import dev.southside.utils.ITickCounterAccessor;
import net.minecraft.client.render.RenderTickCounter.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Dynamic.class)
public class MixinRenderTickCounter_Dynamic implements ITickCounterAccessor {
   @Mutable
   @Shadow
   @Final
   public float tickTime;
   @Shadow
   public float lastFrameDuration;

   @Inject(
      method = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;beginRenderTick(J)I",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J", opcode = 181)
   )
   private void onBeginRenderTickField(long l, CallbackInfoReturnable<Integer> cir) {
      this.lastFrameDuration = this.lastFrameDuration * TimerSpeedManager.getTimerSpeed();
   }

   @Override
   public void setTickTime(float f) {
      this.tickTime = f;
   }
}
