package dev.southside.utils.misc;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public enum REDType {
   RED(Formatting.RED, Blocks.RED_BED, "红", 10040115, 16733525),
   BLUE(Formatting.BLUE, Blocks.BLUE_BED, "蓝", 3361970, 5592575),
   GREEN(Formatting.GREEN, Blocks.LIME_BED, "绿", 6717235, 5635925),
   YELLOW(Formatting.YELLOW, Blocks.YELLOW_BED, "黄", 15066419, 16777045),
   AQUA(Formatting.AQUA, Blocks.CYAN_BED, "青", 5013401, 5636095),
   WHITE(Formatting.WHITE, Blocks.WHITE_BED, "白", 16777215),
   PINK(Formatting.LIGHT_PURPLE, Blocks.PINK_BED, "粉", 15892389, 16733695),
   GRAY(Formatting.DARK_GRAY, Blocks.GRAY_BED, "灰", 5000268, 11184810);

   private static final Map<Block, REDType> redtypeMap;
   private static final EquipmentSlot[] equipmentSlotArray;
   @SerializedName("formatting")
   private final Formatting formatting;
   @SerializedName("bedBlock")
   private final Block bedBlock;
   @SerializedName("chineseName")
   private final String chineseName;
   @SerializedName("candidateRgb")
   private final int[] candidateRgb;

   REDType(Formatting formatting2, Block block, String text, int... values) {
      this.formatting = formatting2;
      this.bedBlock = block;
      this.chineseName = text;
      this.candidateRgb = values;
   }

   public Formatting getFormatting() {
      return this.formatting;
   }

   public String getChineseName() {
      return this.chineseName;
   }

   @Nullable
   public static REDType getRedtype(@Nullable BlockState state) {
      return state == null ? null : redtypeMap.get(state.getBlock());
   }

   public static REDType getRedtype(int n) {
      REDType redtype = null;
      int max_value = Integer.MAX_VALUE;

      for (REDType redtype2 : values()) {
         for (int n2 : redtype2.candidateRgb) {
            int n3 = computeInt(n, n2);
            if (n3 < max_value) {
               max_value = n3;
               redtype = redtype2;
            }
         }
      }

      return redtype;
   }

   public static int computeInt() {
      MinecraftClient instance = MinecraftClient.getInstance();
      if (instance != null && instance.player != null) {
         for (EquipmentSlot equipmentSlot : equipmentSlotArray) {
            DyedColorComponent dyedColorComponent = (DyedColorComponent)instance.player.getEquippedStack(equipmentSlot).get(DataComponentTypes.DYED_COLOR);
            if (dyedColorComponent != null) {
               return dyedColorComponent.rgb();
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   @Nullable
   public static REDType getRedtype() {
      int n = computeInt();
      return n < 0 ? null : getRedtype(n);
   }

   @Nullable
   public static REDType getRedtype(@Nullable String text) {
      if (text != null && !text.isEmpty()) {
         String text2 = text.toLowerCase(Locale.ROOT);

         for (REDType redtype : values()) {
            if (text2.contains(redtype.name().toLowerCase(Locale.ROOT)) || text.contains(redtype.chineseName)) {
               return redtype;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Nullable
   public static REDType getRedtype2() {
      MinecraftClient instance = MinecraftClient.getInstance();
      if (instance != null && instance.player != null) {
         REDType redtype = getRedtype();
         if (redtype != null) {
            return redtype;
         }

         Team team = getTeam(instance);
         if (team == null) {
            return null;
         }

         REDType redtype2 = getRedtype(team.getName());
         if (redtype2 != null) {
            return redtype2;
         }

         int teamColorValue = instance.player.getTeamColorValue();
         REDType redtype22 = getRedtype2(teamColorValue);
         return redtype22 != null ? redtype22 : getRedtype(teamColorValue);
      } else {
         return null;
      }
   }

   private static int computeInt(int n, int n2) {
      int n3 = (n >> 16 & 0xFF) - (n2 >> 16 & 0xFF);
      int n4 = (n >> 8 & 0xFF) - (n2 >> 8 & 0xFF);
      int n5 = (n & 0xFF) - (n2 & 0xFF);
      return n3 * n3 + n4 * n4 + n5 * n5;
   }

   @Nullable
   private static REDType getRedtype2(int n) {
      for (REDType redtype : values()) {
         if (redtype.formatting.getColorValue() == n) {
            return redtype;
         }
      }

      return null;
   }

   @Nullable
   private static Team getTeam(MinecraftClient client) {
      ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
      if (networkHandler == null) {
         return null;
      }

      PlayerListEntry playerListEntry = networkHandler.getPlayerListEntry(client.player.getUuid());
      return playerListEntry == null ? null : playerListEntry.getScoreboardTeam();
   }

   // $VF: synthetic method
   private static REDType[] getRedtypeArray() {
      return new REDType[]{RED, BLUE, GREEN, YELLOW, AQUA, WHITE, PINK, GRAY};
   }

   static {
      redtypeMap = new HashMap<>();
                                    equipmentSlotArray = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

                                    for (REDType redtype : values()) {
                                       redtypeMap.put(redtype.bedBlock, redtype);
                                    }

                                    redtypeMap.put(Blocks.GREEN_BED, GREEN);
                                    redtypeMap.put(Blocks.LIGHT_BLUE_BED, AQUA);
                                    redtypeMap.put(Blocks.MAGENTA_BED, PINK);
                                    redtypeMap.put(Blocks.PURPLE_BED, PINK);
                                    redtypeMap.put(Blocks.LIGHT_GRAY_BED, GRAY);
                                    redtypeMap.put(Blocks.BLACK_BED, GRAY);
   }


}
