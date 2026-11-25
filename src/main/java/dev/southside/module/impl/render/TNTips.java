package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.ProjectionUtils;
import dev.southside.utils.NumberValue;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class TNTips extends Module {
   private static final int DEFAULT_INT = 40;
   @SerializedName("tntWarning")
   public final BooleanValue tntWarning = new BooleanValue("TNT Warning", true);
   @SerializedName("warningRange")
   public final NumberValue warningRange = new NumberValue("Warning Range", 30.0F, 1.0F, 10.0F, 1.0F);
   @SerializedName("warningSound")
   public final BooleanValue warningSound = new BooleanValue("Warning Sound", true);
   @SerializedName("tntCountdown")
   public final BooleanValue tntCountdown = new BooleanValue("TNT Countdown", true);
   @SerializedName("countdownScale")
   public final NumberValue countdownScale = new NumberValue("Countdown Scale", 1.5F, 0.5F, 5.0F, 0.1F);
   @SerializedName("warningTicks")
   private int warningTicks;
   @SerializedName("warnedTntIds")
   private final Set<Integer> warnedTntIds = new HashSet<>();

   public TNTips() {
      super(Category.Render, "TNTips", "TNT warning & countdown");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.addDependency(this.warningRange.getLabel(), this.tntWarning.getLabel());
      this.addDependency(this.warningSound.getLabel(), this.tntWarning.getLabel());
      this.addDependency(this.countdownScale.getLabel(), this.tntCountdown.getLabel());
   }

   @Override
   public void onEnable() {
      this.initPostRunnable();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.initPostRunnable();
   }

   private void initPostRunnable() {
      this.warningTicks = 0;
      this.warnedTntIds.clear();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE && mc2.world != null && mc2.player != null) {
         if (this.warningTicks > 0) {
            this.warningTicks--;
         }

         if ((Boolean)this.tntWarning.getValue()) {
            double d = ((Number)this.warningRange.getValue()).doubleValue();
            this.warnedTntIds.removeIf(integer -> mc2.world.getEntityById(integer) == null);

            for (Entity entity : mc2.world.getEntities()) {
               if (entity instanceof TntEntity && mc2.player.squaredDistanceTo(entity) <= d * d && this.warnedTntIds.add(entity.getId())) {
                  this.warningTicks = 40;
                  if ((Boolean)this.warningSound.getValue()) {
                     mc2.player.playSound(SoundEvents.BLOCK_BELL_USE, 1.0F, 1.0F);
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (mc2.world != null && mc2.player != null) {
         DrawContext drawContext = drawContextEvent.getDrawContext();
         if ((Boolean)this.tntWarning.getValue() && this.warningTicks > 0) {
            this.render(drawContext);
         }

         if ((Boolean)this.tntCountdown.getValue()) {
            for (Entity entity : mc2.world.getEntities()) {
               if (entity instanceof TntEntity tntEntity) {
                  this.render(drawContext, drawContextEvent.getTickDelta(), tntEntity);
               }
            }
         }
      }
   }

   private void render(DrawContext context, float f, TntEntity tntEntity) {
      Vec3d vec = tntEntity.getLerpedPos(f).add(0.0, tntEntity.getHeight() + 0.5, 0.0);
      Vec2f vec2f = ProjectionUtils.getVec2f(vec.x, vec.y, vec.z, 0.0, 0.0);
      if (vec2f != null) {
         float f2 = tntEntity.getFuse() / 20.0F;
         String formatted = String.format("%.1fs", f2);
         int n = f2 > 2.0F ? -11141291 : (f2 > 1.0F ? -171 : -43691);
         MatrixStack matrices = context.getMatrices();
         matrices.push();

         try {
            matrices.translate(vec2f.x, vec2f.y, 0.0F);
            float f3 = ((Number)this.countdownScale.getValue()).floatValue();
            matrices.scale(f3, f3, 1.0F);
            int width = mc2.textRenderer.getWidth(formatted);
            int n2 = -width / 2;
            context.fill(n2 - 3, -11, n2 + width + 3, 2, Integer.MIN_VALUE);
            context.drawText(mc2.textRenderer, formatted, n2, -9, n, true);
         } finally {
            matrices.pop();
         }
      }
   }

   private void render(DrawContext context) {
      String text = "TNT is coming!!!";
      int n = (int)(Math.min(1.0F, this.warningTicks / 10.0F) * 255.0F);
      MatrixStack matrices = context.getMatrices();
      matrices.push();

      try {
         matrices.translate(mc2.getWindow().getScaledWidth() / 2.0F, mc2.getWindow().getScaledHeight() / 4.0F, 0.0F);
         short s = 6563;
         matrices.scale(4.0F, 4.0F, 1.0F);
         long l = 6291525062726666896L;
         int n2 = -mc2.textRenderer.getWidth(text) / 2;
         byte b = -4;
         context.drawText(mc2.textRenderer, text, n2 + 1, b + 1, n << 24 | 4134165, false);
         context.drawText(mc2.textRenderer, text, n2, b, n << 24 | 16733525, false);
      } finally {
         matrices.pop();
      }
   }
}
