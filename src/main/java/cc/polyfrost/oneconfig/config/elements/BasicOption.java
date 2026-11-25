package cc.polyfrost.oneconfig.config.elements;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.values.Value;
import dev.southside.utils.InputHandler;
import dev.southside.utils.ColorUtils;
import dev.southside.utils.OneConfigColors;
import dev.southside.utils.WindowMetrics;
import dev.southside.utils.TooltipRenderer;
import dev.southside.utils.GuiUtils;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.ColorAnimation;
import dev.southside.utils.UiAnimation;
import dev.southside.utils.DummyAnimation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Supplier;

public abstract class BasicOption {
   @SerializedName("size")
   public final int size;
   @SerializedName("field")
   protected final Field field;
   @SerializedName("parent")
   public Object parent;
   @SerializedName("name")
   public final String name;
   @SerializedName("description")
   public final String description;
   @SerializedName("category")
   public String category;
   @SerializedName("subcategory")
   public String subcategory;
   @SerializedName("nameColorAnimation")
   private final ColorAnimation nameColorAnimation = new ColorAnimation(new ColorPalette(OneConfigColors.WHITE_90, OneConfigColors.WHITE, OneConfigColors.WHITE_90));
   @SerializedName("nameColor")
   public int nameColor = OneConfigColors.WHITE_90;
   @SerializedName("dependencies")
   private final Map<String, Supplier<Boolean>> dependencies = new HashMap<>();
   @SerializedName("listeners")
   private final List<Runnable> listeners = new ArrayList<>();
   @SerializedName("hideConditions")
   private final List<Supplier<Boolean>> hideConditions = new ArrayList<>();
   @SerializedName("descriptionAnimation")
   private UiAnimation descriptionAnimation = new DummyAnimation(0.0F);
   @SerializedName("hoverTime")
   private float hoverTime = 0.0F;

   public BasicOption(Field field2, Object obj, String text, String text2, String text3, String text4, int n) {
      this.field = field2;
      this.parent = obj;
      this.name = text;
      this.description = ColorUtils.stripColorCodes(text2);
      this.category = text3;
      this.subcategory = text4;
      this.size = n;
      if (field2 != null) {
         field2.setAccessible(true);
      }

      if (obj instanceof Value) {
         ((Value)obj).basicOption = this;
      }
   }

   public void performAction(Object obj) throws IllegalAccessException {
      if (this.field != null) {
         if (!obj.equals(this.field.get(this.parent))) {
            this.field.set(this.parent, obj);
            this.performAction();
         }
      }
   }

   public void performAction() {
      for (Runnable action : this.listeners) {
         action.run();
      }
   }

   public Object resolveValue() throws IllegalAccessException {
      return this.field == null ? null : this.field.get(this.parent);
   }

   public abstract int getHeight();

   public abstract void draw(long vg, int x, int y, InputHandler input);

   public void drawEx(long vg, int x, int y, InputHandler input) {
      this.drawAlt(vg, x, y, input);
   }

   public void onKeyTyped(int n) {
   }

   public void onKeyTypedEx(int n) {
   }

   public void onMouseClick(double d, double d2, int n) {
   }

   public void onMouseClickEx(double d, double d2, int n) {
   }

   public void drawAlt(long vg, int x, int y, InputHandler input) {
      if (!this.description.trim().isEmpty() || !this.dependencies.isEmpty()) {
         boolean isHovered = input.isHovered(
            this.computeFloat(x), y, NanoVGHelper.INSTANCE.getTextWidth(vg, this.name, 14.0F, OneConfigFonts.ONE_CONFIG_FONT), 32.0F
         );
         this.nameColor = this.nameColorAnimation.computeInt(isHovered, false);
         if (isHovered) {
            this.hoverTime = this.hoverTime + GuiUtils.getProgress();
         } else {
            this.hoverTime = 0.0F;
         }

         String text = null;
         int n = 0;
         ArrayList list = new ArrayList();
         if (!this.dependencies.isEmpty()) {
            for (Entry entry : this.dependencies.entrySet()) {
               String key = (String)entry.getKey();
               Supplier supplier = (Supplier)entry.getValue();
               if (key.startsWith("unknown-")) {
                  n++;
               } else if (!(Boolean)supplier.get()) {
                  list.add(key);
               }
            }
         }

         if (!list.isEmpty() || n != 0) {
            boolean isEmpty = list.isEmpty();
            StringBuilder sb = new StringBuilder("Option disabled by ");

            for (Object obj : list) {
               String text2 = (String)obj;
               sb.append("\"").append(text2).append("\", ");
            }

            sb = new StringBuilder(sb.substring(0, sb.length() - 2));
            if (n != 0) {
               if (isEmpty) {
                  sb.append(" and ");
               }

               sb.append(n).append(" other option").append(n == 1 ? "" : "s");
            }

            sb.append(".");
            text = sb.toString();
         }

         if (!this.description.trim().isEmpty()) {
            TooltipRenderer.drawAndGetHeight(
               vg,
               x,
               y,
               this.description,
               text,
               () -> this.descriptionAnimation,
               uiAnimation -> this.descriptionAnimation = uiAnimation,
               null,
               this.isValid(),
               WindowMetrics.getFramebufferWidth() / 2.0F < input.getProgress() ? TooltipRenderer.Mode.RIGHT : TooltipRenderer.Mode.LEFT,
               input
            );
         }
      }
   }

   public boolean isValid() {
      return this.hoverTime > 350.0F;
   }

   protected float computeFloat(int n) {
      return n;
   }

   public boolean isValidEx() {
      for (Supplier supplier : this.dependencies.values()) {
         if (!(Boolean)supplier.get()) {
            return false;
         }
      }

      return true;
   }

   public boolean isValidAlt() {
      for (Supplier supplier : this.hideConditions) {
         if ((Boolean)supplier.get()) {
            return true;
         }
      }

      return false;
   }

   public void applyText(String text, Supplier<Boolean> supplier) {
      this.dependencies.put(text, supplier);
   }

   @Deprecated
   public void performAction(Supplier<Boolean> supplier) {
      this.dependencies.put("unknown-" + UUID.randomUUID(), supplier);
   }

   public void runTask(Runnable action) {
      this.listeners.add(action);
   }

   public void performActionEx(Supplier<Boolean> supplier) {
      this.hideConditions.add(supplier);
   }

   public Field getField() {
      return this.field;
   }

   public Object getParent() {
      return this.parent;
   }

   public void setParent(Object obj) {
      this.parent = obj;
   }

   public void performActionEx() {
   }
}
