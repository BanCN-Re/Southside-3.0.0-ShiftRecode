package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.EntityEvent;
import dev.southside.event.events.HandEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.AttackTracker;
import dev.southside.utils.EnchantmentUtils;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.InventoryUtils;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class AutoTool extends Module {
   @SerializedName("autoWeapon")
   public final BooleanValue autoWeapon = new BooleanValue("AutoWeapon", false);
   @SerializedName("shieldBreak")
   public final BooleanValue shieldBreak = new BooleanValue("ShieldBreak", false);
   @SerializedName("maceOnFall")
   public final BooleanValue maceOnFall = new BooleanValue("Mace on Fall", true);
   @SerializedName("maceFallDistance")
   public final NumberValue maceFallDistance = new NumberValue("Mace Fall Distance", 4, 2, 10, 1);
   @SerializedName("checkGoldenAxe")
   public final BooleanValue checkGoldenAxe = new BooleanValue("Check Golden Axe", true);
   @SerializedName("spoofItem")
   public final BooleanValue spoofItem = new BooleanValue("Spoof Item", true);
   @SerializedName("returnDelay")
   public final NumberValue returnDelay = new NumberValue("Return Delay", 2, 0, 10, 1);
   @SerializedName("previousSlot")
   private int previousSlot = -1;
   @SerializedName("currentToolSlot")
   private int currentToolSlot = -1;
   @SerializedName("returnDelayTicks")
   private int returnDelayTicks;
   @SerializedName("shieldBreakPerformed")
   private boolean shieldBreakPerformed;
   @SerializedName("shieldBreakPacketSent")
   private boolean shieldBreakPacketSent;
   @SerializedName("miningToolSwitched")
   private boolean miningToolSwitched;

   public AutoTool() {
      super(Category.Player, "AutoTool", "自动打胶");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.maceOnFall.getLabel(), () -> !(Boolean)this.autoWeapon.getValue());
      this.hideIf(this.maceFallDistance.getLabel(), () -> !(Boolean)this.autoWeapon.getValue() || !(Boolean)this.maceOnFall.getValue());
   }

   public void performActionAlt() {
      if ((Boolean)this.autoWeapon.getValue() && mc2.player != null && !isValidItemEx(mc2.player.getMainHandStack())) {
         if ((Boolean)this.maceOnFall.getValue() && mc2.player.fallDistance > ((Number)this.maceFallDistance.getValue()).floatValue()) {
            int n = this.computeInt();
            if (n != -1) {
               mc2.player.getInventory().selectedSlot = n;
               return;
            }
         }

         int n2 = this.computeIntEx();
         if (n2 != -1) {
            mc2.player.getInventory().selectedSlot = n2;
         }
      }
   }

   private int computeInt() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (!stack.isEmpty() && stack.getItem() instanceof MaceItem) {
            return i;
         }
      }

      return -1;
   }

   private int computeIntEx() {
      int n = -1;
      float f = -1.0F;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (!stack.isEmpty()) {
            float f2;
            if (stack.getItem() instanceof SwordItem) {
               f2 = InventoryUtils.computeFloatImpl(stack);
            } else {
               if (!(stack.getItem() instanceof AxeItem) || isValidItem(stack) || !InventoryUtils.isEmptyV3(stack)) {
                  continue;
               }

               f2 = this.computeFloat(stack);
            }

            if (f2 > f) {
               f = f2;
               n = i;
            }
         }
      }

      return n;
   }

   private int computeIntAlt() {
      int n = -1;
      float f = 0.0F;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (stack.getItem() instanceof AxeItem) {
            float f2 = this.computeFloat(stack);
            if (!(f > f2)) {
               f = f2;
               n = i;
            }
         }
      }

      return n;
   }

   private float computeFloat(ItemStack stack) {
      float f = 0.0F;
      if (InventoryUtils.isEmptyV3(stack)) {
         double d = mc2.player.getAttributes().hasAttribute(EntityAttributes.ATTACK_DAMAGE)
            ? mc2.player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE)
            : 0.0;
         f = (float)((AttributeModifiersComponent)stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT))
            .applyOperations(d, EquipmentSlot.MAINHAND);
      }

      int n = EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack);
      return n > 0 ? f + 1.25F * n : f;
   }

   private int computeInt(BlockState state) {
      int n = -1;
      float f = 1.0F;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (!stack.isEmpty() && (!(Boolean)this.checkGoldenAxe.getValue() || !isValidItem(stack))) {
            float miningSpeedMultiplier = stack.getMiningSpeedMultiplier(state);
            if (miningSpeedMultiplier > f) {
               f = miningSpeedMultiplier;
               n = i;
            }
         }
      }

      return n;
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (mc2.player != null && mc2.world != null) {
         if ((Boolean)this.shieldBreak.getValue() && this.shieldBreakPerformed && this.shieldBreakPacketSent) {
            this.performActionImpl();
            this.previousSlot = -1;
            this.shieldBreakPerformed = false;
            this.shieldBreakPacketSent = false;
         }

         if (mc2.interactionManager != null && mc2.interactionManager.isBreakingBlock()) {
            if (!(mc2.crosshairTarget instanceof BlockHitResult hitResult)) {
               return;
            }

            if (!this.miningToolSwitched) {
               this.previousSlot = mc2.player.getInventory().selectedSlot;
               this.miningToolSwitched = true;
               this.returnDelayTicks = 0;
            }

            int n = this.computeInt(mc2.world.getBlockState(hitResult.getBlockPos()));
            if (n != -1 && n != this.currentToolSlot) {
               this.currentToolSlot = n;
               mc2.player.getInventory().selectedSlot = n;
            }
         } else if (this.miningToolSwitched && ++this.returnDelayTicks >= ((Number)this.returnDelay.getValue()).intValue()) {
            this.performActionImpl();
            this.previousSlot = -1;
            this.currentToolSlot = -1;
            this.returnDelayTicks = 0;
            this.miningToolSwitched = false;
         }
      }
   }

   private void performActionImpl() {
      if (this.previousSlot != -1) {
         mc2.player.getInventory().selectedSlot = this.previousSlot;
      }
   }

   @EventTarget
   public void onEntity(EntityEvent entityEvent) {
      if (mc2.player != null && mc2.interactionManager != null && entityEvent.phase == Stage.PRE) {
         if ((Boolean)this.shieldBreak.getValue()) {
            int n = this.computeIntAlt();
            if (entityEvent.entity instanceof PlayerEntity player && isValidTarget(player) && n != -1) {
               this.currentToolSlot = n;
            }

            if (this.currentToolSlot != -1 && this.currentToolSlot == n) {
               this.previousSlot = mc2.player.getInventory().selectedSlot;
               mc2.player.getInventory().selectedSlot = this.currentToolSlot;
            }

            if (n != -1 && mc2.player.getMainHandStack() == mc2.player.getInventory().getStack(n)) {
               entityEvent.setCancelled(true);
               if (!this.isValid(entityEvent)) {
                  return;
               }
            }
         }

         if (entityEvent.entity instanceof PlayerEntity) {
            this.performActionAlt();
         }
      }
   }

   private boolean isValid(EntityEvent entityEvent) {
      for (int i = 0; i < 2; i++) {
         if (mc2.attackCooldown > 0) {
            return false;
         }

         if (mc2.crosshairTarget == null) {
            if (mc2.interactionManager.hasLimitedAttackSpeed()) {
               mc2.attackCooldown = 10;
            }

            return false;
         }

         mc2.interactionManager.syncSelectedSlot();
         PacketUtils.sendPacket(PlayerInteractEntityC2SPacket.attack(entityEvent.entity, mc2.player.isSneaking()));
         mc2.player.attack(entityEvent.entity);
         mc2.player.resetLastAttackedTicks();
         mc2.player.swingHand(Hand.MAIN_HAND);
         AttackTracker.INSTANCE.performAction();
         this.shieldBreakPerformed = true;
      }

      return true;
   }

   @EventTarget
   public void onHand(HandEvent handEvent) {
      if (mc2.player != null && (Boolean)this.spoofItem.getValue() && handEvent.getHand() == Hand.MAIN_HAND && this.miningToolSwitched && this.previousSlot != -1) {
         ItemStack stack = mc2.player.getInventory().getStack(this.previousSlot);
         if (!stack.isEmpty()) {
            handEvent.setItemStack(stack);
         }
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (mc2.player != null
         && (Boolean)this.shieldBreak.getValue()
         && directionEvent.getPacket() instanceof PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket
         && playerInteractEntityC2SPacket.type.getType() == InteractType.ATTACK) {
         int n = this.computeIntAlt();
         if (n != -1 && mc2.player.getMainHandStack() == mc2.player.getInventory().getStack(n)) {
            this.shieldBreakPacketSent = true;
         }
      }
   }

   private static boolean isValidTarget(PlayerEntity player) {
      return player.isBlocking() && (player.getMainHandStack().getItem() instanceof ShieldItem || player.getOffHandStack().getItem() instanceof ShieldItem);
   }

   private static boolean isValidItem(ItemStack stack) {
      return !stack.isEmpty()
         && (
            stack.getName().getString().contains("一刀")
               || stack.getComponents() != null && stack.getComponents().toString().contains("一刀")
               || stack.getItem() == Items.GOLDEN_AXE && EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack) > 100
         );
   }

   private static boolean isValidItemEx(ItemStack stack) {
      return !stack.isEmpty()
         && (
            stack.getItem() == Items.END_CRYSTAL
               || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE
               || isValidItem(stack)
               || stack.getItem() == Items.SLIME_BALL && !stack.contains(DataComponentTypes.CUSTOM_NAME)
         );
   }
}
