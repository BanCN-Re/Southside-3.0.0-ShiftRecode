package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.internal.gui.HudGui;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Watermark;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.UIMatrixStack;
import dev.southside.render.ArrayListRenderer;
import dev.southside.utils.HudPosition;
import dev.southside.utils.ColorValue;
import dev.southside.utils.OneConfigFont;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.LabColorGradient;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.OneKeyBind;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.SmoothFloat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class ArrayListHud extends Hud {
   public static final ModeValue style = new ModeValue("Style", "Southside", "Southside", "SigmaJello", "大香肠");
   public static final BooleanValue removeShadow = new BooleanValue("Remove Shadow", false);
   public static final BooleanValue showSuffix = new BooleanValue("Show Suffix", true);
   public static final BooleanValue background = new BooleanValue("Background", true);
   public static final BooleanValue roundBackground = new BooleanValue("Round Background", true);
   public static final String[] DEFAULT_NAMES = new String[]{"Default", "Inter", "Jello-Light", "Vanilla"};
   public static final ModeValue font = new ModeValue("Font", DEFAULT_NAMES, "Choose which font will show", DEFAULT_NAMES.length, 0);
   @SerializedName("noRenderModules")
   public final BooleanValue noRenderModules = new BooleanValue("No Render Modules", true);
   @SerializedName("onlyBoundModules")
   public final BooleanValue onlyBoundModules = new BooleanValue("Only Bound Modules", false);
   @SerializedName("color")
   public final ModeValue color = new ModeValue(
      "Color", new String[]{"Custom", "Rainbow", "LightRainbow", "Astolfo", "Weird", "Valentine"}, "Choose which color will show in the Arraylist", 5, 1
   );
   @SerializedName("customColorA")
   public final ColorValue customColorA = new ColorValue("Custom Color A", new OneColor(Color.WHITE));
   @SerializedName("customColorB")
   public final ColorValue customColorB = new ColorValue("Custom Color B", new OneColor(Color.WHITE));
   @SerializedName("customSpeed")
   public final NumberValue customSpeed = new NumberValue("Custom Speed", 10.0, 5.0, 20.0);
   @SerializedName("entryRenderer")
   private ArrayListRenderer entryRenderer;
   @SerializedName("gradient")
   private LabColorGradient.Inner gradient;
   @SerializedName("entries")
   public CopyOnWriteArrayList<ArrayListHud.Inner> entries = new CopyOnWriteArrayList<>();
   @SerializedName("entryCache")
   private final Map<Module, ArrayListHud.Inner> entryCache = new HashMap<>();
   @SerializedName("renderScale")
   private float renderScale = 1.0F;
   @SerializedName("unscaledHeight")
   private float unscaledHeight = 50.0F;
   @SerializedName("unscaledWidth")
   private float unscaledWidth = 50.0F;
   @SerializedName("lastModuleCount")
   private int lastModuleCount = -1;

   public ArrayListHud(float x, float y, float z) {
      super(x, y, 2, z);
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      return !style.matches("Southside") && this.entryRenderer != null ? this.entryRenderer.getProgress() : this.unscaledWidth * f;
   }

   @Override
   public float setScale(float f, boolean flag) {
      return !style.matches("Southside") && this.entryRenderer != null ? this.entryRenderer.getProgressEx() : this.unscaledHeight * f;
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      switch (style.getDisplayTextEx()) {
         case "Southside":
            this.render(context, f, f2, f3);
            break;
         case "SigmaJello":
            if (this.entryRenderer == null) {
               this.entryRenderer = new ArrayListRenderer();
            }

            this.entryRenderer.performAction(getOneConfigFont());
            break;
         default:
            int n = Southside.INSTANCE.getModuleManager().getModule(Watermark.class).isEnabled() ? 22 : 2;

            for (ArrayListHud.Inner inner : this.entries) {
               context.drawTextWithShadow(mc.textRenderer, inner.module.getNameKey(), 1, n, -1);
               n += 9;
            }
      }
   }

   private void render(DrawContext context, float f, float f2, float f3) {
      if (this.gradient == null) {
         this.performAction();
      }

      this.renderScale = f3;
      this.render();
      AtomicReference atomicReference = new AtomicReference();
      NanoVGHelper.INSTANCE.performAction(true, l -> atomicReference.set(this.getEntry(l, f, f2, f3)));
      if (isValidImpl()) {
         for (ArrayListHud.Inner inner : this.entries) {
            inner.render(context);
         }
      }

      ArrayListHud.Entry entry = (ArrayListHud.Entry)atomicReference.get();
      if (entry != null && entry.isHasEntries()) {
         if (mc.currentScreen instanceof HudGui hudGui && hudGui.isScaling) {
            return;
         }

         this.unscaledHeight = entry.getHeight() / f3;
         this.unscaledWidth = entry.getWidth() / f3;
      }
   }

   private boolean isValid(Module module) {
      ArrayList keyBinds = ((OneKeyBind)module.getBind().getValue()).getKeyBinds();
      boolean flag = !keyBinds.isEmpty() && (Integer)keyBinds.getFirst() != 0 && (Integer)keyBinds.getFirst() != -1;
      return module.isEnabled()
         && module.isVisible()
         && (module.getCategory() != Category.Render || !(Boolean)this.noRenderModules.getValue())
         && (!(Boolean)this.onlyBoundModules.getValue() || flag);
   }

   private void render() {
      List modules = Southside.INSTANCE.getModuleManager().getModules();
      if (this.lastModuleCount != modules.size()) {
         this.entryRenderer = new ArrayListRenderer();
         this.lastModuleCount = modules.size();
      }

      this.entries = new CopyOnWriteArrayList<>();

      for (Object obj : modules) {
         Module module = (Module)obj;
         ArrayListHud.Inner inner = this.entryCache.computeIfAbsent(module, module2 -> new ArrayListHud.Inner(this, module2));
         if (this.isValid(module)) {
            this.entries.add(inner);
         }
      }

      float f = 6.0F * this.renderScale;
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      nvg.performAction(true, l -> this.entries.sort((inner2, inner3) -> this.draw(nvg, l, f, inner2, inner3)));
   }

   private int draw(NanoVGHelper nvg, long l, float f, ArrayListHud.Inner inner, ArrayListHud.Inner inner2) {
      String text = Southside.INSTANCE.getModuleManager().formatTextEx(inner2.module);
      String text2 = Southside.INSTANCE.getModuleManager().formatTextEx(inner.module);
      float f2 = isValidImpl() ? mc.textRenderer.getWidth(text) * this.renderScale : nvg.getTextWidth(l, text, f, getOneConfigFont());
      float f3 = isValidImpl() ? mc.textRenderer.getWidth(text2) * this.renderScale : nvg.getTextWidth(l, text2, f, getOneConfigFont());
      return (int)(10000.0F * f2 - 10000.0F * f3);
   }

   private ArrayListHud.Entry getEntry(long l, float f, float f2, float f3) {
      float f4 = 0.0F;
      float f5 = 0.0F;
      boolean flag = false;

      byte b = switch (super.position.getValue().anchor) {
         case BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER -> -1;
         default -> 1;
      };

      byte b2 = switch (super.position.getValue().anchor) {
         case BOTTOM_LEFT, TOP_LEFT, MIDDLE_LEFT -> -1;
         default -> 1;
      };

      for (ArrayListHud.Inner inner : this.entries) {
         inner.slideY.setTarget(b * f4);
         inner.slideY.freeze();
         inner.slideX.setTarget(0.0F);
         float f6 = inner.computeFloat(l, f, f2, (Boolean)background.getValue(), f3);
         f4 += inner.entryHeight;
         flag = true;
         f5 = Math.max(f5, f6);
      }

      for (ArrayListHud.Inner inner2 : this.entryCache.values()) {
         if (!this.isValid(inner2.module)) {
            inner2.slideY.setTarget(0.0F);
            inner2.slideX.setTarget(f3 * b2 * 110.0F * 0.3F);
            inner2.computeFloat(l, f, f2, (Boolean)background.getValue(), f3);
         }
      }

      return new ArrayListHud.Entry(flag, f5, f4);
   }

   public void performAction() {
      this.gradient = new LabColorGradient.Inner(((OneColor)this.customColorA.getValue()).blendColor(), ((OneColor)this.customColorB.getValue()).blendColor(), 8);
   }

   public static OneConfigFont getOneConfigFont() {
      return switch (font.getValue()) {
         case 1 -> OneConfigFonts.REGULAR;
         case 2 -> OneConfigFonts.JELLOLIGHT;
         default -> OneConfigFonts.WQY_MICROHEI;
      };
   }

   public static boolean isValidImpl() {
      return font.getValue() == 3;
   }

   private int computeInt(int n, int n2, float f) {
      float f2 = (float)((System.currentTimeMillis() / 10L * ((Number)this.customSpeed.getValue()).doubleValue() + n2) % n);
      f2 /= n;
      return Color.getHSBColor(f2, f, 1.0F).getRGB();
   }

   private static double computeDouble(double d) {
      if (d > 1.0) {
         double d2 = d % 1.0;
         return (long)d % 2L == 0L ? d2 : 1.0 - d2;
      } else {
         return d;
      }
   }

   private static Color blendColor(Color color2, Color color3, double d) {
      double d2 = computeDouble(d);
      double d3 = 1.0 - d2;
      return new Color(
         (int)(color2.getRed() * d3 + color3.getRed() * d2),
         (int)(color2.getGreen() * d3 + color3.getGreen() * d2),
         (int)(color2.getBlue() * d3 + color3.getBlue() * d2)
      );
   }

   private double computeDouble(float f) {
      return Math.abs(System.currentTimeMillis() / 100.0 * ((Number)this.customSpeed.getValue()).doubleValue()) / 100.0 + f / 50.0F;
   }

   public int computeInt(float f) {
      return switch (this.color.getValue()) {
         case 0 -> this.gradient.computeInt(computeDouble(this.computeDouble(f)));
         case 1 -> this.computeInt(6000, (int)(f * 30.0F), 0.85F);
         case 2 -> this.computeInt(6000, (int)(f * 30.0F), 0.55F);
         case 3 -> blendColor(new Color(255, 60, 234), new Color(27, 179, 255), this.computeDouble(f)).getRGB();
         case 4 -> blendColor(new Color(128, 171, 255), new Color(160, 72, 255), this.computeDouble(f)).getRGB();
         case 5 -> blendColor(new Color(255, 129, 202), new Color(255, 15, 0), this.computeDouble(f)).getRGB();
         default -> -1;
      };
   }

   private record Entry(boolean hasEntries, float width, float height) {
      public boolean isHasEntries() {
         return this.hasEntries;
      }

      public float getWidth() {
         return this.width;
      }

      public float getHeight() {
         return this.height;
      }
   }

   private static final class Inner {
      private static final float DEFAULT_FLOAT = 2.1000001F;
      private static final float DEFAULT_FLOAT_VALUE = 30.000002F;
      @SerializedName("parent")
      private final ArrayListHud parent;
      @SerializedName("module")
      public final Module module;
      @SerializedName("slideX")
      public final SmoothFloat slideX = new SmoothFloat(3.0F, 0.0F);
      @SerializedName("slideY")
      public final SmoothFloat slideY = new SmoothFloat(3.0F, 0.0F);
      @SerializedName("entryHeight")
      public float entryHeight;
      @SerializedName("textX")
      private float textX;
      @SerializedName("textY")
      private float textY;
      @SerializedName("textScale")
      private float textScale;
      @SerializedName("textColor")
      private int textColor;
      @SerializedName("text")
      private String text = "";
      @SerializedName("textDirty")
      private boolean textDirty;

      public Inner(ArrayListHud arrayListHud, Module module2) {
         this.parent = arrayListHud;
         this.module = module2;
      }

      public void render(DrawContext context) {
         if (this.textDirty) {
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(this.textX, this.textY, 0.0F);
            matrices.scale(this.textScale, this.textScale, 1.0F);
            context.drawText(IMinecraft.mc.textRenderer, this.text, 0, 0, this.textColor, !(Boolean)ArrayListHud.removeShadow.getValue());
            matrices.pop();
            this.textDirty = false;
         }
      }

      private void performAction(float f, float f2, float f3, int n, float f4, String text2) {
         this.textX = f;
         this.textY = f2 + (f3 - 9.0F * f4) / 2.0F;
         this.textScale = f4;
         this.textColor = n;
         this.text = text2;
         this.textDirty = true;
      }

      public float computeFloat(long l, float f, float f2, boolean flag, float f3) {
         float f4 = 6.0F * f3;

         byte b = switch (this.parent.position.getValue().anchor) {
            case BOTTOM_LEFT, TOP_LEFT, MIDDLE_LEFT -> -1;
            default -> 1;
         };
         String text2 = Southside.INSTANCE.getModuleManager().formatText(this.module);
         String text3 = Southside.INSTANCE.getModuleManager().formatTextEx(this.module);
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         boolean isValidImpl = ArrayListHud.isValidImpl();
         float f5 = isValidImpl ? IMinecraft.mc.textRenderer.getWidth(text3) * f3 : nvg.getTextWidth(l, text3, f4, ArrayListHud.getOneConfigFont());
         float f6 = isValidImpl ? 9.0F * f3 : nvg.getTextHeight(l, f4, ArrayListHud.getOneConfigFont());
         float f7 = f6 + 3.0F * f3;
         this.entryHeight = f7;
         if (b == 1 && this.slideX.getValue() >= 30.000002F * f3) {
            return 0.0F;
         }

         if (b == -1 && this.slideX.getValue() <= -30.000002F * f3) {
            return 0.0F;
         }

         float f8 = f + this.slideX.getValue() - f5;
         float f9 = f2 + this.slideY.getValue();
         switch (this.parent.position.getValue().anchor) {
            case BOTTOM_LEFT:
               f8 += f5 + 2.1000001F * f3;
               f9 += this.parent.setScale(f3, false) - f7;
               break;
            case BOTTOM_RIGHT:
            case BOTTOM_CENTER:
               f8 += this.parent.computeFloat(f3, false);
               f9 += this.parent.setScale(f3, false) - f7;
               break;
            case TOP_LEFT:
            case MIDDLE_LEFT:
               f8 += f5 + 2.1000001F * f3;
               break;
            case TOP_RIGHT:
            case MIDDLE_RIGHT:
            case TOP_CENTER:
            case MIDDLE_CENTER:
               f8 += this.parent.computeFloat(f3, false);
         }

         if (flag) {
            this.draw(l, nvg, f8, f9, f5, f7, f4, f3);
         }

         float f10 = f9 + f7 / 2.0F + 0.3F * f3;
         int n = this.parent.computeInt(f10);
         if (isValidImpl) {
            this.performAction(f8 - 0.6F * f3, f9, f7, n, f3, text2);
         } else {
            nvg.draw(l, text2, f8 - 0.6F * f3, f10, n, f4, ArrayListHud.getOneConfigFont());
         }

         return f5 + 2.1000001F * f3;
      }

      private void draw(long l, NanoVGHelper nvg, float f, float f2, float f3, float f4, float f5, float f6) {
         float f7 = f - 2.1000001F * f6;
         float f8 = f3 + 2.1000001F * f6;
         float f9 = 0.0F;
         float f10 = 0.0F;
         float f11 = 0.0F;
         float f12 = 0.0F;
         int size = this.parent.entries.size();
         if (size > 1) {
            int n = this.parent.entries.indexOf(this);
            boolean flag = n == 0;
            boolean flag2 = n == size - 1;
            boolean flag3 = n < size - 1;
            float f13 = 0.0F;
            if (flag3) {
               ArrayListHud.Inner inner = this.parent.entries.get(n + 1);
               f13 = nvg.getTextWidth(l, Southside.INSTANCE.getModuleManager().formatTextEx(inner.module), f5, ArrayListHud.getOneConfigFont())
                  + 2.1000001F * f6;
            }

            switch (this.parent.position.getValue().anchor) {
               case BOTTOM_LEFT:
                  if (flag) {
                     f11 = 3.0F;
                     f12 = 3.0F;
                  }

                  if (flag2) {
                     f10 = 3.0F;
                     f9 = 3.0F;
                  } else if (flag3) {
                     f10 = Math.min(3.0F, f8 - f13);
                  }
                  break;
               case BOTTOM_RIGHT:
               case BOTTOM_CENTER:
                  if (flag) {
                     f11 = 3.0F;
                     f12 = 3.0F;
                  }

                  if (flag2) {
                     f10 = 3.0F;
                     f9 = 3.0F;
                  } else if (flag3) {
                     f9 = Math.min(3.0F, f8 - f13);
                  }
                  break;
               case TOP_LEFT:
                  if (flag) {
                     f10 = 3.0F;
                     f9 = 3.0F;
                  }

                  if (flag2) {
                     f11 = 3.0F;
                     f12 = 3.0F;
                  } else if (flag3) {
                     f11 = Math.min(3.0F, f8 - f13);
                  }
               case MIDDLE_LEFT:
               default:
                  break;
               case TOP_RIGHT:
               case MIDDLE_RIGHT:
               case TOP_CENTER:
                  if (flag) {
                     f10 = 3.0F;
                     f9 = 3.0F;
                  }

                  if (flag2) {
                     f11 = 3.0F;
                     f12 = 3.0F;
                  } else if (flag3) {
                     f12 = Math.min(3.0F, f8 - f13);
                  }
            }
         } else if (size == 1) {
            f11 = 3.0F;
            f12 = 3.0F;
            f10 = 3.0F;
            f9 = 3.0F;
         }

         if ((Boolean)ArrayListHud.roundBackground.getValue()) {
            nvg.drawRoundedRectVaried(l, f7, f2, f8, f4, 1679826976, f9, f10, f11, f12);
         } else {
            nvg.drawRect(l, f7, f2, f8, f4, 1679826976);
         }
      }
   }
}
