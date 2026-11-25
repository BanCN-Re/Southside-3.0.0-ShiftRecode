package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.misc.SlotIndexRecord;
import java.util.function.Predicate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class ItemScoreUtils {
   private static final ItemScoreUtils.Inner INNER = new ItemScoreUtils.Inner();

   public static boolean isBlockItem(ItemStack stack) {
      return stack.getItem() instanceof BlockItem;
   }

   public static void clickSlot(int n, int n2) {
      IMinecraft.mc.interactionManager.clickSlot(IMinecraft.mc.player.currentScreenHandler.syncId, n, n2, SlotActionType.SWAP, IMinecraft.mc.player);
   }

   public static boolean isValidItem(ItemStack stack) {
      return stack.getItem() == Items.SLIME_BALL && EnchantmentUtils.computeInt(Enchantments.KNOCKBACK, stack) == 3;
   }

   public static double computeDouble(ItemStack stack) {
      return stack.getItem() instanceof BowItem && stack.hasEnchantments() ? EnchantmentUtils.computeInt(Enchantments.POWER, stack) : 0.0;
   }

   public static double computeDoubleEx(ItemStack stack) {
      return stack.getItem() instanceof CrossbowItem && stack.hasEnchantments() ? EnchantmentUtils.computeInt(Enchantments.POWER, stack) : 0.0;
   }

   public static boolean isValid(PlayerInventory inventory) {
      for (ItemStack stack : inventory.main) {
         if (stack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public static boolean isValid(ScreenHandler handler, ItemStack stack, boolean flag) {
      short s = 16246;
      double d = computeDoubleAlt(stack);
      double d2 = 0.0;
      ItemStack empty = ItemStack.EMPTY;
      long l = 1121587647071470731L;

      for (int i = 9; i < 45; i++) {
         ItemStack stack2 = handler.getSlot(i).getStack();
         if (isEmpty(stack2)) {
            d = computeDoubleAlt(stack2);
            if (d > d2) {
               d2 = d;
               empty = stack2;
            }
         }
      }

      if (flag && IMinecraft.mc.player != null) {
         double d3 = 0.0;

         for (int j = 0; j < 36; j++) {
            ItemStack stack3 = IMinecraft.mc.player.getInventory().getStack(j);
            if (isEmpty(stack3)) {
               d = computeDoubleAlt(stack3);
               if (d > d3) {
                  d3 = d;
               }
            }
         }

         return d > d3;
      } else {
         return empty.equals(stack) || d > d2;
      }
   }

   private static boolean isEmpty(ItemStack stack) {
      return !stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof TridentItem);
   }

   private static double computeDoubleAlt(ItemStack stack) {
      double d = IMinecraft.mc.player.getAttributes().hasAttribute(EntityAttributes.ATTACK_DAMAGE)
         ? IMinecraft.mc.player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE)
         : 0.0;
      AttributeModifiersComponent orDefault = (AttributeModifiersComponent)stack.getOrDefault(
         DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT
      );
      double d2 = orDefault.applyOperations(d, EquipmentSlot.MAINHAND);
      int n = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack);
      if (n > 0) {
         d2 += 1.0 + (n - 1) * 0.5;
      }

      return d2;
   }

   public static SlotIndexRecord getSlotIndexRecord(Predicate<ItemStack> predicate, int n) {
      if (IMinecraft.mc.player == null) {
         return new SlotIndexRecord(0, 0);
      }

      int n2 = -1;
      int n3 = 0;

      for (int i = 0; i <= n; i++) {
         ItemStack stack = IMinecraft.mc.player.getInventory().getStack(i);
         if (predicate.test(stack)) {
            if (n2 == -1) {
               n2 = i;
            }

            n3 += stack.getCount();
         }
      }

      return new SlotIndexRecord(n2, n3);
   }

   public static boolean isValid(Item[] itemArray, ItemStack stack) {
      for (Item item : itemArray) {
         if (stack.getItem() == item) {
            return true;
         }
      }

      return false;
   }

   public static SlotIndexRecord getSlotIndexRecord(Predicate<ItemStack> predicate) {
      return IMinecraft.mc.player == null ? new SlotIndexRecord(0, 0) : getSlotIndexRecord(predicate, IMinecraft.mc.player.getInventory().size());
   }

   public static SlotIndexRecord getSlotIndexRecord(Item... itemArray) {
      return getSlotIndexRecord(stack -> isValid(itemArray, stack));
   }

   public static ItemScoreUtils.Inner getInner() {
      INNER.slotActionType = SlotActionType.PICKUP;
      return INNER;
   }

   public record Entry(int slotIndex, double score, ItemStack itemStack) {
      public int getSlotIndex() {
         return this.slotIndex;
      }

      public double getScore() {
         return this.score;
      }

      public ItemStack getItemStack() {
         return this.itemStack;
      }
   }

   public static class Inner {
      @SerializedName("slotActionType")
      public SlotActionType slotActionType;
      @SerializedName("clickBothSlots")
      public boolean clickBothSlots;
      @SerializedName("primarySlot")
      public int primarySlot = -1;
      @SerializedName("secondarySlot")
      public int secondarySlot = -1;
      @SerializedName("button")
      public int button;
      @SerializedName("clickInProgress")
      public boolean clickInProgress;

      public ItemScoreUtils.Inner getInnerEx(int n) {
         this.primarySlot = n;
         return this;
      }

      public ItemScoreUtils.Inner getInnerExEx(int n) {
         return this.getInnerEx(SlotIndexMapper.computeInt(n));
      }

      public void onKeyTyped(int n) {
         this.secondarySlot = n;
         this.performAction();
      }

      public void onKeyTypedEx(int n) {
         this.onKeyTyped(SlotIndexMapper.computeInt(n));
      }

      public void onKeyTypedAlt(int n) {
         this.primarySlot = this.secondarySlot = n;
         this.performAction();
      }

      public void onKeyTypedImpl(int n) {
         this.onKeyTypedAlt(SlotIndexMapper.computeInt(n));
      }

      public void clickSlot(int n) {
         IMinecraft.mc
            .interactionManager
            .clickSlot(IMinecraft.mc.player.currentScreenHandler.syncId, n, this.button, this.slotActionType, IMinecraft.mc.player);
      }

      public void performAction() {
         boolean isEmpty = IMinecraft.mc.player.currentScreenHandler.getCursorStack().isEmpty();
         if (this.slotActionType == SlotActionType.SWAP) {
            this.button = this.primarySlot;
            this.primarySlot = this.secondarySlot;
         }

         if (this.slotActionType != null && this.primarySlot != -1 && this.secondarySlot != -1) {
            this.clickSlot(this.primarySlot);
            if (this.clickBothSlots) {
               this.clickSlot(this.secondarySlot);
            }
         }

         SlotActionType slotActionType2 = this.slotActionType;
         boolean clickBothSlots2 = this.clickBothSlots;
         int primarySlot2 = this.primarySlot;
         int secondarySlot2 = this.secondarySlot;
         this.slotActionType = null;
         this.clickBothSlots = false;
         this.primarySlot = -1;
         this.secondarySlot = -1;
         this.button = 0;
         if (!this.clickInProgress
            && isEmpty
            && slotActionType2 == SlotActionType.PICKUP
            && clickBothSlots2
            && primarySlot2 != -1
            && secondarySlot2 != -1
            && !IMinecraft.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            this.clickInProgress = true;
            ItemScoreUtils.getInner().onKeyTypedAlt(primarySlot2);
            this.clickInProgress = false;
         }
      }
   }
}
