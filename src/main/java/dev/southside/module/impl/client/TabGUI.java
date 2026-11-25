package dev.southside.module.impl.client;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.event.events.KeyPressEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.AssetHelper;
import dev.southside.utils.InterpolatedFloat;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.NanoVGImage;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.RainbowColorUtils;
import dev.southside.utils.NanoVGHelper.Mode;
import java.awt.Color;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL11;

public class TabGUI extends Module {
   private static final float DEFAULT_FLOAT = 2.0F;
   private static final float DEFAULT_FLOAT_VALUE = 15.0F;
   private static final String SLECTOR = "/assets/southside/textures/gui/hud/tabgui/slector.png";
   private static final Color top;
   @SerializedName("state")
   private TabGUI.Inner2 state;

   public TabGUI() {
      super(Category.Client, "TabGUI", "Jello tab gui");
   }

   @Override
   public void onEnable() {
      this.state = new TabGUI.Inner2();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.state = null;
      super.onDisable();
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (this.state == null) {
         this.state = new TabGUI.Inner2();
      }

      TabGUI.Inner inner = this.state.get();
      List list = collectItems(inner.category);
      inner.onKeyTyped(list.size());
      NanoVGHelper.INSTANCE.performAction(l -> this.update(l, drawContextEvent.getTickDelta(), inner, list));
   }

   private void update(long l, float f, TabGUI.Inner inner, List<Module> list) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      draw(l, f, this.state.categoryPanel, 5.0F, 75.0F, this.state.categories.size());
      drawImage(l, 10.0F, 2.0F * (50.0F + this.state.selectorAnimation.getProgress()), 150.0F);

      for (int i = 0; i < this.state.categories.size(); i++) {
         TabGUI.Inner inner2 = this.state.categories.get(i);
         nvg.drawTextEx(
            l, inner2.category.name(), 2.0F * (10.5F + inner2.highlight.getProgress()), 2.0F * (58.0F + i * 15.0F), -1, 20.0F, OneConfigFonts.JELLOLIGHT
         );
      }

      if (this.state.moduleListOpen && !list.isEmpty()) {
         draw(l, f, this.state.modulePanel, 85.5F, 84.0F, list.size());
         drawImage(l, 170.0F, 2.0F * (50.0F + inner.moduleSelector.getProgress()), 170.0F);

         for (int j = 0; j < list.size(); j++) {
            Module module = (Module)list.get(j);
            nvg.drawTextEx(
               l,
               module.getName(),
               2.0F * (90.5F + this.state.getValueUtil2(module).getProgress()),
               2.0F * (58.0F + j * 15.0F),
               -1,
               20.0F,
               module.isEnabled() ? OneConfigFonts.JELLOMEDIUM : OneConfigFonts.JELLOLIGHT
            );
         }
      }
   }

   private static void draw(long l, float f, TabGUI.Inner3 inner3, float f2, float f3, int n) {
      inner3.performAction(2.0F * f2, 100.0F, 2.0F * f3, 30.0F * n);
      NanoVGHelper.INSTANCE.drawRoundedRect(l, inner3.x, inner3.y, inner3.width, inner3.height, 4.0F, 4.0F, 0.0F, top);
      inner3.performAction(l, f);
   }

   private static void drawImage(long l, float f, float f2, float f3) {
      AssetHelper assetHelper = AssetHelper.INSTANCE;
      if (assetHelper.isValid(l, "/assets/southside/textures/gui/hud/tabgui/slector.png", TabGUI.class)) {
         NanoVGImage nanoVGImage = assetHelper.get("/assets/southside/textures/gui/hud/tabgui/slector.png");
         NVGPaint paint = NVGPaint.calloc();

         try {
            NanoVG.nvgImagePattern(l, f, f2, f3, 30.0F, 0.0F, nanoVGImage.getImage(), 1.0F, paint);
            NanoVG.nvgBeginPath(l);
            NanoVG.nvgRect(l, f, f2, f3, 30.0F);
            NanoVG.nvgFillPaint(l, paint);
            NanoVG.nvgFill(l);
         } catch (Throwable e) {
            if (paint != null) {
               try {
                  paint.close();
               } catch (Throwable e2) {
                  e.addSuppressed(e2);
               }
            }

            throw e;
         }

         if (paint != null) {
            paint.close();
         }
      }
   }

   @EventTarget
   public void onKeyCodeEvent2(KeyPressEvent keyPressEvent) {
      if (this.state != null && mc2.currentScreen == null) {
         switch (keyPressEvent.getKeyCode()) {
            case 262:
               this.state.performAction();
               break;
            case 263:
               this.state.moduleListOpen = false;
               break;
            case 264:
               this.state.onKeyTyped(1);
               break;
            case 265:
               this.state.onKeyTyped(-1);
         }
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (this.state != null) {
         this.state.categoryPanel.performAction();
         this.state.modulePanel.performAction();
         TabGUI.Inner inner = this.state.get();

         for (TabGUI.Inner inner2 : this.state.categories) {
            inner2.highlight.performAction(inner2 == inner ? 7.0F : 0.0F, -1.0);
         }

         this.state.selectorAnimation.performAction(this.state.selectedCategory * 15.0F, 3.0);
         List list = collectItems(inner.category);
         inner.onKeyTyped(list.size());
         inner.moduleSelector.performAction(inner.selectedModule * 15.0F, 3.0);

         for (int i = 0; i < list.size(); i++) {
            this.state.getValueUtil2((Module)list.get(i)).performAction(i == inner.selectedModule ? 5.0F : 0.0F, 3.0);
         }
      }
   }

   public static List<Module> collectItems(Category category) {
      return Southside.INSTANCE.getModuleManager().modules.stream().filter(module -> module.getCategory() == category).toList();
   }

   static {
      long l = 4209374690076109736L;
      long l2 = -7855764236293526602L ^ l;
      byte b = -1;
      long l3 = l2;
      top = new Color(0, 0, 0, (int)l3);
   }

   private static final class Inner {
      @SerializedName("category")
      public final Category category;
      @SerializedName("highlight")
      public final InterpolatedFloat highlight = new InterpolatedFloat(0.0F);
      @SerializedName("moduleSelector")
      public final InterpolatedFloat moduleSelector = new InterpolatedFloat(0.0F);
      @SerializedName("selectedModule")
      public int selectedModule;

      public Inner(Category category2) {
         this.category = category2;
      }

      public void onKeyTyped(int n) {
         this.selectedModule = n == 0 ? 0 : MathHelper.clamp(this.selectedModule, 0, n - 1);
      }
   }

   private static final class Inner2 {
      @SerializedName("categories")
      public final List<TabGUI.Inner> categories = List.of(
         new TabGUI.Inner(Category.Combat),
         new TabGUI.Inner(Category.Misc),
         new TabGUI.Inner(Category.Movement),
         new TabGUI.Inner(Category.Player),
         new TabGUI.Inner(Category.World)
      );
      @SerializedName("moduleOffsets")
      private final Map<Module, InterpolatedFloat> moduleOffsets = new HashMap<>();
      @SerializedName("selectorAnimation")
      public final InterpolatedFloat selectorAnimation = new InterpolatedFloat(0.0F);
      @SerializedName("categoryPanel")
      public final TabGUI.Inner3 categoryPanel = new TabGUI.Inner3();
      @SerializedName("modulePanel")
      public final TabGUI.Inner3 modulePanel = new TabGUI.Inner3();
      @SerializedName("selectedCategory")
      public int selectedCategory;
      @SerializedName("moduleListOpen")
      public boolean moduleListOpen;

      public Inner2() {
      }

      public TabGUI.Inner get() {
         return this.categories.get(this.selectedCategory);
      }

      public InterpolatedFloat getValueUtil2(Module module) {
         return this.moduleOffsets.computeIfAbsent(module, module2 -> new InterpolatedFloat(0.0F));
      }

      public void onKeyTyped(int n) {
         if (!this.moduleListOpen) {
            this.selectedCategory = Math.floorMod(this.selectedCategory + n, this.categories.size());
         } else {
            TabGUI.Inner inner = this.get();
            int size = TabGUI.collectItems(inner.category).size();
            inner.selectedModule = size == 0 ? 0 : Math.floorMod(inner.selectedModule + n, size);
         }
      }

      public void performAction() {
         TabGUI.Inner inner = this.get();
         if (!this.moduleListOpen) {
            this.moduleListOpen = true;
            inner.selectedModule = 0;
         } else {
            List list = TabGUI.collectItems(inner.category);
            inner.onKeyTyped(list.size());
            if (!list.isEmpty()) {
               Module module = (Module)list.get(inner.selectedModule);
               module.setEnabled(!module.isEnabled());
            }
         }
      }
   }

   private static final class Inner3 {
      @SerializedName("top")
      private Color top = Color.BLACK;
      @SerializedName("bottom")
      private Color bottom = Color.BLACK;
      @SerializedName("previousTop")
      private Color previousTop = Color.BLACK;
      @SerializedName("previousBottom")
      private Color previousBottom = Color.BLACK;
      @SerializedName("pixels")
      private IntBuffer pixels;
      @SerializedName("x")
      public int x;
      @SerializedName("y")
      public int y;
      @SerializedName("width")
      public int width;
      @SerializedName("height")
      public int height;

      public Inner3() {
      }

      public void performAction() {
         this.previousTop = this.top;
         this.previousBottom = this.bottom;
      }

      public void performAction(float x2, float y2, float width2, float height2) {
         this.x = (int)x2;
         this.y = (int)y2;
         this.width = (int)width2;
         this.height = (int)height2;
      }

      public void performAction(long l, float f) {
         if (!(TabGUI.mc2.currentScreen instanceof ChatScreen)) {
            this.performActionEx();
         }

         float clamped = MathHelper.clamp(f, 0.0F, 1.0F);
         NanoVGHelper.INSTANCE
            .drawGradientRect(
               l,
               this.x,
               this.y,
               this.width,
               this.height,
               blendColor(this.previousTop, this.top, clamped).getRGB(),
               blendColor(this.previousBottom, this.bottom, clamped).getRGB(),
               Mode.DOWN
            );
      }

      private void performActionEx() {
         int framebufferWidth = TabGUI.mc2.getWindow().getFramebufferWidth();
         int framebufferHeight = TabGUI.mc2.getWindow().getFramebufferHeight();
         int clamped = MathHelper.clamp(this.x, 0, framebufferWidth);
         int clamped2 = MathHelper.clamp(this.y, 0, framebufferHeight);
         int clamped3 = MathHelper.clamp(this.x + this.width, 0, framebufferWidth);
         int clamped4 = MathHelper.clamp(this.y + this.height, 0, framebufferHeight);
         int n = clamped3 - clamped;
         int n2 = clamped4 - clamped2;
         if (n > 0 && n2 > 0) {
            int n3 = n * n2;
            if (this.pixels == null || this.pixels.capacity() < n3) {
               this.pixels = BufferUtils.createIntBuffer(n3);
            }

            this.pixels.clear();
            short s = 7277;
            this.pixels.limit(n3);
            long l = 2201784892600646002L;
            int n4 = GL11.glGetInteger(3333);

            try {
               GL11.glPixelStorei(3333, 1);
               GL11.glReadPixels(clamped, framebufferHeight - clamped4, n, n2, 32993, 33639, this.pixels);
            } finally {
               GL11.glPixelStorei(3333, n4);
            }

            Color color = RainbowColorUtils.blendColor(new Color(this.pixels.get(n3 - 1)), new Color(this.pixels.get(n * (n2 - 1))));
            Color color2 = RainbowColorUtils.blendColor(new Color(this.pixels.get(0)), new Color(this.pixels.get(n - 1)));
            this.top = blendColor(this.top, color);
            this.bottom = blendColor(this.bottom, color2);
         }
      }

      private static Color blendColor(Color color, Color color2) {
         return new Color(
            color.getRed() + (color2.getRed() - color.getRed()) / 5,
            color.getGreen() + (color2.getGreen() - color.getGreen()) / 5,
            color.getBlue() + (color2.getBlue() - color.getBlue()) / 5
         );
      }

      private static Color blendColor(Color color, Color color2, float f) {
         return new Color(
            MathHelper.lerp(f, color.getRed(), color2.getRed()),
            MathHelper.lerp(f, color.getGreen(), color2.getGreen()),
            MathHelper.lerp(f, color.getBlue(), color2.getBlue())
         );
      }
   }
}
