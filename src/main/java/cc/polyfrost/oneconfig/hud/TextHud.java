package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.StateEvent;
import dev.southside.render.UIMatrixStack;
import dev.southside.utils.ColorValue;
import dev.southside.utils.OneConfigEventManager;
import dev.southside.utils.HudCore;
import dev.southside.utils.OneConfigTextRenderer;
import dev.southside.utils.OneConfigPlatform;
import dev.southside.utils.ModeValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

public abstract class TextHud extends BasicHud {
   public transient List<String> lines = new ArrayList<>();
   @SerializedName("color")
   public ColorValue color = new ColorValue("Text Color", new OneColor(255, 255, 255));
   @SerializedName("textType")
   public ModeValue textType = new ModeValue("Text Type", new String[]{"No Shadow", "Shadow", "Full Shadow"}, "", 1, 0);

   public TextHud(
      float f,
      float f2,
      float f3,
      boolean flag,
      boolean flag2,
      float f4,
      float f5,
      float f6,
      OneColor color2,
      boolean flag3,
      float f7,
      OneColor color3
   ) {
      super(f, f2, f3, flag, flag2, f4, f5, f6, color2, flag3, f7, color3);
      OneConfigEventManager.INSTANCE.register(new TextHud.Inner());
   }

   public TextHud(int n, int n2) {
      super(n, n2);
      OneConfigEventManager.INSTANCE.register(new TextHud.Inner());
   }

   public TextHud() {
      this(0, 0);
   }

   public abstract void performAction(List<String> list, boolean flag);

   protected void clear(List<String> list, boolean flag) {
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      if (this.lines != null && this.lines.size() != 0) {
         float f4 = f2;

         for (String text : this.lines) {
            this.drawLine(text, f, f4, f3);
            f4 += 12.0F * f3;
         }
      }
   }

   public void drawLine(String text, float f, float f2, float f3) {
      OneConfigTextRenderer.applyText(text, f, f2, ((OneColor)this.color.getValue()).fromHsb(), OneConfigTextRenderer.Mode.fromOrdinal(this.textType.getValue()), f3);
   }

   public float computeFloat(String text, float f) {
      return OneConfigPlatform.getGLPlatform().getWidth(text) * f;
   }

   @Override
   public void setActive(boolean flag) {
      this.clear(this.lines, flag);
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      if (this.lines == null) {
         return 0.0F;
      }

      float f2 = 0.0F;

      for (String text : this.lines) {
         f2 = Math.max(f2, this.computeFloat(text, f));
      }

      return f2;
   }

   @Override
   public float setScale(float f, boolean flag) {
      return this.lines == null ? 0.0F : (this.lines.size() * 12 - 4) * f;
   }

   @Override
   public boolean shouldDrawBackground() {
      return super.shouldDrawBackground() && this.lines != null && this.lines.size() > 0;
   }

   private class Inner {
      public Inner() {
      }

      @EventTarget
      public void onState(StateEvent stateEvent) {
         if (stateEvent.getState() == Stage.PRE && TextHud.this.isValidEx()) {
            TextHud.this.lines.clear();
            TextHud.this.performAction(TextHud.this.lines, HudCore.active);
         }
      }
   }
}
