package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.Stage;
import dev.southside.utils.CancellableEvent;
import net.minecraft.entity.Entity;

public class EntityEvent extends CancellableEvent {
   @SerializedName("entity")
   public Entity entity;
   @SerializedName("phase")
   public Stage phase;

   public EntityEvent(Entity entity2, Stage stage) {
      this.entity = entity2;
      this.phase = stage;
   }
}
