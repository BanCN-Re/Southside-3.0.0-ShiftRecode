package dev.southside.utils;

import dev.southside.mixin.accessors.WorldRendererAccessor;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class FrustumUtils {
   public static Frustum getFrustum() {
      return ((WorldRendererAccessor)IMinecraft.mc.worldRenderer).getFrustum();
   }

   private FrustumUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static boolean isValidTarget(Entity entity) {
      Vec3d pos = IMinecraft.mc.cameraEntity.getPos();
      return isBoxVisible(entity.getBoundingBox()) || entity.shouldRender(pos.x, pos.y, pos.z);
   }

   public static boolean isBoxVisible(Box box) {
      Camera camera = IMinecraft.mc.gameRenderer.getCamera();
      Frustum frustum = getFrustum();
      frustum.setPosition(camera.getPos().x, camera.getPos().y, camera.getPos().z);
      return frustum.isVisible(box);
   }

   public static Vec3d computePosition(float f, float f2, float f3, float f4, float f5, float f6) {
      float tickDelta = IMinecraft.mc.getRenderTickCounter().getTickDelta(false);
      double d = f + (f4 - f) * tickDelta - IMinecraft.mc.getEntityRenderDispatcher().camera.getPos().getX();
      double d2 = f2 + (f5 - f2) * tickDelta - IMinecraft.mc.getEntityRenderDispatcher().camera.getPos().getY();
      double d3 = f3 + (f6 - f3) * tickDelta - IMinecraft.mc.getEntityRenderDispatcher().camera.getPos().getZ();
      return new Vec3d(d, d2, d3);
   }
}
