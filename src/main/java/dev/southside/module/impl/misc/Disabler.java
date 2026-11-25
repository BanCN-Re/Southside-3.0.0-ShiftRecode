package dev.southside.module.impl.misc;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.movement.GrimFly;
import dev.southside.module.impl.player.NoFall;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.SpeedThresholdCheck;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.IText;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.math.RandomUtils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Disabler extends Module {
   @SerializedName("badPacketsA")
   public final BooleanValue badPacketsA = getBooleanValue("Grim", "Bad Packets A", "Filter duplicate selected-slot packets", true);
   @SerializedName("badPacketsB")
   public final BooleanValue badPacketsB = getBooleanValue("Grim", "Bad Packets B", "Filter interactions with your own entity", false);
   @SerializedName("badPacketsD")
   public final BooleanValue badPacketsD = getBooleanValue("Grim", "Bad Packets D", "Filter pitch outside the valid range", false);
   @SerializedName("badPacketsF")
   public final BooleanValue badPacketsF = getBooleanValue("Grim", "Bad Packets F", "Filter repeated sprint-state commands", false);
   @SerializedName("badPacketsP")
   public final BooleanValue badPacketsP = getBooleanValue("Grim", "Bad Packets P", "Filter invalid inventory click buttons", false);
   @SerializedName("badPacketsW")
   public final BooleanValue badPacketsW = getBooleanValue("Grim", "Bad Packets W", "Filter interactions with missing entities", false);
   @SerializedName("badPacketsX")
   public final BooleanValue badPacketsX = getBooleanValue("Grim", "Bad Packets X", "Filter additional sprint commands", false);
   @SerializedName("badPacketsY")
   public final BooleanValue badPacketsY = getBooleanValue("Grim", "Bad Packets Y", "Filter invalid slot-state updates", false);
   @SerializedName("inventory")
   public final BooleanValue inventory = getBooleanValue("Grim", "Inventory Sprint", "Suspend sprinting around inventory actions", false);
   @SerializedName("aimDuplicateLook")
   public final BooleanValue aimDuplicateLook = getBooleanValue("Grim", "Duplicate Look", "Adjust duplicate look packets", false);
   public static final BooleanValue BOOLEAN_VALUE = getBooleanValue("Grim", "Grim Post", "Delay incoming packets until the movement update", false);
   @SerializedName("showLogging")
   public final BooleanValue showLogging = getBooleanValue("Grim", "Show Logging", "Show adjustment messages for both Grim and ACA", true);
   @SerializedName("loggingLag")
   public final BooleanValue loggingLag = getBooleanValue("Grim", "Log Corrections", "Log server position and velocity corrections", false);
   @SerializedName("fastSwitch")
   public final BooleanValue fastSwitch = getBooleanValue("ACA", "Fast Switch", "Send intermediate hotbar selections", false);
   @SerializedName("aimStep")
   public final BooleanValue aimStep = getBooleanValue("ACA", "Aim Step", "Adjust isolated yaw or pitch steps", true);
   @SerializedName("equalRotation")
   public final BooleanValue equalRotation = getBooleanValue("ACA", "Equal Rotation", "Adjust identical consecutive rotations", false);
   @SerializedName("perfectRotation")
   public final BooleanValue perfectRotation = getBooleanValue("ACA", "Perfect Rotation", "Adjust exact 0.1 or 0.25 degree rotation multiples", true);
   private static final List<Packet<?>> storedPackets = new CopyOnWriteArrayList<>();
   private static boolean activeValue;
   private static boolean activeData;
   private static boolean activeRef;
   private static float factor;
   @SerializedName("sprintCommandSeen")
   private boolean sprintCommandSeen;
   @SerializedName("previousSelectedSlot")
   private int previousSelectedSlot;
   @SerializedName("lastSelectedSlot")
   private int lastSelectedSlot;
   @SerializedName("lastYaw")
   private float lastYaw;
   @SerializedName("lastPitch")
   private float lastPitch;
   @SerializedName("perfectRotationInitialized")
   private boolean perfectRotationInitialized;
   @SerializedName("teleportRotationPending")
   private boolean teleportRotationPending;

   public Disabler() {
      super(Category.Misc, "Disabler", "ABCDEFG");
   }

   private static BooleanValue getBooleanValue(final String text, String text2, String text3, boolean flag) {
      return new BooleanValue(text2, text3, flag) {
         @Override
         public String getA() {
            return text;
         }
      };
   }

   @Override
   public void loadFromJson(JsonObject json) {
      JsonObject asJsonObject = json.getAsJsonObject(this.modData.name);
      if (asJsonObject != null) {
         JsonObject json2 = asJsonObject.deepCopy();
         performAction(json2, this.badPacketsA, "BadPackets A");
         performAction(json2, this.badPacketsB, "BadPackets B");
         performAction(json2, this.badPacketsD, "BadPackets D");
         performAction(json2, this.badPacketsF, "BadPackets F");
         performAction(json2, this.badPacketsP, "BadPackets P");
         performAction(json2, this.badPacketsW, "BadPackets W");
         performAction(json2, this.badPacketsX, "BadPackets X");
         performAction(json2, this.badPacketsY, "BadPackets Y");
         performAction(json2, this.inventory, "Inventory");
         performAction(json2, this.aimDuplicateLook, "AimDuplicateLook");
         performAction(json2, BOOLEAN_VALUE, "GrimPost");
         performAction(json2, this.showLogging, "ShowLogging");
         performAction(json2, this.loggingLag, "LoggingLag");
         performAction(json2, this.fastSwitch, "FastSwitch");
         performAction(json2, this.aimStep, "AimStep");
         performAction(json2, this.equalRotation, "equalRotation");
         JsonObject json3 = new JsonObject();
         json3.add(this.modData.name, json2);
         super.loadFromJson(json3);
      }
   }

   private static void performAction(JsonObject json, BooleanValue booleanValue, String text) {
      if (!json.has(booleanValue.getLabel()) && json.has(text)) {
         json.add(booleanValue.getLabel(), json.get(text));
      }
   }

   @Override
   public void onEnable() {
      this.perfectRotationInitialized = false;
      this.teleportRotationPending = false;
   }

   public static float normalizeYawDiff(float yaw, float pitch) {
      while (yaw - pitch > 180.0F) {
         yaw -= 360.0F;
      }

      while (yaw - pitch < -180.0F) {
         yaw += 360.0F;
      }

      return yaw;
   }

   public static void postDelayPackets(boolean flag) {
      activeData = flag;
   }

   public static boolean isValidEx() {
      ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
      NoFall noFall = moduleManager.getModule(NoFall.class);
      GrimFly grimFly = moduleManager.getModule(GrimFly.class);
      Disabler disabler = moduleManager.getModule(Disabler.class);
      boolean flag = grimFly.isEnabled() && grimFly.tickCounter % 2 == 0 && (grimFly.tickCounter >= 20 || grimFly.tickCounter % 4 == 0);
      boolean flag2 = disabler.isEnabled() && (Boolean)BOOLEAN_VALUE.getValue() && !flag && (!noFall.isEnabled() || !noFall.isValidEx());
      if (activeValue && !flag2) {
         activeValue = false;
         mc2.execute(Disabler::performActionAlt);
      }

      activeValue = flag2;
      return flag2;
   }

   public static boolean isValidAlt() {
      return mc2.player != null && !(mc2.currentScreen instanceof DownloadingTerrainScreen) && !(mc2.currentScreen instanceof DeathScreen);
   }

   public static void postDelayPackets(Packet<?> packet) {
      storedPackets.add(packet);
   }

   public static void performActionAlt() {
      for (Packet packet : storedPackets) {
         if (mc2.getNetworkHandler() != null && !ServerPacketManager.shouldHandlePacket(packet)) {
            DirectionEvent directionEvent = new DirectionEvent(TransferOrigin.RECEIVE, packet, mc2.getNetworkHandler().getConnection());
            Southside.INSTANCE.getEventBus().postCancellable(directionEvent);
            if (!directionEvent.isCancelled()) {
               try {
                  PacketUtils.sendOrApplyPacket(packet);
               } catch (IllegalStateException e) {
                  Southside.LOGGER.warn("Failed to handle packet (likely team-related): {}", e.getMessage());
               }
            }
         }
      }

      storedPackets.clear();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      activeRef = false;
      this.lastSelectedSlot = -1;
      this.perfectRotationInitialized = false;
      this.teleportRotationPending = false;
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (mc2.player != null) {
         Packet packet = directionEvent.getPacket();
         if (directionEvent.getDirection() != TransferOrigin.SEND) {
            if (packet instanceof PlayerPositionLookS2CPacket playerPositionLookS2CPacket && (Boolean)this.loggingLag.getValue()) {
               postDelayPackets(playerPositionLookS2CPacket.change());
            }
         } else {
            if (packet instanceof TeleportConfirmC2SPacket) {
               this.teleportRotationPending = true;
            }

            if (packet instanceof PlayerMoveC2SPacket playerMoveC2SPacket && !activeData) {
               if (!playerMoveC2SPacket.changesLook()) {
                  return;
               }

               this.performAction(playerMoveC2SPacket);
               this.performAction(playerMoveC2SPacket, directionEvent);
            }

            if (packet instanceof ClickSlotC2SPacket clickSlotC2SPacket && (Boolean)this.badPacketsP.getValue() && !normalizeYawDiff(clickSlotC2SPacket.getActionType(), clickSlotC2SPacket.getButton())) {
               this.performAction(directionEvent, "§7BadPacketP detected!");
            }

            if (packet instanceof PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket) {
               this.performAction(playerInteractEntityC2SPacket, directionEvent);
            }

            if (packet instanceof SlotChangedStateC2SPacket slotChangedStateC2SPacket && (Boolean)this.badPacketsY.getValue() && (slotChangedStateC2SPacket.slotId() < 0 || slotChangedStateC2SPacket.slotId() > 8)) {
               this.performAction(directionEvent, "§7BadPacketsY detected!");
            }

            if (packet instanceof UpdateSelectedSlotC2SPacket updateSelectedSlotC2SPacket) {
               this.performAction(updateSelectedSlotC2SPacket, directionEvent);
            }

            if (packet instanceof UpdateSelectedSlotS2CPacket updateSelectedSlotS2CPacket && ((Boolean)this.badPacketsA.getValue() || (Boolean)this.fastSwitch.getValue())) {
               this.lastSelectedSlot = updateSelectedSlotS2CPacket.slot();
            }

            if ((Boolean)this.inventory.getValue() && this.shouldHandlePacket(packet)) {
               this.handlePacket(packet, directionEvent);
            }

            if (packet instanceof ClientCommandC2SPacket clientCommandC2SPacket) {
               this.performAction(clientCommandC2SPacket.getMode(), directionEvent);
            }
         }
      }
   }

   private void performAction(PlayerMoveC2SPacket playerMoveC2SPacket, DirectionEvent directionEvent) {
      if ((Boolean)this.badPacketsD.getValue() && (playerMoveC2SPacket.pitch > 90.0F || playerMoveC2SPacket.pitch < -90.0F)) {
         this.applyText("§7BadPacketD detected!");
         directionEvent.cancel();
      }

      if ((Boolean)this.aimStep.getValue()) {
         double d = (double)factor - playerMoveC2SPacket.yaw;
         double d2 = Math.abs(((d + 180.0) % 360.0 + 360.0) % 360.0 - 180.0);
         double d3 = Math.abs(this.lastPitch - playerMoveC2SPacket.pitch);
         if (SpeedThresholdCheck.isValid(d2, d3)) {
            playerMoveC2SPacket.yaw += 1.0E-4F;
            this.applyText("§7AimStep detected!");
         }

         if (SpeedThresholdCheck.isValid(d3, d2)) {
            postDelayPackets(playerMoveC2SPacket);
         }
      }

      if ((Boolean)this.equalRotation.getValue() && this.lastYaw - playerMoveC2SPacket.yaw == 0.0F && this.lastPitch - playerMoveC2SPacket.pitch == 0.0F) {
         playerMoveC2SPacket.yaw += 0.001F;
         postDelayPackets(playerMoveC2SPacket);
         this.applyText("§7equalRotation detected!");
      }

      if ((Boolean)this.aimDuplicateLook.getValue() && playerMoveC2SPacket.yaw == this.lastYaw && playerMoveC2SPacket.pitch == this.lastPitch && playerMoveC2SPacket.yaw != 0.0F) {
         playerMoveC2SPacket.yaw = playerMoveC2SPacket.yaw + RandomUtils.computeFloat(1.0E-4F, 0.001F);
      }

      if (playerMoveC2SPacket.changeLook) {
         this.lastPitch = playerMoveC2SPacket.pitch;
         factor = playerMoveC2SPacket.yaw;
      }

      this.lastYaw = playerMoveC2SPacket.yaw;
   }

   private void performAction(PlayerMoveC2SPacket playerMoveC2SPacket) {
      boolean teleportRotationPending2 = this.teleportRotationPending;
      this.teleportRotationPending = false;
      if (!this.perfectRotationInitialized) {
         this.perfectRotationInitialized = true;
      } else if ((Boolean)this.perfectRotation.getValue()
         && !teleportRotationPending2
         && Float.isFinite(playerMoveC2SPacket.yaw)
         && Float.isFinite(playerMoveC2SPacket.pitch)
         && Float.isFinite(factor)
         && Float.isFinite(this.lastPitch)) {
         float f = computeFloat(playerMoveC2SPacket.yaw, factor, true);
         float f2 = computeFloat(playerMoveC2SPacket.pitch, this.lastPitch, false);
         if (f != playerMoveC2SPacket.yaw || f2 != playerMoveC2SPacket.pitch) {
            playerMoveC2SPacket.yaw = f;
            playerMoveC2SPacket.pitch = f2;
            this.applyText("Adjusted ACA Perfect Rotation");
         }
      }
   }

   private static float computeFloat(float f, float f2, boolean flag) {
      if (flag || !(f < -90.0F) && !(f > 90.0F)) {
         float f3 = flag ? MathHelper.wrapDegrees(f) : f;
         float f4 = flag ? MathHelper.wrapDegrees(f2) : f2;
         double d = (double)f3 - f4;
         double d2 = Math.abs(flag ? MathHelper.wrapDegrees(d) : d);
         if (!(d2 <= 1.0E-10) && (normalizeYawDiff(d2, 0.1) || normalizeYawDiff(d2, 0.25))) {
            float f5 = Math.max(1.0E-4F, Math.ulp(f));
            if (f5 >= 0.25F) {
               return f;
            }

            float f6 = f + f5;
            if (!flag && f6 > 90.0F) {
               f6 = f - f5;
            }

            return f6;
         } else {
            return f;
         }
      } else {
         return f;
      }
   }

   private static boolean normalizeYawDiff(double yaw, double pitch) {
      double d = yaw / pitch;
      return Math.abs(d - Math.rint(d)) <= 1.0E-10;
   }

   private static void postDelayPackets(PlayerMoveC2SPacket playerMoveC2SPacket) {
      playerMoveC2SPacket.pitch = playerMoveC2SPacket.pitch + (playerMoveC2SPacket.pitch == 90.0F ? -1.0E-4F : 1.0E-4F);
   }

   private static boolean normalizeYawDiff(SlotActionType slotActionType, int n) {
      return switch (slotActionType) {
         case PICKUP, QUICK_MOVE, CLONE -> n >= 0 && n <= 2;
         case SWAP -> n >= 0 && n <= 8 || n == 40;
         case THROW -> n == 0 || n == 1;
         case QUICK_CRAFT -> n >= 0 && n <= 10 && n != 3 && n != 7;
         case PICKUP_ALL -> n == 0;
         default -> throw new MatchException(null, null);
      };
   }

   private void performAction(PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket, DirectionEvent directionEvent) {
      if ((Boolean)this.badPacketsB.getValue() && playerInteractEntityC2SPacket.entityId == mc2.player.getId()) {
         this.performAction(directionEvent, "§7BadPacketB detected!");
      }

      if ((Boolean)this.badPacketsW.getValue() && mc2.world != null && mc2.world.getEntityById(playerInteractEntityC2SPacket.entityId) == null) {
         this.performAction(directionEvent, "§7BadPacketsW detected!");
      }
   }

   private void performAction(UpdateSelectedSlotC2SPacket updateSelectedSlotC2SPacket, DirectionEvent directionEvent) {
      int selectedSlot = updateSelectedSlotC2SPacket.getSelectedSlot();
      if ((Boolean)this.badPacketsA.getValue() || (Boolean)this.fastSwitch.getValue()) {
         if (this.lastSelectedSlot == selectedSlot && !(Boolean)this.fastSwitch.getValue()) {
            directionEvent.cancel();
         }

         this.lastSelectedSlot = selectedSlot;
      }

      if ((Boolean)this.fastSwitch.getValue()) {
         this.lastSelectedSlot = this.previousSelectedSlot;
         this.previousSelectedSlot = selectedSlot;
         if (this.lastSelectedSlot != selectedSlot && (this.lastSelectedSlot != 0 || selectedSlot != 8) && (this.lastSelectedSlot != 8 || selectedSlot != 0)) {
            directionEvent.cancel();
            int n = Integer.compare(selectedSlot, this.lastSelectedSlot);

            for (int i = this.lastSelectedSlot + n; i != selectedSlot; i += n) {
               PacketUtils.sendPacketDirect(new UpdateSelectedSlotC2SPacket(i));
            }

            PacketUtils.sendPacketDirect(updateSelectedSlotC2SPacket);
         }
      }
   }

   private boolean shouldHandlePacket(Packet<?> packet) {
      int syncId = mc2.player.playerScreenHandler.syncId;
      return packet instanceof ClickSlotC2SPacket clickSlotC2SPacket && clickSlotC2SPacket.getSyncId() == syncId
         || packet instanceof CloseHandledScreenC2SPacket closeHandledScreenC2SPacket && closeHandledScreenC2SPacket.getSyncId() == syncId;
   }

   private void handlePacket(Packet<?> packet, DirectionEvent directionEvent) {
      directionEvent.cancel();
      if (mc2.player.lastSprinting) {
         PacketUtils.sendPacket(new ClientCommandC2SPacket(mc2.player, Mode.STOP_SPRINTING));
      }

      PacketUtils.sendPacketDirect(packet);
      if (mc2.player.lastSprinting && mc2.player.isSprinting()) {
         PacketUtils.sendPacket(new ClientCommandC2SPacket(mc2.player, Mode.START_SPRINTING));
      }
   }

   private void performAction(Mode mode, DirectionEvent directionEvent) {
      if (mode == Mode.START_SPRINTING || mode == Mode.STOP_SPRINTING) {
         boolean flag = mode == Mode.START_SPRINTING;
         if ((Boolean)this.badPacketsF.getValue()) {
            if (activeRef == flag) {
               this.performAction(directionEvent, flag ? "§7BadPacketF(BadSprintingPacket) detected!" : "§7BadPacketF(BadStopSprintingPacket) detected!");
               return;
            }

            activeRef = flag;
         }

         if ((Boolean)this.badPacketsX.getValue()) {
            if (this.sprintCommandSeen) {
               this.performAction(directionEvent, "§7BadPacketX detected!");
            }

            this.sprintCommandSeen = true;
         }
      }
   }

   private void performAction(DirectionEvent directionEvent, String text) {
      if ((Boolean)this.showLogging.getValue()) {
         this.applyTextCustom(text);
      }

      directionEvent.cancel();
   }

   private void applyText(String text) {
      if ((Boolean)this.showLogging.getValue()) {
         this.applyTextImpl(text);
      }
   }

   private static void postDelayPackets(PlayerPosition playerPosition) {
      short s = 26644;
      Vec3d vec = playerPosition.position();
      short s2 = -24446;
      Vec3d vec2 = playerPosition.deltaMovement();
      IText.applyText(
         String.format(
            "§fPos §7%.1f %.1f %.1f §7| §fVel §7%.2f %.2f %.2f §7| §fYaw §7%.1f §fPitch §7%.1f",
            vec.x,
            vec.y,
            vec.z,
            vec2.x,
            vec2.y,
            vec2.z,
            playerPosition.yaw(),
            playerPosition.pitch()
         )
      );
   }
}
