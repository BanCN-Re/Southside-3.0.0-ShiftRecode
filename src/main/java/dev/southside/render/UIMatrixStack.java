package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.utils.ModelViewStack;
import dev.southside.utils.misc.ModelRecord;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

public final class UIMatrixStack {
   @SerializedName("stack")
   private final Deque<ModelRecord> stack;
   public static final UIMatrixStack INSTANCE = new UIMatrixStack();

   public UIMatrixStack() {
      ArrayDeque arrayDeque = new ArrayDeque();
      arrayDeque.add(new ModelRecord(new Matrix4f().identity(), new Matrix3f().identity()));
      this.stack = arrayDeque;
   }

   private UIMatrixStack(Deque<ModelRecord> deque) {
      this.stack = deque;
   }

   public void onMouseClick(double x, double y, double z) {
      this.translate((float)x, (float)y, (float)z);
   }

   public void translate(float x, float y, float z) {
      if (x != 0.0F || y != 0.0F || z != 0.0F) {
         this.stack.getLast().getModel().translate(x, y, z);
      }
   }

   public void onMouseClickEx(double x, double y, double z) {
      this.scale((float)x, (float)y, (float)z);
   }

   public void scale(float x, float y, float z) {
      if (x != 1.0F || y != 1.0F || z != 1.0F) {
         ModelRecord last = this.stack.getLast();
         last.getModel().scale(x, y, z);
         if (x != y || y != z) {
            float f = 1.0F / x;
            float f2 = 1.0F / y;
            float f3 = 1.0F / z;
            float f4 = (float)Math.cbrt(f * f2 * f3);
            Matrix3f normalMatrix = new Matrix3f();
            normalMatrix.m00 = f4 * f;
            normalMatrix.m11 = f4 * f2;
            normalMatrix.m22 = f4 * f3;
            last.getNormal().mul(normalMatrix);
         } else if (x < 0.0F) {
            last.getNormal().negateX().negateY().negateZ();
         }
      }
   }

   public void rotate(Quaternionfc rotation) {
      this.stack.getLast().getModel().rotate(rotation);
   }

   public UIMatrixStack getUIMatrixStack() {
      ArrayDeque arrayDeque = new ArrayDeque();
      arrayDeque.add(new ModelRecord(new Matrix4f().set(this.stack.getLast().getModel()), new Matrix3f().set(this.stack.getLast().getNormal())));
      return new UIMatrixStack(arrayDeque);
   }

   public void addLast() {
      this.stack.addLast(new ModelRecord(new Matrix4f().set(this.stack.getLast().getModel()), new Matrix3f().set(this.stack.getLast().getNormal())));
   }

   public void removeLast() {
      this.stack.removeLast();
   }

   public ModelRecord getModelRecord() {
      return this.stack.getLast();
   }

   public boolean isValid() {
      return this.stack.size() == 1;
   }

   public void performAction() {
      RenderSystem.getModelViewStack().mul(this.stack.getLast().getModel());
   }

   public <T> T getT(Callable<T> callable) {
      ModelViewStack.Inner.pushMatrix();
      this.performAction();
      Object obj = null;

      try {
         obj = callable.call();
      } catch (Exception e) {
         e.printStackTrace();
      }

      ModelViewStack.Inner.popMatrix();
      return (T)obj;
   }

   public static final class Inner {
      public static final UIMatrixStack.Inner INSTANCE = new UIMatrixStack.Inner();
      private static final List<UIMatrixStack> uimatrixstacks = new ArrayList<>();

      public UIMatrixStack getUIMatrixStack() {
         return uimatrixstacks.isEmpty() ? new UIMatrixStack() : uimatrixstacks.get(uimatrixstacks.size() - 1);
      }

      public <T> T getT(UIMatrixStack matrices, Callable<T> callable) {
         uimatrixstacks.add(matrices);
         Object obj = null;

         try {
            obj = callable.call();
         } catch (Exception e) {
            e.printStackTrace();
         }

         uimatrixstacks.remove(uimatrixstacks.size() - 1);
         return (T)obj;
      }
   }
}
