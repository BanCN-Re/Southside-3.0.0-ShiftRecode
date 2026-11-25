package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.impl.render.PlayerAppearance;
import dev.southside.utils.render.WingsModel;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class WingsFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
   @SerializedName("wingsModel")
   public final WingsModel wingsModel = new WingsModel();
   public static final Identifier TEXTURE_ID;

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n) {
      Color hsbcolor = Color.getHSBColor((float)(System.currentTimeMillis() % 1000L) / 1000.0F, 0.8F, 1.0F);
      VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE_ID));
      float f = (float)(System.currentTimeMillis() % 1000L) / 1000.0F * (float) Math.PI * 2.0F;
      this.wingsModel.setProgress(f);
      matrices.push();
      this.wingsModel.render(matrices, buffer, n, OverlayTexture.DEFAULT_UV, hsbcolor.getRGB());
      matrices.pop();
      matrices.push();
      matrices.scale(-1.0F, 1.0F, 1.0F);
      this.wingsModel.render(matrices, buffer, n, OverlayTexture.DEFAULT_UV, hsbcolor.getRGB());
      matrices.pop();
   }

   public WingsFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> featureRendererContext) {
      super(featureRendererContext);
   }

   @Override
   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, PlayerEntityRenderState renderState, float f, float f2) {
      PlayerAppearance playerAppearance = Southside.INSTANCE.getModuleManager().getModule(PlayerAppearance.class);
      if ((Boolean)playerAppearance.dragonWings.getValue() && playerAppearance.isEnabled() && !renderState.invisible && renderState.id == MinecraftClient.getInstance().player.getId()) {
         matrices.push();
         ((PlayerEntityModel)this.getContextModel()).body.rotate(matrices);
         matrices.translate(0.0, 0.1F, 0.125);
         float f3 = 1.2F;
         matrices.scale(f3, f3, f3);
         this.render(matrices, vertexConsumers, n);
         matrices.pop();
      }
   }

   public void render(MatrixStack matrices, PlayerEntity player, float f) {
      double d = 1.4;
      float interpolated = MathHelper.lerpAngleDegrees(f, player.prevBodyYaw, player.bodyYaw);
      matrices.scale((float)(-d), (float)(-d), (float)d);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F + interpolated));
      matrices.translate(0.0, -1.25 / d, 0.0);
      matrices.translate(0.0, 0.0, 0.2 / d);
      if (player.isInSneakingPose()) {
         matrices.translate(0.0, 0.125 / d, 0.0);
      }
   }

   static {
      TEXTURE_ID = Identifier.of(Southside.label.toLowerCase(), "textures/wings/wings.png");
   }

}
