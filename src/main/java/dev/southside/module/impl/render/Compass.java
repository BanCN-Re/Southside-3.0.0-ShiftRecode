package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.MSAA;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class Compass extends Module {
   @SerializedName("compassOnly")
   public final BooleanValue compassOnly = new BooleanValue("Compass Only", true);
   @SerializedName("noPlayerOnly")
   public final BooleanValue noPlayerOnly = new BooleanValue("No Player Only", true);
   @SerializedName("hasCompass")
   private boolean hasCompass;
   @SerializedName("spawnPosition")
   private BlockPos spawnPosition;
   @SerializedName("interpolatedYaw")
   private float interpolatedYaw;
   @SerializedName("interpolatedX")
   private double interpolatedX;
   @SerializedName("interpolatedZ")
   private double interpolatedZ;

   public Compass() {
      super(Category.Render, "Compass");
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (mc2.player != null && mc2.world != null) {
         this.interpolatedX = MathHelper.lerp(cameraEvent.tickDelta, mc2.player.lastRenderX, mc2.player.getX());
         this.interpolatedZ = MathHelper.lerp(cameraEvent.tickDelta, mc2.player.lastRenderZ, mc2.player.getZ());
         this.interpolatedYaw = MathHelper.lerp(cameraEvent.tickDelta, mc2.player.lastRenderYaw, mc2.player.renderYaw);
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (mc2.player != null && mc2.world != null) {
         this.spawnPosition = mc2.world.getSpawnPos().toImmutable();
         this.hasCompass = false;

         for (int i = 0; i < mc2.player.getInventory().size(); i++) {
            if (mc2.player.getInventory().getStack(i).isOf(Items.COMPASS)) {
               this.hasCompass = true;
               break;
            }
         }
      } else {
         this.hasCompass = false;
         this.spawnPosition = null;
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (mc2.player != null
         && mc2.world != null
         && this.spawnPosition != null
         && (!(Boolean)this.compassOnly.getValue() || this.hasCompass)
         && (!(Boolean)this.noPlayerOnly.getValue() || !mc2.world.getPlayers().stream().anyMatch(player -> player != mc2.player))) {
         float f = (float)(
            Math.toDegrees(Math.atan2(this.spawnPosition.getZ() - this.interpolatedZ, this.spawnPosition.getX() - this.interpolatedX))
               - 90.0
               - this.interpolatedYaw
         );
         float f2 = mc2.getWindow().getScaledWidth() / 2.0F;
         float f3 = mc2.getWindow().getScaledHeight() / 2.0F;
         MSAA.runTask(() -> this.render(drawContextEvent.getDrawContext().getMatrices(), f2, f3, f));
      }
   }

   private void render(MatrixStack matrices, float f, float f2, float f3) {
      matrices.push();

      try {
         matrices.translate(f, f2, 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f3));
         matrices.translate(-f, -f2, 0.0F);
         Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
         float f4 = f2 - 45.0F;
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         bufferBuilder.vertex(positionMatrix, f, f4, 0.0F).color(-1);
         bufferBuilder.vertex(positionMatrix, f - 5.0F, f4 + 10.0F, 0.0F).color(-1);
         bufferBuilder.vertex(positionMatrix, f, f4 + 10.0F, 0.0F).color(-1);
         bufferBuilder.vertex(positionMatrix, f + 5.0F, f4 + 10.0F, 0.0F).color(-1);
         BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      } finally {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         matrices.pop();
      }
   }
}
