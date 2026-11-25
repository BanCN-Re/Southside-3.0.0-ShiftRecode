package dev.southside.mixin.misc;

import dev.southside.Southside;
import dev.southside.utils.ChestOpenEvent;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public class MixinChestBlockEntity {
   @Inject(method = "Lnet/minecraft/block/entity/ChestBlockEntity;onOpen(Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("HEAD"))
   public void onOnOpenHead(PlayerEntity player, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().postEvent(new ChestOpenEvent(player, ((ChestBlockEntity)(Object)this).getPos()));
   }
}
