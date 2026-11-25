package dev.southside.module.impl.world;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ModeValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityEditor extends Module {
   @SerializedName("model")
   public final ModeValue model = new ModeValue(
      "Model", "Pig", "Pig", "Sheep", "Wolf", "Camel", "Donkey", "Creeper", "Skeleton", "Zombie", "Blaze", "Iron Golem"
   );
   @SerializedName("allEntities")
   public final BooleanValue allEntities = new BooleanValue("All Entities", false);
   @SerializedName("players")
   public final BooleanValue players = new BooleanValue("Players", true);
   @SerializedName("mobs")
   public final BooleanValue mobs = new BooleanValue("Mobs", false);

   public EntityEditor() {
      super(Category.World, "EntityEditor", "Replace entity models");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.addListener(this.model.getLabel(), this::initPostRunnable);
      this.hideIf(this.players.getLabel(), this.allEntities::getValue);
      this.hideIf(this.mobs.getLabel(), this.allEntities::getValue);
      this.initPostRunnable();
   }

   private void initPostRunnable() {
      this.setInfoText(this.model.getDisplayTextEx());
   }

   public static EntityEditor getEntityEditor() {
      try {
         return Southside.INSTANCE.getModuleManager().getModule(EntityEditor.class);
      } catch (Exception e) {
         return null;
      }
   }

   public static EntityType<? extends MobEntity> getEntityType() {
      EntityEditor entityEditor = getEntityEditor();
      if (entityEditor == null) {
         return EntityType.PIG;
      }

      return switch (entityEditor.model.getValue()) {
         case 1 -> EntityType.SHEEP;
         case 2 -> EntityType.WOLF;
         case 3 -> EntityType.CAMEL;
         case 4 -> EntityType.DONKEY;
         case 5 -> EntityType.CREEPER;
         case 6 -> EntityType.SKELETON;
         case 7 -> EntityType.ZOMBIE;
         case 8 -> EntityType.BLAZE;
         case 9 -> EntityType.IRON_GOLEM;
         default -> EntityType.PIG;
      };
   }

   public static int computeInt() {
      EntityEditor entityEditor = getEntityEditor();
      return entityEditor == null ? 0 : entityEditor.model.getValue();
   }

   public static boolean shouldReplace(Entity entity) {
      if (Module.mc.player != null && entity != Module.mc.player) {
         EntityEditor entityEditor = getEntityEditor();
         if (entityEditor == null || !entityEditor.isEnabled()) {
            return false;
         } else {
            return entityEditor.allEntities.getValue()
               ? entity instanceof LivingEntity
               : (Boolean)entityEditor.players.getValue() && entity instanceof PlayerEntity || (Boolean)entityEditor.mobs.getValue() && entity instanceof MobEntity;
         }
      } else {
         return false;
      }
   }
}
