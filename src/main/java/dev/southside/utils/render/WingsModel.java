package dev.southside.utils.render;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;

public class WingsModel extends EntityModel<PlayerEntityRenderState> {
   @SerializedName("wing")
   public final ModelPart wing = this.root.getChild("wing");
   @SerializedName("wingTip")
   public final ModelPart wingTip = this.wing.getChild("wingtip");

   @Override
   public void setAngles(PlayerEntityRenderState renderState) {
   }

   public WingsModel() {
      super(getTexturedModelData().createModel());
   }

   public void setProgress(float f) {
      this.wing.pitch = (float)Math.toRadians(-80.0) - (float)Math.cos(f) * 0.2F;
      this.wing.yaw = (float)Math.toRadians(20.0) + (float)Math.sin(f) * 0.4F;
      this.wing.roll = (float)Math.toRadians(20.0);
      this.wingTip.roll = -((float)(Math.sin(f + 2.0F) + 0.5)) * 0.75F;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData modelData = new ModelData();
      ModelPartData root = modelData.getRoot();
      ModelPartData modelPartData = root.addChild(
         "wing",
         ModelPartBuilder.create().uv(0, 0).cuboid(-10.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F).uv(-10, 8).cuboid(-10.0F, 0.0F, 0.5F, 10.0F, 0.0F, 10.0F),
         ModelTransform.pivot(-2.0F, 0.0F, 0.0F)
      );
      modelPartData.addChild(
         "wingtip",
         ModelPartBuilder.create().uv(0, 5).cuboid(-10.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F).uv(-10, 18).cuboid(-10.0F, 0.0F, 0.5F, 10.0F, 0.0F, 10.0F),
         ModelTransform.pivot(-10.0F, 0.0F, 0.0F)
      );
      return TexturedModelData.of(modelData, 30, 30);
   }
}
