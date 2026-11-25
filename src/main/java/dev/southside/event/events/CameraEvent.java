package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class CameraEvent {
   @SerializedName("camera")
   public Camera camera;
   @SerializedName("matrixStack")
   public MatrixStack matrixStack;
   @SerializedName("tickDelta")
   public float tickDelta;

   public Camera getCamera() {
      return this.camera;
   }

   public Matrix4f getMatrix4f() {
      return this.matrixStack.peek().getPositionMatrix();
   }

   public CameraEvent(Camera camera2, MatrixStack matrices, float f) {
      this.tickDelta = f;
      this.camera = camera2;
      this.matrixStack = matrices;
   }

   public MatrixStack getMatrixStack() {
      return this.matrixStack;
   }
}
