package dev.southside.utils;

import net.minecraft.entity.Entity;

public interface IEntityStateHolder {
   void setEntity(Entity entity);

   Entity getEntity();
}
