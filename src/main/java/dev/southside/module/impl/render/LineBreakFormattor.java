package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class LineBreakFormattor extends Module {
   public LineBreakFormattor() {
      super(Category.Render, "LineBreakFormattor", "Render nametags that contain literal \\n on multiple lines.");
   }

   public boolean renderMultilineLabel(EntityRenderState entityRenderState, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n) {
      if (this.isEnabled() && text != null && entityRenderState != null) {
         EntityRenderDispatcher entityRenderDispatcher = Module.mc.getEntityRenderDispatcher();
         if (entityRenderDispatcher == null || Module.mc.textRenderer == null || Module.mc.options == null) {
            return false;
         }

         if (entityRenderState.squaredDistanceToCamera > 4096.0) {
            return false;
         }

         Vec3d nameLabelPos = entityRenderState.nameLabelPos;
         if (nameLabelPos == null) {
            return false;
         }

         LineBreakFormattor.Entry entry = getEntry(text.asOrderedText());
         if (!entry.isHasLineBreak()) {
            return false;
         }

         List lines = entry.getLines();
         boolean sneaking = entityRenderState.sneaking;
         TextRenderer textRenderer = Module.mc.textRenderer;
         float f = "deadmau5".equals(text.getString()) ? -10.0F : 0.0F;
         float f2 = 11.0F;
         float f3 = f - (lines.size() - 1) * f2 / 2.0F;
         matrices.push();

         try {
            matrices.translate(nameLabelPos.x, nameLabelPos.y + 0.5, nameLabelPos.z);
            matrices.multiply(entityRenderDispatcher.getRotation());
            matrices.scale(0.025F, -0.025F, 0.025F);
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            float textBackgroundOpacity = Module.mc.options.getTextBackgroundOpacity(0.25F);
            int n2 = (int)(textBackgroundOpacity * 255.0F) << 24;

            for (Object obj : lines) {
               OrderedText orderedText = (OrderedText)obj;
               render(textRenderer, positionMatrix, vertexConsumers, orderedText, sneaking, n2, n, f3);
               f3 += f2;
            }
         } finally {
            matrices.pop();
         }

         return true;
      } else {
         return false;
      }
   }

   private static void render(TextRenderer textRenderer, Matrix4f matrix, VertexConsumerProvider vertexConsumers, OrderedText orderedText, boolean flag, int n, int n2, float f) {
      float f2 = -textRenderer.getWidth(orderedText) / 2.0F;
      TextLayerType textLayerType = flag ? TextLayerType.SEE_THROUGH : TextLayerType.NORMAL;
      textRenderer.draw(orderedText, f2, f, 553648127, false, matrix, vertexConsumers, textLayerType, n, n2);
      if (!flag) {
         textRenderer.draw(orderedText, f2, f, -1, false, matrix, vertexConsumers, TextLayerType.NORMAL, 0, n2);
      }
   }

   private static LineBreakFormattor.Entry getEntry(OrderedText orderedText) {
      LineBreakFormattor.Class5224Impl class5224Impl = new LineBreakFormattor.Class5224Impl();
      orderedText.accept(class5224Impl);
      return class5224Impl.getEntry();
   }

   public static OrderedText getOrderedText(List<LineBreakFormattor.Entry2> list) {
      List list2 = List.copyOf(list);
      return characterVisitor -> {
         int n = 0;

         for (Object obj : list2) {
            LineBreakFormattor.Entry2 entry2 = (LineBreakFormattor.Entry2)obj;
            if (!characterVisitor.accept(n++, entry2.getStyle(), entry2.getCodePoint())) {
               return false;
            }
         }

         return true;
      };
   }

   private static final class Class5224Impl implements CharacterVisitor {
      @SerializedName("lines")
      private final List<OrderedText> lines = new ArrayList<>();
      @SerializedName("currentLine")
      private final List<LineBreakFormattor.Entry2> currentLine = new ArrayList<>();
      @SerializedName("pendingBackslash")
      private LineBreakFormattor.Entry2 pendingBackslash;
      @SerializedName("hasLineBreak")
      private boolean hasLineBreak;

      public Class5224Impl() {
      }

      public boolean accept(int n, Style style, int n2) {
         if (this.pendingBackslash != null) {
            if (n2 == 110) {
               this.lines.add(LineBreakFormattor.getOrderedText(this.currentLine));
               this.currentLine.clear();
               this.pendingBackslash = null;
               this.hasLineBreak = true;
               return true;
            }

            this.currentLine.add(this.pendingBackslash);
            this.pendingBackslash = null;
         }

         if (n2 == 92) {
            this.pendingBackslash = new LineBreakFormattor.Entry2(style, n2);
         } else {
            this.currentLine.add(new LineBreakFormattor.Entry2(style, n2));
         }

         return true;
      }

      public LineBreakFormattor.Entry getEntry() {
         if (this.pendingBackslash != null) {
            this.currentLine.add(this.pendingBackslash);
         }

         this.lines.add(LineBreakFormattor.getOrderedText(this.currentLine));
         return new LineBreakFormattor.Entry(this.lines, this.hasLineBreak);
      }
   }

   private record Entry(List<OrderedText> lines, boolean hasLineBreak) {
      public List<OrderedText> getLines() {
         return this.lines;
      }

      public boolean isHasLineBreak() {
         return this.hasLineBreak;
      }
   }

   private record Entry2(Style style, int codePoint) {
      public Style getStyle() {
         return this.style;
      }

      public int getCodePoint() {
         return this.codePoint;
      }
   }
}
