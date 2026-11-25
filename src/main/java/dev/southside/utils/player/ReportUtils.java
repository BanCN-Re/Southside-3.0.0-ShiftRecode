package dev.southside.utils.player;

import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ScreenEvent;
import dev.southside.utils.IText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReportUtils {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static boolean active;

   public static void applyText(String text) {
      if (mc.player != null && mc.player.networkHandler != null) {
         active = true;
         Southside.INSTANCE.getEventBus().subscribeClass(ReportUtils.class);
         mc.player.networkHandler.sendCommand("report " + text);
         applyText("Sending report for " + text + "...", null);
      } else {
         applyText("Player is not in game.", Formatting.RED);
      }
   }

   @EventTarget
   public static void onScreen(ScreenEvent screenEvent) {
      if (active && screenEvent.screen instanceof GenericContainerScreen genericContainerScreen) {
         active = false;
         new Thread(() -> performAction(genericContainerScreen)).start();
      }
   }

   private static void performAction(GenericContainerScreen genericContainerScreen) {
      long now = System.currentTimeMillis();

      while (System.currentTimeMillis() - now < 2000L) {
         for (Slot slot : ((GenericContainerScreenHandler)genericContainerScreen.getScreenHandler()).slots) {
            if (slot.getStack().getItem() == Items.DIAMOND_SWORD) {
               mc.execute(() -> performAction(genericContainerScreen, slot));
               return;
            }
         }

         try {
            Thread.sleep(50L);
         } catch (InterruptedException e) {
         }
      }

      mc.execute(ReportUtils::performAction);
   }

   private static void performAction(GenericContainerScreen genericContainerScreen, Slot slot) {
      short s = 30132;
      mc.interactionManager.clickSlot(((GenericContainerScreenHandler)genericContainerScreen.getScreenHandler()).syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);
      short s2 = -17274;
      applyText("Report submitted.", null);
      Southside.INSTANCE.getEventBus().unsubscribeClass(ReportUtils.class);
   }

   private static void performAction() {
      applyText("Diamond sword not found in report menu (timeout 2s).", Formatting.RED);
      Southside.INSTANCE.getEventBus().unsubscribeClass(ReportUtils.class);
   }

   private static void applyText(String text, Formatting formatting) {
      if (mc.inGameHud != null) {
         MutableText text2 = Text.literal(text);
         if (formatting != null) {
            text2.setStyle(Style.EMPTY.withColor(formatting));
         }

         mc.inGameHud.getChatHud().addMessage(Text.empty().append(IText.TEXT).append(text2));
      }
   }
}
