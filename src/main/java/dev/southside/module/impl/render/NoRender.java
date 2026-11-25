package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;

public class NoRender extends Module {
   @SerializedName("potions")
   public final BooleanValue potions = new BooleanValue("Potions", false);
   @SerializedName("explosions")
   public final BooleanValue explosions = new BooleanValue("Explosions", false);
   @SerializedName("flowerRocket")
   public final BooleanValue flowerRocket = new BooleanValue("FlowerRocket", false);
   @SerializedName("hurtCam")
   public final BooleanValue hurtCam = new BooleanValue("HurtCam", false);
   @SerializedName("armor")
   public final BooleanValue armor = new BooleanValue("Armor", false);
   @SerializedName("noArmorStands")
   public final BooleanValue noArmorStands = new BooleanValue("NoArmorStands", false);
   @SerializedName("breakParticles")
   public final BooleanValue breakParticles = new BooleanValue("BreakParticles", false);
   @SerializedName("bossbar")
   public final BooleanValue bossbar = new BooleanValue("Bossbar", false);
   @SerializedName("nausea")
   public final BooleanValue nausea = new BooleanValue("Nausea", false);
   @SerializedName("blindness")
   public final BooleanValue blindness = new BooleanValue("Blindness", false);
   @SerializedName("vanillaEffects")
   public final BooleanValue vanillaEffects = new BooleanValue("Vanilla Effects", false);
   @SerializedName("fishingHook")
   public final BooleanValue fishingHook = new BooleanValue("Fishing Hook", true);

   @EventTarget
   public void onX(XEvent xevent) {
      boolean flag = (Boolean)this.potions.getValue();
      boolean flag2 = (Boolean)this.noArmorStands.getValue();
      if (mc2.player != null && mc2.world != null && (flag || flag2)) {
         ArrayList<Entity> list = new ArrayList<>();

         for (Entity entity : mc2.world.getEntities()) {
            if (flag && entity instanceof PotionEntity || flag2 && entity instanceof ArmorStandEntity) {
               list.add(entity);
            }
         }

         for (Entity entity2 : list) {
            mc2.world.removeEntity(entity2.getId(), RemovalReason.KILLED);
         }
      }
   }

   public NoRender() {
      super(Category.Render, "NoRender", "remove rendering");
   }
}
