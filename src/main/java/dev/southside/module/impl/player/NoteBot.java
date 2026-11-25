package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.DisableRotationEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.PacketDispatchHook;
import dev.southside.utils.NoteBotSongLoader;
import dev.southside.utils.InteractionStateFlags;
import dev.southside.utils.BlinkUtils;
import dev.southside.utils.BlockRaycastHelper;
import dev.southside.utils.NumberValue;
import dev.southside.utils.OptionSubcategory;
import dev.southside.utils.NoteBlockTuner;
import dev.southside.utils.ModeValue;
import dev.southside.utils.OneConfigGui;
import dev.southside.utils.RotationManager;
import dev.southside.utils.OptionCategory;
import dev.southside.utils.ButtonValue;
import dev.southside.utils.Rotation;
import dev.southside.utils.misc.NotesRecord;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class NoteBot extends Module {
   private static final int DEFAULT_INT = 32768;
   private static final int DEFAULT_INT_VALUE = 256;
   private static final long DEFAULT_LONG_VALUE = 500000000L;
   private static final long DEFAULT_LONG_DATA = 5000000000L;
   private static final int DEFAULT_INT_DATA = -100;
   private static final String DEFAULT_TEXT = "First available";
   @SerializedName("autoRefresh")
   public final BooleanValue autoRefresh = new BooleanValue("Auto Refresh", true);
   @SerializedName("songFile")
   public ModeValue songFile = new ModeValue("Song", new String[]{"First available"}, "Choose a song from Southside-nextgen/notebot", 2, 0);
   @SerializedName("refreshSongs")
   public final ButtonValue refreshSongs = new ButtonValue("Refresh Songs", "Refresh", "Refresh the NBS playlist", 2, this::performActionAlt);
   @SerializedName("radius")
   public final NumberValue radius = new NumberValue("Radius", 5, 3, 10, 1);
   @SerializedName("autoTune")
   public final BooleanValue autoTune = new BooleanValue("Auto Tune", true);
   @SerializedName("autoNext")
   public final BooleanValue autoNext = new BooleanValue("Auto Next", false);
   @SerializedName("lookAt")
   public final BooleanValue lookAt = new BooleanValue("Look At Note Blocks", true);
   @SerializedName("wallCheck")
   public final BooleanValue wallCheck = new BooleanValue("Wall Check", "Skip note blocks obstructed by other blocks", true);
   @SerializedName("keepTempo")
   public final BooleanValue keepTempo = new BooleanValue("Keep Tempo", true);
   @SerializedName("swing")
   public final BooleanValue swing = new BooleanValue("Swing", true);
   @SerializedName("rotationSpeed")
   public final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 120, 10, 365, 5);
   @SerializedName("maxNotesPerTick")
   public final NumberValue maxNotesPerTick = new NumberValue("Max Notes Per Tick", 1, 1, 1024, 1);
   @SerializedName("loader")
   private final NoteBotSongLoader loader = new NoteBotSongLoader();
   @SerializedName("pendingWrites")
   private final Set<Packet<?>> pendingWrites = Collections.newSetFromMap(new IdentityHashMap<>());
   @SerializedName("unavailableKeys")
   private final Set<Integer> unavailableKeys = new HashSet<>();
   @SerializedName("playlist")
   private List<String> playlist = List.of();
   @SerializedName("playlistError")
   private String playlistError = "";
   @SerializedName("nextPlaylistRefresh")
   private long nextPlaylistRefresh;
   @SerializedName("current")
   private NoteBotSongLoader.Entry current;
   @SerializedName("phase")
   private NoteBot.Mode phase = NoteBot.Mode.IDLE;
   @SerializedName("observedSongName")
   private String observedSongName = "";
   @SerializedName("songNameChangedAt")
   private long songNameChangedAt;
   @SerializedName("selectionPending")
   private boolean selectionPending;
   @SerializedName("sessionWorld")
   private ClientWorld sessionWorld;
   @SerializedName("sessionPlayer")
   private ClientPlayerEntity sessionPlayer;
   @SerializedName("mapping")
   private NoteBlockTuner mapping;
   @SerializedName("layout")
   private NoteBlockTuner.Entry3 layout;
   @SerializedName("tuneIndex")
   private int tuneIndex;
   @SerializedName("noteCursor")
   private int noteCursor;
   @SerializedName("songPosition")
   private double songPosition;
   @SerializedName("requestedRotation")
   private Rotation requestedRotation;
   @SerializedName("writeDeadline")
   private long writeDeadline;
   @SerializedName("nextPrime")
   private long nextPrime;
   @SerializedName("synchronizePose")
   private boolean synchronizePose;
   @SerializedName("tuningPos")
   private BlockPos tuningPos;
   @SerializedName("tuningPitch")
   private int tuningPitch;
   @SerializedName("tuningSequence")
   private int tuningSequence = -1;
   @SerializedName("tuningStateReceived")
   private boolean tuningStateReceived;
   @SerializedName("tuningAcknowledged")
   private boolean tuningAcknowledged;
   @SerializedName("observedWallCheck")
   private boolean observedWallCheck;

   public NoteBot() {
      super(Category.Player, "NoteBot", "Play NBS songs with nearby note blocks");
      Southside.INSTANCE.getEventBus().subscribe(new Object() {
         @EventTarget
         public void onState(StateEvent stateEvent) {
            if (stateEvent.getState() == Stage.PRE) {
               NoteBot.this.performActionImpl();
            }
         }
      });
   }

   public void performActionAlt() {
      this.loader.performAction(getPath());
   }

   @Override
   public void postInit() {
      super.postInit();
      this.addDependency(this.keepTempo.getLabel(), this.lookAt.getLabel());
      this.addDependency(this.rotationSpeed.getLabel(), this.lookAt.getLabel());
   }

   private String getDisplayText() {
      String displayTextEx = this.songFile.getDisplayTextEx();
      return "First available".equals(displayTextEx) ? "" : displayTextEx;
   }

   private void applyText(String text) {
      String text2 = NoteBotSongLoader.formatText(text);
      ArrayList<String> list = new ArrayList<>();
      list.add("First available");
      list.addAll(this.playlist);
      if (!text2.isEmpty() && !list.contains(text2)) {
         list.add(text2);
      }

      String[] textArray = list.toArray(String[]::new);
      int n = text2.isEmpty() ? 0 : list.indexOf(text2);
      if (Arrays.equals(this.songFile.getOptions(), textArray)) {
         this.songFile.performAction(n);
      } else {
         BasicOption basicOption = this.songFile.basicOption;
         ModeValue modeValue = new ModeValue("Song", textArray, this.songFile.getDescription(), 2, n);
         if (basicOption != null) {
            basicOption.performActionEx();
            BasicOption basicOption2 = ConfigUtils.getBasicOption(modeValue, this.songFile.getA(), this.songFile.getDisplayText());

            for (OptionCategory optionCategory : this.modData.defaultPage.categories.values()) {
               for (OptionSubcategory optionSubcategory : optionCategory.subcategories) {
                  int n2 = optionSubcategory.options.indexOf(basicOption);
                  if (n2 >= 0) {
                     optionSubcategory.options.set(n2, basicOption2);
                  }
               }
            }

            this.optionElements.put("Song", basicOption2);
         }

         this.songFile = modeValue;
      }
   }

   @Override
   public JsonObject saveToJson() {
      JsonObject json = super.saveToJson();
      json.addProperty("Song File", this.getDisplayText());
      return json;
   }

   @Override
   public void loadFromJson(JsonObject json) {
      JsonObject json2 = json.deepCopy();
      JsonObject asJsonObject = json2.getAsJsonObject(this.modData.name);
      if (asJsonObject != null) {
         String text = asJsonObject.has("Song File") ? asJsonObject.get("Song File").getAsString() : "";
         if (!asJsonObject.has("Song File") && asJsonObject.has("Song") && asJsonObject.get("Song").isJsonPrimitive() && asJsonObject.getAsJsonPrimitive("Song").isString()) {
            text = asJsonObject.get("Song").getAsString();
         }

         this.applyText(text);
         asJsonObject.addProperty("Song", this.songFile.getValue());
         super.loadFromJson(json2);
      }
   }

   public void performActionImpl() {
      try {
         List list = this.loader.collectItems();
         if (list != null) {
            String displayText = this.getDisplayText();
            this.playlist = list;
            this.applyText(displayText);
            this.playlistError = "";
         }
      } catch (IOException e) {
         if (!e.toString().equals(this.playlistError)) {
            this.applyTextEx("NoteBot playlist: " + e.getMessage());
         }

         this.playlistError = e.toString();
      }

      if ((Boolean)this.autoRefresh.getValue() && mc2.currentScreen instanceof OneConfigGui) {
         if (System.nanoTime() >= this.nextPlaylistRefresh) {
            this.performActionAlt();
            this.nextPlaylistRefresh = System.nanoTime() + 1000000000L;
         }
      }
   }

   @Override
   public void onEnable() {
      PacketDispatchHook.setActive(true);
      this.observedWallCheck = (Boolean)this.wallCheck.getValue();
      this.observedSongName = NoteBotSongLoader.formatText(this.getDisplayText());
      this.selectionPending = false;
      this.sessionWorld = mc2.world;
      this.sessionPlayer = mc2.player;
      this.setActiveEx(false);
   }

   @Override
   public void onDisable() {
      PacketDispatchHook.setActive(false);
      this.loader.performAction();
      this.selectionPending = false;
      this.performActionSafe();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.PRE) {
         if (this.sessionWorld != mc2.world || this.sessionPlayer != mc2.player) {
            this.sessionWorld = mc2.world;
            this.sessionPlayer = mc2.player;
            PacketDispatchHook.performAction();
            this.performActionCustom();
            if (this.phase != NoteBot.Mode.LOADING && this.phase != NoteBot.Mode.ERROR) {
               this.phase = NoteBot.Mode.IDLE;
            }
         }

         if ((!this.pendingWrites.isEmpty() || this.tuningPos != null) && System.nanoTime() > this.writeDeadline) {
            this.applyTextEx("interaction was cancelled, replaced, delayed or not confirmed; rescan");
         } else {
            if (this.current != null
               && this.observedWallCheck != (Boolean)this.wallCheck.getValue()
               && this.pendingWrites.isEmpty()
               && (this.tuningPos == null || this.tuningStateReceived && this.tuningAcknowledged)) {
               this.observedWallCheck = (Boolean)this.wallCheck.getValue();
               this.performActionCustom();
               this.phase = NoteBot.Mode.IDLE;
            }

            String text = NoteBotSongLoader.formatText(this.getDisplayText());
            long now = System.nanoTime();
            if (!text.equalsIgnoreCase(this.observedSongName)) {
               this.observedSongName = text;
               this.songNameChangedAt = now;
               this.selectionPending = true;
               this.loader.performAction();
               this.performActionSafe();
            }

            if (this.selectionPending) {
               if (now - this.songNameChangedAt < 500000000L) {
                  return;
               }

               this.selectionPending = false;
               this.setActiveEx(false);
            }

            if (this.phase == NoteBot.Mode.LOADING) {
               try {
                  NoteBotSongLoader.Entry entry = this.loader.getEntry();
                  if (entry == null) {
                     return;
                  }

                  this.current = entry;
                  this.applyText(entry.getName());
                  this.observedSongName = entry.getName();
                  this.phase = NoteBot.Mode.IDLE;
                  this.setInfoText(entry.getName());
                  this.applyText("NoteBot loaded " + entry.getName() + " (" + entry.getSong().getNotes().size() + " notes)");
               } catch (IOException e) {
                  this.applyTextEx(e.getMessage());
               }
            }
         }
      }
   }

   private void setActiveEx(boolean flag) {
      String text = flag && this.current != null ? this.current.getName() : null;
      this.performActionSafe();
      this.loader.performAction(getPath(), NoteBotSongLoader.formatText(this.getDisplayText()), text);
      this.phase = NoteBot.Mode.LOADING;
   }

   @EventTarget(priority = -1000000)
   public void onDisableRotation(DisableRotationEvent disableRotationEvent) {
      this.synchronizePose = false;
      if (this.current != null
         && this.phase != NoteBot.Mode.ERROR
         && mc2.player != null
         && mc2.world != null
         && mc2.interactionManager != null
         && mc2.getNetworkHandler() != null) {
         ClientPlayerEntity player = mc2.player;
         if (player.getAbilities().creativeMode || player.isSpectator()) {
            this.applyTextEx("NoteBot requires survival or an adventure map that permits these interactions");
         } else if (mc2.currentScreen == null
            && player.currentScreenHandler == player.playerScreenHandler
            && !mc2.interactionManager.isBreakingBlock()
            && !player.isUsingItem()
            && !player.hasVehicle()
            && !player.isDead()
            && !mc2.options.attackKey.isPressed()
            && !mc2.options.useKey.isPressed()
            && !InteractionStateFlags.active
            && !InteractionStateFlags.activeValue
            && !BlinkUtils.blinking
            && (!(Boolean)this.lookAt.getValue() || !disableRotationEvent.isDisableRotation() && RotationManager.canOverridePriority(-100))) {
            if (!player.getMainHandStack().isEmpty()) {
               this.applyTextEx("NoteBot requires an empty main hand");
            } else {
               if (!(Boolean)this.lookAt.getValue()) {
                  this.requestedRotation = null;
               }

               this.performAction(player);
               if ((Boolean)this.lookAt.getValue() && this.requestedRotation != null) {
                  RotationManager.requestRotation(this.requestedRotation, -100);
               }
            }
         }
      }
   }

   private void performAction(ClientPlayerEntity player) {
      if (this.pendingWrites.isEmpty()) {
         if ((Boolean)this.lookAt.getValue()) {
            if (PacketDispatchHook.getVector() == null || PacketDispatchHook.getRotation() == null) {
               this.synchronizePose = System.nanoTime() >= this.nextPrime;
               return;
            }

            if (PacketDispatchHook.getVector().squaredDistanceTo(player.getPos()) > 1.0E-6) {
               return;
            }
         }

         if (this.phase == NoteBot.Mode.IDLE) {
            this.mapping = new NoteBlockTuner(
               player.getBlockPos(),
               ((Number)this.radius.getValue()).intValue(),
               this.current.getNeededKeys(),
               (Boolean)this.autoTune.getValue(),
               (Boolean)this.wallCheck.getValue()
            );
            this.phase = NoteBot.Mode.PREPARING;
         }

         switch (this.phase) {
            case PREPARING:
               this.performActionEx(player);
               break;
            case TUNING:
               this.performActionAlt(player);
               break;
            case PLAYING:
               this.performActionImpl(player);
         }
      }
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (xevent.getPhase() == Stage.PRE && this.synchronizePose) {
         xevent.setForceFullPacket(true);
         this.synchronizePose = false;
         this.nextPrime = System.nanoTime() + 1000000000L;
      }
   }

   private void performActionEx(ClientPlayerEntity player) {
      NoteBlockTuner.Entry3 entry3 = this.mapping.getEntry3(player, mc2.world);
      if (entry3 != null) {
         this.mapping = null;
         if (entry3.getBlocks().isEmpty()) {
            this.applyTextEx("no reachable note blocks match this song");
         } else {
            this.layout = entry3;
            this.applyText("NoteBot mapped " + this.layout.getBlocks().size() + "/" + this.current.getNeededKeys().size() + " notes");
            if (this.layout.getTuning().isEmpty()) {
               this.performActionSafe(player);
            } else {
               this.tuneIndex = 0;
               this.phase = NoteBot.Mode.TUNING;
            }
         }
      }
   }

   private void performActionAlt(ClientPlayerEntity player) {
      if (this.tuningPos != null) {
         if (!this.tuningStateReceived || !this.tuningAcknowledged) {
            return;
         }

         if (!mc2.world.getBlockState(this.tuningPos).isOf(Blocks.NOTE_BLOCK)
            || (Integer)mc2.world.getBlockState(this.tuningPos).get(NoteBlock.NOTE) != this.tuningPitch) {
            this.applyTextEx("server note pitch differs from the requested pitch; rescan");
            return;
         }

         this.tuningPos = null;
         this.tuningSequence = -1;
      }

      NoteBlockTuner.Entry entry = this.layout.getTuning().get(this.tuneIndex);
      if (this.isValidBlock(entry.getPos()) && this.isValidBlockEx(entry.getPos())) {
         int n = (Integer)mc2.world.getBlockState(entry.getPos()).get(NoteBlock.NOTE);
         if (n == entry.getTargetPitch()) {
            if (++this.tuneIndex == this.layout.getTuning().size()) {
               this.performActionSafe(player);
            }
         } else {
            BlockHitResult hitResult = this.raycastHit(player, entry.getPos());
            if (hitResult != null) {
               mc2.interactionManager.syncSelectedSlot();
               this.tuningPos = entry.getPos();
               this.tuningPitch = (n + 1) % 25;
               this.tuningStateReceived = false;
               this.tuningAcknowledged = false;
               this.writeDeadline = System.nanoTime() + 5000000000L;
               mc2.interactionManager.sendSequencedPacket(mc2.world, n2 -> {
                  this.tuningSequence = n2;
                  PlayerInteractBlockC2SPacket playerInteractBlockC2SPacket = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, n2);
                  this.pendingWrites.add(playerInteractBlockC2SPacket);
                  return playerInteractBlockC2SPacket;
               });
               this.performActionDirect();
            } else {
               if (!(Boolean)this.lookAt.getValue() || !this.isValid(player, entry.getPos())) {
                  this.applyTextEx("note block is obstructed or out of reach; rescan");
               }
            }
         }
      } else {
         this.applyTextEx("note block layout changed during tuning; rescan");
      }
   }

   @EventTarget
   public void onClientConnectionUtilEntry2(PacketDispatchHook.Entry2 entry2) {
      if (this.pendingWrites.remove(entry2.getPacket()) && !entry2.isSuccess()) {
         this.applyTextEx("packet write failed; rescan");
      }
   }

   @EventTarget
   public void onClientConnectionUtilEntry(PacketDispatchHook.Entry entry) {
      if (entry.getPacket() instanceof PlayerPositionLookS2CPacket) {
         this.performActionCustom();
         if (this.phase != NoteBot.Mode.LOADING && this.phase != NoteBot.Mode.ERROR) {
            this.phase = NoteBot.Mode.IDLE;
         }
      } else if (this.tuningPos != null) {
         if (entry.getPacket() instanceof BlockUpdateS2CPacket blockUpdateS2CPacket) {
            this.updateBlock(blockUpdateS2CPacket.getPos(), blockUpdateS2CPacket.getState());
         }

         if (entry.getPacket() instanceof ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket) {
            chunkDeltaUpdateS2CPacket.visitUpdates(this::updateBlock);
         }

         if (entry.getPacket() instanceof PlayerActionResponseS2CPacket playerActionResponseS2CPacket && this.tuningSequence >= 0 && playerActionResponseS2CPacket.sequence() >= this.tuningSequence) {
            this.tuningAcknowledged = true;
         }
      }
   }

   private void updateBlock(BlockPos pos, BlockState state) {
      if (pos.equals(this.tuningPos)) {
         this.tuningStateReceived = state.isOf(Blocks.NOTE_BLOCK) && (Integer)state.get(NoteBlock.NOTE) == this.tuningPitch;
      }
   }

   private Rotation computeRotation() {
      return PacketDispatchHook.getRotation();
   }

   private void performActionImpl(ClientPlayerEntity player) {
      NotesRecord song = this.current.getSong();
      boolean flag = (Boolean)this.lookAt.getValue() && (Boolean)this.keepTempo.getValue();
      if (flag) {
         this.performAction(song, this.songPosition);
      }

      this.songPosition = this.songPosition + song.getTempo() / 20.0;
      int n = (int)Math.floor(this.songPosition);
      int n2 = 0;
      int n3 = 0;
      int n4 = ((Number)this.maxNotesPerTick.getValue()).intValue();
      BlockPos pos = null;

      while (this.noteCursor < song.getNotes().size() && n2++ < 32768 && n3 < n4) {
         NotesRecord.Entry entry = song.getNotes().get(this.noteCursor);
         if (entry.getTick() > n) {
            break;
         }

         BlockPos pos2 = this.layout.getBlocks().get(entry.getHeight());
         if (pos2 != null) {
            if ((Boolean)this.lookAt.getValue() && pos != null && !pos.equals(pos2)) {
               if (!flag) {
                  break;
               }

               this.noteCursor++;
               continue;
            }

            if (!this.isValid(pos2, entry.getHeight())) {
               this.unavailableKeys.add(entry.getHeight());
               this.noteCursor++;
               continue;
            }

            BlockHitResult hitResult = this.raycastHit(player, pos2);
            if (hitResult == null) {
               if ((Boolean)this.lookAt.getValue() && !flag && this.isValid(player, pos2)) {
                  return;
               }

               if (!flag || BlockRaycastHelper.raycastHit(player, mc2.world, pos2, (Boolean)this.wallCheck.getValue()) == null) {
                  this.unavailableKeys.add(entry.getHeight());
               }
            } else {
               if (!this.isValid(hitResult)) {
                  this.applyTextEx("block interaction failed; rescan the note blocks");
                  return;
               }

               this.unavailableKeys.remove(entry.getHeight());
               n3++;
               pos = pos2;
            }
         }

         this.noteCursor++;
      }

      if (flag) {
         this.performAction(song, this.songPosition);
      }

      this.performActionDirect(player);
      if (n3 == 0 && this.noteCursor == song.getNotes().size() && n > song.getNotes().getLast().getTick()) {
         if ((Boolean)this.autoNext.getValue() && this.current.getPlaylistSize() > 1) {
            this.setActiveEx(true);
         } else if (this.isValid(player)) {
            this.performActionCustom();
            this.phase = NoteBot.Mode.IDLE;
         } else {
            this.performActionSafe(player);
         }
      }
   }

   private boolean isValid(ClientPlayerEntity player) {
      for (int n : this.unavailableKeys) {
         BlockPos pos = this.layout.getBlocks().get(n);
         if (!this.isValid(pos, n) || BlockRaycastHelper.raycastHit(player, mc2.world, pos, (Boolean)this.wallCheck.getValue()) == null) {
            return true;
         }
      }

      return false;
   }

   private void performAction(NotesRecord notesRecord, double d) {
      int noteCursor2 = this.noteCursor;
      int size = notesRecord.getNotes().size();

      while (noteCursor2 < size) {
         int n = noteCursor2 + size >>> 1;
         if (notesRecord.getNotes().get(n).getTick() <= d) {
            noteCursor2 = n + 1;
         } else {
            size = n;
         }
      }

      this.noteCursor = noteCursor2;
   }

   private BlockHitResult raycastHit(ClientPlayerEntity player, BlockPos pos) {
      if (!(Boolean)this.lookAt.getValue()) {
         return BlockRaycastHelper.raycastHit(player, mc2.world, pos, (Boolean)this.wallCheck.getValue());
      }

      Rotation rotation = this.computeRotation();
      BlockHitResult hitResult = BlockRaycastHelper.raycastHit(player, mc2.world, pos, rotation, (Boolean)this.wallCheck.getValue());
      if (hitResult != null) {
         this.requestedRotation = rotation;
      }

      return hitResult;
   }

   private void performActionDirect(ClientPlayerEntity player) {
      if ((Boolean)this.lookAt.getValue()) {
         List notes = this.current.getSong().getNotes();
         int n = Math.min(notes.size(), this.noteCursor + 256);

         for (int i = this.noteCursor; i < n; i++) {
            int height = ((NotesRecord.Entry)notes.get(i)).getHeight();
            BlockPos pos = this.layout.getBlocks().get(height);
            if (pos != null && this.isValid(pos, height) && this.isValid(player, pos)) {
               return;
            }
         }
      }
   }

   private boolean isValid(ClientPlayerEntity player, BlockPos pos) {
      if (mc2.world.getBlockState(pos).isOf(Blocks.NOTE_BLOCK) && player.canInteractWithBlockAt(pos, 0.0)) {
         Rotation rotation = RotationManager.computeRotation(player.getEyePos(), Vec3d.ofCenter(pos));
         if (BlockRaycastHelper.raycastHit(player, mc2.world, pos, rotation, (Boolean)this.wallCheck.getValue()) == null) {
            return false;
         }

         Rotation rotation2 = this.computeRotation();
         if (rotation2 == null) {
            rotation2 = new Rotation(player.getYaw(), player.getPitch());
         }

         float wrapped = MathHelper.wrapDegrees(rotation.yaw - rotation2.yaw);
         float f = rotation.pitch - rotation2.pitch;
         float f2 = ((Number)this.rotationSpeed.getValue()).floatValue();
         this.requestedRotation = new Rotation(rotation2.yaw + MathHelper.clamp(wrapped, -f2, f2), rotation2.pitch + MathHelper.clamp(f, -f2, f2));
         return true;
      } else {
         return false;
      }
   }

   private boolean isValid(BlockHitResult hitResult) {
      BlockPos blockPos = hitResult.getBlockPos();
      if (this.isValidBlock(blockPos)
         && !mc2.player.getAbilities().creativeMode
         && mc2.player.getMainHandStack().isEmpty()
         && !mc2.player.isBlockBreakingRestricted(mc2.world, blockPos, mc2.interactionManager.getCurrentGameMode())
         && !(mc2.world.getBlockState(blockPos).calcBlockBreakingDelta(mc2.player, mc2.world, blockPos) >= 1.0F)) {
         mc2.interactionManager.syncSelectedSlot();
         this.writeDeadline = System.nanoTime() + 5000000000L;
         mc2.interactionManager.sendSequencedPacket(mc2.world, n -> {
            PlayerActionC2SPacket playerActionC2SPacket = new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, blockPos, hitResult.getSide(), n);
            this.pendingWrites.add(playerActionC2SPacket);
            return playerActionC2SPacket;
         });
         this.performActionDirect();
         this.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, blockPos, hitResult.getSide()));
         return true;
      } else {
         return false;
      }
   }

   private void performActionDirect() {
      if ((Boolean)this.swing.getValue()) {
         mc2.player.swingHand(Hand.MAIN_HAND);
      } else {
         this.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
      }
   }

   private void sendPacket(Packet<?> packet) {
      this.pendingWrites.add(packet);
      mc2.getNetworkHandler().sendPacket(packet);
   }

   private boolean isValidBlock(BlockPos pos) {
      return mc2.world.getBlockState(pos).isOf(Blocks.NOTE_BLOCK)
         && mc2.world.getBlockState(pos.up()).isAir()
         && mc2.world.getWorldBorder().contains(pos)
         && mc2.player.canInteractWithBlockAt(pos, 0.0);
   }

   private boolean isValidBlockEx(BlockPos pos) {
      for (Entry entry : this.layout.getBlocks().entrySet()) {
         if (((BlockPos)entry.getValue()).equals(pos)) {
            return NoteBlockTuner.getNoteBlockInstrument((Integer)entry.getKey()) == mc2.world.getBlockState(pos).get(NoteBlock.INSTRUMENT);
         }
      }

      return false;
   }

   private boolean isValid(BlockPos pos, int n) {
      return this.isValidBlock(pos)
         && NoteBlockTuner.getNoteBlockInstrument(n) == mc2.world.getBlockState(pos).get(NoteBlock.INSTRUMENT)
         && (Integer)mc2.world.getBlockState(pos).get(NoteBlock.NOTE) == (n & 0xFF);
   }

   private void performActionSafe(ClientPlayerEntity player) {
      this.unavailableKeys.clear();
      this.noteCursor = 0;
      this.songPosition = -1.0;
      this.phase = NoteBot.Mode.PLAYING;
      this.performActionDirect(player);
   }

   private void applyTextEx(String text) {
      this.loader.performAction();
      this.performActionCustom();
      this.phase = NoteBot.Mode.ERROR;
      this.setInfoText("Error");
      this.applyTextEx("NoteBot: " + text);
   }

   private void performActionSafe() {
      this.performActionCustom();
      this.current = null;
      this.phase = NoteBot.Mode.IDLE;
      this.setInfoText("");
   }

   private void performActionCustom() {
      this.mapping = null;
      this.layout = null;
      this.tuneIndex = 0;
      this.noteCursor = 0;
      this.songPosition = -1.0;
      this.requestedRotation = null;
      this.synchronizePose = false;
      this.nextPrime = 0L;
      this.pendingWrites.clear();
      this.unavailableKeys.clear();
      this.tuningPos = null;
      this.tuningSequence = -1;
   }

   public static Path getPath() {
      return Southside.FOLDER.toPath().resolve("notebot");
   }

   private enum Mode {
      IDLE,
      LOADING,
      PREPARING,
      TUNING,
      PLAYING,
      ERROR;

      private static final long DEFAULT_LONG = 3172167260328427526L;

      // $VF: synthetic method
      private static NoteBot.Mode[] getModeList() {
         NoteBot.Mode[] modeArray = new NoteBot.Mode[(int)DEFAULT_LONG];
         modeArray[0] = IDLE;
         modeArray[1] = LOADING;
         modeArray[2] = PREPARING;
         modeArray[3] = TUNING;
         modeArray[4] = PLAYING;
         modeArray[5] = ERROR;
         return modeArray;
      }


   }
}
