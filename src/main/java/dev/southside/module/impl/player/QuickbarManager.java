package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.InventoryUtils;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class QuickbarManager extends Module {
   private static final int DEFAULT_INT = 5;
   @SerializedName("swordSlot")
   public final NumberValue swordSlot = new NumberValue("Sword Slot", 1.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("pickaxeSlot")
   public final NumberValue pickaxeSlot = new NumberValue("Pickaxe Slot", 2.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("axeSlot")
   public final NumberValue axeSlot = new NumberValue("Axe Slot", 3.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("shearsSlot")
   public final NumberValue shearsSlot = new NumberValue("Shears Slot", 4.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("goldenAppleSlot")
   public final NumberValue goldenAppleSlot = new NumberValue("Golden Apple Slot", 5.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("fireballSlot")
   public final NumberValue fireballSlot = new NumberValue("Fireball Slot", 6.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("swapDelayMs")
   public final NumberValue swapDelayMs = new NumberValue("Swap Delay (ms)", 120.0F, 0.0F, 500.0F, 10.0F);
   @SerializedName("swapCooldown")
   private final MillisTimer swapCooldown = new MillisTimer();
   @SerializedName("useCooldownTicks")
   private int useCooldownTicks;
   @SerializedName("autoOffhand")
   private AutoOffhand autoOffhand;

   public QuickbarManager() {
      super(Category.Player, "QuickbarManager", "无需打开背包的快捷栏管理");
   }

   @Override
   public void onDisable() {
      this.useCooldownTicks = 0;
      super.onDisable();
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (xevent.getPhase() == Stage.PRE
         && mc2.player != null
         && mc2.world != null
         && mc2.getNetworkHandler() != null
         && mc2.currentScreen == null
         && !this.isValidEx()
         && !this.isValidAlt()) {
         if (!this.ensureSlot(this.swordSlot, stack -> stack.getItem() instanceof SwordItem, InventoryUtils::computeFloatImpl)
            && !this.ensureSlot(this.pickaxeSlot, stack2 -> stack2.getItem() instanceof PickaxeItem, InventoryUtils::computeFloatEx)
            && !this.ensureSlot(this.axeSlot, stack3 -> stack3.getItem() instanceof AxeItem, InventoryUtils::computeFloatEx)
            && !this.ensureSlot(this.shearsSlot, stack4 -> stack4.getItem() instanceof ShearsItem, QuickbarManager::computeDoubleEx)
            && !this.ensureSlot(this.goldenAppleSlot, QuickbarManager::isValidItem, QuickbarManager::computeDouble)) {
            this.ensureSlot(this.fireballSlot, stack5 -> stack5.getItem() == Items.FIRE_CHARGE, ItemStack::getCount);
         }
      }
   }

   private boolean isValidEx() {
      if (mc2.player.isUsingItem() || mc2.player.handSwinging || mc2.options.useKey.isPressed()) {
         this.useCooldownTicks = 5;
         return true;
      } else if (this.useCooldownTicks > 0) {
         this.useCooldownTicks--;
         return true;
      } else {
         return false;
      }
   }

   private boolean ensureSlot(NumberValue numberValue, Predicate<ItemStack> predicate, ToDoubleFunction<ItemStack> toDoubleFunction) {
      int n = ((Number)numberValue.getValue()).intValue() - 1;
      if (n >= 0 && n < 9 && !predicate.test(mc2.player.getInventory().getStack(n))) {
         int n2 = this.computeInt(predicate, toDoubleFunction);
         return n2 != -1 && this.sendPacket(n2, n);
      } else {
         return false;
      }
   }

   private int computeInt(Predicate<ItemStack> predicate, ToDoubleFunction<ItemStack> toDoubleFunction) {
      int n = -1;
      double negative_infinity = Double.NEGATIVE_INFINITY;

      for (int i = 0; i < mc2.player.getInventory().main.size(); i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (!stack.isEmpty() && predicate.test(stack)) {
            double d = toDoubleFunction.applyAsDouble(stack);
            if (d > negative_infinity) {
               negative_infinity = d;
               n = i;
            }
         }
      }

      return n;
   }

   private boolean sendPacket(int n, int n2) {
      if (n != n2 && n2 >= 0 && n2 < 9 && mc2.interactionManager != null && mc2.player.networkHandler != null) {
         if (!this.swapCooldown.hasTimeElapsed(((Number)this.swapDelayMs.getValue()).longValue()) || mc2.player.lastSprinting) {
            return true;
         } else if (n >= 0 && n < mc2.player.getInventory().main.size()) {
            int n3 = n < 9 ? n + 36 : n;
            int syncId = mc2.player.playerScreenHandler.syncId;
            mc2.interactionManager.clickSlot(syncId, n3, n2, SlotActionType.SWAP, mc2.player);
            mc2.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(syncId));
            this.swapCooldown.reset();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isValidAlt() {
      if (this.autoOffhand == null && Southside.INSTANCE != null) {
         try {
            this.autoOffhand = Southside.INSTANCE.getModuleManager().getModule(AutoOffhand.class);
         } catch (NoSuchElementException e) {
            return false;
         }
      }

      return this.autoOffhand != null && this.autoOffhand.isEnabled() && isValidItem(mc2.player.getOffHandStack());
   }

   private static boolean isValidItem(ItemStack stack) {
      return !stack.isEmpty() && (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE);
   }

   private static double computeDouble(ItemStack stack) {
      return stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE ? 1000 + stack.getCount() : stack.getCount();
   }

   private static double computeDoubleEx(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0.0;
      } else {
         return stack.isDamageable() ? stack.getMaxDamage() - stack.getDamage() : stack.getCount();
      }
   }
}
