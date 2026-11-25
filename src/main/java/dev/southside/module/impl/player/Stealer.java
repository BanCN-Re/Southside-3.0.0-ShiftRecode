package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ItemScoreUtils;
import dev.southside.utils.HotbarItemFinder;
import dev.southside.utils.EnchantmentUtils;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.InventorySlotClassifier;
import dev.southside.utils.InventoryUtils;
import dev.southside.utils.RotationManager;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.Rotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
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
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.WritableBookItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;

public class Stealer extends Module {
   private static final MillisTimer lastTimeUtil = new MillisTimer();
   private static final MillisTimer lastTimeUtil2 = new MillisTimer();
   private static final MillisTimer lastTimeUtil3 = new MillisTimer();
   private static final long DEFAULT_LONG_VALUE = 150L;
   @SerializedName("useSwap")
   public final BooleanValue useSwap = new BooleanValue("Use Swap", false);
   @SerializedName("silentlyStealing")
   public final BooleanValue silentlyStealing = new BooleanValue("Silently Stealing", false);
   @SerializedName("instant")
   public final BooleanValue instant = new BooleanValue("Instant", false);
   @SerializedName("smart")
   public final BooleanValue smart = new BooleanValue("Smart", false);
   @SerializedName("doubleClick")
   public final BooleanValue doubleClick = new BooleanValue("DoubleClick", false);
   @SerializedName("doubleClickHalf")
   public final BooleanValue doubleClickHalf = new BooleanValue("DoubleClickHalf", false);
   @SerializedName("invManage")
   public final BooleanValue invManage = new BooleanValue("InvManage", false);
   @SerializedName("invManagerBoost")
   public final BooleanValue invManagerBoost = new BooleanValue("InvManager Boost", false);
   @SerializedName("startDelay")
   public final NumberValue startDelay = new NumberValue("Start Delay", 350, 0, 600, 1);
   @SerializedName("closeDelay")
   public final NumberValue closeDelay = new NumberValue("Close Delay", 350, 0, 600, 1);
   @SerializedName("minDelay")
   public final NumberValue minDelay = new NumberValue("Min Delay", 80, 0, 200, 1);
   @SerializedName("maxDelay")
   public final NumberValue maxDelay = new NumberValue("Max Delay", 100, 0, 200, 1);
   @SerializedName("multiStackDelay")
   public final NumberValue multiStackDelay = new NumberValue("Multi Stack Delay", 130, 0, 200, 1);
   @SerializedName("enderChest")
   public final BooleanValue enderChest = new BooleanValue("Ender Chest", false);
   @SerializedName("autoDisable")
   public final BooleanValue autoDisable = new BooleanValue("Auto Disable", false);
   @SerializedName("nextClickDelay")
   private long nextClickDelay;
   @SerializedName("previousScreen")
   private Screen previousScreen;

   public Stealer() {
      super(Category.Player, "Stealer", "打不过zen");
   }

   @Override
   public void postInit() {
      this.addRangedValueRestrict(this.minDelay, this.maxDelay);
   }

   @EventTarget(priority = 1)
   public void onX(XEvent xevent) {
      if (xevent.getPhase() != Stage.POST && mc2.player != null && mc2.world != null) {
         if ((Boolean)this.autoDisable.getValue()) {
            if (!mc2.player.isAlive()) {
               this.setEnabled(false);
               return;
            }

            if (mc2.player.age <= 1) {
               this.setEnabled(false);
            }
         }

         Screen currentScreen = mc2.currentScreen;
         if (currentScreen instanceof GenericContainerScreen genericContainerScreen) {
            GenericContainerScreenHandler screenHandler = (GenericContainerScreenHandler)genericContainerScreen.getScreenHandler();
            if (currentScreen != this.previousScreen) {
               lastTimeUtil.reset();
               this.nextClickDelay = (long)((Number)this.startDelay.getValue()).floatValue();
            } else if (this.isValid(genericContainerScreen)) {
               if (!(Boolean)this.instant.getValue() && this.isValidEx(screenHandler) && lastTimeUtil.hasTimeElapsed(this.nextClickDelay)) {
                  this.performActionAlt();
               } else if ((Boolean)this.instant.getValue() && lastTimeUtil.hasTimeElapsed(this.nextClickDelay)) {
                  if (!this.isValid(screenHandler)) {
                     return;
                  }
               } else {
                  this.performAction(screenHandler);
               }
            }
         }

         this.previousScreen = currentScreen;
      }
   }

   private boolean isValid(GenericContainerScreen genericContainerScreen) {
      String string = genericContainerScreen.getTitle().getString();
      return string.equals(Text.translatable("container.chest").getString())
         || string.equals(Text.translatable("container.chestDouble").getString())
         || string.equals("Chest")
         || (Boolean)this.enderChest.getValue() && string.equals(Text.translatable("container.enderchest").getString());
   }

   private boolean isValid(GenericContainerScreenHandler genericContainerScreenHandler) {
      ArrayList list = new ArrayList();
      this.performAction(list, genericContainerScreenHandler);
      if (list.isEmpty()) {
         this.isValidEx();
         return false;
      }

      boolean flag = true;

      for (Object obj : List.copyOf(list)) {
         Stealer.Entry entry = (Stealer.Entry)obj;
         this.performAction(entry);
         if (entry.type == Stealer.Mode.QUICK_MOVE && (Boolean)this.smart.getValue()) {
            this.performActionImpl();
            return false;
         }

         this.performAction(list, genericContainerScreenHandler);
         if (list.isEmpty()) {
            if (!this.isValidEx()) {
               flag = false;
            }
            break;
         }
      }

      if (flag) {
         this.performActionImpl();
      }

      return true;
   }

   private void performAction(Stealer.Entry entry) {
      int syncId = mc2.player.currentScreenHandler.syncId;
      switch (entry.type) {
         case MOVE_TO_INVENTORY:
            clickSlot(syncId, entry.sourceSlot, 40, SlotActionType.SWAP);
            if (!mc2.player.getOffHandStack().isEmpty()) {
               clickSlot(syncId, entry.targetSlot, 40, SlotActionType.SWAP);
               if ((Boolean)this.doubleClick.getValue() && entry.stack != null && isValidItemSafe(entry.stack)) {
                  clickSlot(syncId, entry.targetSlot, 0, SlotActionType.PICKUP);
                  clickSlot(syncId, entry.targetSlot, 0, SlotActionType.PICKUP_ALL);
                  if ((Boolean)this.doubleClickHalf.getValue()) {
                     clickSlot(syncId, entry.targetSlot, 0, SlotActionType.PICKUP);
                  }
               }
            }
            break;
         case MOVE_TO_OFFHAND:
            clickSlot(syncId, entry.sourceSlot, 40, SlotActionType.SWAP);
            break;
         case QUICK_MOVE:
            clickSlot(syncId, entry.sourceSlot, 0, SlotActionType.QUICK_MOVE);
            break;
         case SWAP_TO_HOTBAR:
            isValid(syncId, entry.sourceSlot, entry.targetSlot);
      }
   }

   private void performAction(GenericContainerScreenHandler genericContainerScreenHandler) {
      for (int n : collectItems(genericContainerScreenHandler)) {
         ItemStack stack = genericContainerScreenHandler.getSlot(n).getStack();
         if (isValidItem(stack) && this.isValid(genericContainerScreenHandler, stack) && lastTimeUtil.hasTimeElapsed(this.nextClickDelay)) {
            OptionalInt optionalInt = this.invManagerBoost.getValue() ? this.getOptionalInt(stack) : OptionalInt.empty();
            if (optionalInt.isPresent() && isValid(genericContainerScreenHandler.syncId, n, optionalInt.getAsInt())) {
               this.performActionImpl();
               break;
            }

            boolean flag = !(Boolean)this.instant.getValue();
            if ((Boolean)this.useSwap.getValue()) {
               int n2 = computeInt();
               if (n2 == -1 || n2 + 18 >= 54) {
                  mc2.player.closeHandledScreen();
               } else if (stack.getMaxCount() <= 1) {
                  if (n2 < 9) {
                     clickSlot(genericContainerScreenHandler.syncId, n, n2, SlotActionType.SWAP);
                  } else {
                     clickSlot(genericContainerScreenHandler.syncId, n2 + 18, 8, SlotActionType.SWAP);
                     clickSlot(genericContainerScreenHandler.syncId, n, 8, SlotActionType.SWAP);
                  }
               } else if (lastTimeUtil2.hasTimeElapsed(((Number)this.multiStackDelay.getValue()).longValue())) {
                  clickSlot(genericContainerScreenHandler.syncId, n, 0, SlotActionType.QUICK_MOVE);
                  lastTimeUtil2.reset();
               }
            } else {
               clickSlot(genericContainerScreenHandler.syncId, n, 0, SlotActionType.QUICK_MOVE);
            }

            if (flag) {
               this.performActionImpl();
               break;
            }
         }
      }
   }

   private void performActionAlt() {
      if (lastTimeUtil3.hasTimeElapsed(((Number)this.closeDelay.getValue()).intValue())) {
         Southside.INSTANCE.getChestTracker().performAction();
         mc2.player.closeHandledScreen();
         lastTimeUtil3.reset();
      }
   }

   private boolean isValidEx() {
      if (lastTimeUtil.hasTimeElapsed(this.nextClickDelay)) {
         if ((Boolean)this.instant.getValue() && ItemScoreUtils.isValid(mc2.player.getInventory())) {
            mc2.player.closeHandledScreen();
            this.applyTextCustom("The inventory is full while instant stealing.");
            this.applyTextCustom("Please clean your inventory and reopen the chest");
            return true;
         }

         this.performActionAlt();
      }

      return false;
   }

   private void performActionImpl() {
      lastTimeUtil.reset();
      long l = Math.max(0L, Math.min(((Number)this.minDelay.getValue()).longValue(), ((Number)this.maxDelay.getValue()).longValue()));
      long l2 = Math.max(0L, Math.max(((Number)this.minDelay.getValue()).longValue(), ((Number)this.maxDelay.getValue()).longValue()));
      this.nextClickDelay = ThreadLocalRandom.current().nextLong(l, l2 + 1L);
   }

   public static boolean isValidAlt() {
      return !lastTimeUtil.hasTimeElapsed(150L);
   }

   private static void clickSlot(int n, int n2, int n3, SlotActionType slotActionType) {
      mc2.interactionManager.clickSlot(n, n2, n3, slotActionType, mc2.player);
   }

   private static List<Integer> collectItems(GenericContainerScreenHandler genericContainerScreenHandler) {
      ArrayList list = new ArrayList();

      for (int i = 0; i < genericContainerScreenHandler.getRows() * 9; i++) {
         list.add(i);
      }

      Collections.shuffle(list);
      return list;
   }

   private void performAction(List<Stealer.Entry> list, GenericContainerScreenHandler genericContainerScreenHandler) {
      list.clear();
      List list2 = collectItems(genericContainerScreenHandler);
      boolean flag = true;
      if ((Boolean)this.invManage.getValue()) {
         flag = !this.isValid(list);
         ArrayList list3 = new ArrayList();
         if (!flag) {
            this.performAction(list3);
         }

         for (Object obj : list2) {
            int n = (int)obj;
            if (!isValid(list, n) && !isValid(list3, n)) {
               ItemStack stack = genericContainerScreenHandler.getSlot(n).getStack();
               if (isValidItem(stack)
                  && this.isValid(genericContainerScreenHandler, stack)
                  && (!(Boolean)this.invManagerBoost.getValue() || !this.isValid(list, n, stack))) {
                  performAction(list, list3, n, stack, 0);
               }
            }
         }

         if (!flag) {
            this.performAction(list);
         }
      }

      if (flag) {
         for (Object obj2 : list2) {
            int n2 = (int)obj2;
            ItemStack stack2 = genericContainerScreenHandler.getSlot(n2).getStack();
            if (isValidItem(stack2)
               && this.isValid(genericContainerScreenHandler, stack2)
               && (!(Boolean)this.invManagerBoost.getValue() || !this.isValid(list, n2, stack2))) {
               performAction(list, List.of(), n2, stack2, isValidItemSafe(stack2) && this.smart.getValue() ? -1 : 0);
            }
         }
      }
   }

   private static boolean isValid(List<Stealer.Entry> list, int n) {
      return list.stream().anyMatch(entry -> entry.sourceSlot == n);
   }

   private static boolean isValidEx(List<Stealer.Entry> list, int n) {
      return list.stream().anyMatch(entry -> entry.targetSlot == n);
   }

   private static void performAction(List<Stealer.Entry> list, List<Stealer.Entry> list2, int n, ItemStack stack, int n2) {
      for (Slot slot : mc2.player.currentScreenHandler.slots) {
         if (slot.inventory == mc2.player.getInventory() && slot.getStack().isEmpty() && !isValidEx(list, slot.id) && !isValidEx(list2, slot.id)) {
            list.add(new Stealer.Entry(n, n2, Stealer.Mode.QUICK_MOVE, stack));
            break;
         }
      }
   }

   private boolean isValid(List<Stealer.Entry> list) {
      if (!(Boolean)this.invManage.getValue() || !(Boolean)this.instant.getValue()) {
         return false;
      }

      if (!(mc2.currentScreen instanceof GenericContainerScreen)) {
         return true;
      }

      InvManager invManager = getInvManager();
      if (!invManager.isValidEx()) {
         this.applyTextDirect("Inv 配置错误..");
         return false;
      }

      if ((Boolean)invManager.switchGoldenApple.getValue() && !invManager.offhandItems.matches("Golden Apple")) {
         this.performAction(list, computeInt(invManager.goldenAppleSlot), Items.GOLDEN_APPLE);
      }

      if ((Boolean)invManager.switchBlock.getValue()) {
         int n = computeInt(invManager.blockSlot);
         ItemStack stack = (ItemStack)mc2.player.getInventory().main.get(n);
         ItemStack itemStack9 = InventorySlotClassifier.getItemStack9();
         if (itemStack9 != null && (itemStack9.getCount() > stack.getCount() || isValidItemAlt(stack)) && !invManager.offhandItems.matches("Block")) {
            this.performAction(list, n, itemStack9);
         }
      }

      if ((Boolean)invManager.switchSword.getValue()) {
         int n2 = computeInt(invManager.swordSlot);
         ItemStack itemStack5 = InventorySlotClassifier.getItemStack5();
         ItemStack itemStack2 = InventorySlotClassifier.getItemStack2();
         if (InventorySlotClassifier.computeFloatImpl(itemStack2) > InventorySlotClassifier.computeFloatDirect(itemStack5)) {
            itemStack5 = itemStack2;
         }

         ItemStack stack2 = (ItemStack)mc2.player.getInventory().main.get(n2);
         if (itemStack5 != null && computeFloat(itemStack5) > computeFloat(stack2)) {
            this.performAction(list, n2, itemStack5);
         }
      }

      if ((Boolean)invManager.switchPickaxe.getValue()) {
         this.performAction(list, computeInt(invManager.pickaxeSlot), InventorySlotClassifier.getItemStack6(), PickaxeItem.class);
      }

      if ((Boolean)invManager.switchAxe.getValue()) {
         this.performAction(list, computeInt(invManager.axeSlot), InventorySlotClassifier.getItemStack(), AxeItem.class);
      }

      if ((Boolean)invManager.switchRod.getValue() && !invManager.offhandItems.matches("Fishing Rod")) {
         int n3 = computeInt(invManager.rodSlot);
         if (!(((ItemStack)mc2.player.getInventory().main.get(n3)).getItem() instanceof FishingRodItem)) {
            this.performAction(list, n3, InventorySlotClassifier.getItemStack7());
         }
      }

      if ((Boolean)invManager.switchEnderPearl.getValue()) {
         this.performAction(list, computeInt(invManager.enderPearlSlot), Items.ENDER_PEARL);
      }

      if ((Boolean)invManager.switchWaterBucket.getValue()) {
         this.performAction(list, computeInt(invManager.waterBucketSlot), Items.WATER_BUCKET);
      }

      if ((Boolean)invManager.switchFireball.getValue()) {
         this.performAction(list, computeInt(invManager.fireballSlot), Items.FIRE_CHARGE);
      }

      if (InvManager.isValidImpl() && (Boolean)invManager.switchEggsSnowballs.getValue() && !invManager.offhandItems.matches("Projectile")) {
         int n4 = computeInt(invManager.eggsSnowballsSlot);
         if (computeIntEx(Items.EGG) > 0) {
            this.performAction(list, n4, Items.EGG);
         } else if (computeIntEx(Items.SNOWBALL) > 0) {
            this.performAction(list, n4, Items.SNOWBALL);
         }
      }

      return true;
   }

   private void performAction(List<Stealer.Entry> list, int n, ItemStack stack, Class<? extends Item> clazz) {
      ItemStack stack2 = (ItemStack)mc2.player.getInventory().main.get(n);
      if (stack != null
         && clazz.isInstance(stack.getItem())
         && (InventoryUtils.computeFloatEx(stack) > InventoryUtils.computeFloatEx(stack2) || !clazz.isInstance(stack2.getItem()))) {
         this.performAction(list, n, stack);
      }
   }

   private void performAction(List<Stealer.Entry> list, int n, ItemStack stack) {
      ItemStack stack2 = (ItemStack)mc2.player.getInventory().main.get(n);
      if (isValidItemEx(stack2) && stack != stack2) {
         int n2 = computeIntEx(stack);
         if (n2 != -1) {
            list.add(new Stealer.Entry(n2, computeInt(n), Stealer.Mode.MOVE_TO_INVENTORY, stack));
         }
      }
   }

   private void performAction(List<Stealer.Entry> list, int n, Item item) {
      ItemStack stack = (ItemStack)mc2.player.getInventory().main.get(n);
      if (isValidItemEx(stack)) {
         int n2 = computeIntAlt(item);
         if (n2 != -1) {
            ItemStack stack2 = mc2.player.currentScreenHandler.getSlot(n2).getStack();
            if (stack.getItem() != item || stack.getCount() < stack2.getCount()) {
               list.add(new Stealer.Entry(n2, computeInt(n), Stealer.Mode.MOVE_TO_INVENTORY, stack2));
            }
         }
      }
   }

   private void performAction(List<Stealer.Entry> list) {
      if ((Boolean)this.invManage.getValue() && (Boolean)this.instant.getValue() && mc2.currentScreen instanceof GenericContainerScreen genericContainerScreen) {
         InvManager invManager = getInvManager();
         ItemStack offHandStack = mc2.player.getOffHandStack();
         int n = -1;
         if (invManager.offhandItems.matches("Golden Apple")) {
            n = computeIntImpl(Items.GOLDEN_APPLE);
            if (n != -1 && offHandStack != null && offHandStack.getCount() >= ((GenericContainerScreenHandler)genericContainerScreen.getScreenHandler()).getSlot(n).getStack().getCount()) {
               return;
            }
         } else if (invManager.offhandItems.matches("Projectile")) {
            ItemStack stack = InventorySlotClassifier.collectItems()
               .stream()
               .filter(InventorySlotClassifier::matchesSlot)
               .sorted(Comparator.comparingDouble(InventorySlotClassifier::computeDouble))
               .filter(InventorySlotClassifier::matchesSlotHelper)
               .<ItemStack>map(Slot::getStack)
               .findFirst()
               .orElse(null);
            if (stack != null) {
               n = computeIntEx(stack);
            } else {
               stack = getStream().filter(InventoryUtils::isValidItemAlt).max(Comparator.comparingInt(ItemStack::getCount)).orElse(null);
               n = computeInt(computeInt(stack));
            }

            if (n != -1
               && offHandStack != null
               && isValidItemDirect(offHandStack)
               && offHandStack.getCount() >= ((GenericContainerScreenHandler)genericContainerScreen.getScreenHandler()).getSlot(n).getStack().getCount()) {
               return;
            }
         } else if (invManager.offhandItems.matches("Fishing Rod")) {
            n = computeIntImpl(Items.FISHING_ROD);
         } else if (invManager.offhandItems.matches("Block")) {
            ItemStack itemStack9 = InventorySlotClassifier.getItemStack9();
            if (itemStack9 != null) {
               n = computeIntEx(itemStack9);
            } else {
               itemStack9 = getStream().filter(InventoryUtils::isEmptyHelper).max(Comparator.comparingInt(ItemStack::getCount)).orElse(null);
               n = computeInt(computeInt(itemStack9));
            }
         }

         if (n != -1) {
            list.add(new Stealer.Entry(n, -1, Stealer.Mode.MOVE_TO_OFFHAND, null));
         }
      }
   }

   private boolean isValid(List<Stealer.Entry> list, int n, ItemStack stack) {
      OptionalInt optionalInt = this.getOptionalInt(stack);
      if (optionalInt.isEmpty()) {
         return false;
      }

      list.add(new Stealer.Entry(n, optionalInt.getAsInt(), Stealer.Mode.SWAP_TO_HOTBAR, stack));
      return true;
   }

   private OptionalInt getOptionalInt(ItemStack stack) {
      InvManager invManager = getInvManager();
      if ((Boolean)this.invManagerBoost.getValue() && stack != null && !stack.isEmpty() && mc2.player != null && invManager.isValidEx()) {
         int n = computeInt(
            (Boolean)invManager.switchGoldenApple.getValue() && !invManager.offhandItems.matches("Golden Apple") && stack.isOf(Items.GOLDEN_APPLE),
            invManager.goldenAppleSlot,
            stack2 -> isValid(stack, stack2)
         );
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt(
            (Boolean)invManager.switchBlock.getValue() && !invManager.offhandItems.matches("Block") && isValidItemImpl(stack),
            invManager.blockSlot,
            stack3 -> !isValidItemImpl(stack3) || stack.getCount() > stack3.getCount()
         );
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt(
            (Boolean)invManager.switchSword.getValue() && (stack.getItem() instanceof SwordItem || InventoryUtils.isEmptyV3(stack)),
            invManager.swordSlot,
            stack4 -> computeFloatEx(stack) > computeFloatEx(stack4)
         );
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt(
            (Boolean)invManager.switchPickaxe.getValue() && stack.getItem() instanceof PickaxeItem,
            invManager.pickaxeSlot,
            stack5 -> InventoryUtils.computeFloatEx(stack) > (stack5.getItem() instanceof PickaxeItem ? InventoryUtils.computeFloatEx(stack5) : 0.0F)
         );
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt(
            (Boolean)invManager.switchAxe.getValue() && stack.getItem() instanceof AxeItem && !InventoryUtils.isEmptyV3(stack),
            invManager.axeSlot,
            stack6 -> InventoryUtils.computeFloatEx(stack) > (stack6.getItem() instanceof AxeItem ? InventoryUtils.computeFloatEx(stack6) : 0.0F)
         );
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt(
            (Boolean)invManager.switchRod.getValue() && !invManager.offhandItems.matches("Fishing Rod") && stack.getItem() instanceof FishingRodItem,
            invManager.rodSlot,
            stack7 -> !(stack7.getItem() instanceof FishingRodItem)
         );
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt(
            (Boolean)invManager.switchBowOrCrossbow.getValue() && (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem),
            invManager.bowSlot,
            stack8 -> computeFloat(invManager, stack) > 0.0F && computeFloat(invManager, stack) > computeFloat(invManager, stack8)
         );
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt((Boolean)invManager.switchEnderPearl.getValue() && stack.isOf(Items.ENDER_PEARL), invManager.enderPearlSlot, stack9 -> isValid(stack, stack9));
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt(
            (Boolean)invManager.switchWaterBucket.getValue() && stack.isOf(Items.WATER_BUCKET), invManager.waterBucketSlot, stack10 -> isValid(stack, stack10)
         );
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt((Boolean)invManager.switchFireball.getValue() && stack.isOf(Items.FIRE_CHARGE), invManager.fireballSlot, stack11 -> isValid(stack, stack11));
         if (n != -1) {
            return OptionalInt.of(n);
         }

         n = computeInt(
            InvManager.isValidImpl()
               && (Boolean)invManager.switchEggsSnowballs.getValue()
               && !invManager.offhandItems.matches("Projectile")
               && isValidItemDirect(stack),
            invManager.eggsSnowballsSlot,
            stack12 -> stack12.isEmpty() || !isValidItemDirect(stack12) || stack.getCount() > stack12.getCount()
         );
         return n == -1 ? OptionalInt.empty() : OptionalInt.of(n);
      } else {
         return OptionalInt.empty();
      }
   }

   private static int computeInt(boolean flag, NumberValue numberValue, Predicate<ItemStack> predicate) {
      if (flag && numberValue != null) {
         int n = Math.max(1, Math.min(9, Math.round(((Number)numberValue.getValue()).floatValue()))) - 1;
         return predicate.test((ItemStack)mc2.player.getInventory().main.get(n)) ? n : -1;
      } else {
         return -1;
      }
   }

   private boolean isValidEx(GenericContainerScreenHandler genericContainerScreenHandler) {
      for (int i = 0; i < genericContainerScreenHandler.getRows() * 9; i++) {
         ItemStack stack = genericContainerScreenHandler.getSlot(i).getStack();
         if (!stack.isEmpty() && isValidItem(stack) && this.isValid(genericContainerScreenHandler, stack)) {
            return false;
         }
      }

      return true;
   }

   private boolean isValid(GenericContainerScreenHandler genericContainerScreenHandler, ItemStack stack) {
      if (!InventorySlotClassifier.isValidItemImpl(stack) && !InventoryUtils.isEmptyV3(stack)) {
         for (int i = 0; i < genericContainerScreenHandler.getRows() * 9; i++) {
            ItemStack stack2 = genericContainerScreenHandler.getSlot(i).getStack();
            if (stack.getItem() instanceof ArmorItem && stack2.getItem() instanceof ArmorItem) {
               EquipmentSlot equipmentSlot = ((EquippableComponent)Objects.requireNonNull((EquippableComponent)stack.get(DataComponentTypes.EQUIPPABLE))).slot();
               float f = InventoryUtils.computeFloat(stack);
               if (InventorySlotClassifier.computeFloat(equipmentSlot) >= f) {
                  return false;
               }

               return !(f < computeFloat(stack3 -> InventoryUtils.isValid(equipmentSlot, stack3), InventoryUtils::computeFloat));
            }

            if (stack.getItem() instanceof SwordItem && stack2.getItem() instanceof SwordItem) {
               if (InventoryUtils.computeFloatImpl(stack2) > InventoryUtils.computeFloatImpl(stack)) {
                  return false;
               }
            } else {
               boolean flag = stack.getItem() instanceof PickaxeItem && stack2.getItem() instanceof PickaxeItem
                  || stack.getItem() instanceof AxeItem && stack2.getItem() instanceof AxeItem
                  || stack.getItem() instanceof ShovelItem && stack2.getItem() instanceof ShovelItem;
               if (flag && InventoryUtils.computeFloatEx(stack2) > InventoryUtils.computeFloatEx(stack)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static boolean isValidItem(ItemStack stack) {
      Item item = stack.getItem();
      if (stack.isEmpty() || item == Items.BOOK || item == Items.BOOKSHELF || item == Items.WRITABLE_BOOK) {
         return false;
      }

      if (InventorySlotClassifier.isValidItemImpl(stack) || InventoryUtils.isEmptyV3(stack)) {
         return true;
      }

      if (item instanceof ArmorItem) {
         EquipmentSlot equipmentSlot = ((EquippableComponent)Objects.requireNonNull((EquippableComponent)stack.get(DataComponentTypes.EQUIPPABLE))).slot();
         return !(
            InventoryUtils.computeFloat(stack) <= computeFloat(stack2 -> InventoryUtils.isValid(equipmentSlot, stack2), InventoryUtils::computeFloat)
         );
      }

      if (item instanceof SwordItem) {
         return !(InventoryUtils.computeFloatImpl(stack) <= computeFloat(InventoryUtils::isEmptyEx, InventoryUtils::computeFloatImpl));
      }

      if (item instanceof PickaxeItem) {
         return !(InventoryUtils.computeFloatEx(stack) <= computeFloat(InventoryUtils::isEmptyDirect, InventoryUtils::computeFloatEx));
      }

      if (item instanceof AxeItem) {
         return !(InventoryUtils.computeFloatEx(stack) <= computeFloat(InventoryUtils::isEmptyCustom, InventoryUtils::computeFloatEx));
      }

      if (item instanceof ShovelItem) {
         return !(InventoryUtils.computeFloatEx(stack) <= computeFloat(InventoryUtils::isEmptySafe, InventoryUtils::computeFloatEx));
      }

      if (item instanceof CrossbowItem) {
         return !(InventoryUtils.computeFloatDirect(stack) <= computeFloat(InventoryUtils::isEmptyImpl, InventoryUtils::computeFloatDirect));
      }

      if (item instanceof BowItem) {
         float f = InventoryUtils.computeFloatCustom(stack);
         if (f > 10.0F && isValidItemEx(stack)) {
            return !(f <= computeFloat(InventoryUtils::isEmptyAlt, InventoryUtils::computeFloatCustom));
         }

         float f2 = InventoryUtils.computeFloatSafe(stack);
         if (f2 > 10.0F && isValidItemEx(stack)) {
            return !(f2 <= computeFloat(InventoryUtils::isEmptyAlt, InventoryUtils::computeFloatSafe));
         }
      }

      if (item == Items.COMPASS) {
         return getStream().noneMatch(stack3 -> stack3.getItem() == item);
      } else if (item == Items.WATER_BUCKET && computeInt(item) >= InvManager.getHeightDirect()) {
         return false;
      } else if (item == Items.LAVA_BUCKET && computeInt(item) >= InvManager.getHeightSafe()) {
         return false;
      } else if (item instanceof BlockItem
         && !isValidItemAlt(stack)
         && getStream().filter(InventoryUtils::isEmptyV2).mapToInt(ItemStack::getCount).sum() + stack.getCount() >= InvManager.getHeight()) {
         return false;
      } else if (item == Items.ARROW && computeInt(item) + stack.getCount() >= InvManager.getHeightImpl()) {
         return false;
      } else if (item instanceof FishingRodItem && computeInt(Items.FISHING_ROD) >= 1) {
         return false;
      } else {
         return !isValidItemDirect(stack)
               || computeInt(Items.SNOWBALL) + computeInt(Items.EGG) + stack.getCount() < InvManager.getHeightWithOffset() && InvManager.isValidImpl()
            ? !(item instanceof WrittenBookItem)
               && !(item instanceof WritableBookItem)
               && !(item instanceof ExperienceBottleItem)
               && !(item instanceof FireworkRocketItem)
               && item != Items.CHEST
               && item != Items.ANVIL
               && item != Items.ENCHANTING_TABLE
               && item != Items.COBWEB
               && item != Items.WHEAT_SEEDS
               && item != Items.BEETROOT_SEEDS
               && item != Items.MELON_SEEDS
               && item != Items.PUMPKIN_SEEDS
               && item != Items.TORCHFLOWER_SEEDS
               && item != Items.FLINT_AND_STEEL
            : false;
      }
   }

   private static Stream<ItemStack> getStream() {
      return Stream.concat(mc2.player.getInventory().main.stream(), mc2.player.getInventory().armor.stream());
   }

   private static float computeFloat(Predicate<ItemStack> predicate, Function<ItemStack, Float> fn) {
      return getStream().filter(predicate).<Float>map(fn).max(Float::compareTo).orElse(0.0F);
   }

   private static int computeInt(Item item) {
      return getStream().filter(stack -> stack.getItem() == item).mapToInt(ItemStack::getCount).sum();
   }

   private static int computeIntEx(Item item) {
      return InventorySlotClassifier.collectItems()
         .stream()
         .filter(InventorySlotClassifier::matchesSlotHelper)
         .filter(slot -> slot.getStack().getItem() == item)
         .mapToInt(slot2 -> slot2.getStack().getCount())
         .sum();
   }

   private static InvManager getInvManager() {
      return Southside.INSTANCE.getModuleManager().getModule(InvManager.class);
   }

   private static boolean isValidItemEx(ItemStack stack) {
      if (stack.isEmpty()) {
         return true;
      }

      if (stack.getItem() instanceof PlayerHeadItem) {
         return false;
      }

      String string = stack.getName().getString();
      return !string.contains("Click")
         && !string.contains("Right")
         && !string.contains("点击")
         && !string.contains("Teleport")
         && !string.contains("使用")
         && !string.contains("传送")
         && !string.contains("再来");
   }

   private static boolean isValidItemAlt(ItemStack stack) {
      return stack != null
         && !stack.isEmpty()
         && stack.getItem() instanceof BlockItem blockItem
         && (
            !blockItem.getBlock().getDefaultState().isFullCube((BlockView)(mc2.world != null ? mc2.world : EmptyBlockView.INSTANCE), BlockPos.ORIGIN)
               || Scaffold.invalidBlocks.contains(blockItem.getBlock())
         );
   }

   private static boolean isValidItemImpl(ItemStack stack) {
      return stack.getItem() instanceof BlockItem blockItem && !Scaffold.invalidBlocks.contains(blockItem.getBlock());
   }

   private static boolean isValidItemDirect(ItemStack stack) {
      return stack.isOf(Items.EGG) || stack.isOf(Items.SNOWBALL);
   }

   private static boolean isValidItemSafe(ItemStack stack) {
      return !stack.isEmpty() && stack.getCount() < stack.getMaxCount();
   }

   private static boolean isValid(ItemStack stack, ItemStack stack2) {
      return stack2.isEmpty() || stack2.getItem() != stack.getItem() || stack.getCount() > stack2.getCount();
   }

   private static float computeFloat(ItemStack stack) {
      return stack != null && stack.getItem() instanceof SwordItem ? InventorySlotClassifier.computeFloatDirect(stack) : InventorySlotClassifier.computeFloatImpl(stack);
   }

   private static float computeFloatEx(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && (stack.getItem() instanceof SwordItem || InventoryUtils.isEmptyV3(stack))) {
         double d = mc2.player.getAttributes().hasAttribute(EntityAttributes.ATTACK_DAMAGE)
            ? mc2.player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE)
            : 0.0;
         float f = 0.0F;
         f += (float)((AttributeModifiersComponent)stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT))
            .applyOperations(d, EquipmentSlot.MAINHAND);
         int n = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack);
         if (n > 0) {
            f += 1.25F * n;
         }

         return f;
      } else {
         return 0.0F;
      }
   }

   private static float computeFloat(InvManager invManager, ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return 0.0F;
      } else if (stack.getItem() instanceof CrossbowItem) {
         return InventorySlotClassifier.computeFloatAlt(stack);
      } else if (!(stack.getItem() instanceof BowItem)) {
         return 0.0F;
      } else if (invManager.bowPriority.matches("Power Bow")) {
         return InventorySlotClassifier.computeFloat(stack);
      } else {
         return invManager.bowPriority.matches("Punch Bow")
            ? InventorySlotClassifier.computeFloatEx(stack)
            : Math.max(InventorySlotClassifier.computeFloat(stack), InventorySlotClassifier.computeFloatEx(stack));
      }
   }

   private static int computeInt(NumberValue numberValue) {
      return (int)(((Number)numberValue.getValue()).floatValue() - 1.0F);
   }

   private static int computeInt(int n) {
      if (n < 0) {
         return -1;
      }

      int size = mc2.player.currentScreenHandler.slots.size();
      return n < 9 ? size - 9 + n : size - 45 + n;
   }

   private static int computeInt(ItemStack stack) {
      if (stack != null) {
         for (int i = 0; i < mc2.player.getInventory().main.size(); i++) {
            if (mc2.player.getInventory().main.get(i) == stack) {
               return i;
            }
         }
      }

      return -1;
   }

   private static int computeInt(Predicate<ItemStack> predicate) {
      for (Slot slot : mc2.player.currentScreenHandler.slots) {
         if (slot.inventory != mc2.player.getInventory() && predicate.test(slot.getStack())) {
            return slot.id;
         }
      }

      return -1;
   }

   private static int computeIntAlt(Item item) {
      return item == null ? -1 : computeInt(stack -> !stack.isEmpty() && stack.getItem().equals(item));
   }

   private static int computeIntEx(ItemStack stack) {
      return stack == null ? -1 : computeInt(stack2 -> !stack2.isEmpty() && stack2.equals(stack));
   }

   private static int computeIntImpl(Item item) {
      int n = computeIntAlt(item);
      return n != -1 ? n : computeInt(HotbarItemFinder.computeInt(0, mc2.player.getInventory().main.size(), item));
   }

   private static int computeInt() {
      for (int i = 0; i < mc2.player.getInventory().size(); i++) {
         if (i != 8 && mc2.player.getInventory().getStack(i).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   private static boolean isValid(int n, int n2, int n3) {
      if (mc2.interactionManager != null && mc2.player != null && n3 >= 0 && n3 <= 8) {
         clickSlot(n, n2, n3, SlotActionType.SWAP);
         if (mc2.player.currentScreenHandler == null) {
            return false;
         }

         Slot slot = mc2.player.currentScreenHandler.getSlot(n2);
         if (slot != null && !slot.getStack().isEmpty()) {
            clickSlot(n, n2, 0, SlotActionType.QUICK_MOVE);
         }

         return true;
      } else {
         return false;
      }
   }

   public BlockHitResult raycastHit() {
      if (mc2.player != null && mc2.world != null) {
         Rotation currentRotation = RotationManager.getCurrentRotation();
         float tickDelta = mc2.getRenderTickCounter().getTickDelta(false);
         Vec3d cameraPosVec = mc2.player.getCameraPosVec(tickDelta);
         Vec3d rotationVec = mc2.player.getRotationVec(tickDelta);
         if (currentRotation != null) {
            float f = MathHelper.cos(-currentRotation.yaw * (float) (Math.PI / 180.0));
            float f2 = MathHelper.sin(-currentRotation.yaw * (float) (Math.PI / 180.0));
            float f3 = MathHelper.cos(currentRotation.pitch * (float) (Math.PI / 180.0));
            float f4 = MathHelper.sin(currentRotation.pitch * (float) (Math.PI / 180.0));
            rotationVec = new Vec3d(f2 * f3, -f4, f * f3);
         }

         Vec3d vec = rotationVec.multiply(0.1);

         for (int i = 0; i <= 45; i++) {
            BlockPos pos = new BlockPos((int)Math.floor(cameraPosVec.x), (int)Math.floor(cameraPosVec.y), (int)Math.floor(cameraPosVec.z));
            Block block = mc2.world.getBlockState(pos).getBlock();
            if ((block instanceof ChestBlock || block instanceof EnderChestBlock || block instanceof ShulkerBoxBlock)
               && cameraPosVec.x >= pos.getX() + 0.1
               && cameraPosVec.x <= pos.getX() + 0.9
               && cameraPosVec.y >= pos.getY() + 0.1
               && cameraPosVec.y <= pos.getY() + 0.9
               && cameraPosVec.z >= pos.getZ() + 0.1
               && cameraPosVec.z <= pos.getZ() + 0.9) {
               return BlockLookUtils.raycast(mc2.player.getYaw(), mc2.player.getPitch());
            }

            cameraPosVec = cameraPosVec.add(vec);
         }

         return null;
      } else {
         return null;
      }
   }

   private record Entry(int sourceSlot, int targetSlot, Stealer.Mode type, ItemStack stack) {

      public int getSourceSlot() {
         return this.sourceSlot;
      }

      public int getTargetSlot() {
         return this.targetSlot;
      }

      public Stealer.Mode getType() {
         return this.type;
      }

      public ItemStack getStack() {
         return this.stack;
      }
   }

   private enum Mode {
      MOVE_TO_INVENTORY,
      MOVE_TO_OFFHAND,
      QUICK_MOVE,
      SWAP_TO_HOTBAR;


      // $VF: synthetic method
      private static Stealer.Mode[] createValues() {
         return new Stealer.Mode[]{MOVE_TO_INVENTORY, MOVE_TO_OFFHAND, QUICK_MOVE, SWAP_TO_HOTBAR};
      }


   }
}
