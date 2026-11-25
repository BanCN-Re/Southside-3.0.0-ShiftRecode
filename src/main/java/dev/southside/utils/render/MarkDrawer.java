package dev.southside.utils.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.module.impl.client.Theme;
import dev.southside.utils.MillisTimer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class MarkDrawer {
   public static MinecraftClient mc = MinecraftClient.getInstance();
   public static float factor;
   public static float factorValue;
   public static MillisTimer lastTimeUtil = new MillisTimer();
   public static float factorData = 1.0F;
   public static float factorRef;
   public static float factorPrimary = 1.0F;
   public static boolean active;

   public static void render(BufferBuilder bufferBuilder) {
      BuiltBuffer builtBuffer = bufferBuilder.endNullable();
      if (builtBuffer != null) {
         BufferRenderer.drawWithGlobalProgram(builtBuffer);
      }
   }

   public static double computeDouble(double d) {
      return Math.abs(1.0 + Math.sin(d)) / 2.0;
   }

   public static float getTickDelta() {
      return mc.getRenderTickCounter().getTickDelta(false);
   }

   public static void disableBlend() {
      RenderSystem.disableBlend();
   }

   public static void performAction() {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
   }

   public static double computeDouble(double x, double y, double z) {
      return x + (y - x) * z;
   }

   public static float computeFloat(float x, float y, double z) {
      return (float)computeDouble(x, y, (float)z);
   }

   public static Color blendColor(Color color, int n) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(n, 0, 255));
   }

   public static Color blendColor(Color color, float f) {
      f = Math.min(1.0F, Math.max(0.0F, f));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * f));
   }

   public static void renderTH(int n, int n2, float f, float f2, Entity entity) {
      Camera camera = mc.gameRenderer.getCamera();
      double d = computeDouble(entity.prevX, entity.getX(), getTickDelta()) - camera.getPos().x;
      double d2 = computeDouble(entity.prevY, entity.getY(), getTickDelta()) - camera.getPos().y;
      double d3 = computeDouble(entity.prevZ, entity.getZ(), getTickDelta()) - camera.getPos().z;
      float f3 = (float)computeDouble(entity.age - 1, entity.age, getTickDelta());
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, Identifier.of("southside", "hud/firefly.png"));
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      boolean canSee = mc.player.canSee(entity);
      if (canSee) {
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
      } else {
         RenderSystem.disableDepthTest();
      }

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j <= n; j++) {
            double d4 = Math.toRadians(((j / 1.5F + f3) * n2 + i * 120) % (n2 * 360));
            double d5 = Math.sin(Math.toRadians(f3 * 2.5F + j * (i + 1)) * f2) / f;
            float f4 = (float)j / n;
            MatrixStack matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(d + Math.cos(d4) * entity.getWidth(), d2 + 1.0 + d5, d3 + Math.sin(d4) * entity.getWidth());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            int n3 = (int)(180.0F * f4);
            int rgb = blendColor(Theme.blendColor(n3), f4).getRGB();
            float f5 = Math.max(0.24F * f4, 0.2F);
            bufferBuilder.vertex(positionMatrix, -f5, f5, 0.0F).texture(0.0F, 1.0F).color(rgb);
            bufferBuilder.vertex(positionMatrix, f5, f5, 0.0F).texture(1.0F, 1.0F).color(rgb);
            bufferBuilder.vertex(positionMatrix, f5, -f5, 0.0F).texture(1.0F, 0.0F).color(rgb);
            bufferBuilder.vertex(positionMatrix, -f5, -f5, 0.0F).texture(0.0F, 0.0F).color(rgb);
         }
      }

      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      if (canSee) {
         RenderSystem.depthMask(true);
         RenderSystem.disableDepthTest();
      } else {
         RenderSystem.enableDepthTest();
      }

      RenderSystem.disableBlend();
   }

   public static void updateEntity(Entity entity) {
      Camera camera = mc.gameRenderer.getCamera();
      double d = computeDouble(entity.prevX, entity.getX(), getTickDelta()) - camera.getPos().x;
      double d2 = computeDouble(entity.prevY, entity.getY(), getTickDelta()) - camera.getPos().y;
      double d3 = computeDouble(entity.prevZ, entity.getZ(), getTickDelta()) - camera.getPos().z;
      MatrixStack matrices = new MatrixStack();
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
      matrices.translate(d, d2 + entity.getEyeHeight(entity.getPose()) / 2.0F, d3);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(computeFloat(factorRef, factorData, getTickDelta())));
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, Identifier.of("southside", "hud/capture.png"));
      matrices.translate(-0.75, -0.75, -0.01);
      Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      bufferBuilder.vertex(positionMatrix, 0.0F, 1.5F, 0.0F).texture(0.0F, 1.0F).color(Theme.blendColor(90).getRGB());
      bufferBuilder.vertex(positionMatrix, 1.5F, 1.5F, 0.0F).texture(1.0F, 1.0F).color(Theme.blendColor(0).getRGB());
      bufferBuilder.vertex(positionMatrix, 1.5F, 0.0F, 0.0F).texture(1.0F, 0.0F).color(Theme.blendColor(180).getRGB());
      bufferBuilder.vertex(positionMatrix, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(Theme.blendColor(270).getRGB());
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static void render(MatrixStack matrices, Entity entity) {
      double d = factor + (factorValue - factor) * getTickDelta();
      double d2 = computeDouble(d - 0.45F);
      double d3 = computeDouble(d);
      double d4 = entity.prevX + (entity.getX() - entity.prevX) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
      double denom = entity.prevY + (entity.getY() - entity.prevY) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + d2 * entity.getHeight();
      double d5 = entity.prevZ + (entity.getZ() - entity.prevZ) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
      double denom2 = entity.prevY + (entity.getY() - entity.prevY) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + d3 * entity.getHeight();
      matrices.push();
      performAction();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

      for (int i = 0; i <= 30; i++) {
         float f = (float)(d4 + Math.cos(i * 6.28 / 30.0) * entity.getWidth() * 0.8);
         float f2 = (float)(d5 + Math.sin(i * 6.28 / 30.0) * entity.getWidth() * 0.8);
         bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, (float)denom2, f2).color(blendColor(Theme.blendColor(i), 170).getRGB());
         bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, (float)denom, f2).color(blendColor(Theme.blendColor(i), 0).getRGB());
      }

      render(bufferBuilder);
      RenderSystem.enableCull();
      disableBlend();
      RenderSystem.enableDepthTest();
      matrices.pop();
   }

   public static void renderEx(MatrixStack matrices, @NotNull Entity entity) {
      ArrayList list = new ArrayList();
      ArrayList list2 = new ArrayList();
      ArrayList list3 = new ArrayList();
      double d = entity.prevX + (entity.getX() - entity.prevX) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
      double d2 = entity.prevY + (entity.getY() - entity.prevY) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
      double d3 = entity.prevZ + (entity.getZ() - entity.prevZ) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
      double height = entity.getHeight();

      for (int i = 0; i <= 361; i++) {
         double d4 = Math.sin(Math.toRadians(i));
         double d5 = Math.cos(Math.toRadians(i));
         list.add(new Vec3d((float)(d5 * 0.5), height, (float)(d4 * 0.5)));
         double d6 = Math.sin(Math.toRadians((i + 120) % 360));
         double d7 = Math.cos(Math.toRadians(i + 120) % 360.0);
         list2.add(new Vec3d((float)(d7 * 0.5), height, (float)(d6 * 0.5)));
         double d8 = Math.sin(Math.toRadians((i + 240) % 360));
         double d9 = Math.cos(Math.toRadians((i + 240) % 360));
         list3.add(new Vec3d((float)(d9 * 0.5), height, (float)(d8 * 0.5)));
         height -= 0.004F;
      }

      matrices.push();
      matrices.translate(d, d2, d3);
      performAction();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
      performAction(bufferBuilder, positionMatrix, list);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
      performAction(bufferBuilder, positionMatrix, list2);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
      performAction(bufferBuilder, positionMatrix, list3);
      RenderSystem.enableCull();
      matrices.translate(-d, -d2, -d3);
      disableBlend();
      RenderSystem.enableDepthTest();
      matrices.pop();
   }

   private static void performAction(BufferBuilder bufferBuilder, Matrix4f matrix, List<Vec3d> list) {
      for (int i = 0; i < list.size() - 1; i++) {
         float f = 1.0F - (i + (float)lastTimeUtil.getElapsed() / 5.0F) % 360.0F / 60.0F;
         Vec3d vec = (Vec3d)list.get(i);
         bufferBuilder.vertex(matrix, (float)vec.x, (float)vec.y, (float)vec.z).color(blendColor(Theme.blendColor((int)(i / 20.0F)), (int)(f * 255.0F)).getRGB());
         Vec3d vec2 = (Vec3d)list.get(i + 1);
         bufferBuilder.vertex(matrix, (float)vec2.x, (float)vec2.y + 0.1F, (float)vec2.z)
            .color(blendColor(Theme.blendColor((int)(i / 20.0F)), (int)(f * 255.0F)).getRGB());
      }

      render(bufferBuilder);
   }

   public static void performActionEx() {
      factor = factorValue;
      factorValue += 0.15F;
      factorRef = factorData;
      factorData = factorData + factorPrimary;
      if (factorPrimary > 25.0F) {
         active = true;
      }

      if (factorPrimary < -25.0F) {
         active = false;
      }

      factorPrimary = active ? factorPrimary - 0.5F : factorPrimary + 0.5F;
   }

   static {
      lastTimeUtil.reset();
   }
}
