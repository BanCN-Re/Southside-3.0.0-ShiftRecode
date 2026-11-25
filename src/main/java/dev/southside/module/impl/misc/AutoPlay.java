package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.EaseInOutCubicAnimation;
import dev.southside.utils.OneConfigIcon;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.OneConfigScreen;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class AutoPlay extends Module {
   @SerializedName("autoScreenShot")
   public final BooleanValue autoScreenShot = new BooleanValue("AutoScreenShot", true);
   @SerializedName("screenCopy")
   public final BooleanValue screenCopy = new BooleanValue("ScreenCopy", false);
   @SerializedName("autoJoin")
   public final BooleanValue autoJoin = new BooleanValue("AutoJoin", true);
   @SerializedName("auto3rdPerson")
   public final BooleanValue auto3rdPerson = new BooleanValue("Auto3rdPerson", true);
   @SerializedName("nextGameDelay")
   public final NumberValue nextGameDelay = new NumberValue("NextGameDelay", 5, 0, 20, 1);
   @SerializedName("joinTimer")
   private final MillisTimer joinTimer = new MillisTimer();
   @SerializedName("screenshotTimer")
   private final MillisTimer screenshotTimer = new MillisTimer();
   @SerializedName("autoJoinPending")
   private boolean autoJoinPending;
   @SerializedName("pendingScreenshot")
   private boolean pendingScreenshot;

   public AutoPlay() {
      super(Category.Misc, "AutoPlay");
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getDirection() == TransferOrigin.RECEIVE && mc2.world != null && directionEvent.getPacket() instanceof TitleS2CPacket titleS2CPacket) {
         ClientWorld world = mc2.world;
         String string = titleS2CPacket.text().getString();
         mc2.execute(() -> {
            if (this.isEnabled() && mc2.world == world) {
               this.applyText(string);
            }
         });
      }
   }

   private void applyText(String text) {
      boolean flag = text.contains("胜利");
      if (flag && (Boolean)this.autoScreenShot.getValue()) {
         if ((Boolean)this.auto3rdPerson.getValue()) {
            mc2.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
         }

         this.pendingScreenshot = true;
         this.screenshotTimer.reset();
      }

      if ((Boolean)this.autoJoin.getValue() && (flag || text.contains("观察者"))) {
         applyText(
            "Will join next game after " + this.nextGameDelay.getValue() + "s", Notification.iconUtil4, ((Number)this.nextGameDelay.getValue()).doubleValue()
         );
         this.autoJoinPending = true;
         this.joinTimer.reset();
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (mc2.player != null) {
         if (this.pendingScreenshot && this.screenshotTimer.hasElapsed(500L)) {
            mc2.execute(this::performActionImpl);
            this.pendingScreenshot = false;
         }

         if ((Boolean)this.autoJoin.getValue()
            && this.autoJoinPending
            && this.joinTimer.hasElapsed(((Number)this.nextGameDelay.getValue()).longValue() * 1000L)) {
            this.performActionAlt();
            this.autoJoinPending = false;
            this.joinTimer.reset();
         }
      }
   }

   private void performActionAlt() {
      if (mc2.interactionManager != null) {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc2.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getName().getString().contains("再来一局")) {
               int selectedSlot = mc2.player.getInventory().selectedSlot;
               mc2.player.getInventory().selectedSlot = i;

               try {
                  mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND);
               } finally {
                  mc2.player.getInventory().selectedSlot = selectedSlot;
               }

               applyText("Used item to join next game", Notification.iconUtil4);
               return;
            }
         }
      }
   }

   private void performActionImpl() {
      String text = "[AutoScreenshot]" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".jpg";

      try {
         ScreenshotRecorder.saveScreenshot(mc2.runDirectory, text, mc2.getFramebuffer(), text2 -> this.applyText(text, text2));
      } finally {
         if ((Boolean)this.auto3rdPerson.getValue()) {
            mc2.options.setPerspective(Perspective.FIRST_PERSON);
         }
      }
   }

   private void applyText(String text, Text text2) {
      mc2.execute(() -> {
         mc2.inGameHud.getChatHud().addMessage(text2);
         if ((Boolean)this.screenCopy.getValue()) {
            File file = new File(mc2.runDirectory, "screenshots/" + text);
            EventQueue.invokeLater(() -> performAction(file));
         }
      });
   }

   private static void performAction(File file) {
      try {
         BufferedImage bufferedImage = ImageIO.read(file);
         if (bufferedImage != null) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new AutoPlay.Entry(bufferedImage), null);
            mc2.execute(() -> applyText("Screenshot copied to clipboard", Notification.iconUtil3));
         }
      } catch (Exception e) {
         mc2.execute(() -> applyText("Failed to copy screenshot", Notification.iconUtil4));
      }
   }

   private static void applyText(String text, OneConfigIcon oneConfigIcon) {
      applyText(text, oneConfigIcon, ((Number)Notification.INSTANCE.keepTime.getValue()).doubleValue());
   }

   private static void applyText(String text, OneConfigIcon oneConfigIcon, double d) {
      if (mc2.world != null || mc2.currentScreen instanceof OneConfigScreen) {
         double d2 = d * 1000.0;
         EaseInOutCubicAnimation easeInOutCubicAnimation = new EaseInOutCubicAnimation((int)d2, 0.0F, 1.0F, false);
         Notifications.INSTANCE.send("AutoPlay", text, oneConfigIcon, (float)d2, () -> Notification.computeFloat(easeInOutCubicAnimation));
      }
   }

   private record Entry(BufferedImage image) implements Transferable {
      @Override
      public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
         return DataFlavor.imageFlavor.equals(dataFlavor);
      }

      @Override
      public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[]{DataFlavor.imageFlavor};
      }

      @Override
      public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException {
         if (!this.isDataFlavorSupported(dataFlavor)) {
            throw new UnsupportedFlavorException(dataFlavor);
         } else {
            return this.image;
         }
      }

      public BufferedImage getImage() {
         return this.image;
      }
   }
}
