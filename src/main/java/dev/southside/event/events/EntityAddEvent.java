package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityAddEvent {
   @SerializedName("entity")
   public Entity entity;
   @SerializedName("world")
   public World world;

   public EntityAddEvent(Entity entity2, World world2) {
      this.entity = entity2;
      this.world = world2;
   }
}
