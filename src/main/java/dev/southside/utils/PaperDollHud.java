package dev.southside.utils;

import cc.polyfrost.oneconfig.hud.Hud;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.UIMatrixStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

public final class PaperDollHud extends Hud {
   private static final float DEFAULT_FLOAT = 64.0F;
   private static final float DEFAULT_FLOAT_VALUE = 60.0F;
   private static final Quaternionf orientation = new Quaternionf().rotationZ((float) Math.PI).rotateX((float) (-Math.PI / 18));
   @SerializedName("alwaysShow")
   public final BooleanValue alwaysShow = new BooleanValue("Always Show", true);
   @SerializedName("renderState")
   private final PlayerEntityRenderState renderState = new PlayerEntityRenderState();
   private static final long DEFAULT_LONG = -4458539562084335376L;

   public PaperDollHud() {
      super(8.0F, 28.0F);
      this.resetPosition();
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      ClientPlayerEntity player = mc.player;
      if (mc.world != null && player != null && !player.isSpectator() && player.isAlive() && !player.isSleeping()) {
         if (flag || !mc.options.hudHidden && ((Boolean)this.alwaysShow.getValue() || isValid(player))) {
            if (mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerEntityRenderer playerEntityRenderer) {
               float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
               playerEntityRenderer.updateRenderState(player, this.renderState, tickDelta);
               float f4 = this.renderState.isGliding ? this.renderState.getGlidingProgress() : this.renderState.leaningPitch;
               float interpolated = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);
               this.renderState.bodyYaw = 205.0F + 40.0F * f4;
               this.renderState.yawDegrees = MathHelper.clamp(MathHelper.wrapDegrees(player.getYaw(tickDelta) - interpolated), -60.0F, 60.0F);
               this.renderState.pitch = MathHelper.clamp(player.getPitch(tickDelta), -65.0F, 65.0F) * (1.0F - f4);
               this.renderState.applyFlyingRotation = false;
               this.renderState.displayName = null;
               this.renderState.playerName = null;
               this.renderState.invisible = false;
               this.renderState.invisibleToPlayer = false;
               this.renderState.hasOutline = false;
               float f5 = 24.0F * f3 / this.renderState.baseScale;
               float f6 = f + (32.0F + (this.renderState.isSwimming ? 0.0F : 18.0F * f4)) * f3;
               float f7 = f2 + (55.0F - 22.0F * f4) * f3;
               context.draw();
               MatrixStack matrices2 = context.getMatrices();
               matrices2.push();

               try {
                  matrices2.translate(f6, f7, 50.0F);
                  matrices2.scale(f5, f5, -f5);
                  matrices2.multiply(orientation);
                  DiffuseLighting.method_34742();
                  context.draw(vertexConsumers -> playerEntityRenderer.render(this.renderState, matrices2, vertexConsumers, (int)DEFAULT_LONG));
               } finally {
                  matrices2.pop();
                  DiffuseLighting.enableGuiDepthLighting();
               }
            }
         }
      }
   }

   private static boolean isValid(ClientPlayerEntity player) {
      return player.limbAnimator.isLimbMoving()
         || player.isSprinting()
         || player.isInSneakingPose()
         || player.isSwimming()
         || player.isGliding()
         || !player.isOnGround()
         || player.isUsingItem()
         || player.handSwinging;
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      return 64.0F * f;
   }

   @Override
   public float setScale(float f, boolean flag) {
      return 60.0F * f;
   }
}
