package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.EntityEvent;
import dev.southside.event.events.EntityRemoveEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.values.BooleanValue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Vec3d;

public class KillEffect extends Module {
   private static final long DEFAULT_LONG_VALUE = 15000L;
   private static final long DEFAULT_LONG_DATA = 5000L;
   @SerializedName("all")
   public final BooleanValue all = new BooleanValue("All", false);
   @SerializedName("mobs")
   public final BooleanValue mobs = new BooleanValue("Mobs", false);
   @SerializedName("attackedTimestamps")
   private final ConcurrentMap<Integer, Long> attackedTimestamps = new ConcurrentHashMap<>();
   @SerializedName("lightningTimestamps")
   private final ConcurrentMap<Integer, Long> lightningTimestamps = new ConcurrentHashMap<>();

   public KillEffect() {
      super(Category.Render, "KillEffect", "Kill special effects");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.addListener(this.all.getLabel(), this::initPostRunnable);
      this.initPostRunnable();
   }

   private void initPostRunnable() {
      this.setInfoText(this.all.getValue() ? "All" : "Attacked");
   }

   @Override
   public void onDisable() {
      this.clear();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.clear();
   }

   private void clear() {
      this.attackedTimestamps.clear();
      this.lightningTimestamps.clear();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.POST && mc2.world != null && mc2.player != null) {
         long now = System.currentTimeMillis();
         this.attackedTimestamps.entrySet().removeIf(entry -> now - entry.getValue() > 15000L);
         this.lightningTimestamps.entrySet().removeIf(entry2 -> now - entry2.getValue() > 5000L);
      }
   }

   @EventTarget
   public void onEntity(EntityEvent entityEvent) {
      if (entityEvent.phase == Stage.POST && this.isValidTarget(entityEvent.entity)) {
         this.attackedTimestamps.put(entityEvent.entity.getId(), System.currentTimeMillis());
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (mc2.world != null && directionEvent.getPacket() instanceof EntityStatusS2CPacket entityStatusS2CPacket && entityStatusS2CPacket.getStatus() == 3) {
         this.updateEntity(entityStatusS2CPacket.getEntity(mc2.world));
      }
   }

   @EventTarget
   public void onEntityEvent2(EntityRemoveEvent entityRemoveEvent) {
      this.updateEntity(entityRemoveEvent.entity);
   }

   private boolean isValidTarget(Entity entity) {
      return mc2.world != null
         && mc2.player != null
         && entity instanceof LivingEntity
         && entity != mc2.player
         && ((Boolean)this.mobs.getValue() || entity instanceof PlayerEntity)
         && (!(entity instanceof PlayerEntity) || !AntiBot.isBot(entity));
   }

   private void updateEntity(Entity entity) {
      if (this.isValidTarget(entity) && ((Boolean)this.all.getValue() || this.attackedTimestamps.containsKey(entity.getId()))) {
         if (this.lightningTimestamps.putIfAbsent(entity.getId(), System.currentTimeMillis()) == null) {
            ClientWorld world = mc2.world;
            Vec3d pos = entity.getPos();
            mc2.execute(() -> {
               if (this.isEnabled() && mc2.world == world) {
                  LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                  lightningEntity.refreshPositionAfterTeleport(pos.x, pos.y, pos.z);
                  world.addEntity(lightningEntity);
               }
            });
         }
      }
   }
}
