package dev.southside.render.pipeline;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.render.GlStateTracker;
import dev.southside.render.ShaderProgram;
import dev.southside.utils.IMinecraft;
import java.awt.Color;
import java.nio.ByteBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4fStack;
import org.lwjgl.system.MemoryUtil;

public class Mesh {
   @SerializedName("renderer")
   public final RenderPass renderer;
   @SerializedName("matrixStack")
   public MatrixStack matrixStack;
   @SerializedName("vertexFormat")
   public final VertexFormat vertexFormat;
   @SerializedName("drawMode")
   public final DrawMode drawMode;
   @SerializedName("vertexStride")
   public final int vertexStride;
   @SerializedName("firstVertexCount")
   public final int firstVertexCount;
   @SerializedName("vertexData")
   public ByteBuffer vertexData;
   @SerializedName("vertexDataAddress")
   public long vertexDataAddress;
   @SerializedName("vertexWritePointer")
   public long vertexWritePointer;
   @SerializedName("indexData")
   public ByteBuffer indexData;
   @SerializedName("indexDataAddress")
   public long indexDataAddress;
   @SerializedName("vertexCount")
   public int vertexCount;
   @SerializedName("indexCount")
   public int indexCount;
   @SerializedName("building")
   public boolean building;
   @SerializedName("worldSpace")
   public boolean worldSpace;

   public Mesh(ShaderProgram program, VertexFormat vertexFormat2, DrawMode drawMode2) {
      this.renderer = new RenderPass(vertexFormat2, drawMode2);
      this.renderer.setShader(program);
      this.vertexFormat = vertexFormat2;
      this.drawMode = drawMode2;
      this.indexData = MemoryUtil.memAlloc(2048);
      this.indexDataAddress = MemoryUtil.memAddress0(this.indexData);
      this.vertexData = MemoryUtil.memAlloc(1024);
      this.vertexWritePointer = this.vertexDataAddress = MemoryUtil.memAddress0(this.vertexData);
      this.vertexStride = vertexFormat2.getVertexSizeByte();
      this.firstVertexCount = drawMode2.firstVertexCount;
   }

   public Mesh(RenderPass renderPass) {
      this.renderer = renderPass;
      this.vertexFormat = renderPass.vertexFormat;
      this.drawMode = renderPass.drawMode;
      this.indexData = MemoryUtil.memAlloc(2048);
      this.indexDataAddress = MemoryUtil.memAddress0(this.indexData);
      this.vertexData = MemoryUtil.memAlloc(1024);
      this.vertexWritePointer = this.vertexDataAddress = MemoryUtil.memAddress0(this.vertexData);
      this.vertexStride = this.vertexFormat.getVertexSizeByte();
      this.firstVertexCount = this.drawMode.firstVertexCount;
   }

   public void performAction() {
      this.worldSpace = true;
   }

   public void performActionEx() {
      this.worldSpace = false;
   }

   public Mesh getMesh(MatrixStack matrices) {
      this.matrixStack = matrices;
      return this;
   }

   public ShaderProgram getShader() {
      return this.renderer.getShader();
   }

   public void performActionAlt() {
      if (this.building) {
         throw new IllegalStateException("Mesh.begin() called while already building.");
      }

      this.building = true;
      this.vertexWritePointer = this.vertexDataAddress;
      this.vertexCount = 0;
      this.indexCount = 0;
   }

   public Mesh getMesh(float x, float y, float z) {
      long vertexWritePointer2 = this.vertexWritePointer;
      if (this.worldSpace) {
         float f = (float)(x - IMinecraft.mc.getEntityRenderDispatcher().camera.getPos().getX());
         float f2 = (float)(z - IMinecraft.mc.getEntityRenderDispatcher().camera.getPos().getZ());
         MemoryUtil.memPutFloat(vertexWritePointer2, f);
         MemoryUtil.memPutFloat(vertexWritePointer2 + 4L, y);
         MemoryUtil.memPutFloat(vertexWritePointer2 + 8L, f2);
      } else {
         MemoryUtil.memPutFloat(vertexWritePointer2, x);
         MemoryUtil.memPutFloat(vertexWritePointer2 + 4L, y);
         MemoryUtil.memPutFloat(vertexWritePointer2 + 8L, z);
      }

      this.vertexWritePointer += 12L;
      return this;
   }

   public Mesh getMesh(double x, double y, double z) {
      return this.getMesh((float)x, (float)y, (float)z);
   }

   public Mesh getMesh(float x, float y) {
      return this.getMesh(x, y, 0.0F);
   }

   public Mesh getMesh(double x, double y) {
      return this.getMesh(x, y, 0.0);
   }

   public Mesh getMesh(int n, int n2, int n3, int n4) {
      long vertexWritePointer2 = this.vertexWritePointer;
      MemoryUtil.memPutByte(vertexWritePointer2, (byte)n);
      MemoryUtil.memPutByte(vertexWritePointer2 + 1L, (byte)n2);
      MemoryUtil.memPutByte(vertexWritePointer2 + 2L, (byte)n3);
      MemoryUtil.memPutByte(vertexWritePointer2 + 3L, (byte)n4);
      this.vertexWritePointer += 4L;
      return this;
   }

   public Mesh getMesh(float x, float y, float width, float height) {
      return this.getMesh((int)x * 255, (int)y * 255, (int)width * 255, (int)height * 255);
   }

   public Mesh getMesh(Color color) {
      return this.getMesh(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
   }

   public Mesh getMesh(int n) {
      return this.getMesh(new Color(n));
   }

   public int getHeight() {
      this.performActionEx(1, 0);
      return this.vertexCount++;
   }

   public void performAction(int n, int n2) {
      this.performActionDirect();
      long l = this.indexDataAddress + this.indexCount * 4L;
      MemoryUtil.memPutInt(l, n);
      MemoryUtil.memPutInt(l + 4L, n2);
      this.indexCount += 2;
   }

   public void performAction(int n, int n2, int n3) {
      this.performActionSafe();
      long l = this.indexDataAddress + this.indexCount * 4L;
      MemoryUtil.memPutInt(l, n);
      MemoryUtil.memPutInt(l + 4L, n2);
      MemoryUtil.memPutInt(l + 8L, n3);
      this.indexCount += 3;
   }

   public void performAction(int n, int n2, int n3, int n4) {
      this.performActionCustom();
      long l = this.indexDataAddress + this.indexCount * 4L;
      MemoryUtil.memPutInt(l, n);
      MemoryUtil.memPutInt(l + 4L, n2);
      MemoryUtil.memPutInt(l + 8L, n3);
      MemoryUtil.memPutInt(l + 12L, n3);
      MemoryUtil.memPutInt(l + 16L, n4);
      MemoryUtil.memPutInt(l + 20L, n);
      this.indexCount += 6;
   }

   public void renderBox(Box box, Color color) {
      int height = this.getMesh(box.minX, box.minY, box.minZ).getMesh(color).getHeight();
      int height2 = this.getMesh(box.minX, box.minY, box.maxZ).getMesh(color).getHeight();
      int height3 = this.getMesh(box.maxX, box.minY, box.minZ).getMesh(color).getHeight();
      int height4 = this.getMesh(box.maxX, box.minY, box.maxZ).getMesh(color).getHeight();
      int height5 = this.getMesh(box.minX, box.maxY, box.minZ).getMesh(color).getHeight();
      int height6 = this.getMesh(box.minX, box.maxY, box.maxZ).getMesh(color).getHeight();
      int height7 = this.getMesh(box.maxX, box.maxY, box.minZ).getMesh(color).getHeight();
      int height8 = this.getMesh(box.maxX, box.maxY, box.maxZ).getMesh(color).getHeight();
      this.performAction(height, height2, height6, height5);
      this.performAction(height3, height7, height8, height4);
      this.performAction(height, height5, height7, height3);
      this.performAction(height2, height4, height8, height6);
      this.performAction(height, height3, height4, height2);
      this.performAction(height5, height6, height8, height7);
   }

   public void renderBoxEx(Box box, Color color) {
      int height = this.getMesh(box.minX, box.minY, box.minZ).getMesh(color).getHeight();
      int height2 = this.getMesh(box.minX, box.minY, box.maxZ).getMesh(color).getHeight();
      int height3 = this.getMesh(box.maxX, box.minY, box.minZ).getMesh(color).getHeight();
      int height4 = this.getMesh(box.maxX, box.minY, box.maxZ).getMesh(color).getHeight();
      int height5 = this.getMesh(box.minX, box.maxY, box.minZ).getMesh(color).getHeight();
      int height6 = this.getMesh(box.minX, box.maxY, box.maxZ).getMesh(color).getHeight();
      int height7 = this.getMesh(box.maxX, box.maxY, box.minZ).getMesh(color).getHeight();
      int height8 = this.getMesh(box.maxX, box.maxY, box.maxZ).getMesh(color).getHeight();
      this.performAction(height, height5);
      this.performAction(height2, height6);
      this.performAction(height3, height7);
      this.performAction(height4, height8);
      this.performAction(height, height2);
      this.performAction(height3, height4);
      this.performAction(height, height3);
      this.performAction(height2, height4);
      this.performAction(height5, height6);
      this.performAction(height7, height8);
      this.performAction(height5, height7);
      this.performAction(height6, height8);
   }

   public void render() {
      if (!this.building) {
         throw new IllegalStateException("Mesh.end() called while not building.");
      }

      this.building = false;
      this.renderer.setIndexData(this.getByteBuffer2());
      this.renderer.setVertexData(this.getByteBuffer());
   }

   public void push() {
      Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
      modelViewStack.pushMatrix();
      if (this.matrixStack != null) {
         modelViewStack.mul(this.matrixStack.peek().getPositionMatrix());
      }

      if (this.worldSpace) {
         Vec3d pos = IMinecraft.mc.gameRenderer.getCamera().getPos();
         modelViewStack.translate(0.0F, (float)(-pos.y), 0.0F);
      }

      this.renderer.onKeyTyped(this.indexCount);
      modelViewStack.popMatrix();
   }

   public void performActionImpl() {
      this.render();
      GlStateTracker.disableDepthTest();
      GlStateTracker.enableBlend();
      GlStateTracker.disableCull();
      this.push();
   }

   public boolean isBuilding() {
      return this.building;
   }

   public int getVertexCount() {
      return this.vertexCount;
   }

   public int getIndexCount() {
      return this.indexCount;
   }

   public int getHeightEx() {
      return (int)(this.vertexWritePointer - this.vertexDataAddress);
   }

   public ByteBuffer getByteBuffer() {
      this.vertexData.limit(this.getHeightEx());
      return this.vertexData;
   }

   public ByteBuffer getByteBuffer2() {
      this.indexData.limit(this.indexCount * 4);
      return this.indexData;
   }

   public void performActionEx(int n, int n2) {
      if ((this.vertexCount + n) * this.vertexStride >= this.vertexData.capacity()) {
         int heightEx = this.getHeightEx();
         int n3 = this.vertexData.capacity() * 2;
         ByteBuffer buffer = MemoryUtil.memAlloc(n3);
         MemoryUtil.memCopy(MemoryUtil.memAddress0(this.vertexData), MemoryUtil.memAddress0(buffer), heightEx);
         MemoryUtil.memFree(this.vertexData);
         this.vertexData = buffer;
         this.vertexDataAddress = MemoryUtil.memAddress0(this.vertexData);
         this.vertexWritePointer = this.vertexDataAddress + heightEx;
      }

      if ((this.indexCount + n2) * 4 >= this.indexData.capacity()) {
         int n4 = this.indexData.capacity() * 2;
         ByteBuffer buffer2 = MemoryUtil.memAlloc(n4);
         MemoryUtil.memCopy(MemoryUtil.memAddress0(this.indexData), MemoryUtil.memAddress0(buffer2), this.indexCount * 4L);
         MemoryUtil.memFree(this.indexData);
         this.indexData = buffer2;
         this.indexDataAddress = MemoryUtil.memAddress0(this.indexData);
      }
   }

   public void performActionDirect() {
      this.performActionEx(2, 2);
   }

   public void performActionSafe() {
      this.performActionEx(3, 3);
   }

   public void performActionCustom() {
      this.performActionEx(4, 6);
   }
}
