package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class NoteBlockTuner {
   private static final int DEFAULT_INT = 256;
   private static final int DEFAULT_INT_VALUE = 2048;
   public static final NoteBlockInstrument[] NOTE_BLOCK_INSTRUMENT_ARRAY = new NoteBlockInstrument[]{
      NoteBlockInstrument.HARP,
      NoteBlockInstrument.BASS,
      NoteBlockInstrument.BASEDRUM,
      NoteBlockInstrument.SNARE,
      NoteBlockInstrument.HAT,
      NoteBlockInstrument.GUITAR,
      NoteBlockInstrument.FLUTE,
      NoteBlockInstrument.BELL,
      NoteBlockInstrument.CHIME,
      NoteBlockInstrument.XYLOPHONE,
      NoteBlockInstrument.IRON_XYLOPHONE,
      NoteBlockInstrument.COW_BELL,
      NoteBlockInstrument.DIDGERIDOO,
      NoteBlockInstrument.BIT,
      NoteBlockInstrument.BANJO,
      NoteBlockInstrument.PLING
   };
   @SerializedName("neededKeys")
   private final List<Integer> neededKeys;
   @SerializedName("autoTune")
   private final boolean autoTune;
   @SerializedName("wallCheck")
   private final boolean wallCheck;
   @SerializedName("noteBlockMap")
   private final Map<Integer, BlockPos> noteBlockMap = new HashMap<>();
   @SerializedName("tuneTasks")
   private final List<NoteBlockTuner.Entry> tuneTasks = new ArrayList<>();
   @SerializedName("scanCursor")
   private NoteBlockTuner.Inner scanCursor;
   @SerializedName("mappingCursor")
   private NoteBlockTuner.Inner2 mappingCursor;

   public NoteBlockTuner(BlockPos pos, int n, List<Integer> list, boolean flag, boolean flag2) {
      this.neededKeys = List.copyOf(list);
      this.autoTune = flag;
      this.wallCheck = flag2;
      this.scanCursor = new NoteBlockTuner.Inner(pos.toImmutable(), n);
   }

   public NoteBlockTuner.Entry3 getEntry3(ClientPlayerEntity player, ClientWorld world) {
      if (this.scanCursor != null) {
         this.performAction(player, world);
         return null;
      } else {
         return this.getEntry3();
      }
   }

   private void performAction(ClientPlayerEntity player, ClientWorld world) {
      int n = 0;

      while (n++ < 256 && this.scanCursor.isValid()) {
         BlockPos pos = this.scanCursor.findBlockPos();
         BlockState blockState = world.getBlockState(pos);
         if (blockState.isOf(Blocks.NOTE_BLOCK)
            && player.canInteractWithBlockAt(pos, 0.0)
            && world.getBlockState(pos.up()).isAir()
            && BlockRaycastHelper.raycastHit(player, world, pos, this.wallCheck) != null) {
            NoteBlockInstrument noteBlockInstrument = (NoteBlockInstrument)blockState.get(NoteBlock.INSTRUMENT);
            int n2 = (Integer)blockState.get(NoteBlock.NOTE);
            double dist = player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos));
            this.scanCursor.availableBlocks.computeIfAbsent(noteBlockInstrument, noteBlockInstrument2 -> new ArrayList<>()).add(new NoteBlockTuner.Entry2(pos, n2, dist));
         }
      }

      if (!this.scanCursor.isValid()) {
         this.mappingCursor = new NoteBlockTuner.Inner2(this.neededKeys, this.scanCursor.availableBlocks);
         this.scanCursor = null;
      }
   }

   private NoteBlockTuner.Entry3 getEntry3() {
      int n = 0;

      while (n++ < 2048 && this.mappingCursor.isValid()) {
         int n2 = this.mappingCursor.get();
         List list = this.mappingCursor.collectItems();
         if (this.mappingCursor.candidateIndex < list.size()) {
            NoteBlockTuner.Entry2 entry2 = (NoteBlockTuner.Entry2)list.get(this.mappingCursor.candidateIndex++);
            if (!this.mappingCursor.used.contains(entry2.getPos())) {
               int n3 = (computeIntEx(n2) - entry2.getPitch() + 25) % 25;
               if ((this.autoTune || n3 == 0) && this.mappingCursor.isValid(entry2, n3)) {
                  this.mappingCursor.best = entry2;
                  this.mappingCursor.bestClicks = n3;
               }
            }
         } else {
            if (this.mappingCursor.best != null) {
               this.noteBlockMap.put(n2, this.mappingCursor.best.getPos());
               this.mappingCursor.used.add(this.mappingCursor.best.getPos());
               if (this.autoTune && this.mappingCursor.bestClicks > 0) {
                  this.tuneTasks.add(new NoteBlockTuner.Entry(this.mappingCursor.best.getPos(), this.mappingCursor.bestClicks, computeIntEx(n2)));
               }
            }

            this.mappingCursor.performAction();
         }
      }

      return this.mappingCursor.isValid() ? null : new NoteBlockTuner.Entry3(this.noteBlockMap, this.tuneTasks);
   }

   public static NoteBlockInstrument getNoteBlockInstrument(int n) {
      return NOTE_BLOCK_INSTRUMENT_ARRAY[computeInt(n)];
   }

   public static int computeInt(int n) {
      return n >>> 8;
   }

   private static int computeIntEx(int n) {
      return n & 0xFF;
   }

   public record Entry(BlockPos pos, int clicks, int targetPitch) {
      public BlockPos getPos() {
         return this.pos;
      }

      public int getClicks() {
         return this.clicks;
      }

      public int getTargetPitch() {
         return this.targetPitch;
      }
   }

   private record Entry2(BlockPos pos, int pitch, double distanceSqr) {
      public BlockPos getPos() {
         return this.pos;
      }

      public int getPitch() {
         return this.pitch;
      }

      public double getDistanceSqr() {
         return this.distanceSqr;
      }
   }

   public record Entry3(Map<Integer, BlockPos> blocks, List<NoteBlockTuner.Entry> tuning) {
      public Entry3(Map<Integer, BlockPos> blocks, List<NoteBlockTuner.Entry> tuning) {
         blocks = Map.copyOf(blocks);
         tuning = List.copyOf(tuning);
         this.blocks = blocks;
         this.tuning = tuning;
      }

      public Map<Integer, BlockPos> getBlocks() {
         return this.blocks;
      }

      public List<NoteBlockTuner.Entry> getTuning() {
         return this.tuning;
      }
   }

   private static final class Inner {
      @SerializedName("center")
      private final BlockPos center;
      @SerializedName("radius")
      private final int radius;
      @SerializedName("side")
      private final int side;
      @SerializedName("total")
      private final int total;
      @SerializedName("availableBlocks")
      public final Map<NoteBlockInstrument, List<NoteBlockTuner.Entry2>> availableBlocks = new EnumMap<>(NoteBlockInstrument.class);
      @SerializedName("index")
      private int index;

      public Inner(BlockPos pos, int n) {
         this.center = pos;
         this.radius = n;
         this.side = n * 2 + 1;
         this.total = this.side * this.side * this.side;
      }

      public boolean isValid() {
         return this.index < this.total;
      }

      public BlockPos findBlockPos() {
         int n = this.index++;
         int n2 = n % this.side - this.radius;
         int n3 = n / this.side % this.side - this.radius;
         int n4 = n / (this.side * this.side) - this.radius;
         return this.center.add(n2, n3, n4);
      }
   }

   private static final class Inner2 {
      @SerializedName("targetKeys")
      private final List<Integer> targetKeys;
      @SerializedName("availableBlocks")
      private final Map<NoteBlockInstrument, List<NoteBlockTuner.Entry2>> availableBlocks;
      @SerializedName("used")
      public final Set<BlockPos> used = new HashSet<>();
      @SerializedName("targetIndex")
      private int targetIndex;
      @SerializedName("candidateIndex")
      public int candidateIndex;
      @SerializedName("best")
      public NoteBlockTuner.Entry2 best;
      @SerializedName("bestClicks")
      public int bestClicks = Integer.MAX_VALUE;

      public Inner2(List<Integer> list, Map<NoteBlockInstrument, List<NoteBlockTuner.Entry2>> map) {
         this.targetKeys = list;
         this.availableBlocks = map;
      }

      public boolean isValid() {
         return this.targetIndex < this.targetKeys.size();
      }

      public int get() {
         return this.targetKeys.get(this.targetIndex);
      }

      public List<NoteBlockTuner.Entry2> collectItems() {
         NoteBlockInstrument noteBlockInstrument = NoteBlockTuner.NOTE_BLOCK_INSTRUMENT_ARRAY[NoteBlockTuner.computeInt(this.get())];
         return this.availableBlocks.getOrDefault(noteBlockInstrument, List.of());
      }

      public boolean isValid(NoteBlockTuner.Entry2 entry2, int n) {
         if (this.best == null || n < this.bestClicks) {
            return true;
         }

         if (n > this.bestClicks) {
            return false;
         }

         int n2 = Double.compare(entry2.getDistanceSqr(), this.best.getDistanceSqr());
         return n2 != 0 ? n2 < 0 : Long.compare(entry2.getPos().asLong(), this.best.getPos().asLong()) < 0;
      }

      public void performAction() {
         this.targetIndex++;
         this.candidateIndex = 0;
         this.best = null;
         this.bestClicks = Integer.MAX_VALUE;
      }
   }
}
