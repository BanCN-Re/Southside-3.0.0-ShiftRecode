package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.UIMatrixStack;
import dev.southside.utils.ColorValue;
import dev.southside.utils.OneConfigTextRenderer;
import dev.southside.utils.ModeValue;
import dev.southside.utils.StringValue;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

public abstract class SingleTextHud extends TextHud {
   @SerializedName("brackets")
   public BooleanValue brackets = new BooleanValue("Brackets", false);
   @SerializedName("bracketsColor")
   public ColorValue bracketsColor = new ColorValue("Brackets Color", new OneColor(-1));
   @SerializedName("title")
   public StringValue title = new StringValue("Title", "", false, false, "", 1, "");
   @SerializedName("titleLocation")
   protected ModeValue titleLocation = new ModeValue("Title Location", new String[]{"Left", "Right"}, "", 1, 0);
   private static final long DEFAULT_LONG_VALUE = -1997452537610895240L;

   public SingleTextHud(
      String text,
      boolean flag,
      float f,
      float f2,
      float f3,
      boolean flag2,
      boolean flag3,
      float f4,
      float f5,
      float f6,
      OneColor color,
      boolean flag4,
      float f7,
      OneColor color2
   ) {
      super(f, f2, f3, flag2, flag3, f4, f5, f6, color, flag4, f7, color2);
      this.title.performAction(text);
   }

   public SingleTextHud(String text, boolean flag) {
      this(text, flag, 0.0F, 0.0F, 1.0F, true, false, 2.0F, 5.0F, 5.0F, new OneColor(0, 0, 0, (int)DEFAULT_LONG_VALUE), false, 2.0F, new OneColor(0, 0, 0));
   }

   protected abstract String getDisplayText();

   protected String getDisplayTextEx() {
      return null;
   }

   @Override
   public void performAction(List<String> list, boolean flag) {
      list.add(this.formatText(this.getDisplayText()));
   }

   @Override
   protected void clear(List<String> list, boolean flag) {
      String displayTextEx = this.getDisplayTextEx();
      if (displayTextEx != null) {
         list.clear();
         list.add(this.formatText(displayTextEx));
      }
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      float f4 = f;
      if ((Boolean)this.brackets.getValue()) {
         this.drawLine("[", f, f2, (OneColor)this.bracketsColor.getValue(), f3);
         f4 = f + this.computeFloat("[", f3);
      }

      this.drawLine(super.lines.get(0), f4, f2, f3);
      if ((Boolean)this.brackets.getValue()) {
         f4 += this.computeFloat(super.lines.get(0), f3);
         this.drawLine("]", f4, f2, (OneColor)this.bracketsColor.getValue(), f3);
      }
   }

   protected void drawLine(String text, float f, float f2, OneColor color, float f3) {
      OneConfigTextRenderer.applyText(text, f, f2, color.fromHsb(), OneConfigTextRenderer.Mode.fromOrdinal(super.textType.getValue()), f3);
   }

   protected final String formatText(String text) {
      boolean flag = !((String)this.title.getValue()).trim().isEmpty();
      StringBuilder sb = new StringBuilder();
      if (flag && this.titleLocation.getValue() == 0) {
         sb.append((String)this.title.getValue()).append(": ");
      }

      sb.append(text);
      if (flag && this.titleLocation.getValue() == 1) {
         sb.append(" ").append(this.title);
      }

      return sb.toString();
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      if (super.lines == null) {
         return 0.0F;
      } else {
         return !this.brackets.getValue() ? this.computeFloat(super.lines.get(0), f) : this.computeFloat("[" + super.lines.get(0) + "]", f);
      }
   }
}
