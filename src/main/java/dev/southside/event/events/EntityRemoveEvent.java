package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.world.World;

public class EntityRemoveEvent {
   @SerializedName("entity")
   public Entity entity;
   @SerializedName("removalReason")
   public RemovalReason removalReason;
   @SerializedName("world")
   public World world;

   public EntityRemoveEvent(Entity entity2, RemovalReason removalReason2, World world2) {
      this.entity = entity2;
      this.removalReason = removalReason2;
      this.world = world2;
   }
}
