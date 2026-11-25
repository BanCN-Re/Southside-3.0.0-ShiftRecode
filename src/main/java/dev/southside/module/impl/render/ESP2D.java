package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.ProjectionUtils;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.FrustumUtils;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import java.awt.Color;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ESP2D extends Module {
   private static final int DEFAULT_INT = -1778384896;
   private static final int DEFAULT_INT_VALUE = -1;
   @SerializedName("players")
   public final BooleanValue players = new BooleanValue("Players", true);
   @SerializedName("animals")
   public final BooleanValue animals = new BooleanValue("Animals", true);
   @SerializedName("mobs")
   public final BooleanValue mobs = new BooleanValue("Mobs", false);
   @SerializedName("invisible")
   public final BooleanValue invisible = new BooleanValue("Invisible", false);
   @SerializedName("passives")
   public final BooleanValue passives = new BooleanValue("Passives", true);
   @SerializedName("droppedItems")
   public final BooleanValue droppedItems = new BooleanValue("Dropped Items", false);
   @SerializedName("healthBar")
   public final BooleanValue healthBar = new BooleanValue("Health Bar", true);
   @SerializedName("tags")
   public final BooleanValue tags = new BooleanValue("Tags", true);
   @SerializedName("filled")
   public final BooleanValue filled = new BooleanValue("Filled", true);
   @SerializedName("box")
   public final BooleanValue box = new BooleanValue("Box", true);
   @SerializedName("boxMode")
   public final ModeValue boxMode = new ModeValue("BoxMode", "Box", "Box", "Corners");
   @SerializedName("tagSize")
   public final NumberValue tagSize = new NumberValue("Tag Size", 5.0, 1.0, 20.0, 0.1);
   @SerializedName("thickness")
   public final NumberValue thickness = new NumberValue("Thickness", 1.5F, 0.25F, 5.0F, 0.25F);

   public ESP2D() {
      super(Category.Render, "ESP2D", "啥都视奸 (2D版本)");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.boxMode.getLabel(), () -> !(Boolean)this.box.getValue());
      this.hideIf(this.tagSize.getLabel(), () -> !(Boolean)this.tags.getValue());
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (mc2.player != null && mc2.world != null) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.performAction(true, l -> {
            for (Entity entity : mc2.world.getEntities()) {
               if (this.isValidTarget(entity) && FrustumUtils.isValidTarget(entity) && !(mc2.player.distanceTo(entity) < 1.0F)) {
                  ESP2D.Entry entry = this.getEntry(entity, drawContextEvent.getTickDelta());
                  if (entry != null) {
                     if ((Boolean)this.healthBar.getValue() && entity instanceof LivingEntity living) {
                        this.fromHsb(nvg, l, entry, living);
                     }

                     if ((Boolean)this.tags.getValue()) {
                        this.performAction(nvg, l, entry, entity);
                     }

                     if ((Boolean)this.filled.getValue()) {
                        nvg.drawEx(l, entry.getLeft(), entry.getTop(), entry.getRight(), entry.getBottom(), 2030043135);
                     }

                     if ((Boolean)this.box.getValue()) {
                        if (this.boxMode.matches("Box")) {
                           this.performAction(nvg, l, entry);
                        } else if (this.boxMode.matches("Corners")) {
                           this.performActionEx(nvg, l, entry);
                        }
                     }
                  }
               }
            }
         });
      }
   }

   private ESP2D.Entry getEntry(Entity entity, float f) {
      Vec3d lerpedPos = entity.getLerpedPos(f);
      double d = entity.getWidth() / 1.5;
      double d2 = entity.getHeight() + (entity.isSneaking() ? -0.3 : 0.2) + 0.05;
      Box box2 = new Box(lerpedPos.x - d, lerpedPos.y, lerpedPos.z - d, lerpedPos.x + d, lerpedPos.y + d2, lerpedPos.z + d);
      double positive_infinity = Double.POSITIVE_INFINITY;
      double positive_infinity2 = Double.POSITIVE_INFINITY;
      double negative_infinity = Double.NEGATIVE_INFINITY;
      double negative_infinity2 = Double.NEGATIVE_INFINITY;

      for (int i = 0; i < 8; i++) {
         Vec3d vec = ProjectionUtils.computePosition(
            new Vec3d((i & 2) == 0 ? box2.minX : box2.maxX, (i & 1) == 0 ? box2.minY : box2.maxY, (i & 4) == 0 ? box2.minZ : box2.maxZ)
         );
         if (vec != null && vec.z >= 0.0 && vec.z < 1.0 && Double.isFinite(vec.x) && Double.isFinite(vec.y)) {
            positive_infinity = Math.min(positive_infinity, vec.x);
            positive_infinity2 = Math.min(positive_infinity2, vec.y);
            negative_infinity = Math.max(negative_infinity, vec.x);
            negative_infinity2 = Math.max(negative_infinity2, vec.y);
         }
      }

      return Double.isFinite(positive_infinity) ? new ESP2D.Entry(positive_infinity, positive_infinity2, negative_infinity, negative_infinity2) : null;
   }

   private void fromHsb(NanoVGHelper nvg, long l, ESP2D.Entry entry, LivingEntity living) {
      double left = entry.getLeft();
      double top = entry.getTop();
      double bottom = entry.getBottom();
      float f = living.getMaxHealth() > 0.0F ? MathHelper.clamp(living.getHealth() / living.getMaxHealth(), 0.0F, 1.0F) : 0.0F;
      if (living.getHealth() > 0.0F) {
         nvg.draw(l, left - 4.0, top - 0.5, 2.0, entry.getCurrentValue() + 0.5, 0.5, -1778384896);
         nvg.drawEx(l, left - 4.0, top - 0.5, left - 2.0, bottom + 0.5, 1174405120);
         int n = Color.HSBtoRGB(f / 3.0F, 1.0F, 0.8F) | 0xFF000000;
         nvg.drawEx(l, left - 3.5, bottom - entry.getCurrentValue() * f, left - 2.5, bottom, n);
      }

      if (entry.getCurrentValue() - 0.5 > 50.0) {
         for (int i = 1; i < 10; i++) {
            double d = bottom - 0.5 + (top - bottom + 0.5) / 10.0 * i;
            nvg.drawEx(l, left - 3.5, d, left - 2.5, d - 1.0, -1778384896);
         }
      }
   }

   private void performAction(NanoVGHelper nvg, long l, ESP2D.Entry entry, Entity entity) {
      String text = entity instanceof ItemEntity itemEntity ? itemEntity.getStack().getName().getString() : entity.getDisplayName().getString();
      float f = ((Number)this.tagSize.getValue()).floatValue();
      float textWidth = nvg.getTextWidth(l, text, f, OneConfigFonts.WQY_MICROHEI);
      float f2 = (float)((entry.getLeft() + entry.getRight()) / 2.0) - textWidth / 2.0F;
      float f3 = (float)(entry.getTop() - 10.0);
      nvg.drawRect(l, f2, f3, textWidth, nvg.getTextHeight(l, f, OneConfigFonts.WQY_MICROHEI), 1677721600);
      nvg.drawEx(l, text, f2, f3, -1, f, OneConfigFonts.WQY_MICROHEI);
   }

   private void performAction(NanoVGHelper nvg, long l, ESP2D.Entry entry) {
      double left = entry.getLeft();
      double top = entry.getTop();
      double right = entry.getRight();
      double bottom = entry.getBottom();
      double d = ((Number)this.thickness.getValue()).doubleValue();
      nvg.drawEx(l, left - 1.0, top, left + d, bottom + 0.5, -1778384896);
      nvg.drawEx(l, left - 1.0, top - 0.5, right + 0.5, top + 0.5 + d, -1778384896);
      nvg.drawEx(l, right - 0.5 - d, top, right + 0.5, bottom + 0.5, -1778384896);
      nvg.drawEx(l, left - 1.0, bottom - d - 0.5, right + 0.5, bottom + 0.5, -1778384896);
      nvg.drawEx(l, left - 0.5, top, left + d - 0.5, bottom, -1);
      nvg.drawEx(l, left, bottom - d, right, bottom, -1);
      nvg.drawEx(l, left - 0.5, top, right, top + d, -1);
      nvg.drawEx(l, right - d, top, right, bottom, -1);
   }

   private void performActionEx(NanoVGHelper nvg, long l, ESP2D.Entry entry) {
      double left = entry.getLeft();
      double top = entry.getTop();
      double right = entry.getRight();
      double bottom = entry.getBottom();
      double d = (right - left) / 3.0;
      double d2 = entry.getCurrentValue() / 4.0;
      nvg.drawEx(l, left + 0.5, top, left - 1.0, top + d2 + 0.5, -1778384896);
      nvg.drawEx(l, left - 1.0, bottom, left + 0.5, bottom - d2 - 0.5, -1778384896);
      nvg.drawEx(l, left - 1.0, top - 0.5, left + d + 0.5, top + 1.0, -1778384896);
      nvg.drawEx(l, right - d - 0.5, top - 0.5, right, top + 1.0, -1778384896);
      nvg.drawEx(l, right - 1.0, top, right + 0.5, top + d2 + 0.5, -1778384896);
      nvg.drawEx(l, right - 1.0, bottom, right + 0.5, bottom - d2 - 0.5, -1778384896);
      nvg.drawEx(l, left - 1.0, bottom - 1.0, left + d + 0.5, bottom + 0.5, -1778384896);
      nvg.drawEx(l, right - d - 0.5, bottom - 1.0, right + 0.5, bottom + 0.5, -1778384896);
      nvg.drawEx(l, left, top, left - 0.5, top + d2, -1);
      nvg.drawEx(l, left, bottom, left - 0.5, bottom - d2, -1);
      nvg.drawEx(l, left - 0.5, top, left + d, top + 0.5, -1);
      nvg.drawEx(l, right - d, top, right, top + 0.5, -1);
      nvg.drawEx(l, right - 0.5, top, right, top + d2, -1);
      nvg.drawEx(l, right - 0.5, bottom, right, bottom - d2, -1);
      nvg.drawEx(l, left, bottom - 0.5, left + d, bottom, -1);
      nvg.drawEx(l, right - d, bottom - 0.5, right - 0.5, bottom, -1);
   }

   private boolean isValidTarget(Entity entity) {
      if (entity instanceof LivingEntity living) {
         boolean flag = (Boolean)this.players.getValue() && living instanceof PlayerEntity
            || (Boolean)this.mobs.getValue() && (living instanceof MobEntity || living instanceof SlimeEntity)
            || (Boolean)this.passives.getValue() && (living instanceof VillagerEntity || living instanceof GolemEntity)
            || (Boolean)this.animals.getValue() && living instanceof AnimalEntity;
         return flag && entity.isAlive() && (!entity.isInvisible() || (Boolean)this.invisible.getValue());
      } else {
         return (Boolean)this.droppedItems.getValue() && entity instanceof ItemEntity;
      }
   }

   private record Entry(double left, double top, double right, double bottom) {
      public double getCurrentValue() {
         return this.bottom - this.top;
      }

      public double getLeft() {
         return this.left;
      }

      public double getTop() {
         return this.top;
      }

      public double getRight() {
         return this.right;
      }

      public double getBottom() {
         return this.bottom;
      }
   }
}
