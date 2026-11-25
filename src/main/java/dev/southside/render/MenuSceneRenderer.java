package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.utils.MenuIslandLandscape;
import dev.southside.utils.MenuBlockParticles;
import dev.southside.utils.MenuPlayerPreview;
import dev.southside.utils.misc.StartRecord;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public final class MenuSceneRenderer {
   public static final MenuSceneRenderer INSTANCE = new MenuSceneRenderer();
   private static final Vector3f vector = new Vector3f(0.55F, -0.85F, 0.8F).normalize();
   private static final Vector3f vectorValue = new Vector3f(-0.7F, 0.15F, -0.8F).normalize();
   private static final float DEFAULT_FLOAT = 0.074F;
   private static final float DEFAULT_FLOAT_VALUE = 0.05F;
   private static final Quaternionf orientation = RotationAxis.POSITIVE_X.rotationDegrees(12.0F).mul(RotationAxis.POSITIVE_Y.rotationDegrees(-25.0F));
   @SerializedName("sky")
   private final MenuSkyRenderer sky = new MenuSkyRenderer();
   @SerializedName("landscape")
   private final MenuIslandLandscape landscape = new MenuIslandLandscape();
   @SerializedName("blockParticles")
   private final MenuBlockParticles blockParticles = new MenuBlockParticles(this.landscape);
   @SerializedName("player")
   private final MenuPlayerPreview player = new MenuPlayerPreview();
   @SerializedName("inverseScene")
   private final Matrix4f inverseScene = new Matrix4f();
   @SerializedName("epoch")
   private final long epoch = Util.getMeasuringTimeMs();
   @SerializedName("lastFrame")
   private long lastFrame;
   @SerializedName("sceneValid")
   private boolean sceneValid;
   @SerializedName("blockPointerCaptured")
   private boolean blockPointerCaptured;

   private MenuSceneRenderer() {
   }

   public void render(DrawContext context, float f, float f2, boolean flag) {
      this.sceneValid = false;
      Window window = MinecraftClient.getInstance().getWindow();
      int scaledWidth = window.getScaledWidth();
      int scaledHeight = window.getScaledHeight();
      if (scaledWidth > 0 && scaledHeight > 0) {
         long now = Util.getMeasuringTimeMs();
         float f3 = (float)(now - this.epoch) / 1000.0F;
         float f4 = this.lastFrame == 0L ? 0.0F : Math.min(0.05F, (float)(now - this.lastFrame) / 1000.0F);
         this.lastFrame = now;
         this.player.performAction(f, f2, true, now, f4);
         this.blockParticles.update(now);
         context.draw();
         this.sky.renderSky(context, scaledWidth, scaledHeight, f3);
         float f5 = Math.min(scaledHeight * 0.074F, scaledWidth * 0.05F);
         this.render(context, scaledWidth, scaledHeight, f3, now, f5, f, f2, flag);
         this.player.render(context, now);
      } else {
         this.player.performAction();
         this.blockPointerCaptured = false;
         this.blockParticles.clear();
      }
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void render(DrawContext context, int n, int n2, float f, long l, float f2, float f3, float f4, boolean flag) {
      context.draw();
      float[] shaderColor = RenderSystem.getShaderColor();
      float f5 = shaderColor[0];
      float f6 = shaderColor[1];
      float f7 = shaderColor[2];
      float f8 = shaderColor[3];
      boolean flag2 = GL11.glIsEnabled(2929);
      boolean flag3 = GL11.glGetBoolean(2930);
      int n3 = GL11.glGetInteger(2932);
      boolean flag4 = GL11.glIsEnabled(2884);
      boolean flag5 = GL11.glIsEnabled(3042);
      int n4 = GL11.glGetInteger(32969);
      int n5 = GL11.glGetInteger(32968);
      int n6 = GL11.glGetInteger(32971);
      int n7 = GL11.glGetInteger(32970);
      MatrixStack matrices = new MatrixStack();
      boolean flag6 = false /* VF: Semaphore variable */;

      try {
         flag6 = true;
         RenderSystem.depthMask(true);
         RenderSystem.clear(256);
         short s = 27151;
         RenderSystem.enableDepthTest();
         long l2 = 638273727368058102L;
         RenderSystem.depthFunc(515);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShaderLights(vector, vectorValue);
         matrices.translate(n * 0.75F, n2 * 0.6F, 120.0F);
         matrices.scale(f2, -f2, f2);
         matrices.multiply(orientation);
         this.inverseScene.set(matrices.peek().getPositionMatrix()).invert();
         this.sceneValid = this.inverseScene.isFinite();
         context.draw(vertexConsumers -> {
            this.landscape.render(matrices, vertexConsumers);
            this.player.render(matrices, vertexConsumers, f, l, f2);
         });
         context.draw(vertexConsumers2 -> this.landscape.render(matrices, vertexConsumers2, this.player.getVisibility()));
         MenuIslandLandscape.Entry2 entry2 = flag ? this.getEntry2(f3, f4) : null;
         if (entry2 != null) {
            this.render(context, matrices, entry2.getBlock(), f2);
         }

         context.draw(vertexConsumers3 -> this.blockParticles.render(matrices, vertexConsumers3, orientation));
         s = 8420;
         flag6 = false;
      } finally {
         if (flag6) {
            DiffuseLighting.disableGuiDepthLighting();
            short s2 = 8420;
            RenderSystem.setShaderColor(f5, f6, f7, f8);
            long l3 = 6884271752440946199L;
            RenderSystem.depthMask(true);
            RenderSystem.clear(256);
            RenderSystem.depthMask(flag3);
            RenderSystem.depthFunc(n3);
            if (flag2) {
               RenderSystem.enableDepthTest();
            } else {
               RenderSystem.disableDepthTest();
            }

            if (flag4) {
               RenderSystem.enableCull();
            } else {
               RenderSystem.disableCull();
            }

            RenderSystem.blendFuncSeparate(n4, n5, n6, n7);
            if (flag5) {
               RenderSystem.enableBlend();
            } else {
               RenderSystem.disableBlend();
            }
         }
      }

      DiffuseLighting.disableGuiDepthLighting();
      long l4 = 6884271752440946199L;
      RenderSystem.setShaderColor(f5, f6, f7, f8);
      RenderSystem.depthMask(true);
      RenderSystem.clear(256);
      RenderSystem.depthMask(flag3);
      RenderSystem.depthFunc(n3);
      if (flag2) {
         RenderSystem.enableDepthTest();
      } else {
         RenderSystem.disableDepthTest();
      }

      if (flag4) {
         RenderSystem.enableCull();
      } else {
         RenderSystem.disableCull();
      }

      RenderSystem.blendFuncSeparate(n4, n5, n6, n7);
      if (flag5) {
         RenderSystem.enableBlend();
      } else {
         RenderSystem.disableBlend();
      }
   }

   private void render(DrawContext context, MatrixStack matrices, MenuIslandLandscape.Entry entry, float f) {
      Matrix4f matrix = new Matrix4f(RenderSystem.getProjectionMatrix());
      ProjectionType projectionType = RenderSystem.getProjectionType();

      try {
         RenderSystem.setProjectionMatrix(new Matrix4f(matrix).translate(0.0F, 0.0F, f * 0.002F).scale(1.0039216F), projectionType);
         context.draw(vertexConsumers -> this.landscape.drawOutline(matrices, vertexConsumers, entry));
      } finally {
         RenderSystem.setProjectionMatrix(matrix, projectionType);
      }
   }

   public boolean isValid(double d, double d2, int n) {
      if (n == 0 && this.sceneValid) {
         MenuIslandLandscape.Entry2 entry2 = this.getEntry2(d, d2);
         if (entry2 != null && this.landscape.isValid(entry2.getBlock())) {
            this.blockParticles.performAction(entry2.getBlock(), Util.getMeasuringTimeMs());
            this.blockPointerCaptured = true;
            return true;
         } else {
            return this.player.isValid(d, d2, n);
         }
      } else {
         return false;
      }
   }

   private MenuIslandLandscape.Entry2 getEntry2(double x, double y) {
      if (!this.sceneValid) {
         return null;
      }

      MenuIslandLandscape.Entry2 hitResult = this.landscape.raycast(StartRecord.getStartRecord(this.inverseScene, x, y));
      return hitResult != null && hitResult.getFraction() < this.player.computeDouble(x, y) ? hitResult : null;
   }

   public boolean isValid(double x, double y) {
      return this.blockPointerCaptured ? true : this.player.isValid(x, y);
   }

   public boolean isKeyDown(int n) {
      if (n == 0 && this.blockPointerCaptured) {
         this.blockPointerCaptured = false;
         return true;
      } else {
         return this.player.isKeyDown(n);
      }
   }

   public void performAction() {
      this.player.performAction();
      this.sceneValid = false;
      this.blockPointerCaptured = false;
      this.blockParticles.clear();
      this.landscape.performAction();
      this.lastFrame = 0L;
   }
}
