package dev.southside.gui;

import com.google.gson.annotations.SerializedName;
import dev.southside.render.UIMatrixStack;
import dev.southside.utils.WindowMetrics;
import dev.southside.utils.MinecraftAccess;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public abstract class CustomScreen extends Screen {
   public static final CustomScreen.Inner INNER = new CustomScreen.Inner();
   @SerializedName("restoreCurrentGuiOnClose")
   private final boolean restoreCurrentGuiOnClose;
   @SerializedName("newGuiScale")
   private int newGuiScale;
   @SerializedName("unlocalizedName")
   private String unlocalizedName;
   @SerializedName("guiScaleToRestore")
   private int guiScaleToRestore;
   @SerializedName("screenToRestore")
   private final Screen screenToRestore;

   public CustomScreen(boolean flag, int n, String text) {
      super(Text.literal("one_config"));
      this.restoreCurrentGuiOnClose = flag;
      this.newGuiScale = n;
      this.unlocalizedName = text;
      this.guiScaleToRestore = -1;
      this.screenToRestore = flag ? INNER.getScreen() : null;
   }

   public CustomScreen(boolean flag, int n) {
      this(flag, n, null);
   }

   public CustomScreen(boolean flag, CustomScreen.Mode mode) {
      this(flag, mode.ordinal());
   }

   public CustomScreen(boolean flag, String text) {
      this(flag, 0, text);
   }

   public CustomScreen(boolean flag) {
      this(flag, 0, null);
   }

   public CustomScreen() {
      this(false, 0, null);
   }

   public final void init() {
      this.performActionEx();
      this.performAction(this.width, this.height);
   }

   public void performActionEx() {
   }

   public void performAction(int n, int n2) {
      super.init();
   }

   public final void render(DrawContext context, int n, int n2, float f) {
      this.render(new UIMatrixStack(), context, n, n2, f);
   }

   private void render(UIMatrixStack matrices, DrawContext context, int n, int n2, float f) {
      UIMatrixStack.Inner.INSTANCE.getT(matrices, () -> {
         this.renderEx(context, n, n2, f);
         return null;
      });
   }

   public void renderEx(DrawContext context, int n, int n2, float f) {
      this.renderAll(UIMatrixStack.Inner.INSTANCE.getUIMatrixStack(), context, n, n2, f);
   }

   public void renderAll(UIMatrixStack matrices, DrawContext context, int n, int n2, float f) {
      matrices.getT(() -> {
         super.render(context, n, n2, f);
         return null;
      });
   }

   public void renderBackground(DrawContext context, int n, int n2, float f) {
      this.render(new UIMatrixStack(), context, f);
   }

   public final void render(UIMatrixStack matrices, DrawContext context, float f) {
      UIMatrixStack.Inner.INSTANCE.getT(matrices, () -> {
         this.render(context, f);
         return null;
      });
   }

   public void render(DrawContext context, float f) {
      this.renderEx(UIMatrixStack.Inner.INSTANCE.getUIMatrixStack(), context, f);
   }

   public void renderEx(UIMatrixStack matrices, DrawContext context, float f) {
      matrices.getT(() -> {
         this.renderPanoramaBackground(context, f);
         return null;
      });
   }

   public void renderPanoramaBackground(DrawContext context, float f) {
      if (this.client.world == null) {
         super.renderPanoramaBackground(context, f);
      }
   }

   public final boolean keyPressed(int n, int n2, int n3) {
      this.performAction(n, n2, n3);
      return true;
   }

   public void performAction(int n, int n2, int n3) {
      super.keyPressed(n, n2, n3);
   }

   public final boolean keyReleased(int n, int n2, int n3) {
      this.performActionEx(n, n2, n3);
      return true;
   }

   public void performActionEx(int n, int n2, int n3) {
   }

   public final boolean charTyped(char ch, int n) {
      this.performActionEx(ch, n);
      return true;
   }

   public void performActionEx(int n, int n2) {
      super.charTyped((char)n, n2);
   }

   public final boolean mouseClicked(double d, double d2, int n) {
      this.onMouseClick(d, d2, n);
      return true;
   }

   public void onMouseClick(double d, double d2, int n) {
      super.mouseClicked((int)d, (int)d2, n);
   }

   public final boolean mouseReleased(double d, double d2, int n) {
      this.onMouseClickEx(d, d2, n);
      return true;
   }

   public void onMouseClickEx(double d, double d2, int n) {
      super.mouseReleased(d, d2, n);
   }

   public final boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
      this.mouseDraggedEx(d, d2, n, d3, d4);
      return true;
   }

   public void mouseDraggedEx(double d, double d2, int n, double d3, double d4) {
      super.mouseDragged(d, d2, n, d3, d4);
   }

   public final boolean mouseScrolled(double x, double y, double width, double height) {
      this.performAction(height);
      return true;
   }

   public void performAction(double d) {
   }

   public final void tick() {
      this.tickEx();
   }

   public void tickEx() {
      super.tick();
   }

   public final void close() {
      try {
         this.performAction();
      } finally {
         super.close();
      }
   }

   public void performAction() {
   }

   public boolean shouldPause() {
      return false;
   }

   public final void performActionAlt() {
      INNER.setScreen(this.screenToRestore);
   }

   public static Screen getScreen() {
      return INNER.getScreen();
   }

   public static void openScreen(Screen screen) {
      INNER.setScreen(screen);
   }

   public final boolean isRestoreCurrentGuiOnClose() {
      return this.restoreCurrentGuiOnClose;
   }

   public int getNewGuiScale() {
      return this.newGuiScale;
   }

   public void setNewGuiScale(int n) {
      this.newGuiScale = n;
   }

   public String getUnlocalizedName() {
      return this.unlocalizedName;
   }

   public void setUnlocalizedName(String text) {
      this.unlocalizedName = text;
   }

   public static final class Inner {
      public Screen getScreen() {
         return MinecraftAccess.getMc().currentScreen;
      }

      public void setScreen(Screen screen) {
         MinecraftAccess.getMc().setScreen(screen);
      }
   }

   public enum Mode {
      Auto,
      Small,
      Medium,
      Large,
      VeryLarge;

      private static final int DEFAULT_INT;

      public static CustomScreen.Mode fromOrdinal(int n) {
         return values()[n];
      }

      public static CustomScreen.Mode getMode() {
         return getMode(650);
      }

      public static CustomScreen.Mode getMode(int n) {
         if (DEFAULT_INT != -1) {
            return fromOrdinal(MathHelper.clamp(DEFAULT_INT, 0, 4));
         }

         int windowWidth = WindowMetrics.getWindowWidth();
         int scaledHeight = WindowMetrics.getScaledHeight();
         int clamped = MathHelper.clamp(windowWidth / n, 1, 4);
         int clamped2 = MathHelper.clamp(scaledHeight / (n / 16 * 9), 1, 4);
         return fromOrdinal(Math.min(clamped, clamped2));
      }

      // $VF: synthetic method
      private static CustomScreen.Mode[] createValues() {
         return new CustomScreen.Mode[]{Auto, Small, Medium, Large, VeryLarge};
      }

      static {
         DEFAULT_INT = Integer.parseInt(System.getProperty("universal.gui.scale", "-1"));
      }


   }
}
