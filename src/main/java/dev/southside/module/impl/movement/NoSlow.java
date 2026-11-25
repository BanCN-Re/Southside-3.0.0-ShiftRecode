package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.SlowDownEvent;
import dev.southside.event.events.RightClickEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.FovMultiplierEvent;
import dev.southside.event.events.ItemUseEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.InteractionStateFlags;
import dev.southside.utils.BlinkUtils;
import dev.southside.utils.MovementMathUtils;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.MovementUtils;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class NoSlow extends Module {
   public static boolean activeValue;
   @SerializedName("old")
   public final BooleanValue old = new BooleanValue("Old", false);
   @SerializedName("bowNoSlow")
   public final BooleanValue bowNoSlow = new BooleanValue("BowNoSlow", true);
   @SerializedName("useBlink")
   public final BooleanValue useBlink = new BooleanValue("Use Blink", true);
   @SerializedName("flushPending")
   private boolean flushPending;
   @SerializedName("skipNext")
   private boolean skipNext;
   @SerializedName("swapPending")
   private boolean swapPending;
   @SerializedName("lagTicks")
   private int lagTicks;
   @SerializedName("bypassPrimed")
   private boolean bypassPrimed;
   @SerializedName("holdingPackets")
   private boolean holdingPackets;

   public NoSlow() {
      super(Category.Movement, "NoSlow", "666这个入开挂了，使用不减速。。");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.addDependency(this.useBlink.getLabel(), this.old.getLabel());
      this.hideIf(this.bowNoSlow.getLabel(), this.old::getValue);
      this.addListener(this.old.getLabel(), (Runnable) this::performActionAlt);
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (!(Boolean)this.old.getValue() && mc2.player != null && !this.skipNext && directionEvent.getDirection() == TransferOrigin.SEND) {
         if (directionEvent.getPacket() instanceof PlayerActionC2SPacket playerActionC2SPacket && playerActionC2SPacket.getAction() == Action.RELEASE_USE_ITEM && activeValue) {
            this.flushPending = true;
         } else if (directionEvent.getPacket() instanceof PlayerInteractItemC2SPacket playerInteractItemC2SPacket) {
            ItemStack stackInHand = mc2.player.getStackInHand(playerInteractItemC2SPacket.getHand());
            if (MovementMathUtils.isCrossbowItem(stackInHand) || isValidItemEx(stackInHand)) {
               return;
            }

            UseAction useAction = stackInHand.getUseAction();
            if (!this.isValid(useAction)) {
               return;
            }

            activeValue = true;
            if (!MovementMathUtils.isValid() && (useAction == UseAction.EAT || useAction == UseAction.DRINK)) {
               directionEvent.setCancelled(true);
               this.performActionSafe();
               this.swapPending = true;
               Hand hand = playerInteractItemC2SPacket.getHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
               PacketUtils.sendPacketDirect(new PlayerInteractItemC2SPacket(hand, playerInteractItemC2SPacket.getSequence(), playerInteractItemC2SPacket.getYaw(), playerInteractItemC2SPacket.getPitch()));
            }

            ServerPacketManager.performAction();
         }
      }
   }

   @EventTarget
   public void onCustomEvent2(RightClickEvent rightClickEvent) {
      if (!(Boolean)this.old.getValue() && mc2.crosshairTarget != null && mc2.crosshairTarget.getType() != Type.MISS) {
         this.skipNext = true;
      }
   }

   @EventTarget
   public void onClientPlayerEntityEvent2(SlowDownEvent slowDownEvent) {
      if (mc2.player != null) {
         if (!(Boolean)this.old.getValue()) {
            if (activeValue && !this.skipNext && !isValidItemEx(mc2.player.getActiveItem()) && this.isValid(mc2.player.getActiveItem().getUseAction())) {
               slowDownEvent.setCancelled(true);
               mc2.player.setSprinting(true);
            }
         } else {
            if (mc2.player.getItemUseTime() % 3 == 0 || this.isValidEx()) {
               this.bypassPrimed = false;
            } else if (!this.bypassPrimed) {
               this.bypassPrimed = true;
            } else {
               slowDownEvent.setCancelled(true);
               mc2.player.setSprinting(true);
            }
         }
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (!(Boolean)this.old.getValue() && mc2.player != null && stateEvent.getState() != Stage.POST) {
         this.skipNext = false;
         if (this.flushPending) {
            this.performActionImpl();
            this.flushPending = false;
         }

         int n = MovementMathUtils.isValidEx(mc2.player.getActiveItem().getUseAction()) ? 35 : 32;
         if (activeValue && ServerPacketManager.active && ServerPacketManager.count >= n) {
            this.flushPending = true;
            mc2.options.useKey.setPressed(false);
         }

         if (this.swapPending && !activeValue && !mc2.player.isUsingItem() && !ServerPacketManager.active) {
            this.performActionDirect();
         }
      }
   }

   @EventTarget
   public void onHandEvent2(ItemUseEvent itemUseEvent) {
      if (!(Boolean)this.old.getValue()
         && mc2.player != null
         && mc2.world != null
         && itemUseEvent.getHand() == Hand.MAIN_HAND
         && !isValidItem(itemUseEvent.getItemStack())
         && isValidItem(mc2.player.getOffHandStack())) {
         if (itemUseEvent.getItemStack().isOf(Items.FIRE_CHARGE)) {
            this.skipNext = true;
         } else {
            ActionResult actionResult = itemUseEvent.getItemStack().copy().getItem().use(mc2.world, mc2.player, Hand.MAIN_HAND);
            if (actionResult instanceof Success) {
               this.skipNext = true;
            } else {
               itemUseEvent.setCancelled(true);
            }
         }
      }
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (!(Boolean)this.old.getValue()) {
         if (activeValue && Southside.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()) {
            ServerPacketManager.performActionImpl();
            activeValue = false;
         }
      } else if (mc2.player != null && mc2.world != null && (Boolean)this.useBlink.getValue()) {
         if (this.holdingPackets && !BlinkUtils.blinking) {
            this.holdingPackets = false;
            this.lagTicks = 0;
         }

         if (this.holdingPackets) {
            this.lagTicks++;
         }

         if (mc2.player.getItemUseTime() > 16 && !this.isValidEx() && BlinkUtils.startBlink()) {
            this.holdingPackets = true;
            this.lagTicks = mc2.player.getItemUseTime();
            mc2.options.useKey.setPressed(false);
         }

         if (this.holdingPackets && this.lagTicks >= 32) {
            this.performActionCustom();
         }
      } else {
         this.performActionCustom();
      }
   }

   @EventTarget
   public void onFovMultiplier(FovMultiplierEvent fovMultiplierEvent) {
      if ((Boolean)this.old.getValue() && mc2.player != null && MovementUtils.isValid() && mc2.player.isUsingItem()) {
         fovMultiplierEvent.setFovMultiplier2(1.0F);
      }
   }

   @Override
   public void onDisable() {
      this.performActionAlt();
   }

   private void performActionAlt() {
      this.performActionCustom();
      this.bypassPrimed = false;
      if (activeValue || this.swapPending) {
         this.performActionImpl();
      }

      this.skipNext = false;
      this.flushPending = false;
   }

   private void performActionImpl() {
      ServerPacketManager.performActionImpl();
      this.performActionDirect();
      activeValue = false;
   }

   private void performActionDirect() {
      if (this.swapPending) {
         this.performActionSafe();
         this.swapPending = false;
      }
   }

   private void performActionSafe() {
      PacketUtils.sendPacketDirect(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
      InteractionStateFlags.activeValue = true;
   }

   private boolean isValid(UseAction useAction) {
      return switch (useAction) {
         case EAT, DRINK, SPEAR -> true;
         case BOW, CROSSBOW -> this.bowNoSlow.getValue();
         default -> false;
      };
   }

   private static boolean isValidItem(ItemStack stack) {
      return switch (stack.getUseAction()) {
         case EAT, DRINK, SPEAR, BOW, CROSSBOW -> true;
         default -> false;
      };
   }

   private static boolean isValidItemEx(ItemStack stack) {
      return stack.isOf(Items.MUSHROOM_STEW) || stack.isOf(Items.RABBIT_STEW) || stack.isOf(Items.BEETROOT_SOUP) || stack.isOf(Items.SUSPICIOUS_STEW);
   }

   private boolean isValidEx() {
      return mc2.player.getActiveItem().getItem() instanceof BowItem || mc2.player.getActiveItem().getItem() instanceof CrossbowItem;
   }

   private void performActionCustom() {
      if (this.holdingPackets) {
         BlinkUtils.stopBlink();
         this.holdingPackets = false;
      }

      this.lagTicks = 0;
   }
}
