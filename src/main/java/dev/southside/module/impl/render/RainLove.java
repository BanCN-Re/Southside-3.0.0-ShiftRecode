package dev.southside.module.impl.render;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.IslandVisitorEntity;
import dev.southside.utils.NumberValue;
import java.util.Map;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public final class RainLove extends Module {
   @SerializedName("amplitude")
   public final NumberValue amplitude = new NumberValue("Amplitude", 0.4F, 0.1F, 0.7F, 0.05F);
   @SerializedName("period")
   public final NumberValue period = new NumberValue("Cycle Duration (s)", 1.2F, 0.4F, 3.0F, 0.1F);
   @SerializedName("allPlayers")
   public final BooleanValue allPlayers = new BooleanValue("Affect Other Players", false);
   @SerializedName("renderingWorld")
   private boolean renderingWorld;

   public RainLove() {
      super(Category.Render, "RainLove", "雨爱：循环压扁、拉伸玩家模型，只改变外观。");
   }

   @Override
   public void loadFromJson(JsonObject json) {
      this.loadConfigWithAliases(json, Map.of(this.amplitude.getLabel(), "幅度", this.period.getLabel(), "周期 (秒)", this.allPlayers.getLabel(), "影响其他玩家"));
   }

   public void setRenderingWorld(boolean flag) {
      this.renderingWorld = flag;
   }

   public void render(PlayerEntityRenderState renderState, MatrixStack matrices) {
      if (this.renderingWorld
         && mc2.world != null
         && mc2.player != null
         && !renderState.spectator
         && !IslandVisitorEntity.isKeyDown(renderState.id)
         && ((Boolean)this.allPlayers.getValue() || renderState.id == mc2.player.getId())) {
         float f = renderState.age / (20.0F * ((Number)this.period.getValue()).floatValue()) % 1.0F;
         float f2 = 1.0F + ((Number)this.amplitude.getValue()).floatValue() * MathHelper.sin(f * (float) (Math.PI * 2));
         float f3 = 1.0F / MathHelper.sqrt(f2);
         matrices.scale(f3, f2, f3);
      }
   }
}
