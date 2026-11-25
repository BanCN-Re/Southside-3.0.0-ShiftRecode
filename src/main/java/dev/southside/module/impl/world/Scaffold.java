package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.CameraEvent;
import dev.southside.event.events.SlowDownEvent;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.event.events.ForwardEvent;
import dev.southside.event.events.FovMultiplierEvent;
import dev.southside.event.events.HandEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.event.events.PreTickEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ColorValue;
import dev.southside.utils.InteractionStateFlags;
import dev.southside.utils.MathRandomUtils;
import dev.southside.utils.BlinkUtils;
import dev.southside.utils.ItemScoreUtils;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.SlotIndexMapper;
import dev.southside.utils.PacketUtils;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.ScissorHelper;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.IText;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.ModeValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.MovementFreezeManager;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.BlockLookUtils;
import dev.southside.utils.ScissorRect;
import dev.southside.utils.PlayerMoveSimulator;
import dev.southside.utils.Rotation;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.PosRecord;
import dev.southside.utils.rotation.MoveFixMode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Telly", "Telly", "Snap", "Normal");
   @SerializedName("alwaysUpdateRot")
   public final BooleanValue alwaysUpdateRot = new BooleanValue("Always Update Rotation", false);
   @SerializedName("placeTick")
   public final NumberValue placeTick = new NumberValue("PlaceTick", 1, 1, 5, 1);
   @SerializedName("rotTick")
   public final NumberValue rotTick = new NumberValue("RotationTick", 1, 1, 5, 1);
   @SerializedName("spoofItem")
   public final BooleanValue spoofItem = new BooleanValue("Spoof Item", true);
   @SerializedName("noSwing")
   public final BooleanValue noSwing = new BooleanValue("No Swing", false);
   @SerializedName("eagle")
   public final BooleanValue eagle = new BooleanValue("Eagle", false);
   @SerializedName("snap")
   public final BooleanValue snap = new BooleanValue("Snap", false);
   @SerializedName("noUptelly")
   public final BooleanValue noUptelly = new BooleanValue("No Uptelly", true);
   @SerializedName("godBridge")
   public final BooleanValue godBridge = new BooleanValue("GodBridge", false);
   @SerializedName("smoothed")
   public final BooleanValue smoothed = new BooleanValue("Heypixel UpTelly", true);
   @SerializedName("safeMode")
   public final BooleanValue safeMode = new BooleanValue("Safe Mode", false);
   @SerializedName("testOnGround")
   public final BooleanValue testOnGround = new BooleanValue("Test OnGround", false);
   @SerializedName("fixRotation")
   public final BooleanValue fixRotation = new BooleanValue("Fix Rotation", true);
   @SerializedName("randomSlow")
   public final BooleanValue randomSlow = new BooleanValue("SlowUpTelly", false);
   @SerializedName("blockFly")
   public final BooleanValue blockFly = new BooleanValue("Block Fly", false);
   @SerializedName("abuseRotation")
   public final BooleanValue abuseRotation = new BooleanValue("Abuse Rotation", false);
   @SerializedName("blockSlotMode")
   public final ModeValue blockSlotMode = new ModeValue("Block Slot Mode", "Farthest", "Farthest", "Most Blocks");
   @SerializedName("jumpMode")
   public final ModeValue jumpMode = new ModeValue("Jump Mode", "Normal", "Parkour", "Normal", "None");
   @SerializedName("safeDistance")
   public final NumberValue safeDistance = new NumberValue("Clutch Safe Distance", 4.5, 1.0, 5.0, 0.25);
   @SerializedName("tellyEagleTick")
   public final NumberValue tellyEagleTick = new NumberValue("EagleTick", 1, 1, 5, 1);
   @SerializedName("keepEagleSneakTick")
   public final NumberValue keepEagleSneakTick = new NumberValue("KeepEagleTick", 1, 1, 5, 1);
   @SerializedName("dbgV")
   public final BooleanValue dbgV = new BooleanValue("Debug", false);
   @SerializedName("keepFoV")
   public final BooleanValue keepFoV = new BooleanValue("Keep FoV", true);
   @SerializedName("fovValue")
   public final NumberValue fovValue = new NumberValue("Fov", 1.1, 1.0, 2.1, 0.05);
   @SerializedName("mark")
   public final BooleanValue mark = new BooleanValue("Mark", true);
   @SerializedName("duplicateRotPlace")
   private final BooleanValue duplicateRotPlace = new BooleanValue("DuplicateRotPlace", true);
   @SerializedName("interactItem")
   private final BooleanValue interactItem = new BooleanValue("Interact Item Before Place", false);
   @SerializedName("markSideColor")
   private final ColorValue markSideColor = new ColorValue("Mark Side Color", true, new OneColor(255, 255, 255, 70));
   @SerializedName("markLineColor")
   private final ColorValue markLineColor = new ColorValue("Mark Line Color", true, new OneColor(255, 255, 255, 150));
   @SerializedName("blockCount")
   public final BooleanValue blockCount = new BooleanValue("BlockCount", "BlockCount", true);
   @SerializedName("blockCountStyle")
   public final ModeValue blockCountStyle = new ModeValue("BlockCount Style", "Retro", "Retro", "Old");
   @SerializedName("blockCountOffset")
   public final NumberValue blockCountOffset = new NumberValue("BlockCount Y Offset", 0, 0, 200, 1);
   public static final List<Block> invalidBlocks = Arrays.asList(
      Blocks.ENCHANTING_TABLE,
      Blocks.OAK_SIGN,
      Blocks.CHEST,
      Blocks.ENDER_CHEST,
      Blocks.TRAPPED_CHEST,
      Blocks.ANVIL,
      Blocks.SAND,
      Blocks.COBWEB,
      Blocks.TORCH,
      Blocks.CRAFTING_TABLE,
      Blocks.FURNACE,
      Blocks.WATER_CAULDRON,
      Blocks.DISPENSER,
      Blocks.STONE_PRESSURE_PLATE,
      Blocks.BAMBOO_PRESSURE_PLATE,
      Blocks.NOTE_BLOCK,
      Blocks.DROPPER,
      Blocks.TNT,
      Blocks.REDSTONE_TORCH,
      Blocks.DAYLIGHT_DETECTOR,
      Blocks.OAK_SIGN,
      Blocks.BIRCH_SIGN,
      Blocks.SPRUCE_SIGN,
      Blocks.JUNGLE_SIGN,
      Blocks.ACACIA_SIGN,
      Blocks.DARK_OAK_SIGN,
      Blocks.MANGROVE_SIGN,
      Blocks.CHERRY_SIGN,
      Blocks.BAMBOO_SIGN,
      Blocks.CRIMSON_SIGN,
      Blocks.WARPED_SIGN,
      Blocks.OAK_HANGING_SIGN,
      Blocks.BIRCH_HANGING_SIGN,
      Blocks.SPRUCE_HANGING_SIGN,
      Blocks.JUNGLE_HANGING_SIGN,
      Blocks.ACACIA_HANGING_SIGN,
      Blocks.DARK_OAK_HANGING_SIGN,
      Blocks.MANGROVE_HANGING_SIGN,
      Blocks.CHERRY_HANGING_SIGN,
      Blocks.BAMBOO_HANGING_SIGN,
      Blocks.CRIMSON_HANGING_SIGN,
      Blocks.WARPED_HANGING_SIGN
   );
   @SerializedName("slot")
   private Scaffold.Entry slot;
   @SerializedName("blockSlot")
   private Scaffold.Entry blockSlot;
   @SerializedName("count")
   public int count;
   @SerializedName("lastCount")
   public int lastCount = 0;
   @SerializedName("startHotbarCount")
   private int startHotbarCount = 1;
   @SerializedName("canPlace")
   private boolean canPlace;
   @SerializedName("blockData")
   private PosRecord blockData;
   @SerializedName("lastBlockData")
   private PosRecord lastBlockData;
   @SerializedName("rotateCount")
   private int rotateCount = 0;
   @SerializedName("posY")
   private double posY;
   @SerializedName("lastPlacePosition")
   private BlockPos lastPlacePosition = null;
   @SerializedName("tellyJumpTicks")
   private int tellyJumpTicks;
   @SerializedName("waitingForEagleSneak")
   private boolean waitingForEagleSneak;
   @SerializedName("lastRotation")
   private Rotation lastRotation;
   @SerializedName("rot")
   private Rotation rot;
   @SerializedName("boxExpand")
   private double boxExpand = 0.15;
   @SerializedName("oldSlot")
   public int oldSlot;
   @SerializedName("placeCount")
   public int placeCount = 0;
   @SerializedName("ups")
   public int ups = 0;

   public Scaffold() {
      super(Category.World, "Scaffold", "自动打撸");
   }

   @Override
   public void onEnable() {
      this.placeCount = 0;
      this.ups = 0;
      if (mc2.player != null) {
         if ((Boolean)this.blockFly.getValue()) {
            ServerPacketManager.setActive(true);
         }

         this.boxExpand = RandomUtils.computeDouble(0.1, 0.2);
         this.lastRotation = new Rotation(mc2.player.getYaw(), mc2.player.getPitch());
         this.slot = new Scaffold.Entry(mc2.player.getInventory().selectedSlot, Hand.MAIN_HAND);
         this.oldSlot = mc2.player.getInventory().selectedSlot;
         this.blockSlot = null;
         this.startHotbarCount = Math.max(1, this.computeIntImpl());
         this.blockData = null;
         this.canPlace = true;
         this.lastPlacePosition = null;
         this.tellyJumpTicks = 0;
         this.waitingForEagleSneak = false;
         this.rot = null;
         super.onEnable();
      }
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.jumpMode.getLabel(), () -> !this.mode.matches("Telly"));
      this.hideIf(this.placeTick.getLabel(), () -> !this.mode.matches("Telly"));
      this.hideIf(this.smoothed.getLabel(), () -> !this.mode.matches("Telly"));
      this.hideIf(this.safeMode.getLabel(), () -> !(Boolean)this.smoothed.getValue());
      this.hideIf(this.godBridge.getLabel(), () -> !this.mode.matches("Normal"));
      this.hideIf(this.tellyEagleTick.getLabel(), () -> !(Boolean)this.eagle.getValue());
      this.hideIf(this.keepEagleSneakTick.getLabel(), () -> !(Boolean)this.eagle.getValue());
      this.hideIf(this.fovValue.getLabel(), () -> !(Boolean)this.keepFoV.getValue());
      this.hideIf(this.markLineColor.getLabel(), () -> !(Boolean)this.mark.getValue());
      this.hideIf(this.markSideColor.getLabel(), () -> !(Boolean)this.mark.getValue());
      this.hideIf(this.blockCountStyle.getLabel(), () -> !(Boolean)this.blockCount.getValue());
      this.hideIf(this.blockCountOffset.getLabel(), () -> !(Boolean)this.blockCount.getValue());
   }

   @Override
   public void onDisable() {
      if (mc2.player != null) {
         mc2.player.getInventory().selectedSlot = this.slot.getSlot();
         mc2.player.getInventory().selectedSlot = this.oldSlot;
         mc2.options.sneakKey.setPressed(false);
         MovementFreezeManager.performActionEx();
         super.onDisable();
      }
   }

   @EventTarget
   public void onFovMultiplier(FovMultiplierEvent fovMultiplierEvent) {
      if ((Boolean)this.keepFoV.getValue() && MovementUtils.isValid()) {
         fovMultiplierEvent.setFovMultiplier(((Number)this.fovValue.getValue()).floatValue() + MovementUtils.computeInt() * 0.13F);
      }
   }

   @EventTarget
   public void onCamera(CameraEvent cameraEvent) {
      if (this.lastPlacePosition != null && (Boolean)this.mark.getValue()) {
         Box box = new Box(this.lastPlacePosition);
         BoxMeshRenderer.renderBox(box, ((OneColor)this.markLineColor.getValue()).blendColor());
         BoxMeshRenderer.renderBoxEx(box, ((OneColor)this.markSideColor.getValue()).blendColor());
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (this.isEnabled() && mc2.player != null && (Boolean)this.blockCount.getValue()) {
         int n = Math.max(0, this.computeIntImpl());
         if (n > this.startHotbarCount) {
            this.startHotbarCount = n;
         }

         float clamped = MathHelper.clamp((float)n / this.startHotbarCount, 0.0F, 1.0F);
         float f = mc2.getWindow().getScaledWidth() / 2.0F;
         float f2 = mc2.getWindow().getScaledHeight() / 2.0F;
         float f3 = f2 + 15.0F + ((Number)this.blockCountOffset.getValue()).floatValue();
         if (this.blockCountStyle.matches("Old")) {
            String text = n + " Blocks";
            int n2 = Math.round(f - mc2.textRenderer.getWidth(text) / 2.0F);
            drawContextEvent.getDrawContext().drawText(mc2.textRenderer, text, n2, Math.round(f3), this.computeInt(n), true);
            this.lastCount = n;
            this.count = n;
         } else {
            if (this.blockCountStyle.matches("Retro")) {
               ItemStack itemStack = this.getItemStack();
               short s = 23961;
               float f4 = 1.0F;
               short s2 = -23723;
               float f5 = 26.0F * f4;
               float f6 = 5.0F * f4;
               float f7 = 5.0F * f4;
               float f8 = 18.0F * f4;
               float f9 = 4.0F * f4;
               float f10 = 10.0F * f4;
               float f11 = 10.0F * f4;
               float f12 = 0.0F * f4;
               NanoVGHelper nvg = NanoVGHelper.INSTANCE;
               ScissorHelper scissor = ScissorHelper.INSTANCE;
               float[] floats = new float[]{0.0F};
               String text2 = "Blocks";
               String text3 = String.valueOf(n);
               float f13 = 3.0F * f4;
               nvg.performAction(true, l -> {
                  float textWidth = nvg.getTextWidth(l, text2, f11, OneConfigFonts.REGULAR);
                  float textWidth2 = nvg.getTextWidth(l, text3, f10, OneConfigFonts.SEMIBOLD);
                  floats[0] = textWidth + f13 + textWidth2;
                  float f14 = f9 + f8 + f7 + floats[0] + f7;
                  float f15 = f - f14 / 2.0F;
                  int rgb = new Color(0, 0, 0, 90).getRGB();
                  int rgb2 = new Color(255, 255, 255, 90).getRGB();
                  int n3 = this.computeInt(n);
                  int n4 = this.computeIntEx(n);
                  textWidth = nvg.getTextWidth(l, text2, f11, OneConfigFonts.REGULAR);
                  float f16 = f15 + f9 + f8 + f7;
                  float f17 = f15 + f14 - f7;
                  float f18 = Math.max(0.0F, f17 - f16);
                  float f19 = floats[0];
                  float f20 = f16 + Math.max(0.0F, (f18 - f19) / 2.0F);
                  float f21 = f3 + f5 / 2.0F + f12;
                  float f22 = f3 + f5 / 2.0F + f12;
                  ScissorRect clip = scissor.scissor(l, f15 + f14 * clamped, f3, f14 - f14 * clamped, f5);
                  nvg.drawRoundedRect(l, f15, f3, f14, f5, rgb, f6);
                  nvg.drawTextEx(l, text2, f20, f21, new Color(220, 220, 220).getRGB(), f11, OneConfigFonts.REGULAR);
                  nvg.drawTextEx(l, text3, f20 + textWidth + f13, f22, n3, f10, OneConfigFonts.SEMIBOLD);
                  scissor.resetScissor(l, clip);
                  ScissorRect clip2 = scissor.scissor(l, f15, f3, f14 * clamped, f5);
                  nvg.drawRoundedRect(l, f15, f3, f14, f5, rgb2, f6);
                  nvg.drawTextEx(l, text2, f20, f21, new Color(0, 0, 0, 204).getRGB(), f11, OneConfigFonts.REGULAR);
                  nvg.drawTextEx(l, text3, f20 + textWidth + f13, f22, n4, f10, OneConfigFonts.SEMIBOLD);
                  scissor.resetScissor(l, clip2);
               });
               if (!itemStack.isEmpty()) {
                  float f23 = f9 + f8 + f7 + floats[0] + f7;
                  float f24 = f - f23 / 2.0F;
                  float f25 = f24 + f9;
                  float f26 = f3 + (f5 - f8) / 2.0F;
                  float f27 = f8 / 16.0F;
                  MatrixStack matrices = drawContextEvent.getDrawContext().getMatrices();
                  matrices.push();
                  matrices.translate(f25, f26, 0.0F);
                  matrices.scale(f27, f27, 1.0F);
                  drawContextEvent.getDrawContext().drawItem(itemStack, 0, 0);
                  matrices.pop();
               }
            }

            this.lastCount = n;
            this.count = n;
         }
      }
   }

   @EventTarget
   public void onHand(HandEvent handEvent) {
      if ((Boolean)this.spoofItem.getValue() && handEvent.getHand() == Hand.MAIN_HAND) {
         handEvent.setItemStack2(mc2.player.getInventory().getStack(this.oldSlot));
      }
   }

   private void performAction(ForwardEvent forwardEvent) {
      if (mc2.player != null
         && mc2.world != null
         && this.blockSlot != null
         && !this.blockSlot.isValid()
         && mc2.player.isOnGround()
         && !mc2.player.hasVehicle()
         && !mc2.player.getAbilities().flying
         && !mc2.player.isTouchingWater()
         && !mc2.player.isInLava()
         && !forwardEvent.isJump()) {
         boolean flag = forwardEvent.isForward() != forwardEvent.isBackward() || forwardEvent.isLeft() != forwardEvent.isRight();
         int n = this.smoothed.getValue() && this.safeMode.getValue() && !this.testOnGround.getValue() ? 1 : 0;
         if (flag && MovementUtils.countData > n && this.mode.matches("Telly")) {
            switch (this.jumpMode.getDisplayTextAlt()) {
               case "None":
               default:
                  break;
               case "Normal":
                  forwardEvent.setJump(true);
                  break;
               case "Parkour":
                  double d = Math.toRadians(mc2.player.getYaw());
                  double d2 = -Math.sin(d);
                  double d3 = Math.cos(d);
                  BlockPos pos = BlockPos.ofFloored(mc2.player.getX() + d2, mc2.player.getY() - 0.1, mc2.player.getZ() + d3);
                  BlockPos pos2 = BlockPos.ofFloored(mc2.player.getX() + d2 * 2.0, mc2.player.getY() - 0.1, mc2.player.getZ() + d3 * 2.0);
                  if (mc2.world.getBlockState(pos).isAir() || mc2.world.getBlockState(pos2).isAir()) {
                     forwardEvent.setJump(true);
                  }
            }

            if (forwardEvent.isJump() && (Boolean)this.eagle.getValue()) {
               this.waitingForEagleSneak = true;
               this.tellyJumpTicks = 0;
            }
         }
      }
   }

   private Rotation computeRotation(boolean flag) {
      Rotation rotation = this.blockData != null
         ? RotationManager.computeRotation(this.blockData.getPos(), this.blockData.getFacing(), RotationManager.getServerRotation().yaw, RotationManager.getServerRotation().pitch)
         : null;
      if (rotation == null) {
         if (RotationManager.getAngleDifference(mc2.player.getYaw() + 100.0F, RotationManager.getServerRotation().yaw)
            < RotationManager.getAngleDifference(mc2.player.getYaw() - 100.0F, RotationManager.getServerRotation().yaw)) {
            rotation = new Rotation(mc2.player.getYaw() + 100.0F, RotationManager.getServerRotation().pitch);
         } else {
            rotation = new Rotation(mc2.player.getYaw() - 100.0F, RotationManager.getServerRotation().pitch);
         }
      }

      if (MovementFreezeManager.active) {
         return RotationManager.computeRotation(
            this.blockData.getPos(), this.blockData.getFacing(), RotationManager.getServerRotation().yaw, RotationManager.getServerRotation().pitch
         );
      }

      double angleDelta = RotationManager.getAngleDelta(rotation.yaw, RotationManager.getServerRotation().yaw);
      if (this.mode.matches("Telly")) {
         if (mc2.options.jumpKey.isPressed() && (Boolean)this.noUptelly.getValue()) {
            return rotation;
         }

         if (mc2.options.jumpKey.isPressed() && (Boolean)this.randomSlow.getValue()) {
            this.ups++;
            if (this.ups % 2 == 0) {
               return rotation;
            }
         }

         if (!(Boolean)this.smoothed.getValue() || MovementUtils.countValue >= ((Number)this.rotTick.getValue()).intValue() && !(Boolean)this.safeMode.getValue()) {
            if ((Boolean)this.snap.getValue() && mc2.options.jumpKey.isPressed()) {
               if (this.lastBlockData == null || MovementUtils.countValue < ((Number)this.rotTick.getValue()).intValue()) {
                  return new Rotation(mc2.player.getYaw(), 85.0F + (float)Math.random());
               }
            } else if (MovementUtils.countValue < ((Number)this.rotTick.getValue()).intValue()) {
               return new Rotation(mc2.player.getYaw(), 85.0F + (float)Math.random());
            }
         } else if (MovementUtils.countData > 0) {
            if (!(Boolean)this.safeMode.getValue() || (Boolean)this.testOnGround.getValue() && !mc2.options.jumpKey.isPressed()) {
               return new Rotation(mc2.player.getYaw(), 75.5F);
            }

            switch (MovementUtils.countData) {
               case 1:
                  if (!flag) {
                     rotation.yaw = RotationManager.getServerRotation().getYaw() + RotationManager.computeFloat((float)angleDelta, (float)(angleDelta / 2.0));
                     rotation.pitch = 75.5F;
                  } else {
                     rotation = RotationManager.computeRotation(
                        this.blockData.getPos(), this.blockData.getFacing(), mc2.player.getYaw(), RotationManager.getServerRotation().pitch
                     );
                  }

                  mc2.player.jumpingCooldown = 2;
                  break;
               case 2:
                  return new Rotation(mc2.player.getYaw(), 75.5F);
            }
         } else {
            float f = switch (MovementUtils.countValue) {
               case 1 -> 80.0F;
               default -> 50.0F;
            };
            f -= RandomUtils.computeFloat(0.001F, 0.005F);
            rotation.yaw = RotationManager.getServerRotation().getYaw() + RotationManager.computeFloat((float)angleDelta, f);
         }
      }

      if (this.lastRotation != null
         && this.blockData != null
         && BlockLookUtils.isValid(mc2.player, this.lastRotation.yaw, this.lastRotation.pitch, this.blockData.getPos(), this.blockData.getFacing(), true)) {
         return this.lastRotation;
      } else if (this.blockData != null
         && !(Boolean)this.alwaysUpdateRot.getValue()
         && MovementUtils.countValue >= ((Number)this.rotTick.getValue()).intValue()
         && !BlockLookUtils.isValid(mc2.player, rotation.yaw, rotation.pitch, this.blockData.getPos(), this.blockData.getFacing(), true)
         && MovementUtils.countValue >= ((Number)this.rotTick.getValue()).intValue()) {
         this.lastRotation.yaw = this.lastRotation.yaw + (float)Math.random();
         return this.lastRotation;
      } else {
         this.lastRotation = rotation;
         return rotation;
      }
   }

   private void performActionAlt() {
      if (this.blockData != null && !BlinkUtils.blinking) {
         if (RotationManager.getCurrentRotation() == null) {
            return;
         }

         if (mc2.interactionManager == null) {
            return;
         }

         if (!this.canPlace) {
            return;
         }

         BlockHitResult hitResult = BlockLookUtils.raycastHit(RotationManager.getCurrentRotation().yaw, RotationManager.getCurrentRotation().pitch);
         if (!BlockLookUtils.isValid(
            mc2.player, RotationManager.getCurrentRotation().yaw, RotationManager.getCurrentRotation().pitch, this.blockData.getPos(), this.blockData.getFacing(), true
         )) {
            return;
         }

         if (this.blockSlot.getHand().equals(Hand.MAIN_HAND)) {
            mc2.player.getInventory().selectedSlot = this.blockSlot.getSlot();
         }

         if ((Boolean)this.duplicateRotPlace.getValue() && MovementUtils.value > 2.0) {
            double d = Math.abs(MovementUtils.value - MovementUtils.valueValue);
            if (d < 1.0E-4) {
               return;
            }
         }

         if ((Boolean)this.blockFly.getValue()) {
            if (!ServerPacketManager.active) {
               ServerPacketManager.performAction();
            }

            if (this.placeCount == 0) {
               PacketUtils.sendPacketDirect(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
               InteractionStateFlags.activeValue = true;
            }
         }

         if ((Boolean)this.interactItem.getValue()) {
            mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND);
         }

         ActionResult actionResult = mc2.interactionManager.interactBlock(mc2.player, this.blockSlot.getHand(), hitResult);
         InteractionStateFlags.active = true;
         if (actionResult == ActionResult.SUCCESS) {
            this.placeCount++;
            this.lastPlacePosition = this.blockData.getPos().offset(this.blockData.getFacing());
            if (MovementUtils.value > 0.0) {
               MovementUtils.valueValue = MovementUtils.value;
            }

            if ((Boolean)this.noSwing.getValue()) {
               PacketUtils.sendPacket(new HandSwingC2SPacket(this.blockSlot.getHand()));
            } else {
               mc2.player.swingHand(this.blockSlot.getHand());
            }
         }
      }
   }

   @EventTarget
   public void onMinecraftClientEvent2(PreTickEvent preTickEvent) {
      preTickEvent.setCancelled(true);
   }

   @EventTarget
   public void onMinecraftClient(MinecraftClientEvent minecraftClientEvent) {
      if ((Boolean)this.abuseRotation.getValue() && RotationManager.currentRotation != null) {
         this.performAction(30.0F, RotationManager.currentRotation.yaw);
      }

      this.performActionAlt();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState().equals(Stage.POST) && (Boolean)this.blockFly.getValue() && ServerPacketManager.count > 16) {
         ServerPacketManager.setActiveEx(true);
      }
   }

   @EventTarget
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      this.blockSlot = null;
      if (ItemScoreUtils.isBlockItem(mc2.player.getOffHandStack()) && this.isValid(mc2.player.getOffHandStack().getItem())) {
         this.blockSlot = new Scaffold.Entry(45, Hand.OFF_HAND);
      }

      if (this.blockSlot == null
         && !this.blockSlotMode.matches("Most Blocks")
         && ItemScoreUtils.isBlockItem(mc2.player.getMainHandStack())
         && this.isValid(mc2.player.getMainHandStack().getItem())) {
         this.blockSlot = new Scaffold.Entry(mc2.player.getInventory().selectedSlot, Hand.MAIN_HAND);
      }

      if (this.blockSlot == null) {
         int n = this.computeInt();
         if (n != -1) {
            this.blockSlot = new Scaffold.Entry(n, Hand.MAIN_HAND);
         }
      }

      if (this.blockSlot != null && !this.blockSlot.isValid()) {
         if (RotationManager.canOverridePriority(this.getHeightAlt())) {
            if (mc2.player.isOnGround()) {
               this.posY = Math.floor(mc2.player.getY() - 1.0);
            }

            if (mc2.options.jumpKey.isPressed()) {
               this.posY = mc2.player.getBlockY() - 1;
            }

            PosRecord posRecord = BlockLookUtils.isValidBlock(
                  mc2.world
                     .getBlockState(new BlockPos((int)Math.floor(mc2.player.getX()), (int)Math.floor(mc2.player.getY()), (int)Math.floor(mc2.player.getZ())))
               )
               ? this.getPosRecord2(new BlockPos((int)Math.floor(mc2.player.getX()), (int)this.posY, (int)Math.floor(mc2.player.getZ())))
               : null;
            if (posRecord != null) {
               this.blockData = posRecord;
            }

            this.lastBlockData = posRecord;
            if (this.mode.matches("Normal")) {
               this.canPlace = true;
            } else if (this.mode.matches("Snap")) {
               this.canPlace = this.isTransparent(1);
            } else {
               this.canPlace = MovementUtils.countValue >= ((Number)this.placeTick.getValue()).intValue();
               if ((Boolean)this.safeMode.getValue() && (Boolean)this.testOnGround.getValue() && !this.canPlace && mc2.options.jumpKey.isPressed()) {
                  this.canPlace = MovementUtils.countData == 1;
               }
            }

            if (this.blockSlot.getHand().equals(Hand.MAIN_HAND)) {
               mc2.player.getInventory().selectedSlot = this.blockSlot.slot;
            }

            PlayerMoveSimulator playerMoveSimulator = new PlayerMoveSimulator(mc2.player);
            boolean flag = true;
            playerMoveSimulator.onKeyTyped(1);
            Vec3d vec = playerMoveSimulator.computePosition();
            playerMoveSimulator.onKeyTyped(1);
            PosRecord posRecord2 = this.getPosRecord2(
               new BlockPos((int)Math.floor(mc2.player.getX()), mc2.player.getBlockY() - 1, (int)Math.floor(mc2.player.getZ()))
            );
            boolean flag2 = false;
            if (posRecord2 != null) {
               if ((Boolean)this.safeMode.getValue() && (Boolean)this.testOnGround.getValue() && MovementUtils.countData == 1 && mc2.options.jumpKey.isPressed()) {
                  flag2 = true;
               }

               double dist = vec.distanceTo(posRecord2.getPos().toCenterPos());
               if (dist >= this.safeDistance.getCurrentValueEx() || posRecord2.getPos().getY() > playerMoveSimulator.getY()) {
                  this.canPlace = true;
                  flag = false;
                  this.blockData = this.lastBlockData = posRecord2;
               }
            }

            if (this.blockData != null) {
               Box box = new Box(this.blockData.getPos()).withMinY(this.blockData.getPos().getY() - 1).withMaxY(this.blockData.getPos().getY() + 1);
               if (this.blockData.getPos().getY() > playerMoveSimulator.getY() && !box.contains(mc2.player.getPos())) {
                  this.canPlace = true;
                  flag = false;
                  this.posY = mc2.player.getBlockY() - 1;
                  this.blockData = this.lastBlockData = this.getPosRecord2(
                     new BlockPos((int)Math.floor(mc2.player.getX()), (int)Math.floor(this.posY), (int)Math.floor(mc2.player.getZ()))
                  );
               }
            }

            if (!flag && this.rotateCount < 8) {
               if ((Boolean)this.dbgV.getValue() && this.rotateCount == 1) {
                  IText.applyText("working");
               }

               MovementFreezeManager.performAction();
               this.rotateCount++;
            } else {
               this.rotateCount = 0;
            }

            this.rot = this.computeRotation(flag2);
            if ((Boolean)this.duplicateRotPlace.getValue()) {
               this.rot.pitch = this.rot.pitch - RandomUtils.computeFloat(0.001F, 0.003F);
               this.rot.yaw = this.rot.yaw - RandomUtils.computeFloat(1.0E-4F, 3.0E-4F);

               do {
                  this.rot.pitch = this.rot.pitch - RandomUtils.computeFloat(0.001F, 0.003F);
               } while (this.rot.pitch > 90.0F);

               if (this.rot.pitch < -90.0F) {
                  this.rot.pitch = -90.0F;
               }
            }

            if (isValid(this.blockData, this.rot)) {
               MovementFreezeManager.performActionEx();
               this.rotateCount = 0;
            }

            if ((Boolean)this.fixRotation.getValue() && this.rot != null) {
               this.rot.setProgress(((Double)mc2.options.getMouseSensitivity().getValue()).floatValue());
            }

            RotationManager.requestRotationWithMoveFix(this.rot, this.getHeightAlt(), MoveFixMode.Silent);
            if (this.blockData != null) {
               if (mc2.player.isSpectator()) {
                  this.setConfigEnabled(false);
               }

               if (!this.mode.matches("Telly")) {
                  if (this.waitingForEagleSneak) {
                     this.tellyJumpTicks++;
                     if (this.tellyJumpTicks == ((Number)this.tellyEagleTick.getValue()).intValue() && !mc2.options.sneakKey.isPressed()) {
                        mc2.options.sneakKey.setPressed(true);
                     }

                     if (this.tellyJumpTicks == ((Number)this.tellyEagleTick.getValue()).intValue() + ((Number)this.keepEagleSneakTick.getValue()).intValue()) {
                        mc2.options.sneakKey.setPressed(false);
                        this.waitingForEagleSneak = false;
                        this.tellyJumpTicks = 0;
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isValid(PosRecord posRecord, Rotation rotation) {
      return posRecord == null || !BlockLookUtils.isValid(rotation, posRecord.getPos(), posRecord.getFacing(), true);
   }

   @EventTarget
   public void onForward(ForwardEvent forwardEvent) {
      this.setInfoText(this.mode.getDisplayTextAlt());
      this.performAction(forwardEvent);
      if (this.mode.matches("Telly") && (Boolean)this.eagle.getValue()) {
         forwardEvent.setSneak(this.placeCount % 4 == 0);
      }
   }

   @EventTarget
   public void onClientPlayerEntityEvent2(SlowDownEvent slowDownEvent) {
      if (MovementUtils.countData == 1
         && (Boolean)this.testOnGround.getValue()
         && (Boolean)this.smoothed.getValue()
         && !(Boolean)this.noUptelly.getValue()
         && (Boolean)this.safeMode.getValue()
         && mc2.options.jumpKey.isPressed()) {
         mc2.player.input.movementForward *= 0.2F;
         mc2.player.input.movementSideways *= 0.2F;
      }
   }

   public boolean isTransparent(int n) {
      return MovementUtils.getBlock(0.0, -n, 0.0).getDefaultState().isTransparent();
   }

   public static Vec3d computePosition(BlockPos pos, Direction direction) {
      double d = pos.getX() + 0.5;
      double d2 = pos.getY() + 0.5;
      double d3 = pos.getZ() + 0.5;
      if (direction != Direction.UP && direction != Direction.DOWN) {
         d2 += MathRandomUtils.computeDouble(0.3, -0.3);
      } else {
         d += MathRandomUtils.computeDouble(0.3, -0.3);
         d3 += MathRandomUtils.computeDouble(0.3, -0.3);
      }

      if (direction == Direction.WEST || direction == Direction.EAST) {
         d3 += MathRandomUtils.computeDouble(0.3, -0.3);
      }

      if (direction == Direction.SOUTH || direction == Direction.NORTH) {
         d += MathRandomUtils.computeDouble(0.3, -0.3);
      }

      return new Vec3d(d, d2, d3);
   }

   public void onKeyTyped(int n) {
      for (int i = 9; i < 45; i++) {
         if (mc2.player.getInventory().getStack(i).isStackable() && mc2.currentScreen == null) {
            ItemStack stack = mc2.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem && this.isValid(blockItem)) {
               if (36 + n != i) {
                  ItemScoreUtils.clickSlot(i, n);
               }
               break;
            }
         }
      }
   }

   public Rotation computeRotation(Vec3d vec, Vec3d vec2) {
      double dx = vec.x - vec2.x;
      double dy = vec.y - vec2.y;
      double dz = vec.z - vec2.z;
      return new Rotation(
         MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F),
         MathHelper.wrapDegrees((float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))))
      );
   }

   private boolean isValid(Item item) {
      return item instanceof BlockItem && !invalidBlocks.contains(((BlockItem)item).getBlock()) && SlotIndexMapper.isValid(item);
   }

   private int computeInt() {
      if (this.blockSlotMode.matches("Most Blocks")) {
         return this.computeIntEx();
      }

      int n = -1;

      for (int i = 0; i <= 8; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (ItemScoreUtils.isBlockItem(stack) && this.isValid(stack.getItem())) {
            n = i;
         }
      }

      return n;
   }

   private int computeIntEx() {
      int selectedSlot = mc2.player.getInventory().selectedSlot;
      int n = -1;
      int n2 = -1;
      ItemStack stack = mc2.player.getInventory().getStack(selectedSlot);
      if (ItemScoreUtils.isBlockItem(stack) && this.isValid(stack.getItem())) {
         n = selectedSlot;
         n2 = stack.getCount();
      }

      for (int i = 0; i <= 8; i++) {
         ItemStack stack2 = mc2.player.getInventory().getStack(i);
         if (ItemScoreUtils.isBlockItem(stack2) && this.isValid(stack2.getItem()) && stack2.getCount() > n2) {
            n = i;
            n2 = stack2.getCount();
         }
      }

      return n;
   }

   private ItemStack getItemStack() {
      if (mc2.player == null) {
         return ItemStack.EMPTY;
      }

      ItemStack mainHandStack = mc2.player.getMainHandStack();
      if (this.isValidItem(mainHandStack)) {
         return mainHandStack;
      }

      ItemStack offHandStack = mc2.player.getOffHandStack();
      return this.isValidItem(offHandStack) ? offHandStack : ItemStack.EMPTY;
   }

   private boolean isValidItem(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? this.isValid(stack.getItem()) : false;
   }

   private int computeInt(int n) {
      if (n < 16) {
         return new Color(255, 80, 80).getRGB();
      } else {
         return n < 32 ? new Color(255, 220, 80).getRGB() : Color.WHITE.getRGB();
      }
   }

   private int computeIntEx(int n) {
      if (n < 16) {
         return new Color(255, 80, 80).getRGB();
      } else {
         return n < 32 ? new Color(255, 220, 80).getRGB() : Color.BLACK.getRGB();
      }
   }

   public int computeIntAlt() {
      int n = 0;

      for (int i = 9; i < 45; i++) {
         if (mc2.player == null) {
            return -1;
         }

         if (mc2.player.getInventory().getStack(i).isStackable()) {
            ItemStack stack = mc2.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem && this.isValid(blockItem)) {
               n += stack.getCount();
            }
         }
      }

      return n;
   }

   public int computeIntImpl() {
      if (mc2.player == null) {
         return 0;
      }

      int n = 0;

      for (int i = 0; i <= 8; i++) {
         ItemStack stack = mc2.player.getInventory().getStack(i);
         if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem && this.isValid(blockItem)) {
            n += stack.getCount();
         }
      }

      ItemStack offHandStack = mc2.player.getOffHandStack();
      if (!offHandStack.isEmpty() && offHandStack.getItem() instanceof BlockItem blockItem2 && this.isValid(blockItem2)) {
         n += offHandStack.getCount();
      }

      return n;
   }

   private PosRecord getPosRecord2(BlockPos pos) {
      PosRecord posRecord;
      if (this.getPosRecord(pos) == null) {
         BlockPos pos2 = this.findBlockPos();
         if (pos2 == null) {
            return null;
         }

         Direction direction = this.getDirection(pos2);
         if (direction == null) {
            return null;
         }

         posRecord = new PosRecord(pos2, direction);
      } else {
         posRecord = this.getPosRecord(pos);
      }

      return BlockLookUtils.isValidBlock(mc2.world.getBlockState(posRecord.getPos().offset(posRecord.getFacing()))) ? posRecord : null;
   }

   private Direction getDirection(BlockPos pos) {
      ArrayList<PosRecord> list = new ArrayList<>();
      BlockPos pos2 = new BlockPos((int)Math.floor(mc2.player.getX()), (int)Math.floor(mc2.player.getY()), (int)Math.floor(mc2.player.getZ()));
      if (this.isValidBlock(pos.east()) && !pos.east().equals(pos2)) {
         list.add(new PosRecord(pos.east(), Direction.EAST));
      }

      if (this.isValidBlock(pos.north()) && !pos.north().equals(pos2)) {
         list.add(new PosRecord(pos.north(), Direction.NORTH));
      }

      if (this.isValidBlock(pos.south()) && !pos.south().equals(pos2)) {
         list.add(new PosRecord(pos.south(), Direction.SOUTH));
      }

      if (this.isValidBlock(pos.west()) && !pos.west().equals(pos2)) {
         list.add(new PosRecord(pos.west(), Direction.WEST));
      }

      if (list.isEmpty()) {
         return null;
      }

      list.sort(Comparator.comparingDouble(posRecord -> posRecord.getPos().getSquaredDistance(pos2)));
      list.removeIf(posRecord2 -> !BlockLookUtils.isValidBlock(mc2.world.getBlockState(posRecord2.getPos().offset(posRecord2.getFacing()))));
      return ((PosRecord)list.getFirst()).getFacing();
   }

   private BlockPos findBlockPos() {
      BlockPos pos = new BlockPos((int)Math.floor(mc2.player.getX()), (int)Math.floor(mc2.player.getY()), (int)Math.floor(mc2.player.getZ()));
      ArrayList<BlockPos> list = new ArrayList<>();
      Map map = this.buildMap(5);

      for (Object obj : map.entrySet()) {
         Map.Entry entry = (Map.Entry)obj;
         if (this.isValidBlockEx((BlockPos)entry.getKey())) {
            list.add((BlockPos)entry.getKey());
         }
      }

      list.removeIf(pos2 -> pos2.getY() >= pos.getY());
      if (list.isEmpty()) {
         return null;
      }

      list.sort(Comparator.comparingDouble(pos3 -> pos3.getSquaredDistance(pos)));
      return (BlockPos)list.getFirst();
   }

   public boolean isValidBlock(BlockPos pos) {
      return BlockLookUtils.isValidBlock(mc2.world.getBlockState(pos));
   }

   public float computeFloat(Entity entity, BlockPos pos) {
      float f = (float)(entity.getX() - pos.getX());
      float f2 = (float)(entity.getY() - pos.getY());
      float f3 = (float)(entity.getZ() - pos.getZ());
      return MathHelper.sqrt(f * f + f2 * f2 + f3 * f3);
   }

   public Block getBlock(BlockPos pos) {
      return mc2.world.getBlockState(pos).getBlock();
   }

   public Map<BlockPos, Block> buildMap(int n) {
      HashMap map = new HashMap();
      ClientPlayerEntity player = mc2.player;
      if (player == null) {
         return map;
      }

      for (int i = n; i >= -n + 1; i--) {
         for (int j = n; j >= -n + 1; j--) {
            for (int k = n; k >= -n + 1; k--) {
               BlockPos pos = new BlockPos(player.getBlockX() + i, player.getBlockY() + j, player.getBlockZ() + k);
               Block block = this.getBlock(pos);
               if (block != null) {
                  map.put(pos, block);
               }
            }
         }
      }

      return map;
   }

   public PosRecord getPosRecord(BlockPos pos) {
      if (this.isValidBlockEx(pos.add(-1, 0, 0))) {
         return new PosRecord(pos.add(-1, 0, 0), Direction.EAST);
      } else if (this.isValidBlockEx(pos.add(1, 0, 0))) {
         return new PosRecord(pos.add(1, 0, 0), Direction.WEST);
      } else if (this.isValidBlockEx(pos.add(0, 0, 1))) {
         return new PosRecord(pos.add(0, 0, 1), Direction.NORTH);
      } else if (this.isValidBlockEx(pos.add(0, 0, -1))) {
         return new PosRecord(pos.add(0, 0, -1), Direction.SOUTH);
      } else {
         return this.isValidBlockEx(pos.add(0, -1, 0)) ? new PosRecord(pos.add(0, -1, 0), Direction.UP) : null;
      }
   }

   public boolean isValidBlockEx(BlockPos pos) {
      Block block = mc2.world.getBlockState(pos).getBlock();
      return !(block instanceof TrapdoorBlock) && !(block instanceof DoorBlock) && !(block instanceof FenceGateBlock)
         ? !Arrays.asList(
                  Blocks.ANVIL,
                  Blocks.AIR,
                  Blocks.WATER,
                  Blocks.FIRE,
                  Blocks.LAVA,
                  Blocks.SKELETON_SKULL,
                  Blocks.OAK_SIGN,
                  Blocks.TRAPPED_CHEST,
                  Blocks.CHEST,
                  Blocks.ENCHANTING_TABLE,
                  Blocks.ENDER_CHEST,
                  Blocks.CRAFTING_TABLE,
                  Blocks.DAYLIGHT_DETECTOR,
                  Blocks.COBWEB,
                  Blocks.SHORT_GRASS,
                  Blocks.FLOWER_POT,
                  Blocks.CHORUS_FLOWER,
                  Blocks.SUNFLOWER,
                  Blocks.CORNFLOWER,
                  Blocks.TORCHFLOWER,
                  Blocks.OAK_BUTTON,
                  Blocks.ACACIA_BUTTON,
                  Blocks.BIRCH_BUTTON,
                  Blocks.CRIMSON_BUTTON,
                  Blocks.CHERRY_BUTTON,
                  Blocks.DARK_OAK_BUTTON,
                  Blocks.JUNGLE_BUTTON,
                  Blocks.STONE_BUTTON,
                  Blocks.WARPED_BUTTON,
                  Blocks.SPRUCE_BUTTON,
                  Blocks.NOTE_BLOCK,
                  Blocks.PLAYER_HEAD
               )
               .contains(block)
            && !BlockLookUtils.isValidBlock(mc2.world.getBlockState(pos))
         : false;
   }

   public void performAction(float x, float y) {
      double angleDelta = RotationManager.getAngleDelta(RotationManager.getPrevRotation().yaw, y);
      int n = (int)(Math.abs(angleDelta) / x);
      float yaw = RotationManager.getPrevRotation().yaw;

      for (int i = 0; i < n; i++) {
         float f = RotationManager.computeFloat((float)angleDelta, x);
         yaw += f;
         RotationManager.currentRotation.yaw = yaw;
         mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND);
         InteractionStateFlags.active = true;
      }

      RotationManager.currentRotation.yaw = y;
   }

   @Override
   public int getHeightAlt() {
      return 5;
   }

   public record Entry(int slot, Hand hand) {

      public boolean isValid() {
         if (Scaffold.mc2.player == null) {
            return false;
         } else if (this.hand.equals(Hand.OFF_HAND)) {
            ItemStack offHandStack = Scaffold.mc2.player.getOffHandStack();
            return offHandStack.isEmpty() || !(offHandStack.getItem() instanceof BlockItem);
         } else {
            return Scaffold.mc2.player.getInventory().getStack(this.slot).isEmpty()
               || !(Scaffold.mc2.player.getInventory().getStack(this.slot).getItem() instanceof BlockItem);
         }
      }

      public int getSlot() {
         return this.slot;
      }

      public Hand getHand() {
         return this.hand;
      }
   }
}
