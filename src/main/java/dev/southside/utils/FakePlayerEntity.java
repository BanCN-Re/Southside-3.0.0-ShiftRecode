package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;

public class FakePlayerEntity extends OtherClientPlayerEntity {
   @SerializedName("spawned")
   public boolean spawned;
   @SerializedName("targeted")
   public boolean targeted;

   public FakePlayerEntity(PlayerEntity player, String text, float f, boolean flag) {
      super(IMinecraft.mc.world, new GameProfile(UUID.randomUUID(), text));
      this.copyPositionAndRotation(player);
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
      this.headYaw = player.headYaw;
      this.prevHeadYaw = this.headYaw;
      this.bodyYaw = player.bodyYaw;
      this.prevBodyYaw = this.bodyYaw;
      Byte val = (Byte)player.getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS);
      this.dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, val);
      this.getAttributes().setFrom(player.getAttributes());
      this.setPose(player.getPose());
      this.capeX = this.getX();
      this.capeY = this.getY();
      this.capeZ = this.getZ();
      if (f <= 20.0F) {
         this.setHealth(f);
      } else {
         this.setHealth(f);
         this.setAbsorptionAmount(f - 20.0F);
      }

      if (flag) {
         this.getInventory().clone(player.getInventory());
      }
   }

   public void addEntity() {
      if (IMinecraft.mc.world != null) {
         this.unsetRemoved();
         IMinecraft.mc.world.addEntity(this);
      }
   }

   public void performAction() {
      if (IMinecraft.mc.world != null) {
         IMinecraft.mc.world.removeEntity(this.getId(), RemovalReason.DISCARDED);
         this.setRemoved(RemovalReason.DISCARDED);
      }
   }
}
