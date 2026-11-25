package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.render.ProjectionUtils;
import dev.southside.utils.LoadedBlockEntityIterator;
import dev.southside.utils.SlotIndexMapper;
import dev.southside.utils.NumberValue;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class BedShow extends Module {
   private static final int DEFAULT_INT = 3;
   private static final int DEFAULT_INT_VALUE = 12;
   private static final int DEFAULT_INT_DATA = 4;
   private static final int DEFAULT_INT_REF = 16;
   private static final int DEFAULT_INT_PRIMARY = 2;
   private static final int DEFAULT_INT_SECONDARY = 4;
   @SerializedName("searchRadius")
   public final NumberValue searchRadius = new NumberValue("Search Radius", 18.0F, 6.0F, 48.0F, 1.0F);
   @SerializedName("scale")
   public final NumberValue scale = new NumberValue("Scale", 1.0F, 0.5F, 2.5F, 0.05F);
   @SerializedName("backgroundAlpha")
   public final NumberValue backgroundAlpha = new NumberValue("Background Alpha", 0.2F, 0.0F, 1.0F, 0.05F);

   public BedShow() {
      super(Category.Render, "BedShow", "显示床防御所用方块");
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (mc2.world != null && mc2.player != null) {
         double d = ((Number)this.searchRadius.getValue()).doubleValue() + 3.0;
         LoadedBlockEntityIterator loadedBlockEntityIterator = new LoadedBlockEntityIterator();

         while (loadedBlockEntityIterator.hasNext()) {
            if (loadedBlockEntityIterator.next() instanceof BedBlockEntity bedBlockEntity) {
               BlockPos pos = bedBlockEntity.getPos();
               if (!(mc2.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > d * d)) {
                  BlockState blockState = mc2.world.getBlockState(pos);
                  if (blockState.getBlock() instanceof BedBlock && blockState.get(BedBlock.PART) == BedPart.HEAD) {
                     List list = this.collectItems(pos, blockState);
                     if (!list.isEmpty()) {
                        Vec3d vec = Vec3d.ofCenter(pos).add(0.0, 0.75, 0.0);
                        Vec2f vec2f = ProjectionUtils.getVec2f(vec.x, vec.y, vec.z, 0.0, 0.0);
                        if (vec2f != null) {
                           this.renderBedInfo(drawContextEvent.getDrawContext(), vec2f, list);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private List<ItemStack> collectItems(BlockPos pos, BlockState state) {
      BlockPos pos2 = pos.offset(((Direction)state.get(BedBlock.FACING)).getOpposite());
      LinkedHashSet linkedHashSet = new LinkedHashSet<>(List.of(pos, pos2));
      LinkedHashSet linkedHashSet2 = new LinkedHashSet();

      for (Object obj : linkedHashSet) {
         BlockPos pos3 = (BlockPos)obj;
         short s = 10343;
         linkedHashSet2.add(pos3.up());
         long l = 4586040403168492164L;

         for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
               BlockPos pos4 = pos3.add(i, 0, j);
               if (!linkedHashSet.contains(pos4)) {
                  linkedHashSet2.add(pos4);
               }
            }
         }
      }

      ArrayList list = new ArrayList();
      LinkedHashSet linkedHashSet3 = new LinkedHashSet();

      for (Object obj2 : linkedHashSet2) {
         BlockPos pos5 = (BlockPos)obj2;
         BlockState blockState = mc2.world.getBlockState(pos5);
         if (!blockState.isAir() && SlotIndexMapper.isValidBlock(blockState.getBlock())) {
            Item item = blockState.getBlock().asItem();
            if (item != Items.AIR && linkedHashSet3.add(item)) {
               list.add(new ItemStack(item));
               if (list.size() == 12) {
                  break;
               }
            }
         }
      }

      return list;
   }

   private void renderBedInfo(DrawContext context, Vec2f vec2, List<ItemStack> list) {
      int n = (list.size() + 4 - 1) / 4;
      int n2 = Math.min(4, list.size());
      int n3 = n2 * 16 + (n2 - 1) * 2 + 8;
      int n4 = n * 16 + (n - 1) * 2 + 8;
      int n5 = -n3 / 2;
      int n6 = -(n4 + 2);
      MatrixStack matrices = context.getMatrices();
      matrices.push();

      try {
         matrices.translate(vec2.x, vec2.y, 0.0F);
         float f = ((Number)this.scale.getValue()).floatValue();
         matrices.scale(f, f, 1.0F);
         int n7 = (int)(MathHelper.clamp(((Number)this.backgroundAlpha.getValue()).floatValue(), 0.0F, 1.0F) * 255.0F);
         context.fill(n5, n6, n5 + n3, n6 + n4, n7 << 24);

         for (int i = 0; i < list.size(); i++) {
            ItemStack stack = ((ItemStack)list.get(i)).copy();
            if (!stack.isEmpty()) {
               stack.setCount(1);
               int n8 = n5 + 4 + i % 4 * 18;
               int n9 = n6 + 4 + i / 4 * 18;
               context.drawItem(stack, n8, n9);
               if (mc2.textRenderer != null) {
                  context.drawStackOverlay(mc2.textRenderer, stack, n8, n9);
               }
            }
         }
      } finally {
         matrices.pop();
      }
   }
}
