package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.misc.StartRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Quad;
import net.minecraft.client.model.ModelPart.Vertex;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class MenuPlayerPreview {
   private static final int DEFAULT_INT = 15728880;
   private static final float DEFAULT_FLOAT = -0.16F;
   private static final float DEFAULT_FLOAT_VALUE = (float)Math.sin(-0.16F);
   private static final float DEFAULT_FLOAT_DATA = (float)Math.cos(-0.16F);
   private static final float DEFAULT_FLOAT_REF = 0.28F;
   private static final float DEFAULT_FLOAT_PRIMARY = -0.7F;
   @SerializedName("skin")
   private final PlayerSkinProfile skin = new PlayerSkinProfile();
   @SerializedName("look")
   private final HeadLookController look = new HeadLookController();
   @SerializedName("hearts")
   private final HeartParticleEffect hearts = new HeartParticleEffect();
   @SerializedName("headPose")
   private final Matrix4f headPose = new Matrix4f();
   @SerializedName("point")
   private final Vector3f point = new Vector3f();
   @SerializedName("hitParts")
   private final List<MenuPlayerPreview.Entry> hitParts = new ArrayList<>();
   @SerializedName("loadedModels")
   private LoadedEntityModels loadedModels;
   @SerializedName("slimModel")
   private PlayerEntityModel slimModel;
   @SerializedName("unit")
   private float unit = 1.0F;
   @SerializedName("pointerX")
   private float pointerX;
   @SerializedName("pointerY")
   private float pointerY;
   @SerializedName("follow")
   private float follow;
   @SerializedName("visibility")
   private float visibility = 1.0F;
   @SerializedName("waveStarted")
   private long waveStarted = -10000L;
   @SerializedName("showPlayer")
   private boolean showPlayer;
   @SerializedName("hitBoxValid")
   private boolean hitBoxValid;
   @SerializedName("pointerCaptured")
   private boolean pointerCaptured;
   @SerializedName("pressedTarget")
   private MenuPlayerPreview.Mode pressedTarget;
   @SerializedName("pointerTravel")
   private double pointerTravel;
   @SerializedName("headLeft")
   private float headLeft;
   @SerializedName("headTop")
   private float headTop;
   @SerializedName("headRight")
   private float headRight;
   @SerializedName("headBottom")
   private float headBottom;
   @SerializedName("groinLeft")
   private float groinLeft;
   @SerializedName("groinTop")
   private float groinTop;
   @SerializedName("groinRight")
   private float groinRight;
   @SerializedName("groinBottom")
   private float groinBottom;
   @SerializedName("hurtAt")
   private long hurtAt = -10000L;

   public void performAction(float f, float f2, boolean flag, long l, float f3) {
      this.showPlayer = flag;
      this.pointerX = f;
      this.pointerY = f2;
      float f4 = Float.isFinite(f3) ? MathHelper.clamp(f3, 0.0F, 0.05F) : 0.0F;
      this.follow = 1.0F - (float)Math.exp(-f4 * 9.0F);
      this.visibility = this.visibility + ((flag ? 1 : 0) - this.visibility) * this.follow;
      this.hitBoxValid = false;
      if (!flag) {
         this.performAction();
      }
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float f, long l, float f2) {
      this.unit = Math.max(1.0F, f2);
      this.hitBoxValid = false;
      this.hitParts.clear();
      if (!(this.visibility <= 0.005F)) {
         LoadedEntityModels loadedEntityModels = MinecraftClient.getInstance().getLoadedEntityModels();
         if (loadedEntityModels != this.loadedModels) {
            this.loadedModels = loadedEntityModels;
            this.slimModel = new PlayerEntityModel(loadedEntityModels.getModelPart(EntityModelLayers.PLAYER_SLIM), true);
         }

         SkinTextures skinTextures = this.skin.getSkinTextures();
         PlayerEntityModel slimModel2 = this.slimModel;
         slimModel2.resetTransforms();
         slimModel2.setVisible(true);
         performAction(slimModel2);
         slimModel2.leftLeg.pitch = -0.7F + (float)Math.sin(f * 1.6F) * 0.035F;
         slimModel2.rightLeg.pitch = -0.7F + (float)Math.sin(f * 1.6F + Math.PI) * 0.035F;
         slimModel2.leftLeg.yaw = (float) (-Math.PI / 10);
         slimModel2.rightLeg.yaw = (float) (Math.PI / 10);
         slimModel2.leftLeg.roll = -0.07853982F;
         slimModel2.rightLeg.roll = 0.07853982F;
         performAction(slimModel2.rightArm, l - this.waveStarted);
         matrices.push();

         try {
            matrices.translate(0.55F, 0.82F - (1.0F - this.visibility) * 0.35F, 1.1F);
            matrices.scale(this.visibility, -this.visibility, -this.visibility);
            this.headPose.set(matrices.peek().getPositionMatrix()).translate(slimModel2.head.pivotX / 16.0F, slimModel2.head.pivotY / 16.0F, slimModel2.head.pivotZ / 16.0F);
            this.look.performAction(this.headPose, this.pointerX, this.pointerY, this.unit, this.follow);
            slimModel2.head.yaw = this.look.getYaw();
            slimModel2.head.pitch = this.look.getPitch();
            this.performAction(matrices.peek().getPositionMatrix());
            this.render(matrices, slimModel2.head);
            this.forEachCuboid(matrices, slimModel2);
            int n = l - this.hurtAt < 450L ? OverlayTexture.getUv(0.0F, true) : OverlayTexture.DEFAULT_UV;
            slimModel2.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(skinTextures.texture())), 15728880, n, -1);
            this.hitBoxValid = true;
         } finally {
            matrices.pop();
         }
      }
   }

   public void render(DrawContext context, long l) {
      if (this.showPlayer && this.hitBoxValid) {
         this.hearts.render(context, l, (this.headLeft + this.headRight) * 0.5F, this.headTop, this.unit);
      }
   }

   private static void performAction(ModelPart modelPart, long l) {
      if (l >= 0L && l < 1800L) {
         float f = evaluate((float)l / 300.0F);
         float f2 = 1.0F - evaluate((float)(l - 1350L) / 450.0F);
         float f3 = f * f2;
         float clamped = MathHelper.clamp((float)(l - 300L) / 1050.0F, 0.0F, 1.0F);
         modelPart.pitch = MathHelper.lerp(f3, 0.28F, -2.4F);
         modelPart.roll = modelPart.roll + f3 * (0.28F + (float)Math.sin(clamped * Math.PI * 6.0) * 0.24F);
      }
   }

   private static float evaluate(float f) {
      float clamped = MathHelper.clamp(f, 0.0F, 1.0F);
      return clamped * clamped * (3.0F - 2.0F * clamped);
   }

   private static void performAction(PlayerEntityModel playerEntityModel) {
      performAction(playerEntityModel.body);
      performAction(playerEntityModel.head);
      performAction(playerEntityModel.leftArm);
      performAction(playerEntityModel.rightArm);
      playerEntityModel.body.pitch = -0.16F;
      playerEntityModel.leftArm.roll = -0.08F;
      playerEntityModel.rightArm.roll = 0.08F;
      playerEntityModel.leftArm.pitch = 0.28F;
      playerEntityModel.rightArm.pitch = 0.28F;
   }

   private static void performAction(ModelPart modelPart) {
      float f = modelPart.pivotY - 12.0F;
      float pivotZ = modelPart.pivotZ;
      modelPart.pivotY = 12.0F + f * DEFAULT_FLOAT_DATA - pivotZ * DEFAULT_FLOAT_VALUE;
      modelPart.pivotZ = f * DEFAULT_FLOAT_VALUE + pivotZ * DEFAULT_FLOAT_DATA;
   }

   public float getVisibility() {
      return this.visibility;
   }

   private void performAction(Matrix4f matrix) {
      this.groinLeft = this.groinTop = Float.POSITIVE_INFINITY;
      this.groinRight = this.groinBottom = Float.NEGATIVE_INFINITY;

      for (byte i = -1; i <= 1; i += 2) {
         for (byte j = -1; j <= 1; j += 2) {
            for (byte k = -1; k <= 1; k += 2) {
               matrix.transformPosition(this.point.set(i * 0.22F, j > 0 ? 0.94F : 0.63F, k * 0.15F));
               this.groinLeft = Math.min(this.groinLeft, this.point.x);
               this.groinRight = Math.max(this.groinRight, this.point.x);
               this.groinTop = Math.min(this.groinTop, this.point.y);
               this.groinBottom = Math.max(this.groinBottom, this.point.y);
            }
         }
      }
   }

   private void render(MatrixStack matrices, ModelPart modelPart) {
      this.headLeft = this.headTop = Float.POSITIVE_INFINITY;
      this.headRight = this.headBottom = Float.NEGATIVE_INFINITY;
      matrices.push();

      try {
         modelPart.rotate(matrices);
         Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

         for (byte i = -1; i <= 1; i += 2) {
            for (byte j = -1; j <= 1; j += 2) {
               for (byte k = -1; k <= 1; k += 2) {
                  positionMatrix.transformPosition(this.point.set(i * 4.5F / 16.0F, (j > 0 ? 0.5F : -8.5F) / 16.0F, k * 4.5F / 16.0F));
                  this.headLeft = Math.min(this.headLeft, this.point.x);
                  this.headRight = Math.max(this.headRight, this.point.x);
                  this.headTop = Math.min(this.headTop, this.point.y);
                  this.headBottom = Math.max(this.headBottom, this.point.y);
               }
            }
         }
      } finally {
         matrices.pop();
      }
   }

   private void forEachCuboid(MatrixStack matrices, PlayerEntityModel playerEntityModel) {
      playerEntityModel.getRootPart()
         .forEachCuboid(
            matrices,
            (entry, text, n, cuboid) -> {
               float positive_infinity = Float.POSITIVE_INFINITY;
               float f = positive_infinity;
               float f2 = positive_infinity;
               float negative_infinity = Float.NEGATIVE_INFINITY;
               float f3 = negative_infinity;
               float f4 = negative_infinity;

               for (Quad quad : cuboid.sides) {
                  for (Vertex vertex : quad.vertices()) {
                     Vector3f vec = vertex.pos();
                     positive_infinity = Math.min(positive_infinity, vec.x);
                     f = Math.min(f, vec.y);
                     f2 = Math.min(f2, vec.z);
                     negative_infinity = Math.max(negative_infinity, vec.x);
                     f3 = Math.max(f3, vec.y);
                     f4 = Math.max(f4, vec.z);
                  }
               }

               if (Float.isFinite(positive_infinity)) {
                  Matrix4f matrix = new Matrix4f(entry.getPositionMatrix()).invert();
                  if (matrix.isFinite()) {
                     this.hitParts
                        .add(
                           new MenuPlayerPreview.Entry(
                              matrix,
                              new Box(positive_infinity / 16.0F, f / 16.0F, f2 / 16.0F, negative_infinity / 16.0F, f3 / 16.0F, f4 / 16.0F),
                              text.equals("/head") || text.startsWith("/head/")
                           )
                        );
                  }
               }
            }
         );
   }

   public double computeDouble(double x, double y) {
      MenuPlayerPreview.Entry2 hitResult = this.raycast(x, y);
      return hitResult == null ? Double.POSITIVE_INFINITY : hitResult.fraction;
   }

   private MenuPlayerPreview.Entry2 raycast(double x, double y) {
      if (!this.isValid()) {
         return null;
      }

      MenuPlayerPreview.Entry2 entry2 = null;

      for (MenuPlayerPreview.Entry entry : this.hitParts) {
         StartRecord startRecord = StartRecord.getStartRecord(entry.inverse, x, y);
         if (startRecord != null) {
            Optional hitResult = entry.bounds.raycast(startRecord.getStart(), startRecord.getEnd());
            if (!hitResult.isEmpty()) {
               double d = startRecord.computeDouble((Vec3d)hitResult.get());
               if (entry2 == null || !(d >= entry2.fraction)) {
                  MenuPlayerPreview.Mode mode = entry.head ? MenuPlayerPreview.Mode.HEAD : MenuPlayerPreview.Mode.BODY;
                  if (!entry.head && x >= this.groinLeft && x <= this.groinRight && y >= this.groinTop && y <= this.groinBottom) {
                     mode = MenuPlayerPreview.Mode.GROIN;
                  }

                  entry2 = new MenuPlayerPreview.Entry2(d, mode);
               }
            }
         }
      }

      return entry2;
   }

   public boolean isValid(double d, double d2, int n) {
      if (n != 0) {
         return false;
      }

      MenuPlayerPreview.Entry2 hitResult = this.raycast(d, d2);
      if (hitResult == null) {
         return false;
      }

      this.pointerCaptured = true;
      this.pointerTravel = 0.0;
      this.pressedTarget = hitResult.target;
      return true;
   }

   public boolean isValid(double x, double y) {
      if (!this.pointerCaptured) {
         return false;
      }

      this.pointerTravel = this.pointerTravel + (Math.abs(x) + Math.abs(y));
      return true;
   }

   public boolean isKeyDown(int n) {
      if (this.pointerCaptured && n == 0) {
         this.pointerCaptured = false;
         if (!this.isValid()) {
            return true;
         }

         if (this.pointerTravel < 4.0) {
            long now = Util.getMeasuringTimeMs();
            switch (this.pressedTarget) {
               case HEAD:
                  this.hearts.performAction(now);
                  break;
               case BODY:
                  this.waveStarted = now;
                  break;
               case GROIN:
                  this.hurtAt = now;
                  this.waveStarted = -10000L;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public void performAction() {
      this.pointerCaptured = false;
      this.pressedTarget = null;
      this.hitBoxValid = false;
      this.hitParts.clear();
      this.waveStarted = -10000L;
      this.hearts.performAction();
   }

   private boolean isValid() {
      return this.showPlayer && this.hitBoxValid && this.visibility >= 0.8F;
   }

   private record Entry(Matrix4f inverse, Box bounds, boolean head) {

      public Matrix4f getInverse() {
         return this.inverse;
      }

      public Box getBounds() {
         return this.bounds;
      }

      public boolean isHead() {
         return this.head;
      }
   }

   private record Entry2(double fraction, MenuPlayerPreview.Mode target) {

      public double getFraction() {
         return this.fraction;
      }

      public MenuPlayerPreview.Mode getTarget() {
         return this.target;
      }
   }

   private enum Mode {
      HEAD,
      BODY,
      GROIN;


      // $VF: synthetic method
      private static MenuPlayerPreview.Mode[] createValues() {
         return new MenuPlayerPreview.Mode[]{HEAD, BODY, GROIN};
      }


   }
}
