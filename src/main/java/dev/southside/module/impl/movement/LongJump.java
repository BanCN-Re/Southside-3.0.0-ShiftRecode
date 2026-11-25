package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.IText;
import dev.southside.utils.RotationManager;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.Rotation;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class LongJump extends Module {
   @SerializedName("packetLock")
   private final Object packetLock = new Object();
   @SerializedName("bufferedPackets")
   private final ArrayDeque<LongJump.Entry> bufferedPackets = new ArrayDeque<>();
   @SerializedName("knockbackMagnitudes")
   private final List<Double> knockbackMagnitudes = new ArrayList<>();
   @SerializedName("intercepting")
   private boolean intercepting;
   @SerializedName("captureReady")
   private boolean captureReady;
   @SerializedName("releasedCount")
   private int releasedCount;
   @SerializedName("rotation")
   private Rotation rotation;
   @SerializedName("checkInitialMovement")
   private boolean checkInitialMovement;
   @SerializedName("startedWithoutMovement")
   private boolean startedWithoutMovement;
   @SerializedName("mouse4WasPressed")
   private boolean mouse4WasPressed;
   @SerializedName("mouse5WasPressed")
   private boolean mouse5WasPressed;
   @SerializedName("usingFireCharge")
   private boolean usingFireCharge;
   @SerializedName("previousHotbarSlot")
   private int previousHotbarSlot = -1;
   @SerializedName("fireChargeStage")
   private LongJump.Mode fireChargeStage = LongJump.Mode.IDLE;
   @SerializedName("fireChargesBeforeUse")
   private int fireChargesBeforeUse;

   public LongJump() {
      super(Category.Movement, "LongJump", "Allows you to jump higher");
   }

   @Override
   public int getHeightAlt() {
      return 1;
   }

   @Override
   public void onEnable() {
      this.performActionCustom();
      if (mc2.inGameHud != null) {
         mc2.inGameHud.getChatHud().addMessage(Text.empty().append(IText.TEXT).append(Text.literal("Press Mouse4 Start && Mouse5 to release")));
      }
   }

   @Override
   public void onDisable() {
      this.performActionImpl();
      this.performActionDirect();
      this.performActionSafe();
      this.performActionCustom();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.performActionSafe();
      this.performActionCustom();
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getDirection() == TransferOrigin.RECEIVE && mc2.player != null) {
         synchronized (this.packetLock) {
            Packet packet = directionEvent.getPacket();
            boolean flag = packet instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket && entityVelocityUpdateS2CPacket.getEntityId() == mc2.player.getId();
            if (this.intercepting || this.captureReady && flag) {
               directionEvent.setCancelled(true);
               if (!(packet instanceof PlayerPositionLookS2CPacket)) {
                  if (flag) {
                     EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket2 = (EntityVelocityUpdateS2CPacket)packet;
                     double velocityX = entityVelocityUpdateS2CPacket2.getVelocityX();
                     double velocityY = entityVelocityUpdateS2CPacket2.getVelocityY();
                     double velocityZ = entityVelocityUpdateS2CPacket2.getVelocityZ();
                     this.knockbackMagnitudes.add(Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ));
                  }

                  this.bufferedPackets.addLast(new LongJump.Entry(packet, flag));
                  this.intercepting = true;
               }
            }
         }
      }
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (mc2.player != null) {
         if (xevent.getPhase() == Stage.PRE) {
            if (this.fireChargeStage == LongJump.Mode.AIMING) {
               synchronized (this.packetLock) {
                  this.captureReady = true;
               }

               mc2.options.jumpKey.setPressed(true);
               this.rotation = this.startedWithoutMovement ? new Rotation(mc2.player.getYaw(), 90.0F) : new Rotation(mc2.player.getYaw() - 180.0F, 88.0F);
               this.fireChargeStage = LongJump.Mode.USING;
            } else if (this.fireChargeStage == LongJump.Mode.USING) {
               this.fireChargeStage = LongJump.Mode.IDLE;
               int n = this.computeInt();
               if (n == -1) {
                  this.setEnabled(false);
                  return;
               }

               mc2.player.getInventory().selectedSlot = n;
               this.fireChargesBeforeUse = this.computeIntEx();
               mc2.options.useKey.setPressed(true);
               this.usingFireCharge = true;
            }
         } else if (this.usingFireCharge) {
            if (this.computeIntEx() < this.fireChargesBeforeUse) {
               this.performActionSafe();
            } else if (this.computeInt() == -1) {
               this.performActionSafe();
               this.performActionDirect();
            }
         }

         if (this.rotation != null) {
            RotationManager.requestRotationWithMoveFix(this.rotation, 0, Southside.INSTANCE.getModuleManager().getModule(StrafeFix.class).getMoveFixMode());
         }
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (mc2.player != null) {
         synchronized (this.packetLock) {
            this.captureReady = true;
         }

         if (this.checkInitialMovement) {
            this.startedWithoutMovement = !MovementUtils.isValid();
            this.checkInitialMovement = false;
         }

         long handle = mc2.getWindow().getHandle();
         boolean flag = GLFW.glfwGetMouseButton(handle, 3) == 1;
         if (flag && !this.mouse4WasPressed && !this.usingFireCharge && this.fireChargeStage == LongJump.Mode.IDLE) {
            int n = this.computeInt();
            if (n == -1) {
               this.setEnabled(false);
               return;
            }

            this.previousHotbarSlot = mc2.player.getInventory().selectedSlot;
            mc2.player.getInventory().selectedSlot = n;
            this.fireChargeStage = LongJump.Mode.AIMING;
         }

         this.mouse4WasPressed = flag;
         boolean flag2 = GLFW.glfwGetMouseButton(handle, 4) == 1;
         if (flag2 && !this.mouse5WasPressed) {
            this.performActionAlt();
         }

         this.mouse5WasPressed = flag2;
      }
   }

   private void performActionAlt() {
      ArrayList<Packet<?>> list = new ArrayList<>();
      boolean flag;
      synchronized (this.packetLock) {
         if (this.intercepting && this.releasedCount < this.knockbackMagnitudes.size()) {
            LongJump.Entry entry;
            while ((entry = this.bufferedPackets.pollFirst()) != null) {
               list.add(entry.getPacket());
               if (entry.isPlayerVelocity()) {
                  break;
               }
            }

            this.releasedCount++;
            flag = this.releasedCount >= this.knockbackMagnitudes.size();
            if (flag) {
               this.intercepting = false;
               this.captureReady = false;
            }
         } else {
            flag = !this.intercepting;
         }
      }

      list.forEach(PacketUtils::sendOrApplyDirect);
      if (flag) {
         this.setEnabled(false);
      }
   }

   private void performActionImpl() {
      ArrayList<Packet<?>> list = new ArrayList<>();
      synchronized (this.packetLock) {
         this.intercepting = false;
         this.captureReady = false;

         LongJump.Entry entry;
         while ((entry = this.bufferedPackets.pollFirst()) != null) {
            list.add(entry.getPacket());
         }
      }

      list.forEach(PacketUtils::sendOrApplyDirect);
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      nvg.performAction(true, l -> this.fromHsb(nvg, l));
   }

   private void fromHsb(NanoVGHelper nvg, long l) {
      double d;
      int n;
      synchronized (this.packetLock) {
         d = this.knockbackMagnitudes.stream().skip(this.releasedCount).mapToDouble(Double::doubleValue).sum() / 1000.0;
         n = this.knockbackMagnitudes.size();
      }

      String formatted = String.format("Knockback strength: %.2f (%d)", d, n);
      double dist = Math.sqrt(Math.min(d / 50.0, 1.0));
      int n2 = Color.HSBtoRGB((float)((1.0 - dist) * 120.0 / 360.0), 1.0F, 1.0F);
      float textWidth = nvg.getTextWidth(l, formatted, 10.0F, OneConfigFonts.WQY_MICROHEI);
      float f = (mc2.getWindow().getScaledWidth() - textWidth) / 2.0F;
      float f2 = mc2.getWindow().getScaledHeight() / 2.0F + 50.0F;
      nvg.draw(l, formatted, f, f2, n2, 10.0F, OneConfigFonts.WQY_MICROHEI);
   }

   private int computeInt() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (!stack.isEmpty() && stack.isOf(Items.FIRE_CHARGE)) {
            return i;
         }
      }

      return -1;
   }

   private int computeIntEx() {
      int n = 0;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (stack.isOf(Items.FIRE_CHARGE)) {
            n += stack.getCount();
         }
      }

      return n;
   }

   private void performActionDirect() {
      if (this.previousHotbarSlot != -1 && mc2.player != null) {
         mc2.player.getInventory().selectedSlot = this.previousHotbarSlot;
      }

      this.previousHotbarSlot = -1;
   }

   private void performActionSafe() {
      mc2.options.useKey.setPressed(false);
      mc2.options.jumpKey.setPressed(false);
      this.rotation = null;
      this.usingFireCharge = false;
   }

   private void performActionCustom() {
      synchronized (this.packetLock) {
         this.bufferedPackets.clear();
         this.knockbackMagnitudes.clear();
         this.intercepting = false;
         this.captureReady = false;
         this.releasedCount = 0;
      }

      this.rotation = null;
      this.fireChargeStage = LongJump.Mode.IDLE;
      this.previousHotbarSlot = -1;
      this.usingFireCharge = false;
      this.checkInitialMovement = true;
      this.startedWithoutMovement = false;
      this.fireChargesBeforeUse = 0;
      this.mouse4WasPressed = false;
      this.mouse5WasPressed = false;
   }

   private record Entry(Packet<?> packet, boolean playerVelocity) {
      public Packet<?> getPacket() {
         return this.packet;
      }

      public boolean isPlayerVelocity() {
         return this.playerVelocity;
      }
   }

   private enum Mode {
      IDLE,
      AIMING,
      USING;


      // $VF: synthetic method
      private static LongJump.Mode[] createValues() {
         return new LongJump.Mode[]{IDLE, AIMING, USING};
      }


   }
}
