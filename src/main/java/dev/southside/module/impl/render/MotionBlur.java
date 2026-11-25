package dev.southside.module.impl.render;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.render.MotionBlurRenderer;
import dev.southside.utils.NumberValue;
import java.util.Map;

public final class MotionBlur extends Module {
   @SerializedName("persistence")
   public final NumberValue persistence = new NumberValue("Trail Half-Life (ms)", 35, 10, 100, 5);
   @SerializedName("renderer")
   private final MotionBlurRenderer renderer = new MotionBlurRenderer();

   public MotionBlur() {
      super(Category.Render, "MotionBlur", "动态模糊：为世界画面添加短暂残影，保留 HUD 清晰。");
   }

   @Override
   public void loadFromJson(JsonObject json) {
      this.loadConfigWithAliases(json, Map.of(this.persistence.getLabel(), "残影半衰期 (ms)"));
   }

   public void render() {
      if (this.isEnabled() && mc2.world != null) {
         if (mc2.currentScreen == null && mc2.getOverlay() == null && mc2.isWindowFocused()) {
            this.renderer
               .performAction(
                  mc2.getFramebuffer(), mc2.world, mc2.getCameraEntity(), mc2.options.getPerspective(), ((Number)this.persistence.getValue()).doubleValue()
               );
         } else {
            this.renderer.performAction();
         }
      } else if (this.renderer.isValid()) {
         mc2.getFramebuffer().beginWrite(true);
      }
   }

   public void renderEx() {
      this.renderer.isValid();
   }

   @Override
   public void onDisable() {
      this.renderer.performAction();
   }
}
