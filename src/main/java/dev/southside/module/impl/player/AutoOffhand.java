package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ForwardEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.SlotIndexMapper;
import dev.southside.utils.NumberValue;
import dev.southside.utils.InventoryUtils;
import dev.southside.utils.ModeValue;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.potion.Potions;
import net.minecraft.screen.slot.SlotActionType;

public class AutoOffhand extends Module {
   private static final List<Item> THROWABLE_PRIORITY = List.of(Items.SNOWBALL, Items.EGG);
   @SerializedName("minHealth")
   public final NumberValue minHealth = new NumberValue("Min Health", 8.0F, 1.0F, 24.0F, 0.5F);
   @SerializedName("healItem")
   public final ModeValue healItem = new ModeValue("Heal Item", "Golden Apple", "Golden Apple", "Healing Potion");
   @SerializedName("bedwars")
   public final BooleanValue bedwars = new BooleanValue("Bedwars", false);
   @SerializedName("swapCooldownTicks")
   private int swapCooldownTicks;
   @SerializedName("sprintBlocked")
   private boolean sprintBlocked;

   public AutoOffhand() {
      super(Category.Player, "AutoOffhand", "自动副手");
   }

   @Override
   public void onDisable() {
      this.swapCooldownTicks = 0;
      this.sprintBlocked = false;
      super.onDisable();
   }

   @EventTarget
   public void onForward(ForwardEvent forwardEvent) {
      if (this.sprintBlocked && mc2.player != null && (!mc2.options.jumpKey.isPressed() || !mc2.player.isOnGround())) {
         mc2.options.sprintKey.setPressed(false);
         mc2.player.setSprinting(false);
         forwardEvent.setSprint(false);
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.POST) {
         this.sprintBlocked = false;
      }
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (xevent.getPhase() == Stage.PRE
         && mc2.player != null
         && mc2.world != null
         && mc2.getNetworkHandler() != null
         && !mc2.player.isDead()
         && !(mc2.currentScreen instanceof InventoryScreen)) {
         if (mc2.player.isUsingItem() || mc2.player.handSwinging || mc2.options.useKey.isPressed()) {
            this.swapCooldownTicks = 4;
         } else if (this.swapCooldownTicks > 0) {
            this.swapCooldownTicks--;
         } else {
            ItemStack offHandStack = mc2.player.getOffHandStack();
            if (mc2.player.getHealth() + mc2.player.getAbsorptionAmount() <= ((Number)this.minHealth.getValue()).doubleValue()) {
               this.performAction(offHandStack);
            } else {
               if ((Boolean)this.bedwars.getValue()) {
                  int n = this.computeIntEx();
                  if (n != -1) {
                     if (!isValidItemAlt(offHandStack)) {
                        this.sendPacket(n);
                     }

                     return;
                  }

                  if (isValidItemAlt(offHandStack)) {
                     return;
                  }
               }

               boolean isValidItemImpl = isValidItemImpl(offHandStack);
               int n2 = this.computeInt();
               if (n2 != -1) {
                  if (!isValidItemImpl) {
                     this.sendPacket(n2);
                  }
               } else if (!isValidItemImpl) {
                  this.performAction(offHandStack);
               }
            }
         }
      }
   }

   private void performAction(ItemStack stack) {
      int n = this.computeIntAlt();
      if (n != -1 && !this.isValidItem(stack)) {
         this.sendPacket(n);
      }
   }

   private int computeInt() {
      ItemStack stack = Stream.<ItemStack>concat(mc2.player.getInventory().main.stream(), mc2.player.getInventory().armor.stream())
         .filter(InventoryUtils::isValidItemAlt)
         .max(Comparator.comparingInt(ItemStack::getCount))
         .orElse(null);
      if (stack != null) {
         for (int i = 0; i < 9; i++) {
            if (mc2.player.getInventory().getStack(i) == stack) {
               return i;
            }
         }
      }

      for (Item item : THROWABLE_PRIORITY) {
         int n = this.computeInt(item);
         if (n != -1) {
            return n;
         }
      }

      return -1;
   }

   private int computeIntEx() {
      for (int i = 8; i >= 0; i--) {
         if (isValidItemAlt(mc2.player.getInventory().getStack(i))) {
            return i;
         }
      }

      return -1;
   }

   private int computeInt(Item item) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (!stack.isEmpty() && stack.getItem() == item) {
            return i;
         }
      }

      return -1;
   }

   private int computeIntAlt() {
      if (!this.healItem.matches("Healing Potion")) {
         int n = this.computeInt(Items.ENCHANTED_GOLDEN_APPLE);
         return n != -1 ? n : this.computeInt(Items.GOLDEN_APPLE);
      }

      int n2 = -1;
      int min_value = Integer.MIN_VALUE;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (isValidItemEx(stack)) {
            int n3 = computeInt(stack);
            if (n3 > min_value) {
               min_value = n3;
               n2 = i;
            }
         }
      }

      return n2;
   }

   private boolean isValidItem(ItemStack stack) {
      return this.healItem.matches("Healing Potion")
         ? isValidItemEx(stack)
         : !stack.isEmpty() && (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE);
   }

   private static boolean isValidItemEx(ItemStack stack) {
      Item item = stack.getItem();
      if (!stack.isEmpty() && (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION)) {
         PotionContentsComponent orDefault = (PotionContentsComponent)stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
         return orDefault.matches(Potions.HEALING) || orDefault.matches(Potions.STRONG_HEALING);
      } else {
         return false;
      }
   }

   private static int computeInt(ItemStack stack) {
      Item item = stack.getItem();
      int n = item == Items.SPLASH_POTION ? 3000 : (item == Items.POTION ? 2000 : (item == Items.LINGERING_POTION ? 1000 : 0));
      return n + stack.getCount();
   }

   private static boolean isValidItemAlt(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof BlockItem && SlotIndexMapper.isValid(stack.getItem());
   }

   private static boolean isValidItemImpl(ItemStack stack) {
      return !stack.isEmpty() && THROWABLE_PRIORITY.contains(stack.getItem());
   }

   private void sendPacket(int n) {
      if (n >= 0 && n < 9 && mc2.interactionManager != null && mc2.player.networkHandler != null) {
         if (!Southside.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()) {
            this.sprintBlocked = true;
         }

         if (!mc2.player.lastSprinting) {
            ItemStack stack = mc2.player.getInventory().getStack(n);
            if (stack.isEmpty() || !ItemStack.areItemsEqual(stack, mc2.player.getOffHandStack())) {
               int syncId = mc2.player.playerScreenHandler.syncId;
               mc2.interactionManager.clickSlot(syncId, n + 36, 40, SlotActionType.SWAP, mc2.player);
               mc2.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(syncId));
               this.swapCooldownTicks = Math.max(this.swapCooldownTicks, 3);
            }
         }
      }
   }
}
