package dev.southside.mixin.render;

import dev.southside.utils.IEntityStateHolder;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class MixinEntityRenderState implements IEntityStateHolder {
   @Unique
   private Entity x;

   @Override
   public void setEntity(Entity entity) {
      this.x = entity;
   }

   @Override
   public Entity getEntity() {
      return this.x;
   }
}
