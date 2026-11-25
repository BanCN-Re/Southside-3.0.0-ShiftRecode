package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.ElapsedTimer;
import dev.southside.utils.Rotation;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;

public class AutoPot extends Module {
   @SerializedName("health")
   public final NumberValue health = new NumberValue("Health", 15, 0, 20, 1);
   @SerializedName("healing")
   public final BooleanValue healing = new BooleanValue("Healing", true);
   @SerializedName("speed")
   public final BooleanValue speed = new BooleanValue("Speed", false);
   @SerializedName("jumpBoost")
   public final BooleanValue jumpBoost = new BooleanValue("Jump Boost", false);
   @SerializedName("fireResistance")
   public final BooleanValue fireResistance = new BooleanValue("Fire Resistance", false);
   @SerializedName("onlyGround")
   public final BooleanValue onlyGround = new BooleanValue("Only Ground", true);
   @SerializedName("autoRefill")
   public final BooleanValue autoRefill = new BooleanValue("Auto Refill", false);
   @SerializedName("delay")
   public final NumberValue delay = new NumberValue("Delay", 500, 100, 2000, 50);
   @SerializedName("throwTimer")
   private final ElapsedTimer throwTimer = new ElapsedTimer();
   @SerializedName("refillTimer")
   private final ElapsedTimer refillTimer = new ElapsedTimer();
   @SerializedName("refillSlots")
   private final Map<AutoPot.Mode, Integer> refillSlots = new EnumMap<>(AutoPot.Mode.class);
   @SerializedName("throwPending")
   private boolean throwPending;
   @SerializedName("potionSlot")
   private int potionSlot = -1;
   @SerializedName("postThrowTicks")
   private int postThrowTicks = -1;
   private static final long n = 50L;

   public AutoPot() {
      super(Category.Player, "AutoPot", "Auto throw splash potions");
   }

   @Override
   public void onDisable() {
      this.throwPending = false;
      this.potionSlot = -1;
      this.postThrowTicks = -1;
      this.refillSlots.clear();
      super.onDisable();
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      if (mc2.player != null && mc2.world != null) {
         if (this.postThrowTicks >= 0) {
            if (this.postThrowTicks > 0) {
               this.setProgress(89.0F);
            }

            this.postThrowTicks--;
         } else if (mc2.player.age > 10
            && (!(Boolean)this.onlyGround.getValue() || mc2.player.isOnGround())
            && this.throwTimer.hasElapsed(((Number)this.delay.getValue()).longValue())) {
            if ((Boolean)this.autoRefill.getValue() && !mc2.player.lastSprinting && this.refillTimer.hasElapsed(n)) {
               this.performActionAlt();
               this.refillTimer.reset();
            }

            this.potionSlot = this.computeInt();
            this.throwPending = this.potionSlot != -1;
            if (this.throwPending) {
               this.setProgress(80.0F);
            }
         }
      }
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if (this.throwPending && this.potionSlot != -1 && mc2.player != null && mc2.interactionManager != null) {
         this.throwPending = false;
         int selectedSlot = mc2.player.getInventory().selectedSlot;
         mc2.player.getInventory().selectedSlot = this.potionSlot;

         try {
            if (mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND) instanceof Success success && success.swingSource() == SwingSource.CLIENT) {
               PacketUtils.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
         } finally {
            mc2.player.getInventory().selectedSlot = selectedSlot;
            mc2.interactionManager.syncSelectedSlot();
         }

         this.potionSlot = -1;
         this.postThrowTicks = 1;
         this.throwTimer.reset();
      } else {
         this.throwPending = false;
      }
   }

   private void setProgress(float f) {
      RotationManager.requestRotation(new Rotation(mc2.player.getYaw(), f), this.getHeightAlt());
   }

   private int computeInt() {
      for (AutoPot.Mode mode : AutoPot.Mode.values()) {
         if (this.isValid(mode)) {
            boolean flag = mode == AutoPot.Mode.HEALING
               ? mc2.player.getHealth() < ((Number)this.health.getValue()).floatValue()
               : !mc2.player.hasStatusEffect(mode.effect);
            if (flag) {
               int n2 = this.computeInt(mode, 0, 9);
               if (n2 != -1) {
                  return n2;
               }
            }
         }
      }

      return -1;
   }

   private boolean isValid(AutoPot.Mode mode) {
      return switch (mode) {
         case HEALING -> this.healing.getValue();
         case SPEED -> this.speed.getValue();
         case JUMP_BOOST -> this.jumpBoost.getValue();
         case FIRE_RESISTANCE -> this.fireResistance.getValue();
      };
   }

   private int computeInt(AutoPot.Mode mode, int n2, int n3) {
      for (int i = n2; i < n3; i++) {
         if (mode.isValidItem(mc2.player.getInventory().getStack(i))) {
            return i;
         }
      }

      return -1;
   }

   private void performActionAlt() {
      if (mc2.interactionManager != null) {
         Set set = Southside.INSTANCE.getModuleManager().getModule(InvManager.class).getSet();
         set.addAll(this.refillSlots.values());

         for (AutoPot.Mode mode : AutoPot.Mode.values()) {
            Integer integer = this.refillSlots.get(mode);
            if (integer != null && !mode.isValidItem(mc2.player.getInventory().getStack(integer))) {
               this.refillSlots.remove(mode);
            }
         }

         for (AutoPot.Mode mode2 : AutoPot.Mode.values()) {
            if (this.isValid(mode2) && this.computeInt(mode2, 0, 9) == -1) {
               int n2 = this.computeInt(mode2, 9, 36);
               if (n2 != -1) {
                  int n3 = this.computeInt(set);
                  if (n3 != -1) {
                     mc2.interactionManager.clickSlot(0, n2, n3, SlotActionType.SWAP, mc2.player);
                     this.refillSlots.put(mode2, n3);
                     return;
                  }
               }
            }
         }
      }
   }

   private int computeInt(Set<Integer> set) {
      for (int i = 0; i < 9; i++) {
         if (!set.contains(i) && mc2.player.getInventory().getStack(i).isEmpty()) {
            return i;
         }
      }

      for (int j = 0; j < 9; j++) {
         if (!set.contains(j)) {
            return j;
         }
      }

      return -1;
   }

   private enum Mode {
      HEALING(null, List.of(Potions.HEALING, Potions.STRONG_HEALING)),
      SPEED(StatusEffects.SPEED, List.of(Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS)),
      JUMP_BOOST(StatusEffects.JUMP_BOOST, List.of(Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING)),
      FIRE_RESISTANCE(StatusEffects.FIRE_RESISTANCE, List.of(Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE));

      @SerializedName("effect")
      public final RegistryEntry<StatusEffect> effect;
      @SerializedName("variants")
      private final List<RegistryEntry<Potion>> variants;

      Mode(RegistryEntry<StatusEffect> registryEntry, List<RegistryEntry<Potion>> list) {
         this.effect = registryEntry;
         this.variants = list;
      }

      public boolean isValidItem(ItemStack stack) {
         if (!stack.isEmpty() && stack.getItem() == Items.SPLASH_POTION) {
            PotionContentsComponent potionContentsComponent = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
            return potionContentsComponent != null && this.variants.stream().anyMatch(potionContentsComponent::matches);
         } else {
            return false;
         }
      }

      // $VF: synthetic method
      private static AutoPot.Mode[] createValues() {
         return new AutoPot.Mode[]{HEALING, SPEED, JUMP_BOOST, FIRE_RESISTANCE};
      }


   }
}
