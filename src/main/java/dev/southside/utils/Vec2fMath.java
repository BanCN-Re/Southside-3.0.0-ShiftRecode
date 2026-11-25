package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.nio.FloatBuffer;

public class Vec2fMath {
   @SerializedName("x")
   public float x;
   @SerializedName("y")
   public float y;

   public Vec2fMath getVec2fMath(float f) {
      float f2 = this.x * f;
      float f3 = this.y * f;
      return new Vec2fMath(f2, f3);
   }

   public Vec2fMath getVec2fMath() {
      return this.getVec2fMath(-1.0F);
   }

   public float getProgress() {
      return (float)Math.sqrt(this.getProgressEx());
   }

   public float computeFloat(Vec2fMath other) {
      return this.x * other.x + this.y * other.y;
   }

   public Vec2fMath getVec2fMath(Vec2fMath other) {
      return this.getVec2fMathEx(other.getVec2fMath());
   }

   public float getProgressEx() {
      return this.x * this.x + this.y * this.y;
   }

   public Vec2fMath getVec2fMath(Vec2fMath vec2fMath, float f) {
      return this.getVec2fMath(1.0F - f).getVec2fMathEx(vec2fMath.getVec2fMath(f));
   }

   public void performAction(FloatBuffer buffer) {
      buffer.put(this.x).put(this.y);
      buffer.flip();
   }

   public float getY() {
      return this.y;
   }

   public Vec2fMath getVec2fMathEx(Vec2fMath other) {
      float f = this.x + other.x;
      float f2 = this.y + other.y;
      return new Vec2fMath(f, f2);
   }

   public Vec2fMath getVec2fMathEx() {
      float progress = this.getProgress();
      return this.getVec2fMathEx(progress);
   }

   public Vec2fMath() {
      this.x = 0.0F;
      this.y = 0.0F;
   }

   public Vec2fMath getVec2fMathEx(float f) {
      return this.getVec2fMath(1.0F / f);
   }

   public Vec2fMath(float x2, float y2) {
      this.x = x2;
      this.y = y2;
   }

   public float getX() {
      return this.x;
   }
}
