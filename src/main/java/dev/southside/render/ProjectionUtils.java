package dev.southside.render;

import dev.southside.utils.IMinecraft;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class ProjectionUtils {
   public static final Vector3d vector = new Vector3d();
   public static final Vector3d vectorValue = new Vector3d();
   public static Matrix4f matrix = new Matrix4f();
   public static Matrix4f matrixValue = new Matrix4f();
   public static Matrix4f matrixData = new Matrix4f();
   public static double value;

   public static Vec3d computePosition(Vec3d vec) {
      short s = 29745;
      Camera camera = IMinecraft.mc.getEntityRenderDispatcher().camera;
      long l = 6499590768744336571L;
      int height = IMinecraft.mc.getWindow().getHeight();
      int[] values = new int[4];
      GL11.glGetIntegerv(2978, values);
      Vector3f vec2 = new Vector3f();
      double dx = vec.x - camera.getPos().x;
      double dy = vec.y - camera.getPos().y;
      double dz = vec.z - camera.getPos().z;
      Vector4f vec4 = new Vector4f((float)dx, (float)dy, (float)dz, 1.0F).mul(matrix);
      Matrix4f matrix2 = new Matrix4f(matrixData);
      Matrix4f matrix3 = new Matrix4f(matrixValue);
      matrix2.mul(matrix3).project(vec4.x(), vec4.y(), vec4.z(), values, vec2);
      return new Vec3d(vec2.x / value, (height - vec2.y) / value, vec2.z);
   }

   public static Vec2f getVec2f(double d, double d2, double d3, double d4, double d5) {
      Box box = new Box(d - d4, d2, d3 - d4, d + d4, d2 + d5, d3 + d4);
      List list = Arrays.asList(
         new Vec3d(box.minX, box.minY, box.minZ),
         new Vec3d(box.minX, box.maxY, box.minZ),
         new Vec3d(box.maxX, box.minY, box.minZ),
         new Vec3d(box.maxX, box.maxY, box.minZ),
         new Vec3d(box.minX, box.minY, box.maxZ),
         new Vec3d(box.minX, box.maxY, box.maxZ),
         new Vec3d(box.maxX, box.minY, box.maxZ),
         new Vec3d(box.maxX, box.maxY, box.maxZ)
      );
      Vector4d vec4 = null;

      for (Object obj : list) {
         Vec3d vec = (Vec3d)obj;
         vec = computePosition(vec);
         if (vec.z >= 0.0 && vec.z < 1.0 && Double.isFinite(vec.x) && Double.isFinite(vec.y)) {
            if (vec4 == null) {
               vec4 = new Vector4d(vec.x, vec.y, vec.x, vec.y);
            }

            vec4 = new Vector4d(Math.min(vec.x, vec4.x()), Math.min(vec.y, vec4.y()), Math.max(vec.x, vec4.z()), Math.max(vec.y, vec4.w()));
         }
      }

      return vec4 == null ? null : new Vec2f((float)(vec4.x() + (vec4.z() - vec4.x()) / 2.0), (float)(vec4.y() - 2.0));
   }

   public static void performAction(Matrix4f matrix2, Matrix4f matrix3, Matrix4f matrix4) {
      matrix.set(matrix2);
      matrixValue.set(matrix3);
      matrixData.set(matrix4);
      vector.set(IMinecraft.mc.gameRenderer.getCamera().getPos().toVector3f());
      vectorValue.set(vector);
      vectorValue.negate();
      value = IMinecraft.mc.getWindow().getScaleFactor();
   }
}
