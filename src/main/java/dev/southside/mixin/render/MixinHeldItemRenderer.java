package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.event.events.HandEvent;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.HeldItemAnimationGuard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   private ItemStack offHand;

   @Shadow
   public abstract void renderItem(
      LivingEntity living, ItemStack stack, ModelTransformationMode modelTransformationMode, boolean flag, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n
   );

   @Inject(
      method = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRenderFirstPersonItemHead(
      AbstractClientPlayerEntity player,
      float f,
      float f2,
      Hand hand,
      float f3,
      ItemStack stack,
      float f4,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int n,
      CallbackInfo ci
   ) {
      if (!player.isUsingSpyglass()) {
         OldHitting oldHitting = Southside.INSTANCE.getModuleManager().getModule(OldHitting.class);
         boolean flag = hand == Hand.MAIN_HAND;
         Arm arm = flag ? player.getMainArm() : player.getMainArm().getOpposite();
         boolean flag2 = arm == Arm.RIGHT;
         int n2 = flag2 ? 1 : -1;
         matrices.push();
         if (stack.isEmpty()) {
            if (flag && !player.isInvisible()) {
               this.renderArmHoldingItem(matrices, vertexConsumers, n, f4, f3, arm);
            }
         } else if (stack.contains(DataComponentTypes.MAP_ID)) {
            if (flag && this.offHand.isEmpty()) {
               this.renderMapInBothHands(matrices, vertexConsumers, n, f2, f4, f3);
            } else {
               this.renderMapInOneHand(matrices, vertexConsumers, n, f4, arm, f3, stack);
            }
         } else {
            if (stack.isOf(Items.CROSSBOW)) {
               this.applyCrossbowTransform(player, hand, stack, f, f3, f4, matrices, arm);
            } else if (this.shouldRenderLegacyBlocking(player, flag, stack, oldHitting)) {
               oldHitting.render(matrices, n2, f4, f3, arm);
            } else {
               if (oldHitting.isEnabled() && flag) {
                  matrices.translate(oldHitting.mouseXOffset.getCurrentValue(), oldHitting.mouseYOffset.getCurrentValue(), oldHitting.mouseZOffset.getCurrentValue());
               }

               if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                  this.applyItemUseTransform(player, stack, arm, f, f3, f4, matrices, oldHitting);
               } else if (player.isUsingRiptide()) {
                  this.applyEquipOffset(matrices, arm, f4);
                  matrices.translate(n2 * -0.4F, 0.8F, 0.3F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n2 * 65.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(n2 * -85.0F));
               } else {
                  this.swingArm(f3, f4, matrices, n2, arm);
               }
            }

            ModelTransformationMode modelTransformationMode = flag2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
            this.renderItem(player, stack, modelTransformationMode, !flag2, matrices, vertexConsumers, n);
         }

         matrices.pop();
         ci.cancel();
      }
   }

   @Unique
   private boolean hasOffHandProjectileOrPotion(AbstractClientPlayerEntity player) {
      ItemStack offHandStack = player.getOffHandStack();
      return !offHandStack.isEmpty()
         && (
            offHandStack.getItem() instanceof SnowballItem
               || offHandStack.getItem() instanceof EggItem
               || offHandStack.getItem() instanceof EnderPearlItem
               || offHandStack.getItem() instanceof ExperienceBottleItem
               || offHandStack.getItem() instanceof PotionItem
               || offHandStack.getItem() instanceof LingeringPotionItem
               || offHandStack.getItem() instanceof SplashPotionItem
         );
   }

   @Unique
   private boolean shouldRenderLegacyBlocking(AbstractClientPlayerEntity player, boolean flag, ItemStack stack, OldHitting oldHitting) {
      boolean hasOffHandProjectileOrPotion = this.hasOffHandProjectileOrPotion(player);
      boolean flag2 = player.getMainHandStack().getUseAction() == UseAction.EAT || player.getMainHandStack().getUseAction() == UseAction.DRINK;
      boolean flag3 = player.getOffHandStack().getUseAction() == UseAction.EAT || player.getOffHandStack().getUseAction() == UseAction.DRINK;
      boolean flag4 = false;
      if (flag) {
         flag4 = HeldItemAnimationGuard.isValid()
            || this.client.options.useKey.isPressed()
               && player.currentScreenHandler == player.playerScreenHandler
               && !hasOffHandProjectileOrPotion
               && (!flag2 || player.getActiveHand() != Hand.MAIN_HAND)
               && (!flag3 || player.getActiveHand() != Hand.OFF_HAND);
      }

      boolean flag5 = stack.getItem() instanceof SwordItem;
      if (oldHitting.axeItemTransform.getValue()) {
         flag5 = stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
      }

      return oldHitting.isEnabled() && flag5 && flag4;
   }

   @Unique
   private void applyCrossbowTransform(
      AbstractClientPlayerEntity player, Hand hand, ItemStack stack, float f, float f2, float f3, MatrixStack matrices, Arm arm
   ) {
      int n = arm == Arm.RIGHT ? 1 : -1;
      boolean isCharged = CrossbowItem.isCharged(stack);
      if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
         this.applyEquipOffset(matrices, arm, f3);
         matrices.translate(n * -0.4785682F, -0.094387F, 0.05731531F);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n * 65.3F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(n * -9.785F));
         float f4 = stack.getMaxUseTime(player) - (player.getItemUseTimeLeft() - f + 1.0F);
         float f5 = f4 / CrossbowItem.getPullTime(stack, player);
         if (f5 > 1.0F) {
            f5 = 1.0F;
         }

         this.applyChargeShake(matrices, f4, f5);
         matrices.translate(f5 * 0.0F, f5 * 0.0F, f5 * 0.04F);
         matrices.scale(1.0F, 1.0F, 1.0F + f5 * 0.2F);
         matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(n * 45.0F));
      } else {
         this.swingArm(f2, f3, matrices, n, arm);
         if (isCharged && f2 < 0.001F && hand == Hand.MAIN_HAND) {
            matrices.translate(n * -0.641864F, 0.0F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n * 10.0F));
         }
      }
   }

   @Unique
   private void applyItemUseTransform(
      AbstractClientPlayerEntity player, ItemStack stack, Arm arm, float f, float f2, float f3, MatrixStack matrices, OldHitting oldHitting
   ) {
      int n = arm == Arm.RIGHT ? 1 : -1;
      UseAction useAction = stack.getUseAction();
      if (useAction == UseAction.NONE) {
         this.applyEquipOffset(matrices, arm, f3);
      } else if (useAction == UseAction.EAT || useAction == UseAction.DRINK) {
         this.applyConsumableTransform(player, stack, arm, f, f2, f3, matrices, oldHitting);
      } else if (useAction == UseAction.BLOCK) {
         this.applyEquipOffset(matrices, arm, f3);
         if (!(stack.getItem() instanceof ShieldItem)) {
            matrices.translate(n * -0.14142136F, 0.08F, 0.14142136F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n * 13.365F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(n * 78.05F));
         }
      } else if (useAction == UseAction.BOW) {
         this.applyBowTransform(player, stack, arm, f, f3, matrices);
      } else if (useAction == UseAction.SPEAR) {
         this.applySpearTransform(player, stack, arm, f, f3, matrices);
      } else if (useAction == UseAction.BRUSH) {
         this.applyBrushTransformation(matrices, f, arm, stack, player, f3);
      } else if (useAction == UseAction.BUNDLE) {
         this.swingArm(f2, f3, matrices, n, arm);
      }
   }

   @Unique
   private void applyBowTransform(AbstractClientPlayerEntity player, ItemStack stack, Arm arm, float f, float f2, MatrixStack matrices) {
      int n = arm == Arm.RIGHT ? 1 : -1;
      this.applyEquipOffset(matrices, arm, f2);
      matrices.translate(n * -0.2785682F, 0.18344387F, 0.15731531F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n * 35.3F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(n * -9.785F));
      float f3 = stack.getMaxUseTime(player) - (player.getItemUseTimeLeft() - f + 1.0F);
      float f4 = f3 / 20.0F;
      f4 = (f4 * f4 + f4 * 2.0F) / 3.0F;
      if (f4 > 1.0F) {
         f4 = 1.0F;
      }

      this.applyChargeShake(matrices, f3, f4);
      matrices.translate(f4 * 0.0F, f4 * 0.0F, f4 * 0.04F);
      matrices.scale(1.0F, 1.0F, 1.0F + f4 * 0.2F);
      matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(n * 45.0F));
   }

   @Unique
   private void applySpearTransform(AbstractClientPlayerEntity player, ItemStack stack, Arm arm, float f, float f2, MatrixStack matrices) {
      int n = arm == Arm.RIGHT ? 1 : -1;
      this.applyEquipOffset(matrices, arm, f2);
      matrices.translate(n * -0.5F, 0.7F, 0.1F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n * 35.3F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(n * -9.785F));
      float f3 = stack.getMaxUseTime(player) - (player.getItemUseTimeLeft() - f + 1.0F);
      float f4 = f3 / 10.0F;
      if (f4 > 1.0F) {
         f4 = 1.0F;
      }

      this.applyChargeShake(matrices, f3, f4);
      matrices.translate(0.0F, 0.0F, f4 * 0.2F);
      matrices.scale(1.0F, 1.0F, 1.0F + f4 * 0.2F);
      matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(n * 45.0F));
   }

   @Unique
   private void applyChargeShake(MatrixStack matrices, float f, float f2) {
      if (f2 > 0.1F) {
         float f3 = MathHelper.sin((f - 0.1F) * 1.3F);
         float f4 = f3 * (f2 - 0.1F);
         matrices.translate(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
      }
   }

   @Unique
   private void applyConsumableTransform(
      AbstractClientPlayerEntity player, ItemStack stack, Arm arm, float f, float f2, float f3, MatrixStack matrices, OldHitting oldHitting
   ) {
      boolean isEnabled = oldHitting.isEnabled();
      if (isEnabled && f2 != 0.0F) {
         int n = arm == Arm.RIGHT ? 1 : -1;
         matrices.translate(n * 0.56, -0.52 + f3 * -0.6, -0.72);
         float f4 = MathHelper.sin(f2 * f2 * (float) Math.PI);
         matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(f4 * 20.0F));
         matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(f4 * 20.0F));
         matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(f4 * 80.0F));
         matrices.translate(-0.8F, 0.2F, 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0F));
         matrices.scale(1.4F, 1.4F, 1.4F);
      } else {
         this.applyEatOrDrinkTransformation(matrices, f, arm, stack, player);
         this.applyEquipOffset(matrices, arm, f3);
         if (isEnabled) {
            this.applyConsumableSwingTransform(matrices, f2);
         }
      }
   }

   @Unique
   private void applyConsumableSwingTransform(MatrixStack matrices, float f) {
      float f2 = MathHelper.sin(f * f * (float) Math.PI);
      float f3 = MathHelper.sin(MathHelper.sqrt(f) * (float) Math.PI);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f2 * -20.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f3 * -20.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f3 * -80.0F));
   }

   @Shadow
   protected abstract void renderMapInBothHands(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, float f, float f2, float f3);

   @Shadow
   protected abstract void swingArm(float f, float f2, MatrixStack matrices, int n, Arm arm);

   @Shadow
   protected abstract void applyEatOrDrinkTransformation(MatrixStack matrices, float f, Arm arm, ItemStack stack, PlayerEntity player);

   @Shadow
   protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float f);

   @Redirect(
      method = "Lnet/minecraft/client/render/item/HeldItemRenderer;updateHeldItems()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;")
   )
   public ItemStack redirectUpdateMainHandItem(ClientPlayerEntity player) {
      HandEvent handEvent = new HandEvent(Hand.MAIN_HAND, player.getMainHandStack());
      if (player == MinecraftClient.getInstance().player) {
         Southside.INSTANCE.getEventBus().postCancellable(handEvent);
      }

      return handEvent.getItemStack();
   }

   @Shadow
   protected abstract void applyBrushTransformation(MatrixStack matrices, float f, Arm arm, ItemStack stack, PlayerEntity player, float f2);

   @Redirect(
      method = "Lnet/minecraft/client/render/item/HeldItemRenderer;updateHeldItems()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getOffHandStack()Lnet/minecraft/item/ItemStack;")
   )
   public ItemStack redirectUpdateOffHandItem(ClientPlayerEntity player) {
      HandEvent handEvent = new HandEvent(Hand.OFF_HAND, player.getOffHandStack());
      if (player == MinecraftClient.getInstance().player) {
         Southside.INSTANCE.getEventBus().postCancellable(handEvent);
      }

      return handEvent.getItemStack();
   }

   @Shadow
   protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, float f, float f2, Arm arm);

   @Shadow
   protected abstract void renderMapInOneHand(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int n, float f, Arm arm, float f2, ItemStack stack);
}
