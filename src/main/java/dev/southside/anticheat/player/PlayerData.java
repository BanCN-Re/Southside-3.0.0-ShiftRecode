package dev.southside.anticheat.player;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.impl.misc.HackerDetector;
import dev.southside.utils.EaseInOutCubicAnimation;
import dev.southside.utils.IText;
import dev.southside.utils.OneConfigScreen;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlayerData {
   @SerializedName("player")
   private final PlayerEntity player;
   @SerializedName("violationLevel")
   private float violationLevel;
   @SerializedName("flagAnnouncementReady")
   private boolean flagAnnouncementReady = true;
   @SerializedName("lastPosition")
   private Vec3d lastPosition;
   @SerializedName("blinkCounter")
   private int blinkCounter;
   @SerializedName("stutterCounter")
   private int stutterCounter;
   @SerializedName("lastBlinkTick")
   private int lastBlinkTick = -1000;
   @SerializedName("fastWebCounter")
   private int fastWebCounter;
   @SerializedName("lastFastWebFlagTick")
   private int lastFastWebFlagTick = -1000;
   @SerializedName("yawHistory")
   private final LinkedList<Float> yawHistory = new LinkedList<>();
   @SerializedName("rotationSnapCounter")
   private int rotationSnapCounter;
   @SerializedName("rotationSumFlags")
   private int rotationSumFlags;
   @SerializedName("rotationVarianceFlags")
   private int rotationVarianceFlags;
   @SerializedName("rotationSwitchCounter")
   private int rotationSwitchCounter;
   @SerializedName("towerJumpCount")
   private int towerJumpCount;
   @SerializedName("towerDecayTicks")
   private int towerDecayTicks;
   @SerializedName("lastTowerRiseTick")
   private int lastTowerRiseTick = -1000;
   @SerializedName("lastTowerFlagTick")
   private int lastTowerFlagTick = -1000;
   @SerializedName("placementTimestamps")
   private final List<Long> placementTimestamps = new ArrayList<>();
   @SerializedName("placementSneaking")
   private final List<Boolean> placementSneaking = new ArrayList<>();
   @SerializedName("targetBlockPos")
   private BlockPos targetBlockPos;
   @SerializedName("lastTargetBlockY")
   private int lastTargetBlockY = -1;
   @SerializedName("lastScaffoldSpeedFlagTick")
   private int lastScaffoldSpeedFlagTick = -1000;

   public PlayerData(PlayerEntity player2) {
      this.player = player2;
      this.lastPosition = player2.getPos();
   }

   public PlayerEntity getPlayer() {
      return this.player;
   }

   public float getViolationLevel() {
      return this.violationLevel;
   }

   public void setViolationLevel(float f) {
      this.violationLevel = f;
   }

   public boolean isFlagAnnouncementReady() {
      return this.flagAnnouncementReady;
   }

   public void setFlagAnnouncementReady(boolean flag) {
      this.flagAnnouncementReady = flag;
   }

   public Vec3d getLastPosition() {
      return this.lastPosition;
   }

   public void setLastPosition(Vec3d vec) {
      this.lastPosition = vec;
   }

   public int getBlinkCounter() {
      return this.blinkCounter;
   }

   public void setBlinkCounter(int n) {
      this.blinkCounter = n;
   }

   public int getStutterCounter() {
      return this.stutterCounter;
   }

   public void setStutterCounter(int n) {
      this.stutterCounter = n;
   }

   public int getLastBlinkTick() {
      return this.lastBlinkTick;
   }

   public void setLastBlinkTick(int n) {
      this.lastBlinkTick = n;
   }

   public int getFastWebCounter() {
      return this.fastWebCounter;
   }

   public void setFastWebCounter(int n) {
      this.fastWebCounter = n;
   }

   public int getLastFastWebFlagTick() {
      return this.lastFastWebFlagTick;
   }

   public void setLastFastWebFlagTick(int n) {
      this.lastFastWebFlagTick = n;
   }

   public LinkedList<Float> getYawHistory() {
      return this.yawHistory;
   }

   public int getRotationSnapCounter() {
      return this.rotationSnapCounter;
   }

   public void setRotationSnapCounter(int n) {
      this.rotationSnapCounter = n;
   }

   public int getRotationSumFlags() {
      return this.rotationSumFlags;
   }

   public void setRotationSumFlags(int n) {
      this.rotationSumFlags = n;
   }

   public int getRotationVarianceFlags() {
      return this.rotationVarianceFlags;
   }

   public void setRotationVarianceFlags(int n) {
      this.rotationVarianceFlags = n;
   }

   public int getRotationSwitchCounter() {
      return this.rotationSwitchCounter;
   }

   public void setRotationSwitchCounter(int n) {
      this.rotationSwitchCounter = n;
   }

   public int getTowerJumpCount() {
      return this.towerJumpCount;
   }

   public void setTowerJumpCount(int n) {
      this.towerJumpCount = n;
   }

   public int getTowerDecayTicks() {
      return this.towerDecayTicks;
   }

   public void setTowerDecayTicks(int n) {
      this.towerDecayTicks = n;
   }

   public int getLastTowerRiseTick() {
      return this.lastTowerRiseTick;
   }

   public void setLastTowerRiseTick(int n) {
      this.lastTowerRiseTick = n;
   }

   public int getLastTowerFlagTick() {
      return this.lastTowerFlagTick;
   }

   public void setLastTowerFlagTick(int n) {
      this.lastTowerFlagTick = n;
   }

   public List<Long> getPlacementTimestamps() {
      return this.placementTimestamps;
   }

   public List<Boolean> getPlacementSneaking() {
      return this.placementSneaking;
   }

   public BlockPos getTargetBlockPos() {
      return this.targetBlockPos;
   }

   public void setTargetBlockPos(BlockPos pos) {
      this.targetBlockPos = pos;
   }

   public int getLastTargetBlockY() {
      return this.lastTargetBlockY;
   }

   public void setLastTargetBlockY(int n) {
      this.lastTargetBlockY = n;
   }

   public int getLastScaffoldSpeedFlagTick() {
      return this.lastScaffoldSpeedFlagTick;
   }

   public void setLastScaffoldSpeedFlagTick(int n) {
      this.lastScaffoldSpeedFlagTick = n;
   }

   public void addVl(float f, String text) {
      HackerDetector hackerDetector = Southside.INSTANCE.getModuleManager().getModule(HackerDetector.class);
      this.violationLevel += f;
      if ((Boolean)hackerDetector.log.getValue() && MinecraftClient.getInstance().inGameHud != null) {
         String formatted = String.format("§7%s §cflagged §7%s §8(+%.1f, VL: %.1f)", this.player.getName().getString(), text, f, this.violationLevel);
         MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.empty().append(IText.TEXT).append(Text.literal(formatted)));
      }

      if ((Boolean)hackerDetector.notification.getValue() && (Module.mc.world != null || Module.mc.currentScreen instanceof OneConfigScreen)) {
         EaseInOutCubicAnimation easeInOutCubicAnimation = new EaseInOutCubicAnimation(3000, 0.0F, 1.0F, false);
         Notifications.INSTANCE
            .send("HackerDetector", this.player.getName().getString() + " → " + text, Notification.iconUtil3, 3000.0F, () -> Notification.computeFloat(easeInOutCubicAnimation));
      }
   }
}
