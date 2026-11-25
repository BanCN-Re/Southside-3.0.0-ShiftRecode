package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.ForwardEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.movement.NoSlow;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.HotbarItemFinder;
import dev.southside.utils.EnchantmentUtils;
import dev.southside.utils.MillisTimer;
import dev.southside.utils.NumberValue;
import dev.southside.utils.InventorySlotClassifier;
import dev.southside.utils.InventoryUtils;
import dev.southside.utils.ModeValue;
import dev.southside.utils.OneConfigGui;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
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
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.WritableBookItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;

public class InvManager extends Module {
   public static final MillisTimer lastTimeUtil = new MillisTimer();
   @SerializedName("delay")
   public final NumberValue delay = new NumberValue("Delay", 50.0F, 0.0F, 200.0F, 1.0F);
   @SerializedName("offhandItems")
   public final ModeValue offhandItems = new ModeValue("Offhand Items", "None", "Golden Apple", "Projectile", "Fishing Rod", "Block", "None");
   @SerializedName("autoArmor")
   public final BooleanValue autoArmor = new BooleanValue("AutoArmor", true);
   @SerializedName("inventoryOnly")
   public final BooleanValue inventoryOnly = new BooleanValue("Inventory Only", true);
   @SerializedName("noMove")
   public final BooleanValue noMove = new BooleanValue("No Move", false);
   @SerializedName("switchSword")
   public final BooleanValue switchSword = new BooleanValue("Switch Sword", true);
   @SerializedName("switchBlock")
   public final BooleanValue switchBlock = new BooleanValue("Switch Block", true);
   @SerializedName("switchPickaxe")
   public final BooleanValue switchPickaxe = new BooleanValue("Switch Pickaxe", true);
   @SerializedName("switchAxe")
   public final BooleanValue switchAxe = new BooleanValue("Switch Axe", true);
   @SerializedName("switchBowOrCrossbow")
   public final BooleanValue switchBowOrCrossbow = new BooleanValue("Switch Bow or Crossbow", true);
   @SerializedName("bowPriority")
   public final ModeValue bowPriority = new ModeValue("Bow Priority", "Crossbow", "Power Bow", "Punch Bow", "Crossbow");
   @SerializedName("switchWaterBucket")
   public final BooleanValue switchWaterBucket = new BooleanValue("Switch Water Bucket", true);
   @SerializedName("switchEnderPearl")
   public final BooleanValue switchEnderPearl = new BooleanValue("Switch Ender Pearl", true);
   @SerializedName("switchFireball")
   public final BooleanValue switchFireball = new BooleanValue("Switch Fireball", true);
   @SerializedName("switchGoldenApple")
   public final BooleanValue switchGoldenApple = new BooleanValue("Switch Golden Apple", true);
   @SerializedName("throwItems")
   public final BooleanValue throwItems = new BooleanValue("Throw Items", true);
   public static final BooleanValue BOOLEAN_VALUE = new BooleanValue("Keep Eggs & Snowballs", true);
   @SerializedName("switchEggsSnowballs")
   public final BooleanValue switchEggsSnowballs = new BooleanValue("Switch Eggs & Snowballs", false);
   @SerializedName("switchRod")
   public final BooleanValue switchRod = new BooleanValue("Switch Rod", false);
   @SerializedName("autoDisable")
   public final BooleanValue autoDisable = new BooleanValue("Auto Disable", false);
   @SerializedName("swordSlot")
   public final NumberValue swordSlot = new NumberValue("Sword Slot", 1.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("blockSlot")
   public final NumberValue blockSlot = new NumberValue("Block Slot", 2.0F, 1.0F, 9.0F, 1.0F);
   public static final NumberValue maxBlockSize = new NumberValue("Max Block Size", 256.0F, 64.0F, 1024.0F, 64.0F);
   @SerializedName("pickaxeSlot")
   public final NumberValue pickaxeSlot = new NumberValue("Pickaxe Slot", 3.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("axeSlot")
   public final NumberValue axeSlot = new NumberValue("Axe Slot", 4.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("bowSlot")
   public final NumberValue bowSlot = new NumberValue("Bow Slot", 5.0F, 1.0F, 9.0F, 1.0F);
   public static final NumberValue maxArrowSize = new NumberValue("Max Arrow Size", 256.0F, 64.0F, 512.0F, 64.0F);
   @SerializedName("waterBucketSlot")
   public final NumberValue waterBucketSlot = new NumberValue("Water Bucket Slot", 6.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("enderPearlSlot")
   public final NumberValue enderPearlSlot = new NumberValue("Ender Pearl Slot", 7.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("fireballSlot")
   public final NumberValue fireballSlot = new NumberValue("Fireball Slot", 8.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("goldenAppleSlot")
   public final NumberValue goldenAppleSlot = new NumberValue("Golden Apple Slot", 9.0F, 1.0F, 9.0F, 1.0F);
   public static final NumberValue keepWaterBuckets = new NumberValue("Keep Water Buckets", 1.0F, 0.0F, 5.0F, 1.0F);
   public static final NumberValue keepLavaBuckets = new NumberValue("Keep Lava Buckets", 1.0F, 0.0F, 5.0F, 1.0F);
   public static final NumberValue keepCobweb = new NumberValue("Keep Cobweb", 1.0F, 0.0F, 10.0F, 1.0F);
   @SerializedName("eggsSnowballsSlot")
   public final NumberValue eggsSnowballsSlot = new NumberValue("Eggs & Snowballs Slot", 9.0F, 1.0F, 9.0F, 1.0F);
   public static final NumberValue NUMBER_VALUE = new NumberValue("Max Eggs & Snowballs Size", 64.0F, 16.0F, 256.0F, 16.0F);
   @SerializedName("rodSlot")
   public final NumberValue rodSlot = new NumberValue("Rod Slot", 9.0F, 1.0F, 9.0F, 1.0F);
   @SerializedName("goldenAppleMergePending")
   private boolean goldenAppleMergePending;
   @SerializedName("usedItemRecently")
   private boolean usedItemRecently;
   @SerializedName("ticksSinceItemUse")
   private int ticksSinceItemUse;
   @SerializedName("blockSprintWhileManaging")
   private boolean blockSprintWhileManaging;
   private static final long n = 200L;

   public InvManager() {
      super(Category.Player, "InvManager", "欣欣哥！");
   }

   public Set<Integer> getSet() {
      HashSet set = new HashSet();
      if (!this.isEnabled()) {
         return set;
      }

      performAction(set, (Boolean)this.switchSword.getValue(), this.swordSlot);
      performAction(set, (Boolean)this.switchBlock.getValue(), this.blockSlot);
      performAction(set, (Boolean)this.switchPickaxe.getValue(), this.pickaxeSlot);
      performAction(set, (Boolean)this.switchAxe.getValue(), this.axeSlot);
      performAction(set, (Boolean)this.switchBowOrCrossbow.getValue(), this.bowSlot);
      performAction(set, (Boolean)this.switchWaterBucket.getValue(), this.waterBucketSlot);
      performAction(set, (Boolean)this.switchEnderPearl.getValue(), this.enderPearlSlot);
      performAction(set, (Boolean)this.switchFireball.getValue(), this.fireballSlot);
      performAction(set, (Boolean)this.switchGoldenApple.getValue() && !this.offhandItems.matches("Golden Apple"), this.goldenAppleSlot);
      performAction(
         set,
         (Boolean)this.switchEggsSnowballs.getValue() && (Boolean)BOOLEAN_VALUE.getValue() && !this.offhandItems.matches("Projectile"),
         this.eggsSnowballsSlot
      );
      performAction(set, (Boolean)this.switchRod.getValue() && !this.offhandItems.matches("Fishing Rod"), this.rodSlot);
      return set;
   }

   private static void performAction(Set<Integer> set, boolean flag, NumberValue numberValue) {
      if (flag) {
         set.add(((Number)numberValue.getValue()).intValue() - 1);
      }
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.noMove.getLabel(), this.inventoryOnly::getValue);
      this.hideIf(this.switchBlock.getLabel(), () -> this.offhandItems.matches("Block"));
      this.hideIf(this.blockSlot.getLabel(), () -> !(Boolean)this.switchBlock.getValue() || this.offhandItems.matches("Block"));
      this.hideIf(maxBlockSize.getLabel(), () -> !(Boolean)this.switchBlock.getValue());
      this.hideIf(this.pickaxeSlot.getLabel(), () -> !(Boolean)this.switchPickaxe.getValue());
      this.hideIf(this.axeSlot.getLabel(), () -> !(Boolean)this.switchAxe.getValue());
      this.hideIf(this.bowSlot.getLabel(), () -> !(Boolean)this.switchBowOrCrossbow.getValue());
      this.hideIf(this.bowPriority.getLabel(), () -> !(Boolean)this.switchBowOrCrossbow.getValue());
      this.hideIf(maxArrowSize.getLabel(), () -> !(Boolean)this.switchBowOrCrossbow.getValue());
      this.hideIf(this.waterBucketSlot.getLabel(), () -> !(Boolean)this.switchWaterBucket.getValue());
      this.hideIf(this.enderPearlSlot.getLabel(), () -> !(Boolean)this.switchEnderPearl.getValue());
      this.hideIf(this.fireballSlot.getLabel(), () -> !(Boolean)this.switchFireball.getValue());
      this.hideIf(this.switchGoldenApple.getLabel(), () -> this.offhandItems.matches("Golden Apple"));
      this.hideIf(this.goldenAppleSlot.getLabel(), () -> !(Boolean)this.switchGoldenApple.getValue() || this.offhandItems.matches("Golden Apple"));
      this.hideIf(keepWaterBuckets.getLabel(), () -> !(Boolean)this.throwItems.getValue());
      this.hideIf(keepLavaBuckets.getLabel(), () -> !(Boolean)this.throwItems.getValue());
      this.hideIf(keepCobweb.getLabel(), () -> !(Boolean)this.throwItems.getValue());
      this.hideIf(this.switchEggsSnowballs.getLabel(), () -> !(Boolean)BOOLEAN_VALUE.getValue() || this.offhandItems.matches("Projectile"));
      this.hideIf(
         this.eggsSnowballsSlot.getLabel(),
         () -> !(Boolean)this.switchEggsSnowballs.getValue() || !(Boolean)BOOLEAN_VALUE.getValue() || this.offhandItems.matches("Projectile")
      );
      this.hideIf(NUMBER_VALUE.getLabel(), () -> !(Boolean)BOOLEAN_VALUE.getValue());
      this.hideIf(this.switchRod.getLabel(), () -> this.offhandItems.matches("Fishing Rod"));
      this.hideIf(this.rodSlot.getLabel(), () -> !(Boolean)this.switchRod.getValue() || this.offhandItems.matches("Fishing Rod"));
   }

   @EventTarget
   public void onForward(ForwardEvent forwardEvent) {
      if (this.blockSprintWhileManaging && mc2.player != null) {
         if (mc2.options.jumpKey.isPressed() && mc2.player.isOnGround()) {
            return;
         }

         mc2.options.sprintKey.setPressed(false);
         mc2.player.setSprinting(false);
         forwardEvent.setSprint(false);
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.POST) {
         this.blockSprintWhileManaging = false;
      }
   }

   @EventTarget(priority = 1)
   public void onX(XEvent xevent) {
      if (xevent.getPhase() == Stage.PRE && (Boolean)this.autoDisable.getValue() && mc2.world != null && mc2.player != null) {
         if (!mc2.player.isAlive()) {
            this.setEnabled(false);
            return;
         }

         if (mc2.player.age <= 1) {
            this.setEnabled(false);
         }
      }
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (mc2.player != null && mc2.world != null) {
         if (!(mc2.currentScreen instanceof OneConfigGui) && !this.isValidEx()) {
            this.applyTextDirect("[Inv]配置错误请检查");
            this.toggle();
         } else {
            boolean isValidAlt = Southside.INSTANCE.getModuleManager().getModule(AutoBucket.class).isValidAlt();
            if (!NoSlow.activeValue && !getStream().anyMatch(InventoryUtils::isValidItem) && !isValidAlt) {
               if (mc2.player.isUsingItem()) {
                  this.ticksSinceItemUse = 0;
                  this.usedItemRecently = true;
               } else if (this.usedItemRecently && ++this.ticksSinceItemUse >= 5) {
                  this.ticksSinceItemUse = 0;
                  this.usedItemRecently = false;
               }

               boolean flag = mc2.player.isUsingItem()
                  || this.usedItemRecently
                  || KillAura.target != null && this.delay.getCurrentValueEx() != 0.0 && mc2.currentScreen == null;
               boolean flag2 = !Stealer.isValidAlt()
                  && !Southside.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()
                  && (this.inventoryOnly.getValue() ? mc2.currentScreen instanceof InventoryScreen : !flag);
               if (!flag2) {
                  this.goldenAppleMergePending = false;
               } else if (!(mc2.currentScreen instanceof HandledScreen handledScreen && handledScreen.getScreenHandler().syncId != mc2.player.playerScreenHandler.syncId)) {
                  if ((Boolean)this.autoArmor.getValue()) {
                     this.performActionAlt();
                  }

                  this.performActionImpl();
                  this.performActionDirect();
                  if ((Boolean)this.throwItems.getValue()) {
                     this.performActionWithOffset();
                  }
               }
            }
         }
      }
   }

   public boolean isValidEx() {
      if (!(Boolean)BOOLEAN_VALUE.getValue()) {
         this.switchEggsSnowballs.performAction(false);
      }

      HashSet set = new HashSet();
      return isValid(set, (Boolean)this.switchSword.getValue(), this.swordSlot)
         && isValid(set, (Boolean)this.switchPickaxe.getValue(), this.pickaxeSlot)
         && isValid(set, (Boolean)this.switchAxe.getValue(), this.axeSlot)
         && isValid(set, (Boolean)this.switchBowOrCrossbow.getValue(), this.bowSlot)
         && isValid(set, (Boolean)this.switchWaterBucket.getValue(), this.waterBucketSlot)
         && isValid(set, (Boolean)this.switchEnderPearl.getValue(), this.enderPearlSlot)
         && isValid(set, (Boolean)this.switchFireball.getValue(), this.fireballSlot)
         && isValid(set, (Boolean)this.switchGoldenApple.getValue() && !this.offhandItems.matches("Golden Apple"), this.goldenAppleSlot)
         && isValid(set, (Boolean)this.switchEggsSnowballs.getValue() && !this.offhandItems.matches("Projectile"), this.eggsSnowballsSlot)
         && isValid(set, (Boolean)this.switchRod.getValue() && !this.offhandItems.matches("Fishing Rod"), this.rodSlot)
         && isValid(set, (Boolean)this.switchBlock.getValue() && !this.offhandItems.matches("Block"), this.blockSlot);
   }

   private static boolean isValid(Set<Integer> set, boolean flag, NumberValue numberValue) {
      return !flag || set.add(computeInt(numberValue));
   }

   private void sendPacket(int n2, int n3, SlotActionType slotActionType) {
      if (!Southside.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled() && !(Boolean)this.noMove.getValue()) {
         this.blockSprintWhileManaging = true;
      }

      if (lastTimeUtil.hasTimeElapsed(((Number)this.delay.getValue()).longValue()) || this.delay.getCurrentValueEx() == 0.0) {
         if ((Boolean)this.inventoryOnly.getValue() || !mc2.player.lastSprinting) {
            mc2.interactionManager.clickSlot(mc2.player.playerScreenHandler.syncId, n2, n3, slotActionType, mc2.player);
            lastTimeUtil.reset();
            if (!(Boolean)this.inventoryOnly.getValue()) {
               mc2.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc2.player.playerScreenHandler.syncId));
            }
         }
      }
   }

   private void performActionAlt() {
      for (int i = 0; i < mc2.player.getInventory().armor.size(); i++) {
         ItemStack stack = (ItemStack)mc2.player.getInventory().armor.get(i);
         if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
            EquipmentSlot equipmentSlot = ((EquippableComponent)Objects.requireNonNull((EquippableComponent)stack.get(DataComponentTypes.EQUIPPABLE))).slot();
            if (computeFloat(equipmentSlot) > InventoryUtils.computeFloat(stack)) {
               this.sendPacket(8 - i, 1, SlotActionType.THROW);
            }
         }
      }

      for (int j = 0; j < mc2.player.getInventory().main.size(); j++) {
         ItemStack stack2 = (ItemStack)mc2.player.getInventory().main.get(j);
         if (!stack2.isEmpty() && stack2.getItem() instanceof ArmorItem) {
            EquipmentSlot equipmentSlot2 = ((EquippableComponent)Objects.requireNonNull((EquippableComponent)stack2.get(DataComponentTypes.EQUIPPABLE))).slot();
            float f = InventoryUtils.computeFloat(stack2);
            if (computeFloat(equipmentSlot2) == f && InventorySlotClassifier.computeFloat(equipmentSlot2) < f) {
               this.sendPacket(computeInt(j), 0, SlotActionType.QUICK_MOVE);
            }
         }
      }
   }

   private void performActionImpl() {
      if (this.goldenAppleMergePending) {
         this.sendPacket(45, 0, SlotActionType.PICKUP);
         this.goldenAppleMergePending = false;
      }

      ItemStack offHandStack = mc2.player.getOffHandStack();
      if (this.offhandItems.matches("Golden Apple")) {
         int n2 = computeIntEx(Items.GOLDEN_APPLE);
         if (n2 != -1) {
            if (offHandStack.isOf(Items.GOLDEN_APPLE)) {
               if (offHandStack.getCount() + ((ItemStack)mc2.player.getInventory().main.get(n2)).getCount() <= 64) {
                  this.sendPacket(computeInt(n2), 0, SlotActionType.PICKUP);
                  this.goldenAppleMergePending = true;
               }
            } else {
               this.onKeyTyped(n2);
            }
         }
      } else if (this.offhandItems.matches("Projectile")) {
         ItemStack itemStack = getItemStack(InventoryUtils::isValidItemAlt, ItemStack::getCount);
         if (itemStack != null && (!offHandStack.isOf(Items.EGG) && !offHandStack.isOf(Items.SNOWBALL) || offHandStack.getCount() < itemStack.getCount())) {
            this.onKeyTyped(computeInt(itemStack));
         }
      } else if (this.offhandItems.matches("Fishing Rod")) {
         int n3 = computeIntEx(Items.FISHING_ROD);
         if (n3 != -1 && !offHandStack.isOf(Items.FISHING_ROD)) {
            this.onKeyTyped(n3);
         }
      } else if (this.offhandItems.matches("Block")) {
         ItemStack itemStack2 = getItemStack(InventoryUtils::isEmptyHelper, ItemStack::getCount);
         if (itemStack2 != null && (isValidItemImpl(offHandStack) || offHandStack.getCount() < itemStack2.getCount())) {
            this.onKeyTyped(computeInt(itemStack2));
         }
      }
   }

   private void performActionDirect() {
      if ((Boolean)this.switchGoldenApple.getValue() && !this.offhandItems.matches("Golden Apple")) {
         this.performAction(computeInt(this.goldenAppleSlot), Items.GOLDEN_APPLE);
      }

      if ((Boolean)this.switchBlock.getValue()) {
         int n2 = computeInt(this.blockSlot);
         ItemStack stack = (ItemStack)mc2.player.getInventory().main.get(n2);
         ItemStack itemStack = getItemStack(InventoryUtils::isEmptyHelper, ItemStack::getCount);
         if (itemStack != null && (itemStack.getCount() > stack.getCount() || isValidItemImpl(stack)) && !this.offhandItems.matches("Block")) {
            this.performAction(n2, itemStack);
         }

         if (getStream().filter(InventoryUtils::isEmptyV2).mapToInt(ItemStack::getCount).sum() > ((Number)maxBlockSize.getValue()).intValue()) {
            this.performAction(getStream().filter(InventoryUtils::isEmptyHelper).min(Comparator.comparingInt(ItemStack::getCount)).orElse(null));
         }
      }

      if ((Boolean)this.switchSword.getValue()) {
         this.performActionSafe();
      }

      if ((Boolean)this.switchPickaxe.getValue()) {
         this.performAction(this.pickaxeSlot, InventoryUtils::isEmptyDirect, InventoryUtils::computeInt, item -> item instanceof PickaxeItem);
      }

      if ((Boolean)this.switchAxe.getValue()) {
         this.performAction(this.axeSlot, InventoryUtils::isEmptyCustom, InventoryUtils::computeInt, item2 -> item2 instanceof AxeItem);
      }

      if ((Boolean)this.switchRod.getValue() && !this.offhandItems.matches("Fishing Rod")) {
         int n3 = computeInt(this.rodSlot);
         ItemStack stack2 = getStream().filter(InventoryUtils::isEmptyByIndex).findAny().orElse(null);
         if (!(((ItemStack)mc2.player.getInventory().main.get(n3)).getItem() instanceof FishingRodItem)) {
            this.performAction(n3, stack2);
         }
      }

      if ((Boolean)this.switchBowOrCrossbow.getValue()) {
         this.performActionCustom();
         if (computeInt(Items.ARROW) > ((Number)maxArrowSize.getValue()).intValue()) {
            this.performAction(getStream().filter(InventoryUtils::isEmptyInternal).min(Comparator.comparingInt(ItemStack::getCount)).orElse(null));
         }
      }

      if ((Boolean)this.switchEnderPearl.getValue()) {
         this.performAction(computeInt(this.enderPearlSlot), Items.ENDER_PEARL);
      }

      if ((Boolean)this.switchWaterBucket.getValue()) {
         this.performAction(computeInt(this.waterBucketSlot), Items.WATER_BUCKET);
      }

      if ((Boolean)this.switchFireball.getValue()) {
         this.performAction(computeInt(this.fireballSlot), Items.FIRE_CHARGE);
      }

      if ((Boolean)BOOLEAN_VALUE.getValue()) {
         if (computeInt(Items.EGG) + computeInt(Items.SNOWBALL) > ((Number)NUMBER_VALUE.getValue()).intValue()) {
            this.performAction(getStream().filter(InventoryUtils::isValidItemEx).min(Comparator.comparingInt(ItemStack::getCount)).orElse(null));
         }

         if ((Boolean)this.switchEggsSnowballs.getValue() && !this.offhandItems.matches("Projectile")) {
            int n4 = computeInt(this.eggsSnowballsSlot);
            if (computeInt(Items.EGG) > 0) {
               this.performAction(n4, Items.EGG);
            } else if (computeInt(Items.SNOWBALL) > 0) {
               this.performAction(n4, Items.SNOWBALL);
            }
         }
      }
   }

   private void performActionSafe() {
      int n2 = computeInt(this.swordSlot);
      ItemStack stack = (ItemStack)mc2.player.getInventory().main.get(n2);
      ItemStack itemStack = getItemStack(InventoryUtils::isEmptyEx, InventoryUtils::computeIntEx);
      ItemStack itemStack2 = getItemStack(InventoryUtils::isEmptyWithOffset, InventoryUtils::computeIntAlt);
      if (computeFloat(itemStack2) > computeFloat(itemStack)) {
         itemStack = itemStack2;
      }

      if (itemStack != null && computeFloat(itemStack) > computeFloat(stack)) {
         this.performAction(n2, itemStack);
      }
   }

   private void performAction(NumberValue numberValue, Predicate<ItemStack> predicate, ToIntFunction<ItemStack> toIntFunction, Predicate<Item> predicate2) {
      int n2 = computeInt(numberValue);
      ItemStack itemStack = getItemStack(predicate, toIntFunction);
      ItemStack stack = (ItemStack)mc2.player.getInventory().main.get(n2);
      if (itemStack != null
         && predicate2.test(itemStack.getItem())
         && (InventoryUtils.computeFloatEx(itemStack) > InventoryUtils.computeFloatEx(stack) || !predicate2.test(stack.getItem()))) {
         this.performAction(n2, itemStack);
      }
   }

   private void performActionCustom() {
      InvManager.Entry entry;
      if (this.bowPriority.matches("Crossbow")) {
         entry = getEntry();
      } else if (this.bowPriority.matches("Power Bow")) {
         entry = getEntryEx();
      } else {
         entry = getEntryAlt();
      }

      if (entry.stack == null) {
         entry = getEntry();
      }

      if (entry.stack == null) {
         entry = getEntryEx();
      }

      if (entry.stack == null) {
         entry = getEntryAlt();
      }

      int n2 = computeInt(this.bowSlot);
      ItemStack stack = (ItemStack)mc2.player.getInventory().main.get(n2);
      if (entry.stack != null && entry.score.apply(entry.stack) > entry.score.apply(stack)) {
         this.performAction(n2, entry.stack);
      }
   }

   private static InvManager.Entry getEntry() {
      return new InvManager.Entry(getItemStack(InventoryUtils::isEmptyImpl, InventoryUtils::computeIntImpl), InventoryUtils::computeFloatDirect);
   }

   private static InvManager.Entry getEntryEx() {
      return new InvManager.Entry(getItemStack(InventoryUtils::isEmptyAlt, InventoryUtils::computeIntDirect), InventoryUtils::computeFloatSafe);
   }

   private static InvManager.Entry getEntryAlt() {
      return new InvManager.Entry(getItemStack(InventoryUtils::isEmptyAlt, InventoryUtils::computeIntSafe), InventoryUtils::computeFloatCustom);
   }

   private void performActionWithOffset() {
      ArrayList list = new ArrayList();

      for (int i = 0; i < mc2.player.getInventory().main.size(); i++) {
         list.add(i);
      }

      Collections.shuffle(list);

      for (Object obj : list) {
         int n2 = (int)obj;
         ItemStack stack = (ItemStack)mc2.player.getInventory().main.get(n2);
         if (!stack.isEmpty() && !isValidItem(stack)) {
            this.performAction(stack);
         }
      }
   }

   private void onKeyTyped(int n2) {
      if (n2 != -1) {
         this.sendPacket(computeInt(n2), 40, SlotActionType.SWAP);
      }
   }

   private void performAction(int n2, Item item) {
      ItemStack stack = (ItemStack)mc2.player.getInventory().main.get(n2);
      if (isValidItemAlt(stack)) {
         int n3 = computeIntEx(item);
         if (n3 != -1) {
            ItemStack stack2 = (ItemStack)mc2.player.getInventory().main.get(n3);
            if (stack.getItem() != item || stack.getCount() < stack2.getCount()) {
               this.sendPacket(computeInt(n3), n2, SlotActionType.SWAP);
            }
         }
      }
   }

   private void performAction(int n2, ItemStack stack) {
      ItemStack stack2 = (ItemStack)mc2.player.getInventory().main.get(n2);
      if (isValidItemAlt(stack2) && stack != stack2) {
         int n3 = computeInt(stack);
         if (n3 != -1) {
            this.sendPacket(computeInt(n3), n2, SlotActionType.SWAP);
         }
      }
   }

   private void performAction(ItemStack stack) {
      if (stack != null && isValidItemAlt(stack)) {
         int n2 = computeInt(stack);
         if (n2 != -1) {
            this.sendPacket(computeInt(n2), 1, SlotActionType.THROW);
         }
      }
   }

   public static boolean isValidItem(ItemStack stack) {
      Item item = stack.getItem();
      if (stack.isEmpty() || item == Items.BOOK || item == Items.BOOKSHELF || item == Items.WRITABLE_BOOK) {
         return false;
      }

      if (InventorySlotClassifier.isValidItemImpl(stack)) {
         return true;
      }

      if (stack.getName().getString().contains("点击使用")) {
         return false;
      }

      if (item instanceof ArmorItem) {
         EquipmentSlot equipmentSlot = ((EquippableComponent)Objects.requireNonNull((EquippableComponent)stack.get(DataComponentTypes.EQUIPPABLE))).slot();
         float f = InventoryUtils.computeFloat(stack);
         return InventorySlotClassifier.computeFloat(equipmentSlot) >= f ? false : !(f < computeFloat(equipmentSlot));
      }

      if (item instanceof SwordItem) {
         return getItemStack(InventoryUtils::isEmptyEx, InventoryUtils::computeIntEx) == stack;
      }

      if (item instanceof PickaxeItem) {
         return getItemStack(InventoryUtils::isEmptyDirect, InventoryUtils::computeInt) == stack;
      }

      if (item instanceof AxeItem && !InventoryUtils.isEmptyV3(stack)) {
         return getItemStack(InventoryUtils::isEmptyCustom, InventoryUtils::computeInt) == stack;
      }

      if (item instanceof ShovelItem) {
         return getItemStack(InventoryUtils::isEmptySafe, InventoryUtils::computeInt) == stack;
      }

      if (item instanceof HoeItem) {
         return false;
      }

      if (item instanceof CrossbowItem) {
         return getItemStack(InventoryUtils::isEmptyImpl, InventoryUtils::computeIntImpl) == stack;
      }

      if (item instanceof BowItem) {
         return isValidItemEx(stack);
      }

      if ((item != Items.WATER_BUCKET || computeInt(item) <= ((Number)keepWaterBuckets.getValue()).intValue())
         && (item != Items.LAVA_BUCKET || computeInt(item) <= ((Number)keepLavaBuckets.getValue()).intValue())
         && (item != Items.COBWEB || computeInt(item) <= ((Number)keepCobweb.getValue()).intValue())
         && (!(item instanceof FishingRodItem) || computeInt(Items.FISHING_ROD) <= 1)) {
         if ((item == Items.SNOWBALL || item == Items.EGG) && !(Boolean)BOOLEAN_VALUE.getValue()) {
            return false;
         } else if (item == Items.TNT) {
            return true;
         } else {
            return !(
                  item instanceof BlockItem blockItem
                     && (
                        blockItem.getBlock() instanceof SlabBlock
                           || blockItem.getBlock() instanceof FlowerBlock
                           || blockItem.getBlock() instanceof PressurePlateBlock
                           || blockItem.getBlock() instanceof StairsBlock
                     )
               )
               ? item != Items.PRISMARINE_SHARD
                  && item != Items.GRAVEL
                  && item != Items.FLINT
                  && !(item instanceof WrittenBookItem)
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
      } else {
         return false;
      }
   }

   private static boolean isValidItemEx(ItemStack stack) {
      if (isValidItemAlt(stack)) {
         if (InventoryUtils.computeFloatCustom(stack) > 10.0F && getItemStack(InventoryUtils::isEmptyAlt, InventoryUtils::computeIntSafe) == stack) {
            return true;
         }

         if (InventoryUtils.computeFloatSafe(stack) > 10.0F && getItemStack(InventoryUtils::isEmptyAlt, InventoryUtils::computeIntDirect) == stack) {
            return true;
         }

         if (InventoryUtils.computeFloatWithOffset(stack) > 10.0F && getItemStack(InventoryUtils::isEmptyAlt, InventoryUtils::computeIntCustom) == stack) {
            return true;
         }
      }

      return computeInt(Items.BOW) <= 1;
   }

   private static Stream<ItemStack> getStream() {
      return Stream.concat(mc2.player.getInventory().main.stream(), mc2.player.getInventory().armor.stream());
   }

   private static ItemStack getItemStack(Predicate<ItemStack> predicate, ToIntFunction<ItemStack> toIntFunction) {
      return getStream().filter(predicate).max(Comparator.comparingInt(toIntFunction)).orElse(null);
   }

   private static float computeFloat(EquipmentSlot equipmentSlot) {
      return getStream()
         .filter(stack -> InventoryUtils.isValid(equipmentSlot, stack))
         .map(InventoryUtils::computeFloat)
         .max(Float::compareTo)
         .orElse(0.0F);
   }

   private static int computeInt(Item item) {
      return getStream().filter(stack -> stack.getItem() == item).mapToInt(ItemStack::getCount).sum();
   }

   private static boolean isValidItemAlt(ItemStack stack) {
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

   private static boolean isValidItemImpl(ItemStack stack) {
      return stack != null
         && !stack.isEmpty()
         && stack.getItem() instanceof BlockItem blockItem
         && (
            !blockItem.getBlock().getDefaultState().isFullCube((BlockView)(mc2.world != null ? mc2.world : EmptyBlockView.INSTANCE), BlockPos.ORIGIN)
               || Scaffold.invalidBlocks.contains(blockItem.getBlock())
         );
   }

   private static float computeFloat(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         float f = 0.0F;
         if (stack.getItem() instanceof SwordItem || InventoryUtils.isEmptyV3(stack)) {
            double d = mc2.player.getAttributes().hasAttribute(EntityAttributes.ATTACK_DAMAGE)
               ? mc2.player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE)
               : 0.0;
            f += (float)((AttributeModifiersComponent)stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT))
               .applyOperations(d, EquipmentSlot.MAINHAND);
         }

         int n2 = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack);
         if (n2 > 0) {
            f += 1.25F * n2;
         }

         return f;
      } else {
         return 0.0F;
      }
   }

   private static int computeInt(NumberValue numberValue) {
      return (int)(((Number)numberValue.getValue()).floatValue() - 1.0F);
   }

   private static int computeInt(int n2) {
      return n2 < 9 ? n2 + 36 : n2;
   }

   private static int computeIntEx(Item item) {
      return HotbarItemFinder.computeInt(0, mc2.player.getInventory().main.size(), item);
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

   public static boolean isValidAlt() {
      return !lastTimeUtil.hasTimeElapsed(n);
   }

   public static boolean isValidImpl() {
      return (Boolean)BOOLEAN_VALUE.getValue();
   }

   public static int getHeight() {
      return ((Number)maxBlockSize.getValue()).intValue();
   }

   public static int getHeightImpl() {
      return ((Number)maxArrowSize.getValue()).intValue();
   }

   public static int getHeightDirect() {
      return ((Number)keepWaterBuckets.getValue()).intValue();
   }

   public static int getHeightSafe() {
      return ((Number)keepLavaBuckets.getValue()).intValue();
   }

   public static int getHeightCustom() {
      return ((Number)keepCobweb.getValue()).intValue();
   }

   public static int getHeightWithOffset() {
      return ((Number)NUMBER_VALUE.getValue()).intValue();
   }

   private record Entry(ItemStack stack, Function<ItemStack, Float> score) {

      public ItemStack getStack() {
         return this.stack;
      }

      public Function<ItemStack, Float> getScore() {
         return this.score;
      }
   }
}
