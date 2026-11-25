package dev.southside.mixin.render;

import dev.southside.module.impl.world.EntityEditor;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.CamelEntityRenderState;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.client.render.entity.state.DonkeyEntityRenderState;
import net.minecraft.client.render.entity.state.IronGolemEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;
import net.minecraft.client.render.entity.state.WolfEntityRenderState;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcherEditor {
   @Shadow
   private Map<EntityType<?>, EntityRenderer<?, ?>> renderers;
   @Shadow
   public Camera camera;

   @Unique
   private LivingEntityRenderState createReplacementRenderState() {
      int n = EntityEditor.computeInt();

      return (LivingEntityRenderState)(switch (n) {
         case 1 -> new SheepEntityRenderState();
         case 2 -> new WolfEntityRenderState();
         case 3 -> new CamelEntityRenderState();
         case 4 -> new DonkeyEntityRenderState();
         case 5 -> new CreeperEntityRenderState();
         case 6 -> new SkeletonEntityRenderState();
         case 7 -> new ZombieEntityRenderState();
         case 8 -> new LivingEntityRenderState();
         case 9 -> new IronGolemEntityRenderState();
         default -> new PigEntityRenderState();
      });
   }

   @Shadow
   public abstract <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T val);

   @Unique
   private void renderReplacementEntity(
      Entity entity, double d, double d2, double d3, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, EntityRenderer entityRenderer
   ) {
      LivingEntityRenderState livingEntityRenderState = this.createReplacementRenderState();
      livingEntityRenderState.x = MathHelper.lerp(f, entity.lastRenderX, entity.getX());
      livingEntityRenderState.y = MathHelper.lerp(f, entity.lastRenderY, entity.getY());
      livingEntityRenderState.z = MathHelper.lerp(f, entity.lastRenderZ, entity.getZ());
      livingEntityRenderState.invisible = entity.isInvisible();
      livingEntityRenderState.age = entity.age + f;
      livingEntityRenderState.width = entity.getWidth();
      livingEntityRenderState.height = entity.getHeight();
      livingEntityRenderState.standingEyeHeight = entity.getStandingEyeHeight();
      livingEntityRenderState.squaredDistanceToCamera = entity.squaredDistanceTo(MinecraftClient.getInstance().gameRenderer.getCamera().getPos());
      livingEntityRenderState.sneaking = entity.isSneaky();
      livingEntityRenderState.onFire = entity.doesRenderOnFire();
      if (entity instanceof LivingEntity living) {
         livingEntityRenderState.bodyYaw = MathHelper.lerpAngleDegrees(f, living.prevBodyYaw, living.bodyYaw);
         livingEntityRenderState.yawDegrees = MathHelper.lerpAngleDegrees(f, living.prevYaw, living.getYaw());
         livingEntityRenderState.pitch = MathHelper.lerp(f, living.prevPitch, living.getPitch());
         livingEntityRenderState.baseScale = living.getScale();
         livingEntityRenderState.ageScale = 1.0F;
         livingEntityRenderState.baby = living.isBaby();
         livingEntityRenderState.hurt = living.hurtTime > 0;
         livingEntityRenderState.deathTime = living.deathTime;
         livingEntityRenderState.limbFrequency = living.limbAnimator.getSpeed(f);
         livingEntityRenderState.limbAmplitudeMultiplier = living.limbAnimator.getPos(f);
         livingEntityRenderState.touchingWater = living.isTouchingWater();
      } else {
         livingEntityRenderState.bodyYaw = MathHelper.lerpAngleDegrees(f, entity.prevYaw, entity.getYaw());
         livingEntityRenderState.yawDegrees = livingEntityRenderState.bodyYaw;
         livingEntityRenderState.pitch = MathHelper.lerp(f, entity.prevPitch, entity.getPitch());
         livingEntityRenderState.baseScale = 1.0F;
         livingEntityRenderState.ageScale = 1.0F;
      }

      if (entity instanceof PlayerEntity player) {
         String text = player.getGameProfile() != null ? player.getGameProfile().getName() : player.getDisplayName().getString();
         livingEntityRenderState.displayName = Text.literal(text);
      } else {
         livingEntityRenderState.displayName = entity.getType().getName();
      }

      Vec3d pointNullable = entity.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getLerpedYaw(f));
      livingEntityRenderState.nameLabelPos = pointNullable;
      matrices.push();

      try {
         Vec3d positionOffset = entityRenderer.getPositionOffset(livingEntityRenderState);
         double d4 = d + positionOffset.getX();
         double d5 = d2 + positionOffset.getY();
         double d6 = d3 + positionOffset.getZ();
         matrices.translate(d4, d5, d6);
         entityRenderer.render(livingEntityRenderState, matrices, vertexConsumers, n);
         matrices.translate(-positionOffset.getX(), -positionOffset.getY(), -positionOffset.getZ());
      } finally {
         matrices.pop();
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private <E extends Entity> void onRenderHead(
      E val, double d, double d2, double d3, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, CallbackInfo ci
   ) {
      if (EntityEditor.shouldReplace(val)) {
         EntityType entityType = EntityEditor.getEntityType();
         EntityRenderer entityRenderer = this.renderers.get(entityType);
         if (entityRenderer != null) {
            try {
               this.renderReplacementEntity(val, d, d2, d3, f, matrices, vertexConsumers, n, entityRenderer);
               ci.cancel();
            } catch (Exception e) {
            }
         }
      }
   }
}
