package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ButtonEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ModeValue;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class KeyPearl extends Module {
   @SerializedName("keyModes")
   public final ModeValue keyModes = new ModeValue("Key Modes", "Mouse3", "Mouse3", "Mouse4", "Middle");
   @SerializedName("previousSlot")
   private int previousSlot = -1;
   @SerializedName("boundMouseButton")
   private int boundMouseButton;
   @SerializedName("mouseDown")
   private boolean mouseDown;
   @SerializedName("pearlSelected")
   private boolean pearlSelected;
   @SerializedName("selectPending")
   private boolean selectPending;
   @SerializedName("releasePending")
   private boolean releasePending;
   private static final long DEFAULT_LONG_VALUE = 850119872082870281L;

   public KeyPearl() {
      super(Category.Player, "KeyPearl");
   }

   @EventTarget
   public void onButton(ButtonEvent buttonEvent) {
      if (mc2.player != null && mc2.world != null) {
         this.boundMouseButton = switch (this.keyModes.getDisplayTextAlt()) {
            case "Mouse3" -> 3;
            case "Mouse4" -> 4;
            case "Middle" -> 2;
            default -> this.boundMouseButton;
         };
         if (buttonEvent.getButton() == this.boundMouseButton) {
            if (buttonEvent.getAction() == 1) {
               this.selectPending = true;
               this.mouseDown = true;
            } else if (buttonEvent.getAction() == 0 && this.mouseDown) {
               this.releasePending = true;
               this.mouseDown = false;
            }
         }
      }
   }

   @EventTarget(priority = -7891)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (mc2.player != null) {
         if (this.selectPending) {
            this.performActionAlt();
            disableRotationEvent.setDisableRotation(true);
            this.selectPending = false;
         }

         if (mc2.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {
            disableRotationEvent.setDisableRotation(true);
         }

         if (this.releasePending) {
            this.performActionImpl();
            disableRotationEvent.setDisableRotation(true);
            this.releasePending = false;
         }
      }
   }

   private void performActionAlt() {
      this.previousSlot = mc2.player.getInventory().selectedSlot;

      for (int i = 0; i < (int)DEFAULT_LONG_VALUE; i++) {
         if (mc2.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
            mc2.player.getInventory().selectedSlot = i;
            this.pearlSelected = true;
            return;
         }
      }
   }

   private void performActionImpl() {
      if (this.pearlSelected && this.previousSlot != -1) {
         try {
            ActionResult actionResult = mc2.interactionManager == null ? ActionResult.FAIL : mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND);
            if (actionResult.isAccepted()) {
               mc2.player.swingHand(Hand.MAIN_HAND);
            }
         } finally {
            mc2.player.getInventory().selectedSlot = this.previousSlot;
            this.pearlSelected = false;
            this.previousSlot = -1;
         }
      }
   }

   public static boolean isWorking() {
      KeyPearl keyPearl = Southside.INSTANCE.getModuleManager().getModule(KeyPearl.class);
      return keyPearl.isEnabled() && (keyPearl.pearlSelected || keyPearl.selectPending || keyPearl.releasePending);
   }
}
