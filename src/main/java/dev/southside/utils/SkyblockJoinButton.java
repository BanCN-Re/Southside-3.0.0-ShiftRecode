package dev.southside.utils;

import dev.southside.render.MenuOverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;

public final class SkyblockJoinButton {
   private SkyblockJoinButton() {
   }

   public static void render(DrawContext context, Screen screen, float f, float f2) {
      SkyblockJoinButton.Entry entry = getEntry(screen);
      boolean isValid = SkyblockPreviewWorld.isValid();
      MenuOverlayRenderer.render(context, entry.x, entry.y, entry.width, entry.height, isValid ? "正在进入…" : "加入空岛", !isValid && entry.isValid(f, f2), entry.fontSize);
   }

   public static boolean isValid(Screen screen, double d, double d2) {
      if (!getEntry(screen).isValid(d, d2)) {
         return false;
      }

      if (!SkyblockPreviewWorld.isValid()) {
         SkyblockPreviewWorld.openScreen(MinecraftClient.getInstance().currentScreen);
      }

      return true;
   }

   private static SkyblockJoinButton.Entry getEntry(Screen screen) {
      Window window = MinecraftClient.getInstance().getWindow();
      CustomMenuController.Entry entryExEx = CustomMenuController.getEntryExEx(screen);
      float scale = entryExEx.getScale();
      float f = 124.0F * scale;
      float f2 = 36.0F * scale;
      return new SkyblockJoinButton.Entry(window.getScaledWidth() - entryExEx.getX() - f, window.getScaledHeight() - entryExEx.getY() - f2, f, f2, 14.0F * scale);
   }

   private record Entry(float x, float y, float width, float height, float fontSize) {

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

      public float getFontSize() {
         return this.fontSize;
      }
   }
}
