package dev.southside.mixin.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.Southside;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.impl.render.MotionBlur;
import dev.southside.module.impl.render.NoRender;
import dev.southside.module.impl.render.RainLove;
import dev.southside.render.ProjectionUtils;
import dev.southside.render.GlStateTracker;
import dev.southside.render.MSAA;
import dev.southside.utils.RaytraceUtils;
import dev.southside.utils.WorldBoxRenderer;
import dev.southside.utils.RotationManager;
import dev.southside.utils.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 4514)
public abstract class MixinGameRenderer {
   @Shadow
   @Final
   MinecraftClient client;
   @Shadow
   private long lastWindowFocusedTime;
   @Shadow
   private boolean postProcessorEnabled;
   @Shadow
   @Final
   private BufferBuilderStorage buffers;
   @Shadow
   @Final
   private Camera camera;
   @Unique
   private Matrix4f worldRenderMatrix;

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;beginWrite(Z)V", shift = Shift.AFTER))
   private void blurWorld(RenderTickCounter renderTickCounter, boolean flag, CallbackInfo ci) {
      Southside southside = Southside.INSTANCE;
      if (southside != null && southside.getModuleManager() != null) {
         southside.getModuleManager().getModule(MotionBlur.class).render();
      }
   }

   @Inject(method = {"reset", "close"}, at = @At("HEAD"))
   private void releaseMotionBlur(CallbackInfo ci) {
      Southside southside = Southside.INSTANCE;
      if (southside != null && southside.getModuleManager() != null) {
         southside.getModuleManager().getModule(MotionBlur.class).renderEx();
      }
   }

   @Shadow
   protected abstract void tiltViewWhenHurt(MatrixStack matrices, float f);

   @Redirect(
      method = "Lnet/minecraft/client/render/GameRenderer;findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;")
   )
   private HitResult redirectFindCrosshairTarget(Entity entity, double d, float f, boolean flag) {
      if (entity != this.client.player) {
         return entity.raycast(d, f, flag);
      }

      Rotation currentRotation = RotationManager.getCurrentRotation();
      if (currentRotation == null) {
         currentRotation = new Rotation(entity.getYaw(f), entity.getPitch(f));
      }

      Entity cameraEntity = RaytraceUtils.mc.cameraEntity;
      if (cameraEntity == null) {
         return null;
      }

      Vec3d cameraPosVec = RaytraceUtils.mc.player.getCameraPosVec(f);
      float f2 = MathHelper.cos(-currentRotation.yaw * (float) (Math.PI / 180.0));
      float f3 = MathHelper.sin(-currentRotation.yaw * (float) (Math.PI / 180.0));
      float f4 = MathHelper.cos(currentRotation.pitch * (float) (Math.PI / 180.0));
      float f5 = MathHelper.sin(currentRotation.pitch * (float) (Math.PI / 180.0));
      Vec3d vec = new Vec3d(f3 * f4, -f5, f2 * f4);
      Vec3d vec2 = cameraPosVec.add(vec.x * d, vec.y * d, vec.z * d);
      FluidHandling fluidHandling = flag ? FluidHandling.ANY : FluidHandling.NONE;
      return RaytraceUtils.mc.world.raycast(new RaycastContext(cameraPosVec, vec2, ShapeType.OUTLINE, fluidHandling, cameraEntity));
   }

   @Redirect(
      method = "Lnet/minecraft/client/render/GameRenderer;renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F")
   )
   private float suppressNauseaInterpolation(float x, float y, float z) {
      NoRender noRender = Southside.INSTANCE.getModuleManager().getModule(NoRender.class);
      return noRender.isEnabled() && noRender.nausea.getValue() ? 0.0F : MathHelper.lerp(x, y, z);
   }

   @Redirect(
      method = "Lnet/minecraft/client/render/GameRenderer;renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"
      )
   )
   private void captureWorldRenderMatrix(
      WorldRenderer worldRenderer, ObjectAllocator objectAllocator, RenderTickCounter renderTickCounter, boolean flag, Camera camera2, GameRenderer gameRenderer, Matrix4f matrix, Matrix4f matrix2
   ) {
      this.worldRenderMatrix = matrix;
      RainLove rainLove = Southside.INSTANCE.getModuleManager().getModule(RainLove.class);
      rainLove.setRenderingWorld(true);

      try {
         worldRenderer.render(objectAllocator, renderTickCounter, flag, camera2, gameRenderer, matrix, matrix2);
      } finally {
         rainLove.setRenderingWorld(false);
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/render/GameRenderer;renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V",
      at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand")
   )
   private void renderWorldOverlays(RenderTickCounter renderTickCounter, CallbackInfo ci) {
      GlStateTracker.glStateNode = null;
      GlStateTracker.GL_STATE_NODE.depthTestEnabled = GL11C.glIsEnabled(2929);
      GlStateTracker.GL_STATE_NODE.blendEnabled = GL11C.glIsEnabled(3042);
      GlStateTracker.GL_STATE_NODE.cullEnabled = GL11C.glIsEnabled(2884);
      GlStateTracker.GL_STATE_NODE.scissorEnabled = GL11C.glIsEnabled(3089);
      MatrixStack matrices = new MatrixStack();
      MSAA.runTask(() -> {
         matrices.push();
         WorldBoxRenderer.render(matrices);
         CameraEvent cameraEvent = new CameraEvent(this.camera, matrices, renderTickCounter.getTickDelta(false));
         Southside.INSTANCE.getEventBus().dispatch(cameraEvent);
         WorldBoxRenderer.performAction();
         matrices.pop();
      });
      GlStateTracker.glStateNode = null;
      if (GlStateTracker.GL_STATE_NODE.depthTestEnabled) {
         GlStateManager._enableDepthTest();
      } else {
         GlStateManager._disableDepthTest();
      }

      if (GlStateTracker.GL_STATE_NODE.blendEnabled) {
         GlStateManager._enableBlend();
      } else {
         GlStateManager._disableBlend();
      }

      if (GlStateTracker.GL_STATE_NODE.cullEnabled) {
         GlStateManager._enableCull();
      } else {
         GlStateManager._disableCull();
      }

      if (GlStateTracker.GL_STATE_NODE.scissorEnabled) {
         GlStateManager._enableScissorTest();
      } else {
         GlStateManager._disableScissorTest();
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(I)V", shift = Shift.BEFORE, ordinal = 1)
   )
   public void renderScreenOverlays(RenderTickCounter renderTickCounter, boolean flag, CallbackInfo ci) {
      DrawContext context = new DrawContext(this.client, this.buffers.getEntityVertexConsumers());
      DrawContextEvent drawContextEvent = new DrawContextEvent(context, renderTickCounter.getTickDelta(false));
      Southside.INSTANCE.getEventBus().dispatch(drawContextEvent);
      context.draw();
   }

   @Inject(
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = 180, ordinal = 0),
      method = "Lnet/minecraft/client/render/GameRenderer;renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V"
   )
   void onRenderWorldField(RenderTickCounter renderTickCounter, CallbackInfo ci) {
      ProjectionUtils.performAction(this.worldRenderMatrix, RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix());
   }

   @Inject(
      method = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onTiltViewWhenHurtHead(MatrixStack matrices, float f, CallbackInfo ci) {
      NoRender noRender = Southside.INSTANCE.getModuleManager().getModule(NoRender.class);
      if (noRender.isEnabled() && noRender.hurtCam.getValue()) {
         ci.cancel();
      }
   }

   @Shadow
   protected abstract void bobView(MatrixStack matrices, float f);

   @Shadow
   public abstract void tick();
}
