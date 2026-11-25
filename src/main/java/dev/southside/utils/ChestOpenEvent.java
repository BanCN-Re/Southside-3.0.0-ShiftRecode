package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ChestOpenEvent {
   @SerializedName("player")
   public PlayerEntity player;
   @SerializedName("pos")
   public BlockPos pos;

   public BlockPos getPos() {
      return this.pos;
   }

   public PlayerEntity getPlayer() {
      return this.player;
   }

   public ChestOpenEvent(PlayerEntity player2, BlockPos pos2) {
      this.player = player2;
      this.pos = pos2;
   }
}
