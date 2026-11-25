package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4dc;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

public class ShaderUniform {
   @SerializedName("program")
   public final ShaderProgram program;
   @SerializedName("name")
   public final String name;
   @SerializedName("location")
   public final int location;
   @SerializedName("pendingSetter")
   public Runnable pendingSetter;
   @SerializedName("matrix3Buffer")
   public final FloatBuffer matrix3Buffer = MemoryUtil.memAllocFloat(9);
   @SerializedName("matrix4Buffer")
   public final FloatBuffer matrix4Buffer = MemoryUtil.memAllocFloat(16);

   public ShaderUniform(ShaderProgram program2, String text) {
      this.program = program2;
      this.name = text;
      this.location = GL45C.glGetUniformLocation(program2.getProgramId(), text);
   }

   public ShaderProgram getProgram() {
      return this.program;
   }

   public String getName() {
      return this.name;
   }

   public int getLocation() {
      return this.location;
   }

   public Runnable getPendingSetter() {
      return this.pendingSetter;
   }

   public FloatBuffer getMatrix3Buffer() {
      return this.matrix3Buffer;
   }

   public FloatBuffer getMatrix4Buffer() {
      return this.matrix4Buffer;
   }

   public void performAction() {
      if (this.pendingSetter != null) {
         this.pendingSetter.run();
         this.pendingSetter = null;
      }
   }

   public void setProgress(float f) {
      this.pendingSetter = () -> this.setFloat(f);
   }

   public void setFloat(float f) {
      this.program.bind();
      GL45C.glUniform1f(this.location, f);
   }

   public void performAction(float x, float y) {
      this.pendingSetter = () -> this.setVec2(x, y);
   }

   public void setVec2(float x, float y) {
      this.program.bind();
      GL45C.glUniform2f(this.location, x, y);
   }

   public void performAction(float x, float y, float z) {
      this.pendingSetter = () -> this.setVec3(x, y, z);
   }

   public void setVec3(float x, float y, float z) {
      this.program.bind();
      GL45C.glUniform3f(this.location, x, y, z);
   }

   public void performAction(float x, float y, float width, float height) {
      this.pendingSetter = () -> this.setVec4(x, y, width, height);
   }

   public void setVec4(float x, float y, float width, float height) {
      this.program.bind();
      GL45C.glUniform4f(this.location, x, y, width, height);
   }

   public void onKeyTyped(int n) {
      this.pendingSetter = () -> this.setInt(n);
   }

   public void setInt(int n) {
      this.program.bind();
      GL45C.glUniform1i(this.location, n);
   }

   public void performAction(int n, int n2) {
      this.pendingSetter = () -> this.setVec2i(n, n2);
   }

   public void setVec2i(int n, int n2) {
      this.program.bind();
      GL45C.glUniform2i(this.location, n, n2);
   }

   public void performAction(int n, int n2, int n3) {
      this.pendingSetter = () -> this.performActionEx(n, n2, n3);
   }

   public void performActionEx(int n, int n2, int n3) {
      this.program.bind();
      GL45C.glUniform3i(this.location, n, n2, n3);
   }

   public void performAction(int n, int n2, int n3, int n4) {
      this.pendingSetter = () -> this.performActionEx(n, n2, n3, n4);
   }

   public void performActionEx(int n, int n2, int n3, int n4) {
      this.program.bind();
      GL45C.glUniform4i(this.location, n, n2, n3, n4);
   }

   public void performAction(double d) {
      this.pendingSetter = () -> this.performActionEx(d);
   }

   public void performActionEx(double d) {
      this.program.bind();
      GL45C.glUniform1d(this.location, d);
   }

   public void performAction(double x, double y) {
      this.pendingSetter = () -> this.performActionEx(x, y);
   }

   public void performActionEx(double x, double y) {
      this.program.bind();
      GL45C.glUniform2d(this.location, x, y);
   }

   public void onMouseClick(double x, double y, double z) {
      this.pendingSetter = () -> this.onMouseClickEx(x, y, z);
   }

   public void onMouseClickEx(double x, double y, double z) {
      this.program.bind();
      GL45C.glUniform3d(this.location, x, y, z);
   }

   public void performAction(double x, double y, double width, double height) {
      this.pendingSetter = () -> this.performActionEx(x, y, width, height);
   }

   public void performActionEx(double x, double y, double width, double height) {
      this.program.bind();
      GL45C.glUniform4d(this.location, x, y, width, height);
   }

   public void performAction(Vector2fc vector2fc) {
      this.performAction(vector2fc.x(), vector2fc.y());
   }

   public void performAction(Vector3fc vector3fc) {
      this.performAction(vector3fc.x(), vector3fc.y(), vector3fc.z());
   }

   public void performAction(Vector4fc vector4fc) {
      this.performAction(vector4fc.x(), vector4fc.y(), vector4fc.z(), vector4fc.w());
   }

   public void performAction(Vector2ic vector2ic) {
      this.performAction(vector2ic.x(), vector2ic.y());
   }

   public void performAction(Vector3ic vector3ic) {
      this.performAction(vector3ic.x(), vector3ic.y(), vector3ic.z());
   }

   public void performAction(Vector4ic vector4ic) {
      this.performAction(vector4ic.x(), vector4ic.y(), vector4ic.z(), vector4ic.w());
   }

   public void performAction(Vector2dc vector2dc) {
      this.performAction(vector2dc.x(), vector2dc.y());
   }

   public void performAction(Vector3dc vector3dc) {
      this.onMouseClick(vector3dc.x(), vector3dc.y(), vector3dc.z());
   }

   public void performAction(Vector4dc vector4dc) {
      this.performAction(vector4dc.x(), vector4dc.y(), vector4dc.z(), vector4dc.w());
   }

   public void applyColor(Color color) {
      this.performAction(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
   }

   public void performAction(float[] floats) {
      this.pendingSetter = () -> this.setFloat(floats);
   }

   public void setFloat(float[] floats) {
      this.program.bind();
      GL45C.glUniform1fv(this.location, floats);
   }

   public void performActionEx(float[] floats) {
      this.pendingSetter = () -> this.setVec2(floats);
   }

   public void setVec2(float[] floats) {
      this.program.bind();
      GL45C.glUniform2fv(this.location, floats);
   }

   public void performActionAlt(float[] floats) {
      this.pendingSetter = () -> this.setVec3(floats);
   }

   public void setVec3(float[] floats) {
      this.program.bind();
      GL45C.glUniform3fv(this.location, floats);
   }

   public void performActionImpl(float[] floats) {
      this.pendingSetter = () -> this.setVec4(floats);
   }

   public void setVec4(float[] floats) {
      this.program.bind();
      GL45C.glUniform4fv(this.location, floats);
   }

   public void performAction(int[] values) {
      this.pendingSetter = () -> this.setInt(values);
   }

   public void setInt(int[] values) {
      this.program.bind();
      GL45C.glUniform1iv(this.location, values);
   }

   public void performActionEx(int[] values) {
      this.pendingSetter = () -> this.setVec2i(values);
   }

   public void setVec2i(int[] values) {
      this.program.bind();
      GL45C.glUniform2iv(this.location, values);
   }

   public void performActionAlt(int[] values) {
      this.pendingSetter = () -> this.performActionImpl(values);
   }

   public void performActionImpl(int[] values) {
      this.program.bind();
      GL45C.glUniform3iv(this.location, values);
   }

   public void performActionDirect(int[] values) {
      this.pendingSetter = () -> this.performActionSafe(values);
   }

   public void performActionSafe(int[] values) {
      this.program.bind();
      GL45C.glUniform4iv(this.location, values);
   }

   public void performAction(double[] coords) {
      this.pendingSetter = () -> this.performActionEx(coords);
   }

   public void performActionEx(double[] coords) {
      this.program.bind();
      GL45C.glUniform1dv(this.location, coords);
   }

   public void performActionAlt(double[] coords) {
      this.pendingSetter = () -> this.performActionImpl(coords);
   }

   public void performActionImpl(double[] coords) {
      this.program.bind();
      GL45C.glUniform2dv(this.location, coords);
   }

   public void performActionDirect(double[] coords) {
      this.pendingSetter = () -> this.performActionSafe(coords);
   }

   public void performActionSafe(double[] coords) {
      this.program.bind();
      GL45C.glUniform3dv(this.location, coords);
   }

   public void performActionCustom(double[] coords) {
      this.pendingSetter = () -> this.performActionWithOffset(coords);
   }

   public void performActionWithOffset(double[] coords) {
      this.program.bind();
      GL45C.glUniform4dv(this.location, coords);
   }

   public void performAction(Matrix3fc matrix3fc) {
      matrix3fc.get(this.matrix3Buffer);
      this.performActionEx(this.matrix3Buffer, false);
   }

   public void performActionEx(Matrix3fc matrix3fc) {
      matrix3fc.getTransposed(this.matrix3Buffer);
      this.performActionEx(this.matrix3Buffer, true);
   }

   public void performAction(Matrix4fc matrix4fc) {
      matrix4fc.get(this.matrix4Buffer);
      this.performActionAlt(this.matrix4Buffer, false);
   }

   public void performActionEx(Matrix4fc matrix4fc) {
      matrix4fc.getTransposed(this.matrix4Buffer);
      this.performActionAlt(this.matrix4Buffer, true);
   }

   public void performAction(float[] floats, boolean flag) {
      this.pendingSetter = () -> this.performAction(flag, floats);
   }

   public void performAction(boolean flag, float[] floats) {
      this.program.bind();
      GL45C.glUniformMatrix2fv(this.location, flag, floats);
   }

   public void performAction(FloatBuffer buffer, boolean flag) {
      this.pendingSetter = () -> this.performAction(flag, buffer);
   }

   public void performAction(boolean flag, FloatBuffer buffer) {
      this.program.bind();
      GL45C.glUniformMatrix2fv(this.location, flag, buffer);
   }

   public void performActionEx(float[] floats, boolean flag) {
      this.pendingSetter = () -> this.setMatrix3f(flag, floats);
   }

   public void setMatrix3f(boolean flag, float[] floats) {
      this.program.bind();
      GL45C.glUniformMatrix3fv(this.location, flag, floats);
   }

   public void performActionEx(FloatBuffer buffer, boolean flag) {
      this.pendingSetter = () -> this.setMatrix3f(flag, buffer);
   }

   public void setMatrix3f(boolean flag, FloatBuffer buffer) {
      this.program.bind();
      GL45C.glUniformMatrix3fv(this.location, flag, buffer);
   }

   public void performActionAlt(float[] floats, boolean flag) {
      this.pendingSetter = () -> this.setMatrix4f(flag, floats);
   }

   public void setMatrix4f(boolean flag, float[] floats) {
      this.program.bind();
      GL45C.glUniformMatrix4fv(this.location, flag, floats);
   }

   public void performActionAlt(FloatBuffer buffer, boolean flag) {
      this.pendingSetter = () -> this.setMatrix4f(flag, buffer);
   }

   public void setMatrix4f(boolean flag, FloatBuffer buffer) {
      this.program.bind();
      GL45C.glUniformMatrix4fv(this.location, flag, buffer);
   }

   public void performAction(double[] coords, boolean flag) {
      this.pendingSetter = () -> this.performAction(flag, coords);
   }

   public void performAction(boolean flag, double[] coords) {
      this.program.bind();
      GL45C.glUniformMatrix2dv(this.location, flag, coords);
   }

   public void performAction(DoubleBuffer doubleBuffer, boolean flag) {
      this.pendingSetter = () -> this.performAction(flag, doubleBuffer);
   }

   public void performAction(boolean flag, DoubleBuffer doubleBuffer) {
      this.program.bind();
      GL45C.glUniformMatrix2dv(this.location, flag, doubleBuffer);
   }

   public void performActionEx(double[] coords, boolean flag) {
      this.pendingSetter = () -> this.performActionEx(flag, coords);
   }

   public void performActionEx(boolean flag, double[] coords) {
      this.program.bind();
      GL45C.glUniformMatrix3dv(this.location, flag, coords);
   }

   public void performActionEx(DoubleBuffer doubleBuffer, boolean flag) {
      this.pendingSetter = () -> this.performActionEx(flag, doubleBuffer);
   }

   public void performActionEx(boolean flag, DoubleBuffer doubleBuffer) {
      this.program.bind();
      GL45C.glUniformMatrix3dv(this.location, flag, doubleBuffer);
   }

   public void performActionAlt(double[] coords, boolean flag) {
      this.pendingSetter = () -> this.performActionAlt(flag, coords);
   }

   public void performActionAlt(boolean flag, double[] coords) {
      this.program.bind();
      GL45C.glUniformMatrix4dv(this.location, flag, coords);
   }

   public void performActionAlt(DoubleBuffer doubleBuffer, boolean flag) {
      this.pendingSetter = () -> this.performActionAlt(flag, doubleBuffer);
   }

   public void performActionAlt(boolean flag, DoubleBuffer doubleBuffer) {
      this.program.bind();
      GL45C.glUniformMatrix4dv(this.location, flag, doubleBuffer);
   }
}
