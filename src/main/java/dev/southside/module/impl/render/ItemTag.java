package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.render.ProjectionUtils;
import dev.southside.utils.EnchantmentUtils;
import dev.southside.utils.NumberValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ItemTag extends Module {
   @SerializedName("scale")
   public final NumberValue scale = new NumberValue("Scale", 0.8F, 0.2F, 3.0F, 0.1F);
   @SerializedName("mergeRadius")
   public final NumberValue mergeRadius = new NumberValue("Merge Radius", 1.0F, 0.0F, 5.0F, 0.5F);
   @SerializedName("backgroundAlpha")
   public final NumberValue backgroundAlpha = new NumberValue("Background Alpha", 0.15F, 0.05F, 1.0F, 0.05F);

   public ItemTag() {
      super(Category.Render, "ItemTag", "Display dropped item name tags");
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (mc2.world != null && mc2.player != null) {
         for (ItemTag.Inner2 inner2 : this.collectItems(drawContextEvent.getTickDelta())) {
            Vec3d vec = inner2.computePosition().add(0.0, 0.5, 0.0);
            Vec2f vec2f = ProjectionUtils.getVec2f(vec.x, vec.y, vec.z, 0.0, 0.0);
            if (vec2f != null) {
               this.renderTag(drawContextEvent.getDrawContext(), vec2f, inner2);
            }
         }
      }
   }

   private List<ItemTag.Inner2> collectItems(float f) {
      ArrayList list = new ArrayList();
      double d = ((Number)this.mergeRadius.getValue()).doubleValue();

      for (Entity entity : mc2.world.getEntities()) {
         if (entity instanceof ItemEntity itemEntity && !itemEntity.isRemoved() && !itemEntity.getStack().isEmpty()) {
            Vec3d lerpedPos = itemEntity.getLerpedPos(f);
            ItemTag.Inner2 inner2 = null;
            if (d > 0.0) {
               for (Object obj : list) {
                  ItemTag.Inner2 inner22 = (ItemTag.Inner2)obj;
                  if (inner22.computePosition().squaredDistanceTo(lerpedPos) <= d * d) {
                     inner2 = inner22;
                     break;
                  }
               }
            }

            if (inner2 == null) {
               inner2 = new ItemTag.Inner2();
               list.add(inner2);
            }

            inner2.performAction(itemEntity.getStack(), lerpedPos);
         }
      }

      return list;
   }

   private void renderTag(DrawContext context, Vec2f vec2, ItemTag.Inner2 inner2) {
      List list = inner2.collectItems();
      int n = 0;

      for (Object obj : list) {
         ItemTag.Entry entry = (ItemTag.Entry)obj;
         n = Math.max(n, mc2.textRenderer.getWidth(entry.getText()));
      }

      if (n > 0) {
         int n2 = n + 8;
         int n3 = 11 * list.size() + 4;
         int n4 = -n2 / 2;
         int n5 = -(n3 + 2);
         MatrixStack matrices = context.getMatrices();
         matrices.push();

         try {
            matrices.translate(vec2.x, vec2.y, 0.0F);
            float f = ((Number)this.scale.getValue()).floatValue();
            matrices.scale(f, f, 1.0F);
            int n6 = (int)(MathHelper.clamp(((Number)this.backgroundAlpha.getValue()).floatValue(), 0.0F, 1.0F) * 255.0F);
            context.fill(n4, n5, n4 + n2, n5 + n3, n6 << 24);
            int n7 = n5 + 2;

            for (Object obj2 : list) {
               ItemTag.Entry entry2 = (ItemTag.Entry)obj2;
               int n8 = n4 + (n2 - mc2.textRenderer.getWidth(entry2.getText())) / 2;
               context.drawText(mc2.textRenderer, entry2.getText(), n8, n7, entry2.getColor(), true);
               n7 += 11;
            }
         } finally {
            matrices.pop();
         }
      }
   }

   public static int getHighlightColor(ItemStack stack) {
      if (stack.isOf(Items.DIAMOND)) {
         return -10308865;
      }

      if (!stack.isOf(Items.EMERALD) && !stack.isOf(Items.SLIME_BALL)) {
         boolean flag = stack.getName().getString().contains("一刀")
            || stack.getComponents().toString().contains("一刀")
            || stack.isOf(Items.GOLDEN_AXE) && EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack) > 100;
         if (!flag && !stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            return stack.isOf(Items.END_CRYSTAL) ? -4027393 : -1;
         } else {
            return -9101;
         }
      } else {
         return -10099826;
      }
   }

   private record Entry(String text, int color) {
      public String getText() {
         return this.text;
      }

      public int getColor() {
         return this.color;
      }
   }

   private static final class Inner {
      @SerializedName("name")
      public final String name;
      @SerializedName("count")
      public int count;
      @SerializedName("color")
      public int color;

      public Inner(String text, int n) {
         this.name = text;
         this.color = n;
      }
   }

   private static final class Inner2 {
      @SerializedName("sumX")
      private double sumX;
      @SerializedName("sumY")
      private double sumY;
      @SerializedName("sumZ")
      private double sumZ;
      @SerializedName("sampleCount")
      private int sampleCount;
      @SerializedName("entries")
      private final Map<String, ItemTag.Inner> entries = new LinkedHashMap<>();

      public Inner2() {
      }

      public void performAction(ItemStack stack, Vec3d vec) {
         this.sumX = this.sumX + vec.x;
         this.sumY = this.sumY + vec.y;
         this.sumZ = this.sumZ + vec.z;
         this.sampleCount++;
         String string = stack.getName().getString();
         int highlightColor = ItemTag.getHighlightColor(stack);
         ItemTag.Inner inner = this.entries.computeIfAbsent(string, text -> new ItemTag.Inner(string, highlightColor));
         inner.count = inner.count + stack.getCount();
         if (inner.color == -1 && highlightColor != -1) {
            inner.color = highlightColor;
         }
      }

      public Vec3d computePosition() {
         return this.sampleCount == 0 ? Vec3d.ZERO : new Vec3d(this.sumX / this.sampleCount, this.sumY / this.sampleCount, this.sumZ / this.sampleCount);
      }

      public List<ItemTag.Entry> collectItems() {
         ArrayList list = new ArrayList();

         for (ItemTag.Inner inner : this.entries.values()) {
            list.add(new ItemTag.Entry(inner.count + "x " + inner.name, inner.color));
         }

         return list;
      }
   }
}
