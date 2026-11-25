package dev.southside.utils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class SkyblockIslandTemplate {
   public static final int DEFAULT_INT = -2;
   public static final int DEFAULT_INT_VALUE = -1;
   private static final long DEFAULT_LONG = 2L;
   private static final List<SkyblockIslandTemplate.Entry> entries = collectItems();
   private static final List<SkyblockIslandTemplate.Entry> entriesValue = collectItemsEx();

   private SkyblockIslandTemplate() {
   }

   public static List<SkyblockIslandTemplate.Entry> getEntries() {
      return entries;
   }

   public static List<SkyblockIslandTemplate.Entry> getEntriesValue() {
      return entriesValue;
   }

   private static List<SkyblockIslandTemplate.Entry> collectItems() {
      ArrayList list = new ArrayList(72);
      BlockState defaultState = Blocks.GRASS_BLOCK.getDefaultState();
      BlockState defaultState2 = Blocks.DIRT.getDefaultState();
      BlockState defaultState3 = Blocks.STONE.getDefaultState();
      performAction(list, defaultState, -1, -4, -2, 0);
      performAction(list, defaultState, -1, -3, -3, 1);
      performAction(list, defaultState, -1, -2, -4, 2);
      performAction(list, defaultState, -1, -1, -4, 3);
      performAction(list, defaultState, -1, 0, -3, 2);
      performAction(list, defaultState, -1, 1, -2, -1);
      performAction(list, defaultState2, -2, -3, -2, 0);
      performAction(list, defaultState2, -2, -2, -3, 1);
      performAction(list, defaultState2, -2, -1, -3, 2);
      performAction(list, defaultState2, -2, 0, -2, 1);
      performAction(list, defaultState3, -3, -2, -2, 0);
      performAction(list, defaultState3, -3, -1, -2, 1);
      performAction(list, defaultState3, -3, 0, -1, 0);
      performAction(list, defaultState3, -4, -2, -1, -1);
      performAction(list, defaultState3, -4, -1, -1, 0);
      list.add(new SkyblockIslandTemplate.Entry(-1, 0, 0, Blocks.POPPY.getDefaultState()));
      list.add(new SkyblockIslandTemplate.Entry(2, 0, -1, Blocks.DANDELION.getDefaultState()));
      list.add(new SkyblockIslandTemplate.Entry(-3, 0, -2, Blocks.AZURE_BLUET.getDefaultState()));
      list.add(new SkyblockIslandTemplate.Entry(1, 0, -2, Blocks.SHORT_GRASS.getDefaultState()));
      list.add(new SkyblockIslandTemplate.Entry(0, 0, -3, Blocks.SHORT_GRASS.getDefaultState()));
      list.add(new SkyblockIslandTemplate.Entry(-3, 0, -1, Blocks.SHORT_GRASS.getDefaultState()));
      return List.copyOf(list);
   }

   private static void performAction(List<SkyblockIslandTemplate.Entry> list, BlockState state, int n, int n2, int n3, int n4) {
      for (int i = n3; i <= n4; i++) {
         list.add(new SkyblockIslandTemplate.Entry(i, n, n2, state));
      }
   }

   private static List<SkyblockIslandTemplate.Entry> collectItemsEx() {
      Random random = Random.create(2L);
      int n = 4 + random.nextInt(3) + random.nextInt(1);
      BlockState defaultState = Blocks.OAK_LOG.getDefaultState();
      BlockState defaultState2 = Blocks.OAK_LEAVES.getDefaultState();
      ArrayList list = new ArrayList(64);

      for (int i = 0; i < n; i++) {
         list.add(new SkyblockIslandTemplate.Entry(-2, i, -1, defaultState));
      }

      for (int j = 0; j >= -3; j--) {
         int n2 = Math.max(1 - j / 2, 0);
         int n3 = n + j;

         for (int k = -n2; k <= n2; k++) {
            for (int m = -n2; m <= n2; m++) {
               boolean flag = Math.abs(k) == n2 && Math.abs(m) == n2;
               if ((!flag || random.nextInt(2) != 0 && j != 0) && (k != 0 || m != 0 || n3 >= n)) {
                  list.add(new SkyblockIslandTemplate.Entry(-2 + k, n3, -1 + m, defaultState2));
               }
            }
         }
      }

      return List.copyOf(list);
   }

   public record Entry(BlockPos pos, BlockState state) {
      public Entry(int n, int n2, int n3, BlockState state2) {
         this(new BlockPos(n, n2, n3), state2);
      }

      public BlockPos getPos() {
         return this.pos;
      }

      public BlockState getState() {
         return this.state;
      }
   }
}
