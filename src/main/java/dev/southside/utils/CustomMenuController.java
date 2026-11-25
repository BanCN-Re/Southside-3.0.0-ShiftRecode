package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.gui.SouthsideTitleScreen;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.render.MenuSceneRenderer;
import dev.southside.render.ImeOverlayManager;
import dev.southside.render.MenuOverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.Window;

public final class CustomMenuController {
   private static final int DEFAULT_INT = 440;
   private static boolean active;

   private CustomMenuController() {
   }

   public static ClientSettings getClientSettings() {
      return Southside.INSTANCE != null && Southside.INSTANCE.getModuleManager() != null
         ? Southside.INSTANCE.getModuleManager().getModule(ClientSettings.class)
         : null;
   }

   public static boolean isValid() {
      ClientSettings clientSettings = getClientSettings();
      return clientSettings != null && (Boolean)clientSettings.customMainMenu.getValue();
   }

   public static boolean isCustomScreen(Screen screen) {
      return screen instanceof SouthsideTitleScreen && MinecraftClient.getInstance().world == null && isValid();
   }

   public static Screen getScreen(Screen screen, Screen screen2) {
      if (MinecraftClient.getInstance().world != null) {
         return (Screen)screen2;
      }

      if ((screen2 == null || screen2 instanceof TitleScreen) && isValid()) {
         screen2 = new SouthsideTitleScreen();
      } else if (screen2 instanceof SouthsideTitleScreen && !isValid()) {
         screen2 = new TitleScreen();
      }

      if (screen instanceof SouthsideTitleScreen || screen2 instanceof SouthsideTitleScreen) {
         MenuSceneRenderer.INSTANCE.performAction();
      }

      if (isCustomScreen((Screen)screen2)) {
         getMenuScreenLayout((Screen)screen2).performAction();
      }

      return (Screen)screen2;
   }

   public static boolean isActive() {
      return active;
   }

   public static MenuScreenLayout getMenuScreenLayout(Screen screen) {
      return ((IMenuScreenExt)screen).menuPage();
   }

   public static int computeInt(Screen screen, int n) {
      return isCustomScreen(screen) ? 440 : n;
   }

   public static int computeIntEx(Screen screen, int n) {
      return isCustomScreen(screen) ? getEntryEx(screen).getPageHeight() : n;
   }

   public static CustomMenuController.Entry getEntryExEx(Screen screen) {
      return getEntryEx(screen);
   }

   private static CustomMenuController.Entry getEntryEx(Screen screen) {
      Window window = MinecraftClient.getInstance().getWindow();
      float f = Math.max(8.0F, Math.min(24.0F, window.getScaledHeight() * 0.055F));
      float f2 = Math.min(window.getScaledWidth() * 0.48F, window.getScaledHeight() * 1.22F);
      f2 = Math.max(120.0F, Math.min(f2, window.getScaledWidth() - f * 2.0F));
      float f3 = Math.max(80.0F, window.getScaledHeight() - f * 2.0F);
      float f4 = f2 / 440.0F;
      return new CustomMenuController.Entry(f, f, f2, f3, f4, Math.round(f3 / f4));
   }

   public static void openScreen(Screen screen, DrawContext context, int n, int n2, float f) {
      MinecraftClient instance = MinecraftClient.getInstance();
      Window window = instance.getWindow();
      double d = computeDouble(instance.mouse.getX(), window.getWidth(), window.getScaledWidth());
      double d2 = computeDouble(instance.mouse.getY(), window.getHeight(), window.getScaledHeight());
      CustomMenuController.Entry entryEx = getEntryEx(screen);
      MenuSceneRenderer.INSTANCE.render(context, (float)d, (float)d2, !entryEx.isValid(d, d2));
      MenuOverlayRenderer.clear();
      context.getMatrices().push();
      context.getMatrices().translate(entryEx.x, entryEx.y, 0.0F);
      context.getMatrices().scale(entryEx.scale, entryEx.scale, 1.0F);
      boolean flag = active;
      active = true;
      context.enableScissor(0, 0, 440, entryEx.pageHeight);

      try {
         getMenuScreenLayout(screen).render(context, (int)Math.floor(entryEx.computeDouble(d)), (int)Math.floor(entryEx.computeDoubleEx(d2)));
         MenuOverlayRenderer.save(context);
         context.draw();
      } finally {
         active = flag;
         context.disableScissor();
         context.getMatrices().pop();
      }

      if (screen instanceof SouthsideTitleScreen) {
         MenuOverlayRenderer.setScreenRect((ScreenRect)null);
         SkyblockJoinButton.render(context, screen, (float)d, (float)d2);
         MenuOverlayRenderer.save(context);
      }

      ImeOverlayManager.INSTANCE.render(context);
   }

   private static double computeDouble(double d, int n, int n2) {
      if (n > 0 && Double.isFinite(d)) {
         double d2 = d / n * n2;
         return Double.isFinite(d2) && Float.isFinite((float)d2) ? d2 : n2 * 0.5;
      } else {
         return n2 * 0.5;
      }
   }

   public static boolean isValid(Screen screen, double d, double d2, int n) {
      screen.blur();
      ImeOverlayManager.INSTANCE.performActionImpl();
      return n == 0 && screen instanceof SouthsideTitleScreen && SkyblockJoinButton.isValid(screen, d, d2)
         ? true
         : MenuSceneRenderer.INSTANCE.isValid(d, d2, n);
   }

   public record Entry(float x, float y, float width, float height, float scale, int pageHeight) {

      public double computeDouble(double d) {
         return (d - this.x) / this.scale;
      }

      public double computeDoubleEx(double d) {
         return (d - this.y) / this.scale;
      }

      public boolean isValid(double x2, double y2) {
         return x2 >= this.x && x2 < this.x + this.width && y2 >= this.y && y2 < this.y + this.height;
      }

      public float getX() {
         return this.x;
      }

      public float getY() {
         return this.y;
      }

      public float getWidth() {
         return this.width;
      }

      public float getHeight() {
         return this.height;
      }

      public float getScale() {
         return this.scale;
      }

      public int getPageHeight() {
         return this.pageHeight;
      }
   }
}
