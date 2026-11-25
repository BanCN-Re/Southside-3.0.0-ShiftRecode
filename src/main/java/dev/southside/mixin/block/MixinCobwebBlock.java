package dev.southside.mixin.block;

import dev.southside.Southside;
import dev.southside.event.events.BlockStateEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CobwebBlock.class)
public class MixinCobwebBlock {
   @Inject(
      method = "Lnet/minecraft/block/CobwebBlock;onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   public void onOnEntityCollisionHead(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
      BlockStateEvent blockStateEvent = new BlockStateEvent(state, world, pos, entity);
      Southside.INSTANCE.getEventBus().postCancellableEvent(blockStateEvent);
      if (blockStateEvent.isCancelled()) {
         ci.cancel();
      }
   }
}
