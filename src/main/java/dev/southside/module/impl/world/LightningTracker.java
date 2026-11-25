package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DirectionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.EaseInOutCubicAnimation;
import dev.southside.utils.OneConfigScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;

public class LightningTracker extends Module {
   private static final int DEFAULT_INT = 10000;
   @SerializedName("useAddEntity")
   public final BooleanValue useAddEntity = new BooleanValue("Use AddEntity", false);
   @SerializedName("useSound")
   public final BooleanValue useSound = new BooleanValue("Use Sound", false);

   public LightningTracker() {
      super(Category.World, "LightningTracker");
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if ((Boolean)this.useAddEntity.getValue() && directionEvent.getPacket() instanceof EntitySpawnS2CPacket entitySpawnS2CPacket && entitySpawnS2CPacket.getEntityType() == EntityType.LIGHTNING_BOLT) {
         this.onMouseClick(entitySpawnS2CPacket.getX(), entitySpawnS2CPacket.getY(), entitySpawnS2CPacket.getZ());
      }

      if ((Boolean)this.useSound.getValue()
         && directionEvent.getPacket() instanceof PlaySoundS2CPacket playSoundS2CPacket
         && (playSoundS2CPacket.getSound().equals(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER) || playSoundS2CPacket.getSound().equals(SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT))) {
         this.onMouseClick(playSoundS2CPacket.getX(), playSoundS2CPacket.getY(), playSoundS2CPacket.getZ());
      }
   }

   private void onMouseClick(double x, double y, double z) {
      String formatted = String.format("[Lightning Tracker] X=%.1f, Y=%.1f, Z=%.1f", x, y, z);
      this.applyTextCustom(formatted);
      if (Module.mc.world != null || Module.mc.currentScreen instanceof OneConfigScreen) {
         EaseInOutCubicAnimation easeInOutCubicAnimation = new EaseInOutCubicAnimation(10000, 0.0F, 1.0F, false);
         Notifications.INSTANCE.send("LightningTracker", formatted, Notification.iconUtil4, 10000.0F, () -> Notification.computeFloat(easeInOutCubicAnimation));
      }
   }
}
