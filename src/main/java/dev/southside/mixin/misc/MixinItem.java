package dev.southside.mixin.misc;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.southside.utils.RotationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class MixinItem {
   @ModifyExpressionValue(
      method = "Lnet/minecraft/item/Item;raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F")
   )
   private static float modifyRaycastYaw(float f, World world, PlayerEntity player, FluidHandling fluidHandling) {
      if (player == MinecraftClient.getInstance().player) {
         return RotationManager.getCurrentRotation() != null ? RotationManager.getCurrentRotation().yaw : f;
      } else {
         return f;
      }
   }

   @ModifyExpressionValue(
      method = "Lnet/minecraft/item/Item;raycast(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/RaycastContext$FluidHandling;)Lnet/minecraft/util/hit/BlockHitResult;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getPitch()F")
   )
   private static float modifyRaycastPitch(float f, World world, PlayerEntity player, FluidHandling fluidHandling) {
      if (player == MinecraftClient.getInstance().player) {
         return RotationManager.getCurrentRotation() != null ? RotationManager.getCurrentRotation().pitch : f;
      } else {
         return f;
      }
   }
}
