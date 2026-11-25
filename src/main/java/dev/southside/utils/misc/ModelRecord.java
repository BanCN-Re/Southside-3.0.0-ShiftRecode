package dev.southside.utils.misc;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record ModelRecord(Matrix4f model, Matrix3f normal) {
   public Matrix4f getModel() {
      return this.model;
   }

   public Matrix3f getNormal() {
      return this.normal;
   }
}
