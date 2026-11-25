package dev.southside.utils;

import dev.southside.module.impl.world.Scaffold;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.WritableBookItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

public class InventorySlotClassifier {
   public static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final long DEFAULT_LONG = 8983992343849861220L;

   private static boolean isValidItem(ItemStack stack) {
      return InventoryUtils.isEmpty(stack);
   }

   public static boolean matchesSlot(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && (stack.getItem() == Items.EGG || stack.getItem() == Items.SNOWBALL) && isValidItem(stack);
   }

   public static boolean matchesSlotEx(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && stack.getItem() instanceof BowItem && isValidItem(stack);
   }

   public static boolean matchesSlotAlt(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && stack.getItem() instanceof CrossbowItem && isValidItem(stack);
   }

   public static boolean matchesSlotImpl(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && stack.getItem() instanceof SwordItem;
   }

   public static boolean matchesSlotDirect(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && stack.getItem() instanceof AxeItem && !isValidItemAlt(stack) && isValidItem(stack);
   }

   public static boolean matchesSlotSafe(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && stack.getItem() instanceof PickaxeItem && isValidItem(stack);
   }

   public static boolean matchesSlotCustom(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && stack.getItem() instanceof ShovelItem && isValidItem(stack);
   }

   public static boolean matchesSlotWithOffset(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && stack.getItem() instanceof FishingRodItem && isValidItem(stack);
   }

   public static boolean matchesSlotByIndex(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem && !Scaffold.invalidBlocks.contains(blockItem.getBlock()) && isValidItem(stack);
   }

   public static boolean matchesSlotInternal(Slot slot) {
      ItemStack stack = slot.getStack();
      return !stack.isEmpty() && stack.getItem() instanceof AxeItem && isValidItemAlt(stack) && isValidItem(stack) && !isValidItemEx(stack);
   }

   private static boolean isValidItemEx(ItemStack stack) {
      return stack.getName().getString().contains("一刀")
         || !stack.getComponents().isEmpty() && stack.getComponents().toString().contains("一刀")
         || stack.getItem() == Items.GOLDEN_AXE && EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack) > (int)DEFAULT_LONG;
   }

   public static boolean isValidItemAlt(ItemStack stack) {
      return InventoryUtils.isEmptyV3(stack);
   }

   public static boolean isValidItemImpl(ItemStack stack) {
      return InventoryUtils.isValidItemSafe(stack);
   }

   public static boolean isValidItemDirect(ItemStack stack) {
      if (stack.isEmpty()) {
         return true;
      }

      Item item = stack.getItem();
      return item instanceof BlockItem blockItem
         ? blockItem.getBlock() != Blocks.ENCHANTING_TABLE && blockItem.getBlock() != Blocks.COBWEB
         : !(item instanceof WrittenBookItem)
            && !(item instanceof WritableBookItem)
            && !(item instanceof ExperienceBottleItem)
            && !(item instanceof FireworkRocketItem)
            && item != Items.WHEAT_SEEDS
            && item != Items.BEETROOT_SEEDS
            && item != Items.MELON_SEEDS
            && item != Items.PUMPKIN_SEEDS
            && item != Items.FLINT_AND_STEEL;
   }

   public static float computeFloat(ItemStack stack) {
      return InventoryUtils.computeFloatSafe(stack);
   }

   public static float computeFloatEx(ItemStack stack) {
      return InventoryUtils.computeFloatCustom(stack);
   }

   public static float computeFloatAlt(ItemStack stack) {
      return InventoryUtils.computeFloatDirect(stack);
   }

   public static float computeFloatImpl(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         float f = 0.0F;
         if (stack.getItem() instanceof AxeItem && isValidItemAlt(stack)) {
            Item item = stack.getItem();
            if (item == Items.WOODEN_AXE || item == Items.GOLDEN_AXE) {
               f += 4.0F;
            } else if (item == Items.STONE_AXE) {
               f += 5.0F;
            } else if (item == Items.IRON_AXE) {
               f += 6.0F;
            } else if (item == Items.DIAMOND_AXE) {
               f += 7.0F;
            }
         }

         int n = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack);
         if (n > 0) {
            f += 1.0F + (n - 1) * 0.5F;
         }

         return f;
      } else {
         return 0.0F;
      }
   }

   public static float computeFloatDirect(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         float f = 0.0F;
         if (stack.getItem() instanceof SwordItem) {
            f += stack.getDamage() + 1.0F;
         }

         int n = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack);
         if (n > 0) {
            f += 1.0F + (n - 1) * 0.5F;
         }

         return f;
      } else {
         return 0.0F;
      }
   }

   public static float computeFloatSafe(ItemStack stack) {
      return InventoryUtils.computeFloatEx(stack);
   }

   public static float computeFloatCustom(ItemStack stack) {
      return InventoryUtils.computeFloat(stack);
   }

   public static float computeFloat(EquipmentSlot equipmentSlot) {
      if (equipmentSlot == null) {
         return 0.0F;
      }

      return switch (equipmentSlot) {
         case HEAD -> computeFloatCustom((ItemStack)mc.player.getInventory().armor.get(3));
         case CHEST -> computeFloatCustom((ItemStack)mc.player.getInventory().armor.get(2));
         case LEGS -> computeFloatCustom((ItemStack)mc.player.getInventory().armor.get(1));
         case FEET -> computeFloatCustom((ItemStack)mc.player.getInventory().armor.get(0));
         default -> 0.0F;
      };
   }

   public static List<Slot> collectItems() {
      return new ArrayList<>(mc.player.currentScreenHandler.slots);
   }

   public static boolean matchesSlotHelper(Slot slot) {
      return slot.inventory != mc.player.getInventory();
   }

   private static Optional<Slot> getOptional(Predicate<Slot> predicate, Comparator<Slot> comparator) {
      return collectItems().stream().filter(predicate).sorted(comparator).filter(InventorySlotClassifier::matchesSlotHelper).findFirst();
   }

   private static int computeInt(float f) {
      return -((int)(f * 100.0F));
   }

   public static double computeDouble(Slot slot) {
      return -slot.getStack().getCount();
   }

   public static ItemStack getItemStack9() {
      return getOptional(InventorySlotClassifier::matchesSlotByIndex, Comparator.comparingDouble(InventorySlotClassifier::computeDouble))
         .<ItemStack>map(Slot::getStack)
         .orElse(null);
   }

   public static ItemStack getItemStack5() {
      return getOptional(InventorySlotClassifier::matchesSlotImpl, Comparator.comparingInt(slot -> (int)(computeFloatDirect(slot.getStack()) * 100.0F)))
         .<ItemStack>map(Slot::getStack)
         .orElse(null);
   }

   public static ItemStack getItemStack2() {
      return getOptional(InventorySlotClassifier::matchesSlotInternal, Comparator.comparingInt(slot -> computeInt(computeFloatImpl(slot.getStack()))))
         .<ItemStack>map(Slot::getStack)
         .orElse(null);
   }

   public static ItemStack getItemStack() {
      return getOptional(InventorySlotClassifier::matchesSlotDirect, Comparator.comparingInt(slot -> computeInt(computeFloatSafe(slot.getStack()))))
         .<ItemStack>map(Slot::getStack)
         .orElse(null);
   }

   public static ItemStack getItemStack6() {
      return getOptional(InventorySlotClassifier::matchesSlotSafe, Comparator.comparingDouble(slot -> -computeFloatSafe(slot.getStack())))
         .<ItemStack>map(Slot::getStack)
         .orElse(null);
   }

   public static ItemStack getItemStack8() {
      return getOptional(InventorySlotClassifier::matchesSlotCustom, Comparator.comparingInt(slot -> computeInt(computeFloatImpl(slot.getStack()))))
         .<ItemStack>map(Slot::getStack)
         .orElse(null);
   }

   public static ItemStack getItemStack10() {
      return getOptional(InventorySlotClassifier::matchesSlotAlt, Comparator.comparingInt(slot -> computeInt(computeFloatAlt(slot.getStack()))))
         .<ItemStack>map(Slot::getStack)
         .orElse(null);
   }

   public static ItemStack getItemStack3() {
      return getOptional(InventorySlotClassifier::matchesSlotEx, Comparator.comparingInt(slot -> computeInt(computeFloat(slot.getStack()))))
         .<ItemStack>map(Slot::getStack)
         .orElse(null);
   }

   public static ItemStack getItemStack4() {
      return getOptional(InventorySlotClassifier::matchesSlotEx, Comparator.comparingInt(slot -> computeInt(computeFloatEx(slot.getStack()))))
         .<ItemStack>map(Slot::getStack)
         .orElse(null);
   }

   public static ItemStack getItemStack7() {
      return collectItems()
         .stream()
         .filter(InventorySlotClassifier::matchesSlotWithOffset)
         .filter(InventorySlotClassifier::matchesSlotHelper)
         .<ItemStack>map(Slot::getStack)
         .findFirst()
         .orElse(null);
   }

   public static float getProgress() {
      return getOptional(InventorySlotClassifier::matchesSlotImpl, Comparator.comparingDouble(slot -> computeFloatDirect(slot.getStack())))
         .map(slot2 -> computeFloatDirect(slot2.getStack()))
         .orElse(0.0F);
   }

   public static float getProgressEx() {
      return getOptional(InventorySlotClassifier::matchesSlotDirect, Comparator.comparingDouble(slot -> -computeFloatSafe(slot.getStack())))
         .map(slot2 -> computeFloatSafe(slot2.getStack()))
         .orElse(0.0F);
   }

   public static float getProgressAlt() {
      return getOptional(InventorySlotClassifier::matchesSlotSafe, Comparator.comparingDouble(slot -> -computeFloatSafe(slot.getStack())))
         .map(slot2 -> computeFloatSafe(slot2.getStack()))
         .orElse(0.0F);
   }

   public static float getProgressImpl() {
      return getOptional(InventorySlotClassifier::matchesSlotCustom, Comparator.comparingDouble(slot -> -computeFloatSafe(slot.getStack())))
         .map(slot2 -> computeFloatSafe(slot2.getStack()))
         .orElse(0.0F);
   }

   public static float getProgressDirect() {
      return getOptional(InventorySlotClassifier::matchesSlotAlt, Comparator.comparingDouble(slot -> -computeFloatAlt(slot.getStack())))
         .map(slot2 -> computeFloatAlt(slot2.getStack()))
         .orElse(0.0F);
   }

   public static float getProgressSafe() {
      return getOptional(InventorySlotClassifier::matchesSlotEx, Comparator.comparingDouble(slot -> -computeFloat(slot.getStack())))
         .map(slot2 -> computeFloat(slot2.getStack()))
         .orElse(0.0F);
   }

   public static float getProgressCustom() {
      return getOptional(InventorySlotClassifier::matchesSlotEx, Comparator.comparingDouble(slot -> -computeFloatEx(slot.getStack())))
         .map(slot2 -> computeFloatEx(slot2.getStack()))
         .orElse(0.0F);
   }

   public static int computeInt(Item item) {
      DefaultedList main = mc.player.getInventory().main;

      for (int i = 0; i < main.size(); i++) {
         if (((ItemStack)main.get(i)).getItem() == item) {
            return i;
         }
      }

      return -1;
   }
}
