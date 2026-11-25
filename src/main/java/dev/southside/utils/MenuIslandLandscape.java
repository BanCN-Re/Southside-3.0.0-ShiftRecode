package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.misc.StartRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;

public final class MenuIslandLandscape {
   private static final int DEFAULT_INT = 15728880;
   private static final int DEFAULT_INT_VALUE = 32;
   private static final int DEFAULT_INT_DATA = -2;
   private static final int DEFAULT_INT_REF = -1;
   private static final Direction[] DIRECTION_ARRAY = Direction.values();
   private static final BlockPos targetPos = new BlockPos(-2, 0, -1);
   private static final float[] DEFAULT_FLOATS = new float[33];
   private static final float[] DEFAULT_FLOATS_VALUE = new float[33];
   @SerializedName("initialIsland")
   private final List<MenuIslandLandscape.Entry> initialIsland = collectItems();
   @SerializedName("initialTree")
   private final List<MenuIslandLandscape.Entry> initialTree = collectItemsEx();
   @SerializedName("island")
   private final List<MenuIslandLandscape.Entry> island = new ArrayList<>();
   @SerializedName("tree")
   private final List<MenuIslandLandscape.Entry> tree = new ArrayList<>();
   @SerializedName("collisions")
   private final List<VoxelShape> collisions = new ArrayList<>();
   @SerializedName("blocksByPos")
   private final Map<BlockPos, MenuIslandLandscape.Entry> blocksByPos = new HashMap<>();
   @SerializedName("modelRandom")
   private final Random modelRandom = Random.create();

   public MenuIslandLandscape() {
      this.performAction();
   }

   public void performAction() {
      this.island.clear();
      this.island.addAll(this.initialIsland);
      this.tree.clear();
      this.tree.addAll(this.initialTree);
      this.performActionEx();
   }

   public MenuIslandLandscape.Entry2 raycast(StartRecord startRecord) {
      if (startRecord == null) {
         return null;
      }

      MenuIslandLandscape.Entry2 hitResult = raycast(this.island, startRecord, null);
      return raycast(this.tree, startRecord, hitResult);
   }

   private static MenuIslandLandscape.Entry2 raycast(List<MenuIslandLandscape.Entry> list, StartRecord startRecord, MenuIslandLandscape.Entry2 entry2) {
      for (MenuIslandLandscape.Entry entry : list) {
         BlockHitResult hitResult = entry.outline.raycast(startRecord.getStart(), startRecord.getEnd(), entry.pos);
         if (hitResult != null) {
            double d = startRecord.computeDouble(hitResult.getPos());
            if (entry2 == null || d < entry2.fraction) {
               entry2 = new MenuIslandLandscape.Entry2(entry, d);
            }
         }
      }

      return entry2;
   }

   public boolean isValid(MenuIslandLandscape.Entry entry) {
      if (!this.island.remove(entry) && !this.tree.remove(entry)) {
         return false;
      }

      this.performActionEx();
      return true;
   }

   public List<VoxelShape> getCollisions() {
      return this.collisions;
   }

   private void performActionEx() {
      this.collisions.clear();
      this.blocksByPos.clear();

      for (MenuIslandLandscape.Entry entry : this.island) {
         this.performAction(entry);
      }

      for (MenuIslandLandscape.Entry entry2 : this.tree) {
         this.performAction(entry2);
      }
   }

   private void performAction(MenuIslandLandscape.Entry entry) {
      this.blocksByPos.put(entry.pos, entry);
      VoxelShape collisionShape = entry.state.getCollisionShape(EmptyBlockView.INSTANCE, entry.pos);
      if (!collisionShape.isEmpty()) {
         this.collisions.add(collisionShape.offset(entry.pos.getX(), entry.pos.getY(), entry.pos.getZ()));
      }
   }

   public void drawOutline(MatrixStack matrices, VertexConsumerProvider vertexConsumers, MenuIslandLandscape.Entry entry) {
      VertexRendering.drawOutline(
         matrices, vertexConsumers.getBuffer(MenuRenderLayers.MENU_BLOCK_OUTLINE), entry.outline, entry.pos.getX(), entry.pos.getY(), entry.pos.getZ(), 1711276032
      );
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
      for (MenuIslandLandscape.Entry entry : this.island) {
         this.render(matrices, vertexConsumers, entry);
      }

      for (MenuIslandLandscape.Entry entry2 : this.tree) {
         this.render(matrices, vertexConsumers, entry2);
      }
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float f) {
      if (this.blocksByPos.containsKey(targetPos) && this.isHovered(-1.5F, -0.5F, 1.3F, 0.65F)) {
         render(matrices, vertexConsumers, -1.5F, -0.5F, 1.3F, 0.65F, 65);
      }

      if (f > 0.005F && this.isHovered(0.55F, 0.76F, 0.48F, 0.25F)) {
         render(matrices, vertexConsumers, 0.55F, 0.76F, 0.48F, 0.25F, (int)(50.0F * f));
      }
   }

   private boolean isHovered(float x, float y, float width, float height) {
      for (int i = (int)Math.floor(x - width); i <= (int)Math.floor(x + width); i++) {
         for (int j = (int)Math.floor(y - height); j <= (int)Math.floor(Math.min(0.99F, y + height)); j++) {
            BlockPos pos = new BlockPos(i, -1, j);
            if (!this.blocksByPos.containsKey(pos)) {
               return false;
            }
         }
      }

      return true;
   }

   private static List<MenuIslandLandscape.Entry> collectItems() {
      return collectItems(SkyblockIslandTemplate.getEntries());
   }

   private static List<MenuIslandLandscape.Entry> collectItemsEx() {
      return collectItems(SkyblockIslandTemplate.getEntriesValue());
   }

   private static List<MenuIslandLandscape.Entry> collectItems(List<SkyblockIslandTemplate.Entry> list) {
      ArrayList list2 = new ArrayList(list.size());

      for (SkyblockIslandTemplate.Entry entry : list) {
         list2.add(new MenuIslandLandscape.Entry(entry.getState(), entry.getPos()));
      }

      return List.copyOf(list2);
   }

   private void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, MenuIslandLandscape.Entry entry) {
      matrices.push();

      try {
         matrices.translate(entry.pos.getX(), entry.pos.getY(), entry.pos.getZ());
         BakedModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(entry.state);
         VertexConsumer buffer = vertexConsumers.getBuffer(MenuRenderLayers.MENU_BLOCKS);
         long renderingSeed = entry.state.getRenderingSeed(entry.pos);

         for (Direction direction : DIRECTION_ARRAY) {
            MenuIslandLandscape.Entry entry2 = this.blocksByPos.get(entry.pos.offset(direction));
            BlockState state = entry2 == null ? Blocks.AIR.getDefaultState() : entry2.state;
            if (Block.shouldDrawSide(entry.state, state, direction)) {
               this.modelRandom.setSeed(renderingSeed);
               render(matrices, buffer, entry.state, model.getQuads(entry.state, direction, this.modelRandom));
            }
         }

         this.modelRandom.setSeed(renderingSeed);
         render(matrices, buffer, entry.state, model.getQuads(entry.state, null, this.modelRandom));
      } finally {
         matrices.pop();
      }
   }

   private static void render(MatrixStack matrices, VertexConsumer consumer, BlockState state, List<BakedQuad> list) {
      BlockColors blockColors = MinecraftClient.getInstance().getBlockColors();

      for (BakedQuad bakedQuad : list) {
         int n = bakedQuad.hasTint() ? blockColors.getColor(state, null, null, bakedQuad.getTintIndex()) : 16777215;
         consumer.quad(
            matrices.peek(), bakedQuad, (n >> 16 & 0xFF) / 255.0F, (n >> 8 & 0xFF) / 255.0F, (n & 0xFF) / 255.0F, 1.0F, 15728880, OverlayTexture.DEFAULT_UV
         );
      }
   }

   private static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float f, float f2, float f3, float f4, int n) {
      VertexConsumer buffer = vertexConsumers.getBuffer(MenuRenderLayers.MENU_SHADOW);

      for (int i = 0; i < 32; i++) {
         buffer.vertex(matrices.peek(), f, 0.006F, f2).color(n << 24);
         buffer.vertex(matrices.peek(), f + DEFAULT_FLOATS[i] * f3, 0.006F, Math.min(0.99F, f2 + DEFAULT_FLOATS_VALUE[i] * f4)).color(0);
         buffer.vertex(matrices.peek(), f + DEFAULT_FLOATS[i + 1] * f3, 0.006F, Math.min(0.99F, f2 + DEFAULT_FLOATS_VALUE[i + 1] * f4)).color(0);
         buffer.vertex(matrices.peek(), f, 0.006F, f2).color(n << 24);
      }
   }

   static {
      for (int i = 0; i <= 32; i++) {
         double d = i * Math.PI * 2.0 / 32.0;
         DEFAULT_FLOATS[i] = (float)Math.cos(d);
         DEFAULT_FLOATS_VALUE[i] = (float)Math.sin(d);
      }
   }

   public record Entry(BlockState state, BlockPos pos, VoxelShape outline) {

      public Entry(BlockState state2, BlockPos pos2) {
         this(state2, pos2, getVoxelShape(state2, pos2));
      }

      public Entry(BlockState state2, int n, int n2, int n3) {
         this(state2, new BlockPos(n, n2, n3), getVoxelShape(state2, new BlockPos(n, n2, n3)));
      }

      private static VoxelShape getVoxelShape(BlockState state2, BlockPos pos2) {
         VoxelShape outlineShape = state2.getOutlineShape(EmptyBlockView.INSTANCE, pos2);
         if (state2.getBlock() instanceof FlowerBlock) {
            Vec3d modelOffset = state2.getModelOffset(pos2);
            outlineShape = outlineShape.offset(-modelOffset.x, -modelOffset.y, -modelOffset.z);
         }

         return outlineShape;
      }

      public BlockState getState() {
         return this.state;
      }

      public BlockPos getPos() {
         return this.pos;
      }

      public VoxelShape getOutline() {
         return this.outline;
      }
   }

   public record Entry2(MenuIslandLandscape.Entry block, double fraction) {

      public MenuIslandLandscape.Entry getBlock() {
         return this.block;
      }

      public double getFraction() {
         return this.fraction;
      }
   }
}
