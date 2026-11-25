package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public final class WorldTimeController {
   @SerializedName("world")
   private ClientWorld world;
   @SerializedName("serverTimeOfDay")
   private long serverTimeOfDay;
   @SerializedName("serverTicksTimeOfDay")
   private boolean serverTicksTimeOfDay;
   @SerializedName("fastTime")
   private long fastTime;
   @SerializedName("lastFastTickMillis")
   private long lastFastTickMillis;
   @SerializedName("fastMode")
   private boolean fastMode;
   @SerializedName("active")
   private boolean active;

   public void performAction(ClientWorld world2) {
      this.world = world2;
      this.active = false;
      this.fastMode = false;
      this.lastFastTickMillis = System.currentTimeMillis();
      if (world2 != null) {
         this.serverTimeOfDay = world2.getTimeOfDay();
         this.serverTicksTimeOfDay = world2.shouldTickTimeOfDay;
         this.fastTime = this.serverTimeOfDay;
      }
   }

   private void performActionEx(ClientWorld world2) {
      if (this.world != world2) {
         this.performAction(world2);
      }
   }

   public void performAction(ClientWorld world2, long l) {
      this.performActionEx(world2);
      if (world2 != null) {
         if (!this.active) {
            this.performAction(world2);
         }

         this.active = true;
         this.fastMode = false;
         world2.setTime(world2.getTime(), l, false);
      }
   }

   public void performAction(ClientWorld world2, double d) {
      this.performActionEx(world2);
      if (world2 != null) {
         if (!this.active) {
            this.performAction(world2);
         }

         long now = System.currentTimeMillis();
         if (!this.fastMode) {
            this.fastTime = world2.getTimeOfDay();
            this.lastFastTickMillis = now;
            this.fastMode = true;
         }

         long l = Math.max(0L, now - this.lastFastTickMillis);
         this.lastFastTickMillis = now;
         this.fastTime += (long)(l / 50.0 * d * 20.0);
         this.active = true;
         world2.setTime(world2.getTime(), this.fastTime, false);
      }
   }

   public void update(ClientWorld world2, WorldTimeUpdateS2CPacket worldTimeUpdateS2CPacket) {
      this.performActionEx(world2);
      if (world2 != null) {
         this.serverTimeOfDay = worldTimeUpdateS2CPacket.timeOfDay();
         this.serverTicksTimeOfDay = worldTimeUpdateS2CPacket.tickDayTime();
         world2.setTime(worldTimeUpdateS2CPacket.time(), this.active ? world2.getTimeOfDay() : this.serverTimeOfDay, !this.active && this.serverTicksTimeOfDay);
      }
   }

   public void performActionAlt(ClientWorld world2) {
      if (this.active && this.world != null && this.world == world2) {
         this.world.setTime(this.world.getTime(), this.serverTimeOfDay, this.serverTicksTimeOfDay);
      }

      this.active = false;
      this.fastMode = false;
      this.world = null;
   }
}
