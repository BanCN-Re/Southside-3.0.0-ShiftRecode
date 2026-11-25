package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.hud.Hud;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.EntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.UIMatrixStack;
import dev.southside.render.TargetHudCard;
import dev.southside.utils.HudPosition;
import dev.southside.utils.HudValue;
import dev.southside.utils.WindowMetrics;
import dev.southside.utils.ScissorHelper;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.TargetHudRect;
import dev.southside.utils.TargetHudRounded;
import dev.southside.utils.TargetHudCompact;
import dev.southside.utils.TargetHudPill;
import dev.southside.utils.TargetHudModern;
import dev.southside.utils.TargetHudBlockRate;
import dev.southside.utils.TargetHudLarge;
import dev.southside.utils.TargetHudSimple;
import dev.southside.utils.ModeValue;
import dev.southside.utils.NanoTimer;
import dev.southside.utils.SmoothFloat;
import dev.southside.utils.ScissorRect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;

public class TargetHUD extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Vanilla", "Vanilla", "Retro", "ModernVanilla", "Old", "Plus", "Myau", "New", "Southside", "Simple");
   @SerializedName("multipleTargets")
   public final BooleanValue multipleTargets = new BooleanValue("Multiple Targets", true);
   @SerializedName("targetHUD")
   public final HudValue targetHUD = new HudValue("Target HUD", new TargetHUD.Inner(100.0F, 0.0F, HudPosition.Mode.MIDDLE_CENTER.ordinal(), 1.0F));
   @SerializedName("targets")
   private final Map<UUID, TargetHUD.Inner2> targets = new HashMap<>();
   @SerializedName("selectionOffset")
   public final SmoothFloat selectionOffset = new SmoothFloat(4.0F, 0.0F);

   public TargetHUD() {
      super(Category.Render, "TargetHUD", "目标显示");
   }

   @EventTarget
   public void onEntity(EntityEvent entityEvent) {
      this.updateEntity(entityEvent.entity);
   }

   @EventTarget(priority = -350234)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (KillAura.targets.isEmpty()) {
         this.clear();
      }

      if ((Boolean)this.multipleTargets.getValue()) {
         KillAura.targets.forEach(this::updateEntity);
      } else {
         this.updateEntity(KillAura.target);
      }
   }

   private void updateEntity(Entity entity) {
      if (entity instanceof AbstractClientPlayerEntity player) {
         UUID uuid = player.getUuid();
         TargetHUD.Inner2 inner2 = this.targets.computeIfAbsent(uuid, uuid2 -> new TargetHUD.Inner2(player));
         inner2.performAction(player);
      }
   }

   public List<TargetHUD.Inner2> collectItems() {
      this.targets.values().removeIf(inner2 -> inner2.lastUpdate.hasTimePassedDouble(10000.0));
      ArrayList list = new ArrayList();

      for (TargetHUD.Inner2 inner22 : this.targets.values()) {
         if ((Boolean)this.multipleTargets.getValue() || inner22.player == KillAura.target) {
            list.add(inner22);
         }
      }

      return list;
   }

   private void clear() {
      this.targets.clear();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.clear();
   }

   @Override
   public void onDisable() {
      this.clear();
      super.onDisable();
   }

   public TargetHUD.Mode getMode() {
      return switch (this.mode.getDisplayTextEx()) {
         case "Vanilla" -> TargetHUD.Mode.VANILLA;
         case "Retro" -> TargetHUD.Mode.RETRO;
         case "ModernVanilla" -> TargetHUD.Mode.MODERN_VANILLA;
         case "Old" -> TargetHUD.Mode.OLD;
         case "Plus" -> TargetHUD.Mode.PLUS;
         case "Myau" -> TargetHUD.Mode.MYAU;
         case "New" -> TargetHUD.Mode.NEW;
         case "Simple" -> TargetHUD.Mode.SIMPLE;
         default -> TargetHUD.Mode.SOUTHSIDE;
      };
   }

   private final class Inner extends Hud {
      public Inner(float f, float f2, int n, float f3) {
         super(f, f2, n, f3);
      }

      @Override
      public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
         TargetHUD.Mode mode = TargetHUD.this.getMode();
         List list = TargetHUD.this.collectItems();
         NanoVGHelper.INSTANCE.performAction(true, l -> this.draw(l, list, mode, f, f2, f3));
         if (mode.isValidEx()) {
            this.render(context, list, mode, f, f2, f3);
         }
      }

      private void draw(long l, List<TargetHUD.Inner2> list, TargetHUD.Mode mode, float f, float f2, float f3) {
         ScissorHelper scissor = ScissorHelper.INSTANCE;
         ScissorRect clip = mode.isValid() ? scissor.scissor(l, f - 5.0F * f3, f2, WindowMetrics.getScaledWidth(), WindowMetrics.getScaledHeight()) : null;

         try {
            float f4 = 0.0F;
            float f5 = -50.0F * f3;

            for (TargetHUD.Inner2 inner2 : list) {
               SmoothFloat timerUtil = inner2.getTimerUtil(mode);
               if (inner2.lastUpdate.hasTimePassedDouble(1000.0)) {
                  timerUtil.setTarget(-mode.exitDistance * f3);
               } else {
                  timerUtil.setTarget(f4);
                  f4 += mode.rowSpacing * f3;
               }

               if (inner2.player == KillAura.target) {
                  f5 = timerUtil.getValue();
               }

               inner2.performAction(mode, l, f3, f, f2);
            }

            if (mode == TargetHUD.Mode.SOUTHSIDE) {
               TargetHUD.this.selectionOffset.setTarget(f5);
               float f6 = TargetHUD.this.selectionOffset.getValue();
               if (f6 > -50.0F * f3) {
                  NanoVGHelper.INSTANCE
                     .drawRoundedRect(l, f - 5.0F * f3, f2 + f6 + 4.0F * f3, 2.0F * f3, Math.max(6.0F * f3, 34.0F * f3), -1, f3);
               }
            }
         } finally {
            if (mode.isValid()) {
               scissor.resetScissor(l, clip);
            }
         }
      }

      private void render(DrawContext context, List<TargetHUD.Inner2> list, TargetHUD.Mode mode, float f, float f2, float f3) {
         float f4 = mode.isValid() ? f - 5.0F * f3 : f;
         int n = mode == TargetHUD.Mode.SIMPLE ? (int)(f + mode.width * f3) : WindowMetrics.getScaledWidth();
         int n2 = mode == TargetHUD.Mode.SIMPLE ? (int)(f2 + 999.0F) : WindowMetrics.getScaledHeight();
         context.getMatrices().push();

         try {
            context.enableScissor((int)f4, (int)f2, n, n2);

            try {
               for (TargetHUD.Inner2 inner2 : list) {
                  inner2.performAction(mode, context, f3, f, f2);
               }
            } finally {
               context.disableScissor();
            }
         } finally {
            context.getMatrices().pop();
         }
      }

      @Override
      public float computeFloat(float f, boolean flag) {
         return TargetHUD.this.getMode().width * f;
      }

      @Override
      public float setScale(float f, boolean flag) {
         return TargetHUD.this.getMode().height * f;
      }
   }

   private static final class Inner2 {
      @SerializedName("player")
      public AbstractClientPlayerEntity player;
      @SerializedName("lastUpdate")
      public final NanoTimer lastUpdate = new NanoTimer();
      @SerializedName("vanilla")
      private final TargetHudCard vanilla;
      @SerializedName("retro")
      private final TargetHudModern retro;
      @SerializedName("modernVanilla")
      private final TargetHudRounded modernVanilla;
      @SerializedName("old")
      private final TargetHudPill old;
      @SerializedName("plus")
      private final TargetHudCompact plus;
      @SerializedName("myau")
      private final TargetHudRect myau;
      @SerializedName("newStyle")
      private final TargetHudLarge newStyle;
      @SerializedName("southside")
      private final TargetHudBlockRate southside;
      @SerializedName("simple")
      private final TargetHudSimple simple;

      public Inner2(AbstractClientPlayerEntity player2) {
         this.vanilla = new TargetHudCard(player2);
         this.retro = new TargetHudModern(player2);
         this.modernVanilla = new TargetHudRounded(player2);
         this.old = new TargetHudPill(player2);
         this.plus = new TargetHudCompact(player2);
         this.myau = new TargetHudRect(player2);
         this.newStyle = new TargetHudLarge(player2);
         this.southside = new TargetHudBlockRate(player2);
         this.simple = new TargetHudSimple(player2);
      }

      public void performAction(AbstractClientPlayerEntity player2) {
         this.player = player2;
         this.vanilla.player = player2;
         this.retro.player = player2;
         this.modernVanilla.player = player2;
         this.old.player = player2;
         this.plus.player = player2;
         this.myau.player = player2;
         this.newStyle.player = player2;
         this.southside.player = player2;
         this.simple.player = player2;
         this.lastUpdate.reset();
      }

      public SmoothFloat getTimerUtil(TargetHUD.Mode mode) {
         return switch (mode) {
            case VANILLA -> this.vanilla.slideOffset;
            case RETRO -> this.retro.offsetAnimation;
            case MODERN_VANILLA -> this.modernVanilla.offsetAnimation;
            case OLD -> this.old.offsetAnimation;
            case PLUS -> this.plus.offsetAnimation;
            case MYAU -> this.myau.offsetAnimation;
            case NEW -> this.newStyle.offsetAnimation;
            case SOUTHSIDE -> this.southside.offsetAnimation;
            case SIMPLE -> this.simple.offsetAnimation;
         };
      }

      public void performAction(TargetHUD.Mode mode, long l, float f, float f2, float f3) {
         switch (mode) {
            case VANILLA:
               this.vanilla.draw(l, f, f2, f3);
               break;
            case RETRO:
               this.retro.draw(l, f, f2, f3);
               break;
            case MODERN_VANILLA:
               this.modernVanilla.fromHsb(l, f, f2, f3);
               break;
            case OLD:
               this.old.draw(l, f, f2, f3);
               break;
            case PLUS:
               this.plus.fromHsb(l, f, f2, f3);
               break;
            case MYAU:
               this.myau.fromHsb(l, f, f2, f3);
               break;
            case NEW:
               this.newStyle.draw(l, f, f2, f3);
               break;
            case SOUTHSIDE:
               this.southside.draw(l, f, f2, f3);
               break;
            case SIMPLE:
               this.simple.draw(l, f, f2, f3);
         }
      }

      public void performAction(TargetHUD.Mode mode, DrawContext context, float f, float f2, float f3) {
         switch (mode) {
            case VANILLA:
               this.vanilla.performAction(f, f2, f3);
               break;
            case RETRO:
               this.retro.performAction(f, f2, f3);
               break;
            case MODERN_VANILLA:
               this.modernVanilla.render(context, f, f2, f3);
            case OLD:
            case NEW:
            default:
               break;
            case PLUS:
               this.plus.render(context, f, f2, f3);
               break;
            case MYAU:
               this.myau.render(context, f, f2, f3);
               break;
            case SOUTHSIDE:
               this.southside.render(context, f, f2, f3);
               break;
            case SIMPLE:
               this.simple.render(context, f, f2, f3);
         }
      }
   }

   private enum Mode {
      VANILLA(197.5F, 68.0F, 73.0F, 68.0F),
      RETRO(197.5F, 62.5F, 67.5F, 50.0F),
      MODERN_VANILLA(160.0F, 40.0F, 50.0F, 50.0F),
      OLD(200.0F, 40.0F, 50.0F, 50.0F),
      PLUS(200.0F, 40.0F, 50.0F, 50.0F),
      MYAU(160.0F, 40.0F, 50.0F, 50.0F),
      NEW(280.0F, 88.0F, 100.0F, 88.0F),
      SOUTHSIDE(120.0F, 42.0F, 50.0F, 50.0F),
      SIMPLE(170.0F, 44.0F, 48.0F, 50.0F);

      @SerializedName("width")
      public final float width;
      @SerializedName("height")
      public final float height;
      @SerializedName("rowSpacing")
      public final float rowSpacing;
      @SerializedName("exitDistance")
      public final float exitDistance;

      Mode(float x, float y, float width2, float height2) {
         this.width = x;
         this.height = y;
         this.rowSpacing = width2;
         this.exitDistance = height2;
      }

      public boolean isValid() {
         return this == VANILLA || this == RETRO || this == SOUTHSIDE;
      }

      public boolean isValidEx() {
         return this != OLD && this != NEW;
      }

      // $VF: synthetic method
      private static TargetHUD.Mode[] createValues() {
         return new TargetHUD.Mode[]{VANILLA, RETRO, MODERN_VANILLA, OLD, PLUS, MYAU, NEW, SOUTHSIDE, SIMPLE};
      }


   }
}
