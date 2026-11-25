package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.EntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.EnchantmentUtils;
import dev.southside.utils.ModeValue;
import dev.southside.utils.MovementFreezeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;

public class Criticals extends Module {
   public static final ModeValue mode = new ModeValue("Mode", "Switch", "Switch", "Stuck", "Packet");
   @SerializedName("previousSlot")
   private int previousSlot;
   @SerializedName("attackCounter")
   private int attackCounter;
   @SerializedName("fakeCritReady")
   private boolean fakeCritReady;

   public Criticals() {
      super(Category.Combat, "Criticals", "idk");
   }

   @EventTarget
   public void onEntity(EntityEvent entityEvent) {
      if (mode.getDisplayTextEx().equalsIgnoreCase("Stuck") && entityEvent.phase == Stage.PRE) {
         this.attackCounter++;
         if (this.attackCounter % 2 == 0) {
            this.fakeCritReady = true;
         }
      }

      if (mode.getDisplayTextEx().equalsIgnoreCase("Switch")) {
         if (entityEvent.phase == Stage.PRE) {
            this.performActionAlt();
         } else if (this.previousSlot != -1) {
            this.sendPacket();
         }
      }
   }

   private void performActionAlt() {
      short s = 19773;
      this.previousSlot = -1;
      long l = 7401734771046767675L;
      PlayerScreenHandler playerScreenHandler = Module.mc.player.playerScreenHandler;
      double d = 0.0;
      int n = -1;

      for (int i = 36; i < 45; i++) {
         ItemStack stack = playerScreenHandler.getSlot(i).getStack();
         if (!stack.isEmpty() && Module.mc.player.getInventory().selectedSlot != i - 36) {
            double d2 = 0.0;
            if (stack.hasEnchantments()) {
               d2 += EnchantmentUtils.computeInt(Enchantments.SHARPNESS, stack) * 0.5;
            }

            if (d2 > d) {
               d = d2;
               n = i - 36;
            }
         }
      }

      if (n != -1 && Module.mc.player.getInventory().selectedSlot != n) {
         this.previousSlot = Module.mc.player.getInventory().selectedSlot;
         Module.mc.player.getInventory().selectedSlot = n;
      }
   }

   private void sendPacket() {
      Module.mc.player.getInventory().selectedSlot = this.previousSlot;
      int selectedSlot = Module.mc.player.getInventory().selectedSlot;
      if (selectedSlot != Module.mc.interactionManager.lastSelectedSlot) {
         Module.mc.interactionManager.lastSelectedSlot = selectedSlot;
         Module.mc.interactionManager.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(Module.mc.interactionManager.lastSelectedSlot));
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (mode.getDisplayTextEx().equalsIgnoreCase("Stuck")) {
         if (KillAura.target != null && Southside.INSTANCE.getModuleManager().getModule(KillAura.class).isValidTarget(KillAura.target)) {
            if (Module.mc.player.fallDistance > 0.0F && !this.fakeCritReady && Module.mc.player.getVelocity().y < 0.05) {
               this.performActionImpl();
            } else if (this.fakeCritReady) {
               this.fakeCritReady = false;
            }
         } else {
            this.attackCounter = 0;
         }
      }
   }

   private void performActionImpl() {
      if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         if (MovementFreezeManager.mc.player.getVelocity() != null && !MovementFreezeManager.activeValue && MovementFreezeManager.mc.player != null && !MovementFreezeManager.active) {
            MovementFreezeManager.active = true;
            MovementFreezeManager.vector = MovementFreezeManager.mc.player.getVelocity();
            MovementFreezeManager.factor = MovementFreezeManager.mc.player.fallDistance;
         }
      }
   }
}
