package dev.southside.mixin;

import dev.southside.Southside;
import dev.southside.event.events.EntityRemoveEvent;
import dev.southside.event.events.EntityAddEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
   @Inject(method = "Lnet/minecraft/client/world/ClientWorld;removeEntity(ILnet/minecraft/entity/Entity$RemovalReason;)V", at = @At("HEAD"))
   private void onRemoveEntityHead(int n, RemovalReason removalReason, CallbackInfo ci) {
      ClientWorld world = (ClientWorld)(Object)this;
      Entity entityById = world.getEntityById(n);
      Southside.INSTANCE.getEventBus().dispatch(new EntityRemoveEvent(entityById, removalReason, world));
   }

   @Inject(method = "Lnet/minecraft/client/world/ClientWorld;addEntity(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
   private void onAddEntityReturn(Entity entity, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().dispatch(new EntityAddEvent(entity, (ClientWorld)(Object)this));
   }
}
