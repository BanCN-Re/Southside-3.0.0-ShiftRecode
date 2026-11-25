package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.ProjectionUtils;
import dev.southside.utils.ColorValue;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.NumberValue;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public class IronESP extends Module {
   private static final Set<Block> TARGET_BLOCKS = Set.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK, Blocks.IRON_BLOCK);
   @SerializedName("range")
   public final NumberValue range = new NumberValue("Range", 32, 16, 64, 1);
   @SerializedName("boxLines")
   public final BooleanValue boxLines = new BooleanValue("Box Lines", true);
   @SerializedName("boxSides")
   public final BooleanValue boxSides = new BooleanValue("Box Sides", true);
   @SerializedName("tag")
   public final BooleanValue tag = new BooleanValue("Tag", true);
   @SerializedName("lineColor")
   public final ColorValue lineColor = new ColorValue("Line Color", new OneColor(255, 255, 255, 200));
   @SerializedName("sideColor")
   public final ColorValue sideColor = new ColorValue("Side Color", new OneColor(255, 255, 255, 50));
   @SerializedName("labels")
   private final List<IronESP.Entry> labels = new ArrayList<>();

   public IronESP() {
      super(Category.Render, "IronESP", "铁矿石透视");
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (mc2.world != null && mc2.player != null) {
         this.labels.clear();
         int n = ((Number)this.range.getValue()).intValue();
         int n2 = n * n;
         Vec3d pos = mc2.player.getPos();
         ChunkPos chunkPos = mc2.player.getChunkPos();
         int n3 = (n >> 4) + 1;
         Color color = this.boxLines.getValue() ? ((OneColor)this.lineColor.getValue()).blendColor() : null;
         Color color2 = this.boxSides.getValue() ? ((OneColor)this.sideColor.getValue()).blendColor() : null;

         for (int i = chunkPos.x - n3; i <= chunkPos.x + n3; i++) {
            for (int j = chunkPos.z - n3; j <= chunkPos.z + n3; j++) {
               if (mc2.world.getChunkManager().isChunkLoaded(i, j)) {
                  WorldChunk chunk = mc2.world.getChunk(i, j);
                  ChunkSection[] sectionArray = chunk.getSectionArray();

                  for (int k = 0; k < sectionArray.length; k++) {
                     ChunkSection chunkSection = sectionArray[k];
                     if (!chunkSection.isEmpty()) {
                        this.performAction(chunkSection, i << 4, chunk.getBottomY() + (k << 4), j << 4, pos, n2, color, color2);
                     }
                  }
               }
            }
         }
      }
   }

   private void performAction(ChunkSection chunkSection, int n, int n2, int n3, Vec3d vec, int n4, Color color, Color color2) {
      for (int i = 0; i < 16; i++) {
         for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
               Block block = chunkSection.getBlockState(i, j, k).getBlock();
               if (TARGET_BLOCKS.contains(block)) {
                  int n5 = n + i;
                  int n6 = n2 + j;
                  int n7 = n3 + k;
                  double d = n5 + 0.5 - vec.x;
                  double d2 = n6 + 0.5 - vec.y;
                  double d3 = n7 + 0.5 - vec.z;
                  if (!(d * d + d2 * d2 + d3 * d3 > n4)) {
                     BlockPos pos = new BlockPos(n5, n6, n7);
                     Box box = new Box(pos);
                     if (color != null) {
                        BoxMeshRenderer.renderBox(box, color);
                     }

                     if (color2 != null) {
                        BoxMeshRenderer.renderBoxEx(box, color2);
                     }

                     if ((Boolean)this.tag.getValue()) {
                        this.labels.add(new IronESP.Entry(pos, formatText(block)));
                     }
                  }
               }
            }
         }
      }
   }

   private static String formatText(Block block) {
      if (block == Blocks.DEEPSLATE_IRON_ORE) {
         return "深层铁矿";
      } else if (block == Blocks.RAW_IRON_BLOCK) {
         return "粗铁块";
      } else {
         return block == Blocks.IRON_BLOCK ? "铁块" : "铁矿";
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (mc2.world != null && mc2.player != null && (Boolean)this.tag.getValue()) {
         DrawContext drawContext = drawContextEvent.getDrawContext();

         for (IronESP.Entry entry : this.labels) {
            Vec3d vec = entry.position.toCenterPos().add(0.0, 0.5, 0.0);
            Vec2f vec2f = ProjectionUtils.getVec2f(vec.x, vec.y, vec.z, 0.0, 0.0);
            if (vec2f != null) {
               int width = mc2.textRenderer.getWidth(entry.text);
               MatrixStack matrices = drawContext.getMatrices();
               matrices.push();

               try {
                  matrices.translate(vec2f.x, vec2f.y, 0.0F);
                  matrices.scale(0.8F, 0.8F, 1.0F);
                  drawContext.fill(-width / 2 - 3, -12, width / 2 + 3, 3, 1711276032);
                  drawContext.drawText(mc2.textRenderer, Text.literal(entry.text), -width / 2, -9, -1, true);
               } finally {
                  matrices.pop();
               }
            }
         }
      }
   }

   private record Entry(BlockPos position, String text) {

      public BlockPos getPosition() {
         return this.position;
      }

      public String getText() {
         return this.text;
      }
   }
}
