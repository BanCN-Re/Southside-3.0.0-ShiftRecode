package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStateEvent extends CancellableEvent {
   @SerializedName("blockState")
   public BlockState blockState;
   @SerializedName("world")
   public World world;
   @SerializedName("blockPos")
   public BlockPos blockPos;
   @SerializedName("entity")
   public Entity entity;

   public void setBlockState(BlockState state) {
      this.blockState = state;
   }

   public BlockStateEvent(BlockState state, World world2, BlockPos pos, Entity entity2) {
      this.blockState = state;
      this.world = world2;
      this.blockPos = pos;
      this.entity = entity2;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public void setBlockPos(BlockPos pos) {
      this.blockPos = pos;
   }

   public void setWorld(World world2) {
      this.world = world2;
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public void setEntity(Entity entity2) {
      this.entity = entity2;
   }

   public World getWorld() {
      return this.world;
   }
}
