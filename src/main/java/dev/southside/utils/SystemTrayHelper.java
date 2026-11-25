package dev.southside.utils;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class SystemTrayHelper implements IMinecraft {
   public static void performAction(SystemTray systemTray, TrayIcon trayIcon) {
      try {
         systemTray.add(trayIcon);
      } catch (Exception e) {
      }
   }

   public static boolean isValid() {
      return SystemTray.getSystemTray() != null;
   }

   public static TrayIcon getTrayIcon(Identifier id, String text) {
      TrayIcon trayIcon = null;

      try {
         BufferedImage bufferedImage = ImageIO.read(((Resource)mc.getResourceManager().getResource(id).orElse(null)).getInputStream());
         trayIcon = new TrayIcon(bufferedImage, text);
         trayIcon.setImageAutoSize(true);
      } catch (Exception e) {
      }

      return trayIcon;
   }
}
