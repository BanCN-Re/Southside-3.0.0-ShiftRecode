package dev.southside.utils;

import dev.southside.module.impl.world.Scaffold;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;

public class InventoryUtils {
   public static final MinecraftClient mc = MinecraftClient.getInstance();

   public static boolean isEmpty(ItemStack stack) {
      if (!stack.isEmpty() && !(stack.getItem() instanceof PlayerHeadItem)) {
         String string = stack.getName().getString();
         return !string.contains("Click")
            && !string.contains("Right")
            && !string.contains("点击")
            && !string.contains("Teleport")
            && !string.contains("使用")
            && !string.contains("传送")
            && !string.contains("再来");
      } else {
         return false;
      }
   }

   public static boolean isValidItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }

      String string = stack.getName().getString();
      return string.contains("长按点击") || string.contains("点击使用") || string.contains("离开游戏") || string.contains("选择一个队伍") || string.contains("再来一局");
   }

   public static boolean isValid(Item item, ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == item;
   }

   public static boolean isEmptyEx(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof SwordItem;
   }

   public static boolean isEmptyAlt(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof BowItem && isEmpty(stack);
   }

   public static boolean isEmptyImpl(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof CrossbowItem && isEmpty(stack);
   }

   public static boolean isEmptyDirect(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof PickaxeItem && isEmpty(stack);
   }

   public static boolean isEmptySafe(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof ShovelItem && isEmpty(stack);
   }

   public static boolean isEmptyCustom(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof AxeItem && !isEmptyV3(stack) && isEmpty(stack);
   }

   public static boolean isEmptyWithOffset(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof AxeItem && isEmptyV3(stack) && isEmpty(stack) && !contains(stack);
   }

   public static boolean isEmptyByIndex(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof FishingRodItem && isEmpty(stack);
   }

   public static boolean isEmptyInternal(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof ArrowItem && isEmpty(stack);
   }

   public static boolean isValidItemEx(ItemStack stack) {
      return !stack.isEmpty() && (stack.getItem() == Items.EGG || stack.getItem() == Items.SNOWBALL);
   }

   public static boolean isValidItemAlt(ItemStack stack) {
      return isValidItemEx(stack) && isEmpty(stack);
   }

   public static boolean isValidItemImpl(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == Items.STICK && EnchantmentUtils.computeInt(Enchantments.KNOCKBACK, stack) > 1;
   }

   public static boolean isValid(EquipmentSlot equipmentSlot, ItemStack stack) {
      return !stack.isEmpty()
         && stack.getItem() instanceof ArmorItem
         && ((EquippableComponent)Objects.requireNonNull((EquippableComponent)stack.get(DataComponentTypes.EQUIPPABLE))).slot() == equipmentSlot;
   }

   public static boolean isValidItemDirect(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SkullBlock;
   }

   public static boolean isEmptyHelper(ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
         BlockState defaultState = blockItem.getBlock().getDefaultState();
         Object world = mc.world;
         if (world == null) {
            world = EmptyBlockView.INSTANCE;
         }

         return defaultState.isFullCube((BlockView)world, BlockPos.ORIGIN) && !Scaffold.invalidBlocks.contains(blockItem.getBlock()) && isEmpty(stack);
      } else {
         return false;
      }
   }

   public static boolean isEmptyV2(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof BlockItem && !stack.getItem().equals(Items.TNT) && isEmptyHelper(stack);
   }

   public static boolean isEmptyV3(ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem() instanceof AxeItem) {
         int n = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack);
         return n >= 8 && n < 50;
      } else {
         return false;
      }
   }

   public static boolean contains(ItemStack stack) {
      return stack.getName().getString().contains("一刀")
         || stack.getComponents() != null && stack.getComponents().toString().contains("一刀")
         || stack.getItem() == Items.GOLDEN_AXE && EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack) > 100;
   }

   public static boolean isValidItemSafe(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }

      Item item = stack.getItem();
      return item instanceof AxeItem && item == Items.GOLDEN_AXE && EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack) > 100
         || item == Items.SLIME_BALL && EnchantmentUtils.computeInt(Enchantments.KNOCKBACK, stack) > 1
         || item == Items.TOTEM_OF_UNDYING
         || item == Items.END_CRYSTAL;
   }

   public static int computeInt(int n, int n2, Item item) {
      if (mc.player == null) {
         return -1;
      }

      for (int i = n; i < n2; i++) {
         if (isValid(item, mc.player.getInventory().getStack(i))) {
            return i;
         }
      }

      return -1;
   }

   public static float computeFloat(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         int n = 0;
         if (stack.getItem() instanceof ArmorItem) {
            EquippableComponent equippableComponent = (EquippableComponent)stack.get(DataComponentTypes.EQUIPPABLE);
            RegistryKey registryKey = equippableComponent == null ? null : (RegistryKey)equippableComponent.assetId().orElse(null);
            if (EquipmentAssetKeys.LEATHER.equals(registryKey)) {
               n += 100;
            } else if (EquipmentAssetKeys.CHAINMAIL.equals(registryKey)) {
               n += 200;
            } else if (EquipmentAssetKeys.IRON.equals(registryKey)) {
               n += 400;
            } else if (EquipmentAssetKeys.GOLD.equals(registryKey)) {
               n += 300;
            } else if (EquipmentAssetKeys.DIAMOND.equals(registryKey)) {
               n += 500;
            } else if (EquipmentAssetKeys.NETHERITE.equals(registryKey)) {
               n += 600;
            }
         }

         n += EnchantmentUtils.computeInt(Enchantments.PROTECTION, stack);
         return n;
      } else {
         return 0.0F;
      }
   }

   public static float computeFloatEx(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && !isValidItemSafe(stack) && !isEmptyV3(stack)) {
         float f = 0.0F;
         if (stack.getItem() instanceof PickaxeItem) {
            f += stack.getMiningSpeedMultiplier(Blocks.STONE.getDefaultState());
         } else if (stack.getItem() instanceof AxeItem) {
            f += stack.getMiningSpeedMultiplier(Blocks.OAK_LOG.getDefaultState());
         } else {
            if (!(stack.getItem() instanceof ShovelItem)) {
               return 0.0F;
            }

            f += stack.getMiningSpeedMultiplier(Blocks.DIRT.getDefaultState());
         }

         int n = EnchantmentUtils.computeInt(Enchantments.EFFICIENCY, stack);
         if (n > 0) {
            f += n * 0.0075F;
         }

         return f;
      } else {
         return 0.0F;
      }
   }

   public static int computeInt(ItemStack stack) {
      return (int)(computeFloatEx(stack) * 100.0F);
   }

   private static float computeFloatAlt(ItemStack stack) {
      RegistryEntry attack_damage = EntityAttributes.ATTACK_DAMAGE;
      double d = IMinecraft.mc.player.getAttributes().hasAttribute(attack_damage) ? IMinecraft.mc.player.getAttributeBaseValue(attack_damage) : 0.0;
      AttributeModifiersComponent orDefault = (AttributeModifiersComponent)stack.getOrDefault(
         DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT
      );
      return (float)orDefault.applyOperations(d, EquipmentSlot.MAINHAND);
   }

   public static float computeFloatImpl(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         float f = 0.0F;
         if (stack.getItem() instanceof SwordItem) {
            f += computeFloatAlt(stack);
         }

         int n = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack);
         if (n > 0) {
            f += 1.25F * n;
         }

         return f;
      } else {
         return 0.0F;
      }
   }

   public static int computeIntEx(ItemStack stack) {
      return (int)(computeFloatImpl(stack) * 100.0F);
   }

   public static int computeIntAlt(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         float f = 0.0F;
         if (stack.getItem() instanceof AxeItem && isEmptyV3(stack)) {
            f += computeFloatAlt(stack);
         }

         int n = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack);
         if (n > 0) {
            f += 1.25F * n;
         }

         return (int)(f * 100.0F);
      } else {
         return 0;
      }
   }

   public static float computeFloatDirect(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && stack.getItem() instanceof CrossbowItem) {
         int n = EnchantmentUtils.computeInt(Enchantments.QUICK_CHARGE, stack);
         n += EnchantmentUtils.computeInt(Enchantments.MULTISHOT, stack);
         n += EnchantmentUtils.computeInt(Enchantments.PIERCING, stack);
         return n;
      } else {
         return 0.0F;
      }
   }

   public static int computeIntImpl(ItemStack stack) {
      return (int)(computeFloatDirect(stack) * 100.0F);
   }

   public static float computeFloatSafe(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && stack.getItem() instanceof BowItem) {
         float f = 10.0F;
         f += EnchantmentUtils.computeInt(Enchantments.PUNCH, stack) / 10.0F;
         f += EnchantmentUtils.computeInt(Enchantments.INFINITY, stack);
         f += EnchantmentUtils.computeInt(Enchantments.FLAME, stack);
         f += EnchantmentUtils.computeInt(Enchantments.POWER, stack);
         return f + (float)stack.getDamage() / stack.getMaxDamage();
      } else {
         return 0.0F;
      }
   }

   public static int computeIntDirect(ItemStack stack) {
      return (int)(computeFloatSafe(stack) * 100.0F);
   }

   public static float computeFloatCustom(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && stack.getItem() instanceof BowItem) {
         float f = 10.0F;
         f += EnchantmentUtils.computeInt(Enchantments.PUNCH, stack);
         f += EnchantmentUtils.computeInt(Enchantments.INFINITY, stack);
         f += EnchantmentUtils.computeInt(Enchantments.FLAME, stack);
         f += EnchantmentUtils.computeInt(Enchantments.POWER, stack) / 10.0F;
         return f + (float)stack.getDamage() / stack.getMaxDamage();
      } else {
         return 0.0F;
      }
   }

   public static int computeIntSafe(ItemStack stack) {
      return (int)(computeFloatCustom(stack) * 100.0F);
   }

   public static float computeFloatWithOffset(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && stack.getItem() instanceof BowItem) {
         float f = 10.0F;
         f += EnchantmentUtils.computeInt(Enchantments.FLAME, stack) * 2.0F;
         f += EnchantmentUtils.computeInt(Enchantments.INFINITY, stack);
         f += EnchantmentUtils.computeInt(Enchantments.POWER, stack) / 10.0F;
         f += EnchantmentUtils.computeInt(Enchantments.PUNCH, stack) / 10.0F;
         return f + (float)stack.getDamage() / stack.getMaxDamage();
      } else {
         return 0.0F;
      }
   }

   public static int computeIntCustom(ItemStack stack) {
      return (int)(computeFloatWithOffset(stack) * 100.0F);
   }

   public static int computeIntWithOffset(ItemStack stack) {
      return EnchantmentUtils.computeInt(Enchantments.PUNCH, stack);
   }

   public static int computeIntByIndex(ItemStack stack) {
      return EnchantmentUtils.computeInt(Enchantments.POWER, stack);
   }
}
