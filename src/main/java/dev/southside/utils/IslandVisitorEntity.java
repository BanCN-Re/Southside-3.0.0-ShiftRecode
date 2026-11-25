package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IslandVisitorEntity extends OtherClientPlayerEntity {
   private static final float DEFAULT_FLOAT = 0.22F;
   private static final float DEFAULT_FLOAT_VALUE = 70.0F;
   private static final float DEFAULT_FLOAT_DATA = 35.0F;
   private static final long DEFAULT_LONG = 1200L;
   private static final float DEFAULT_FLOAT_REF = 0.032F;
   private static final long DEFAULT_LONG_VALUE = 220L;
   private static final long DEFAULT_LONG_DATA = 1700L;
   private static final long DEFAULT_LONG_REF = 900L;
   private static final int DEFAULT_INT = 3;
   private static final long DEFAULT_LONG_PRIMARY = 220L;
   private static final long DEFAULT_LONG_SECONDARY = 4100L;
   private static final PlayerSkinProfile profileUtil = new PlayerSkinProfile();
   private static volatile int count = -1;
   @SerializedName("homeX")
   private final double homeX;
   @SerializedName("homeY")
   private final double homeY;
   @SerializedName("homeZ")
   private final double homeZ;
   @SerializedName("restYaw")
   private final float restYaw;
   @SerializedName("seatedSkin")
   private SkinTextures seatedSkin;
   @SerializedName("seatedSkinTexture")
   private Identifier seatedSkinTexture;
   @SerializedName("nextTouchAt")
   private long nextTouchAt;
   @SerializedName("chewUntil")
   private long chewUntil;
   @SerializedName("heartsUntil")
   private long heartsUntil;
   @SerializedName("nextHeartAt")
   private long nextHeartAt;
   @SerializedName("damageUntil")
   private long damageUntil;
   @SerializedName("nextDamageAt")
   private long nextDamageAt;
   @SerializedName("nectarUntil")
   private long nectarUntil;
   @SerializedName("nextNectarAt")
   private long nextNectarAt;

   public IslandVisitorEntity(ClientWorld world, GameProfile gameProfile, double d, double d2, double d3, float f) {
      super(world, new GameProfile(UUID.randomUUID(), gameProfile.getName()));
      this.homeX = d;
      this.homeY = d2;
      this.homeZ = d3;
      this.restYaw = f;
      this.setPosition(d, d2, d3);
      this.setYaw(f);
      this.prevYaw = f;
      this.bodyYaw = f;
      this.prevBodyYaw = f;
      this.headYaw = f;
      this.prevHeadYaw = f;
      this.setPitch(0.0F);
      this.prevPitch = 0.0F;
      this.setNoGravity(true);
      this.setInvulnerable(true);
      this.setSilent(true);
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         this.getDataTracker().set(PlayerEntity.PLAYER_MODEL_PARTS, (Byte)player.getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS));
      }

      count = this.getId();
   }

   public static boolean isKeyDown(int n) {
      int n2 = count;
      return n2 >= 0 && n == n2;
   }

   public SkinTextures getSkinTextures() {
      SkinTextures skinTextures = profileUtil.getSkinTextures();
      if (this.seatedSkin == null || !skinTextures.texture().equals(this.seatedSkinTexture)) {
         this.seatedSkinTexture = skinTextures.texture();
         this.seatedSkin = skinTextures.model() == Model.SLIM
            ? skinTextures
            : new SkinTextures(skinTextures.texture(), skinTextures.textureUrl(), skinTextures.capeTexture(), skinTextures.elytraTexture(), Model.SLIM, skinTextures.secure());
      }

      return this.seatedSkin;
   }

   public void tick() {
      super.tick();
      this.setVelocity(Vec3d.ZERO);
      this.setPosition(this.homeX, this.homeY, this.homeZ);
      this.setYaw(this.restYaw);
      this.setBodyYaw(this.restYaw);
      this.bodyYaw = this.restYaw;
      this.performAction();
      long now = System.currentTimeMillis();
      if (now < this.heartsUntil && now >= this.nextHeartAt) {
         this.performActionEx();
         this.nextHeartAt = now + 220L;
      }

      if (now >= this.nextDamageAt && now < this.damageUntil) {
         this.hurtTime = 10;
         this.maxHurtTime = 10;

         for (int i = 0; i < 4; i++) {
            this.performActionAlt();
         }

         this.nextDamageAt = now + 900L;
      }

      if (now < this.nectarUntil && now >= this.nextNectarAt) {
         this.performActionAlt();
         this.nextNectarAt = now + 220L;
      }
   }

   private void performAction() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null && (Object)player != this) {
         Vec3d eyePos = this.getEyePos();
         Vec3d eyePos2 = player.getEyePos();
         double dx = eyePos2.x - eyePos.x;
         double dy = eyePos2.y - eyePos.y;
         double dz = eyePos2.z - eyePos.z;
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (!(dist < 0.01) || !(Math.abs(dy) < 0.01)) {
            float f = (float)(MathHelper.atan2(dz, dx) * 57.29577951308232) - 90.0F;
            float f2 = (float)(-(MathHelper.atan2(dy, dist) * 57.29577951308232));
            float wrapped = MathHelper.wrapDegrees(f - this.restYaw);
            wrapped = MathHelper.clamp(wrapped, -70.0F, 70.0F);
            float f3 = this.restYaw + wrapped;
            float wrapped2 = MathHelper.wrapDegrees(f3 - this.headYaw);
            this.headYaw += wrapped2 * 0.22F;
            float clamped = MathHelper.clamp(f2, -35.0F, 35.0F);
            float f4 = this.getPitch() + (clamped - this.getPitch()) * 0.22F;
            long now = System.currentTimeMillis();
            if (now < this.chewUntil) {
               float f5 = Math.min(1.0F, (float)(this.chewUntil - now) / 250.0F);
               f4 += MathHelper.sin((float)(now % 4000L) * 0.032F) * 14.0F * f5;
            }

            this.setPitch(MathHelper.clamp(f4, -80.0F, 60.0F));
         }
      }
   }

   private void performActionEx() {
      if (this.getWorld() instanceof ClientWorld world) {
         ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
         world.addParticle(
            ParticleTypes.HEART,
            this.getX() + (threadLocalRandom.nextDouble() - 0.5) * 0.35,
            this.getY() + 1.25 + threadLocalRandom.nextDouble() * 0.15,
            this.getZ() + (threadLocalRandom.nextDouble() - 0.5) * 0.35,
            0.0,
            0.02,
            0.0
         );
      }
   }

   private void performActionAlt() {
      ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
      RainDropEffect.onMouseClick(this.getX() + (threadLocalRandom.nextDouble() - 0.5) * 0.3, this.getY() + 0.78, this.getZ() + 0.15 + (threadLocalRandom.nextDouble() - 0.5) * 0.3);
   }

   public void performActionImpl() {
      if (count == this.getId()) {
         count = -1;
      }

      this.discard();
   }

   public static void performAction(IslandVisitorEntity islandVisitorEntity, PlayerEntity player) {
      World world = islandVisitorEntity.getWorld();
      if (world != null) {
         long now = System.currentTimeMillis();
         if (now >= islandVisitorEntity.nextTouchAt) {
            islandVisitorEntity.nextTouchAt = now + 1200L;
            islandVisitorEntity.chewUntil = now + 1100L;
            islandVisitorEntity.heartsUntil = now + 1700L;
            islandVisitorEntity.nextHeartAt = now;
            islandVisitorEntity.nextDamageAt = now + 1700L;
            islandVisitorEntity.damageUntil = now + 1700L + 1800L + 500L;
            islandVisitorEntity.nextNectarAt = now + 1700L;
            islandVisitorEntity.nectarUntil = now + 4100L;
            world.playSound(player, islandVisitorEntity.getX(), islandVisitorEntity.getY() + 0.8, islandVisitorEntity.getZ(), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 1.0F, 1.02F);
            CustomParticleRenderer.performAction(islandVisitorEntity.getSkinTextures().texture(), islandVisitorEntity.getX(), islandVisitorEntity.getY() + 0.75, islandVisitorEntity.getZ() + 0.1);
            Southside.LOGGER.info("The menu visitor was touched by {}", player.getName().getString());
         }
      }
   }
}
